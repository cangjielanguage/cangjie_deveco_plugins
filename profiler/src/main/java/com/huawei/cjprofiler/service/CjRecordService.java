/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.cjprofiler.ability.arkmemory.EventKey;
import com.huawei.cjprofiler.ability.arkmemory.parser.CjMemoryParser;
import com.huawei.cjprofiler.dao.CjVmProfilerDao;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapUsages;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.connect.connectserver.ConnectServerWrapper;
import com.huawei.deveco.insight.ohos.ability.connect.devtools.DevToolsServiceWrapper;
import com.huawei.deveco.insight.ohos.ability.schedule.ScheduledTaskManager;
import com.huawei.deveco.insight.ohos.common.NotificationError;
import com.huawei.deveco.insight.ohos.model.dto.request.common.CommonExecuteRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.common.CommonQueryRequest;
import com.huawei.deveco.insight.ohos.service.ark.RecordServiceBase;
import com.huawei.deveco.insight.ohos.utils.BalloonNotification;
import com.huawei.deveco.insight.ohos.utils.ConfigBundle;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.panda.websocket.protocol.types.runtime.HeapUsage;
import com.huawei.deveco.panda.websocket.services.PandaDevToolsService;
import com.huawei.deveco.panda.websocket.services.exceptions.PandaDevToolsInvocationException;

import com.intellij.notification.NotificationType;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * cangjie record service
 *
 * @since 2025/02/08/16:55
 */
public class CjRecordService extends RecordServiceBase {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjRecordService.class);

    /**
     * CJ_HEAP_USAGE_LOCK
     */
    protected static final ReadWriteLock CJ_HEAP_USAGE_LOCK = new ReentrantReadWriteLock();

    /**
     * CJ_HEAP_STATS_LOCK
     */
    protected static final ReadWriteLock CJ_HEAP_STATS_LOCK = new ReentrantReadWriteLock();

    /**
     * HEAP_USAGE_FREQUENCY
     */
    protected static final int INIT_HEAP_USAGE_FREQUENCY = 0;

    private static AtomicBoolean isSnapshotSuccess = new AtomicBoolean(true);

    /**
     * scheduledTaskManager
     */
    protected final ScheduledTaskManager scheduledTaskManager = ScheduledTaskManager.getInstance();

    /**
     * isMemoryRunning
     */
    protected volatile boolean isMemoryRunning = false;

    public CjRecordService(int pid) {
        super(pid, pid);
    }

    /**
     * init
     */
    public static void init() {
        isSnapshotSuccess.set(true);
    }

    /**
     * Periodically collects snapshot information.
     *
     * @param request CommonExecuteRequest
     * @return boolean true/false
     */
    public synchronized boolean takeHeapSnapshot(CommonExecuteRequest request) {
        try {
            if (getDevToolsService().isEmpty()) {
                LOGGER.warn("Failed to task heapSnapshot, cause cangjie connection doesn't exist. pid is {}.", pid);
                return false;
            }
            if (!isSnapshotSuccess.get()) {
                LOGGER.warn("Failed to task CjHeapSnapshot, cause cangjie snapshot failed before.");
                return false;
            }
            long startTime = System.currentTimeMillis();
            CompletableFuture<RawHeapSnapshot> completableFuture = startTakeHeapSnapshot(request.getIsFilterNumber());
            RawHeapSnapshot rawHeapSnapshot = completableFuture.get(WS_REPLY_TIMEOUT_SMALLER, TimeUnit.SECONDS);
            rawHeapSnapshot.setStartTime(startTime);
            CjMemoryService cjMemoryService = CjRecordManager.getInstance()
                .creatCjMemoryService(request.getSessionId());
            cjMemoryService.parseArkHeapSnapshot(request.getSessionId(), request.getPid(), rawHeapSnapshot,
                request.getIsFilterNumber());
            return true;
        } catch (TimeoutException e) {
            LOGGER.warn("Failed to take heapSnapshot, cause: {}. Tid is {}.", e.getMessage(), currentTid);
            BalloonNotification.show(ConfigBundle.getInstance().getBundleMessage("ark.snapshot.timeout"),
                NotificationType.WARNING);
            isSnapshotSuccess.set(false);
            return false;
        } catch (ExecutionException | InterruptedException | PandaDevToolsInvocationException e) {
            LOGGER.warn("Failed to take heapSnapshot, cause: {}. Tid is {}.", e.getMessage(), currentTid);
            return false;
        } catch (OutOfMemoryError e) {
            LOGGER.warn("Failed to take heapSnapshot, cause: {}. Tid is {}.", e.getMessage(), currentTid);
            BalloonNotification.show("Low Memory", NotificationError.SNAPSHOT_OUT_OF_MEMORY_ERROR.getContent(),
                NotificationType.ERROR);
            return false;
        }
    }

    /**
     * Start takeHeapSnapshot.
     *
     * @param isFilterNumber isFilterNumber
     * @return future CompletableFuture<RawHeapSnapshot>
     */
    protected CompletableFuture<RawHeapSnapshot> startTakeHeapSnapshot(boolean isFilterNumber) {
        if (context != null) {
            context = null;
            LOGGER.warn("The method 'takeHeapSnapshot' is running");
        }
        enableEventHeapProfiler();
        handleRawHeapSnapshot();
        getDevToolsService().ifPresent(service -> service.getHeapProfiler().takeHeapSnapshot(isFilterNumber));
        return future;
    }

    /**
     * Obtains the real-time cangjie memory usage and periodically collects heapUsage.
     *
     * @return true/false
     */
    public synchronized boolean startCjHeapUsageTimer() {
        stopCjHeapUsageTimer();
        LOGGER.info("Start cangjie heapUsage timer, tid is {}.", currentTid);
        this.isInsertHeapUsage = false;
        Optional<Runnable> task = createHeapUsageRunnable();
        if (task.isEmpty()) {
            LOGGER.warn("Failed to start cangjie heap usage timer, cause the connection doesn't exist. Tid is {}.",
                currentTid);
            return false;
        }
        String taskKey = String.format(Locale.ENGLISH, TASK_KEY, EventKey.CJ_HEAP_USAGE, currentTid);
        scheduledTaskManager.addTask(taskKey, task.get());
        scheduledTaskManager.scheduleTaskWithFixedDelay(taskKey, INIT_HEAP_USAGE_FREQUENCY, HEAP_USAGE_FREQUENCY,
            TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * createHeapUsageRunnable
     *
     * @return Optional<Runnable>
     */
    private Optional<Runnable> createHeapUsageRunnable() {
        if (getDevToolsService().isEmpty()) {
            LOGGER.warn("Failed to start heap usage timer, cause the connection doesn't exist. Tid is {}.", currentTid);
            return Optional.empty();
        }
        Runnable runnable = () -> {
            HeapUsage usage;
            try {
                CompletableFuture<HeapUsage> heapUsageCompletableFuture = CompletableFuture.supplyAsync(
                    () -> getDevToolsService().get().getRuntime().getHeapUsage());
                usage = heapUsageCompletableFuture.get(WS_REPLY_TIMEOUT, TimeUnit.SECONDS);

                if (usage == null) {
                    addPreUsage();
                } else {
                    HeapUsages usages = new HeapUsages();
                    usages.setTotalSize(usage.getTotalSize());
                    usages.setUsedSize(usage.getUsedSize());
                    usages.setTimestamp(System.currentTimeMillis());
                    addHeapUsage(usages);
                }
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                // 如果采集失败了，取上次采集的数据填充
                addPreUsage();
            }
        };
        return Optional.of(runnable);
    }

    private void addPreUsage() {
        if (!heapUsagesList.isEmpty()) {
            HeapUsages preUsages = heapUsagesList.get(heapUsagesList.size() - 1);
            HeapUsages usages = new HeapUsages(preUsages.getUsedSize(), preUsages.getTotalSize(),
                System.currentTimeMillis());
            addHeapUsage(usages);
        } else {
            HeapUsages usages = new HeapUsages(0D, 0D, System.currentTimeMillis());
            addHeapUsage(usages);
        }
    }

    private void addHeapUsage(HeapUsages usages) {
        getHeapUsageLock().writeLock().lock();
        try {
            heapUsagesList.add(usages);
        } finally {
            getHeapUsageLock().writeLock().unlock();
        }
    }

    /**
     * Stop periodically obtaining the real-time memory usage of the cangjie.
     *
     * @param startRecordTime long
     * @param sessionId sessionId
     * @return true/false
     */
    public boolean stopCjHeapUsageTimerAndSaveData(String sessionId, long startRecordTime) {
        stopCjHeapUsageTimer();
        return handleAllHeapUsage(sessionId, startRecordTime);
    }

    /**
     * 停止heapUsage采集
     */
    public synchronized void stopCjHeapUsageTimer() {
        LOGGER.info("Stop cangjie heapUsage timer, tid is {}.", currentTid);
        String taskKey = String.format(Locale.ENGLISH, TASK_KEY, EventKey.CJ_HEAP_USAGE, currentTid);
        if (scheduledTaskManager.checkExecutorService(taskKey).isPresent()) {
            scheduledTaskManager.shutdownNow(taskKey);
            scheduledTaskManager.removeTask(taskKey);
        }
    }

    /**
     * Start cangjie heapTimeLine.
     *
     * @param isTrackAllocations boolean
     * @return result boolean
     */
    public boolean startCjHeapTimeLine(boolean isTrackAllocations) {
        this.isTrackAllocations = isTrackAllocations;
        if (isMemoryProfiling()) {
            LOGGER.warn("The cangjie heap profiler is running. Tid is {}.", currentTid);
        }
        this.isMemoryRunning = true;
        if (context != null) {
            context = null;
            LOGGER.warn("The method 'takeHeapSnapshot' is running.");
        }
        Optional<PandaDevToolsService> devToolsService = getDevToolsService();
        if (devToolsService.isEmpty() || devToolsService.get().isSessionClosed()) {
            LOGGER.warn("Failed to start cangjie heapTimeline, cause the connection doesn't exist. Tid is {}.",
                currentTid);
            return false;
        }
        try {
            enableEventHeapProfiler();
            enableEventHeapStatsUpdate();
            handleRawHeapSnapshot();
            devToolsService.get().getHeapProfiler().startTrackingHeapObjects(isTrackAllocations);
            return true;
        } catch (PandaDevToolsInvocationException e) {
            this.isMemoryRunning = false;
            LOGGER.warn("Failed to start cangjie heap timeline, cause: {}. Tid is {}.", e.getMessage(), currentTid);
            return false;
        }
    }

    /**
     * Import all heapUsage data to the database and delete cached data.
     *
     * @param startRecordTime long
     * @param sessionId sessionId
     * @return true/false
     */
    private boolean handleAllHeapUsage(String sessionId, long startRecordTime) {
        CJ_HEAP_USAGE_LOCK.writeLock().lock();
        try {
            heapUsagesList.forEach(heapUsages -> {
                if (heapUsages.getTimestamp() >= startRecordTime
                    && heapUsages.getTimestamp() <= System.currentTimeMillis()) {
                    heapUsages.setTimestamp(TimeUnit.MILLISECONDS.toNanos(heapUsages.getTimestamp()
                        - startRecordTime));
                }
            });
        } finally {
            CJ_HEAP_USAGE_LOCK.writeLock().unlock();
        }
        if (!insertHeapUsages(sessionId)) {
            LOGGER.warn("Failed to insert cangjie heapUsages to database. Tid is {}.", currentTid);
            return false;
        }
        this.isInsertHeapUsage = true;
        heapUsagesList.clear();
        return true;
    }

    private boolean isMemoryProfiling() {
        return this.isMemoryRunning;
    }

    /**
     * Stop cangjie heapTimeLine.
     *
     * @param startRecordTime start record time
     * @param sessionId sessionId
     * @return CompletableFuture<RawHeapSnapshot>
     */
    public boolean stopCjHeapTimeLine(String sessionId, long startRecordTime) {
        if (isMemoryProfiling()) {
            isMemoryRunning = false;
            handleAllHeapStats(sessionId, startRecordTime);
            Optional<PandaDevToolsService> service = getDevToolsService();
            if (service.isEmpty() || service.get().isSessionClosed()) {
                LOGGER.warn("Failed to stop cangjie heapTimeline, cause the connection doesn't exist. Tid is {}.",
                    currentTid);
                return true;
            }
            try {
                service.get().getHeapProfiler().stopTrackingHeapObjects();
                future.get(WS_REPLY_TIMEOUT_SMALLER, TimeUnit.SECONDS);
                return true;
            } catch (PandaDevToolsInvocationException | ExecutionException | InterruptedException
                | TimeoutException e) {
                LOGGER.warn("Failed to stop cangjie heapTimeLine, cause: {}. Tid is {}.", e.getMessage(), currentTid);
                return false;
            }
        }
        LOGGER.warn("The cangjie memory profiler is not enabled.");
        return false;
    }

    /**
     * Obtains real-time cangjie memory usage information.
     *
     * @param request CommonQueryRequest
     * @return heapUsagesList List<HeapUsages>
     */
    public List<HeapUsages> queryCjHeapUsages(CommonQueryRequest request) {
        if (!this.isInsertHeapUsage && !heapUsagesList.isEmpty()) {
            long startRecordTime = request.getStartRecordTime();
            long startTime = TimeUnit.NANOSECONDS.toMillis(request.getStartTime()) + startRecordTime;
            long endTime = TimeUnit.NANOSECONDS.toMillis(request.getEndTime()) + startRecordTime;
            List<HeapUsages> usagesList;
            CJ_HEAP_USAGE_LOCK.writeLock().lock();
            try {
                usagesList = heapUsagesList.stream()
                    .filter(object -> object.getTimestamp() >= startTime && object.getTimestamp() <= endTime)
                    .map(item -> new HeapUsages(item.getUsedSize(), item.getTotalSize(),
                        TimeUnit.MILLISECONDS.toNanos(item.getTimestamp() - startRecordTime)))
                    .collect(Collectors.toList());
            } finally {
                CJ_HEAP_USAGE_LOCK.writeLock().unlock();
            }
            return usagesList;
        }
        return CjVmProfilerDao.getInstance()
            .selectCjHeapUsages(request.getSessionId(), request.getTid(), request.getStartTime(), request.getEndTime());
    }

    /**
     * get DevTools Service
     *
     * @return Optional<ChromeDevToolsService>
     */
    @Override
    public Optional<PandaDevToolsService> getDevToolsService() {
        Optional<DevToolsServiceWrapper> devToolsServiceWrapper = ConnectServerWrapper.getInstance()
            .getConnectServerDevToolsWrapper(pid);
        return devToolsServiceWrapper.map(DevToolsServiceWrapper::getService);
    }

    @Override
    protected CustomThread createHandleSnapshotThread() {
        return new CustomThread("handleRawHeapSnapshot", () -> {
            try {
                RawHeapSnapshot rawHeapSnapshot = new CjMemoryParser().parseJson(context);
                future.complete(rawHeapSnapshot);
            } catch (IllegalStateException e) {
                future.completeExceptionally(e);
            } finally {
                context = null;
            }
        });
    }

    @Override
    protected ReadWriteLock getHeapUsageLock() {
        return CJ_HEAP_USAGE_LOCK;
    }

    @Override
    protected ReadWriteLock getHeapStatsLock() {
        return CJ_HEAP_STATS_LOCK;
    }

    @Override
    protected boolean insertHeapStats(String sessionId) {
        return CjVmProfilerDao.getInstance().insertCjHeapStats(sessionId, pid, heapStatsMap, CJ_HEAP_STATS_LOCK);
    }

    @Override
    protected boolean insertHeapUsages(String sessionId) {
        return CjVmProfilerDao.getInstance().insertCjHeapUsages(sessionId, pid, heapUsagesList, CJ_HEAP_USAGE_LOCK);
    }
}
