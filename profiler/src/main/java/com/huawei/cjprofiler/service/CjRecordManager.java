/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import static com.huawei.deveco.insight.ohos.common.constant.ArkConstants.ARK_REPLY_TIMEOUT;

import com.huawei.deveco.insight.ohos.ability.arkcpu.parser.JSTraceParser;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.deepcode.entity.LruCache;
import com.huawei.deveco.insight.ohos.ability.device.IHarmonyDevice;
import com.huawei.deveco.insight.ohos.ability.device.OpenHarmonyDeviceManager;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.common.constant.ArkConstants;
import com.huawei.deveco.insight.ohos.common.constant.ArkExecuteType;
import com.huawei.deveco.insight.ohos.model.bo.TraceFile;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkExecuteRequest;
import com.huawei.deveco.insight.ohos.service.file.JsTraceFileService;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.application.ApplicationManager;

import lombok.Data;

import org.apache.ibatis.exceptions.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * cangjie record manager
 *
 * @since 2025/02/13/16:35
 */
@Data
public class CjRecordManager {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjRecordManager.class);

    private static volatile CjRecordManager cjRecordManager = null;

    private static final Object LOCK = new Object();

    private final Map<Integer, CjRecordService> cjRecordServiceMap = new ConcurrentHashMap<>();

    /**
     * map<sessionId, CjMemoryService>
     */
    private final LruCache<String, CjMemoryService> cjMemoryServiceMap = new LruCache<>(4, 0.75f, true);

    /**
     * Mark which plug-in is being recorded.
     */
    private ArkExecuteType currentMode = ArkExecuteType.UNKNOWN;

    /**
     * Mark the session is being recorded.
     */
    private String currentSession;

    /**
     * getInstance
     *
     * @return CjRecordServerManager
     */
    public static CjRecordManager getInstance() {
        if (cjRecordManager == null) {
            synchronized (CjRecordManager.class) {
                if (cjRecordManager == null) {
                    cjRecordManager = new CjRecordManager();
                }
            }
        }
        return cjRecordManager;
    }

    /**
     * get cangjie record service
     *
     * @param processId process id
     * @return CjRecordService
     */
    public Optional<CjRecordService> getCjRecordService(int processId) {
        if (!cjRecordServiceMap.containsKey(processId)) {
            LOGGER.warn("Failed to get cangjie record service. Instance is {}.", processId);
            return Optional.empty();
        }
        return Optional.of(cjRecordServiceMap.get(processId));
    }

    /**
     * creatCjRecordService
     *
     * @param pid pid
     */
    public void creatCjRecordService(int pid) {
        synchronized (LOCK) {
            cjRecordServiceMap.computeIfAbsent(pid, k -> new CjRecordService(pid));
        }
    }

    /**
     * get cangjie memory service
     *
     * @param sessionId sessionId
     * @return CjMemoryService
     */
    public CjMemoryService getCjMemoryService(String sessionId) {
        if (!cjMemoryServiceMap.containsKey(sessionId)) {
            recoverCjMemoryService(sessionId);
        }
        return cjMemoryServiceMap.get(sessionId);
    }

    /**
     * creatCjMemoryService
     *
     * @param sessionId sessionId
     * @return CjMemoryService
     */
    public CjMemoryService creatCjMemoryService(String sessionId) {
        synchronized (LOCK) {
            if (!cjMemoryServiceMap.containsKey(sessionId)) {
                LOGGER.info("creat cangjie memory service. sessionId: {}", sessionId);
                cjMemoryServiceMap.put(sessionId, new CjMemoryService());
            }
        }
        return cjMemoryServiceMap.get(sessionId);
    }

    private boolean recoverCjMemoryService(String sessionId) {
        CjMemoryService memoryService = creatCjMemoryService(sessionId);
        List<TraceFile> traceFiles = JsTraceFileService.getInstance().getTraceFile(sessionId);
        if (traceFiles == null || traceFiles.isEmpty()) {
            return false;
        }

        if (Objects.equals(traceFiles.get(0).getType(), ArkConstants.TYPE_CJPROF_HEAP_SNAPSHOT)) {
            long startTime = System.currentTimeMillis();
            List<String> filePathList = traceFiles.stream().map(TraceFile::getPath).collect(Collectors.toList());
            memoryService.parseCjprofHeapSnapshotFiles(sessionId, startTime, filePathList, false);
            return true;
        }
        for (TraceFile traceFile : traceFiles) {
            RawHeapSnapshot rawHeapSnapshot = memoryService.handlerTraceFile(traceFile.getPath());
            int tid = JSTraceParser.getTidFromProfilePath(traceFile.getPath());
            memoryService.setStartRecordTimeMap(tid, rawHeapSnapshot.getStartTime());
            memoryService.parseArkHeapTimeline(sessionId, tid, rawHeapSnapshot, true);
        }
        return true;
    }

    /**
     * initData
     *
     * @param mode  cpu/heap
     * @param sessionId sessionId
     */
    public void initData(ArkExecuteType mode, String sessionId) {
        currentMode = mode;
        currentSession = sessionId;
    }

    /**
     * executeCangjieTask
     *
     * @param request ArkExecuteRequest
     * @param action action
     * @param params params
     * @return result true/false
     */
    public boolean executeCJTask(ArkExecuteRequest request, ArkExecuteType action, JSONObject params) {
        // Records instances in the current session when stop
        if (action == ArkExecuteType.PROFILER_STOP || action == ArkExecuteType.HEAP_PROFILER_STOP) {
            currentMode = ArkExecuteType.UNKNOWN;
            OpenHarmonyDeviceManager.getInstance().getDeviceByKey(request.getDeviceKey()).ifPresent(iHarmonyDevice ->
                    iHarmonyDevice.wakeUpProcess(request.getPid()));
        }
        List<Future<Boolean>> futureList = new ArrayList<>();
        Callable<Boolean> task = () -> {
            try {
                Optional<CjRecordService> serviceOpt = getCjRecordService(request.getPid());
                return serviceOpt.filter(cjRecordService ->
                        executeTask(cjRecordService, request.getSessionId(), action, params)).isPresent();
            } catch (PersistenceException e) {
                LOGGER.warn("Failed to execute cangjie task, cause PersistenceException occurred {}.", e.getMessage());
                return false;
            } catch (ProfilerException e) {
                LOGGER.warn("Failed to execute executeCJTask, ProfilerException occurred: ", e);
                return false;
            }
        };
        if (ApplicationManager.getApplication() != null) {
            Future<Boolean> future = ApplicationManager.getApplication().executeOnPooledThread(task);
            futureList.add(future);
        }
        boolean isSuccessFul = isResultValid(futureList);
        // 在数据完全返回后，在执行detach
        if (action == ArkExecuteType.PROFILER_STOP || action == ArkExecuteType.HEAP_PROFILER_STOP) {
            detachApp(request);
        }
        return isSuccessFul;
    }

    private boolean executeTask(CjRecordService service, String sessionId, ArkExecuteType action, JSONObject params) {
        switch (action) {
            case HEAP_PROFILER_START -> {
                boolean isTrackAllocations = false;
                if (params.containsKey("trackAllocations")) {
                    isTrackAllocations = params.getBoolean("trackAllocations");
                }
                return service.startCjHeapTimeLine(isTrackAllocations);
            }
            case HEAP_PROFILER_STOP -> {
                return service.stopCjHeapTimeLine(currentSession, params.getLong("startRecordTime"));
            }
            case HEAP_PROFILER_PARSE -> {
                Optional<RawHeapSnapshot> heapSnapshot = service.getRawHeapSnapshot();
                if (heapSnapshot.isPresent()) {
                    CjMemoryService cjMemoryService = creatCjMemoryService(sessionId);
                    cjMemoryService.setStartRecordTimeMap(service.getTid(), service.getPluginStartTime());
                    heapSnapshot.get().setStartTime(cjMemoryService.getStartRecordTime(service.getTid()));
                    return cjMemoryService.parseArkHeapTimeline(sessionId, service.getTid(), heapSnapshot.get(),
                            false);
                }
                return false;
            }
            case HEAP_USAGE_START -> {
                return service.startCjHeapUsageTimer();
            }
            case HEAP_USAGE_STOP -> {
                return service.stopCjHeapUsageTimerAndSaveData(currentSession, params.getLong("startRecordTime"));
            }
            default -> {
                LOGGER.warn("Unmatched action in executeTask.");
                return false;
            }
        }
    }

    private boolean isResultValid(List<Future<Boolean>> futureList) {
        return futureList.stream().allMatch(future -> {
            try {
                return future.get(ARK_REPLY_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn("Failed to validate result, exception is {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * detachApp
     *
     * @param request ArkExecuteRequest
     */
    private void detachApp(ArkExecuteRequest request) {
        Optional<IHarmonyDevice> device = OpenHarmonyDeviceManager.getInstance().getDeviceByKey(request.getDeviceKey());
        if (device.isEmpty()) {
            LOGGER.warn("Failed to detach app, cause device is offline.");
            return;
        }
        device.get().detachProcess(request.getBundleName());
    }

    /**
     * release cangjie memory service
     *
     * @param sessionId sessionId
     */
    public void releaseCjMemoryService(String sessionId) {
        LOGGER.info("release cangjie memory service. sessionId: {}", sessionId);
        CjMemoryService service = cjMemoryServiceMap.remove(sessionId);
        if (service != null) {
            service.releaseResources();
        }
    }

    /**
     * releaseAllMemoryResources 清除缓存，供profiler调用
     *
     * @return boolean xxx
     */
    public boolean releaseAllMemoryResources() {
        cjMemoryServiceMap.values().forEach(CjMemoryService::releaseResources);
        cjMemoryServiceMap.clear();
        cjRecordServiceMap.clear();
        return true;
    }
}
