/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.cjprofiler.ability.arkmemory.entity.CjStack;
import com.huawei.cjprofiler.ability.arkmemory.parser.CjMemoryDataService;
import com.huawei.cjprofiler.common.enums.RootTypeEnum;
import com.huawei.cjprofiler.dao.CjVmProfilerDao;
import com.huawei.cjprofiler.model.po.cjprof.HeapThreadInfo;
import com.huawei.cjprofiler.model.po.cjprof.StackFrame;
import com.huawei.cjprofiler.model.vo.cjprof.CjMemoryBaseMapper;
import com.huawei.cjprofiler.utils.PageToolsForCjprof;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotConstructorNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDetailNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDiffNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotInstanceNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotRetainerNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotRootNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.arkmemory.instancefilters.MemoryObject;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.common.constant.ArkConstants;
import com.huawei.deveco.insight.ohos.common.constant.CmdConstants;
import com.huawei.deveco.insight.ohos.common.enums.ArkMemoryTypeEnum;
import com.huawei.deveco.insight.ohos.common.enums.UnitKey;
import com.huawei.deveco.insight.ohos.model.bo.LruLinkedHashMap;
import com.huawei.deveco.insight.ohos.model.bo.TraceFile;
import com.huawei.deveco.insight.ohos.model.dto.Time;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkGcRootPathExpandRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkHeapNodeExpandRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkHeapNodeSearchRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkSnapshotComparisonRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkSnapshotParseRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.common.CommonQueryRequest;
import com.huawei.deveco.insight.ohos.service.ark.MemoryServiceBase;
import com.huawei.deveco.insight.ohos.service.ark.SnapshotMessageBuffer;
import com.huawei.deveco.insight.ohos.service.file.JsTraceFileService;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.security.HashUtil;
import com.huawei.deveco.insight.ohos.utils.singleton.SingletonContainer;

import com.cjprof.jni.Cjprof;
import com.cjprof.jni.model.ConstructorDiffNode;
import com.cjprof.jni.model.ConstructorNode;
import com.cjprof.jni.model.InstanceDiffNode;
import com.cjprof.jni.model.InstanceNode;
import com.cjprof.jni.model.JsHeapSnapshotConstructorDiffNode;
import com.cjprof.jni.model.ThreadInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * ArkMemoryService parse the original Ark data.
 *
 * @since 2022-06-19
 */
@NoArgsConstructor
public class CjMemoryService extends MemoryServiceBase {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjMemoryService.class);

    private static final String CJ_HEAP_SNAPSHOT_SUFFIX_NAME = ".cjheapsnapshot";

    private static final String CJ_HEAP_TIMELINE_SUFFIX_NAME = ".cjheaptimeline";

    /**
     * WS_REPLY_TIMEOUT
     */
    private static final int WS_REPLY_TIMEOUT = 60;

    private static final String CANGJIE_RAW_SNAPSHOT_DATA_PREFIX = "raw_cangjie_heap_snapshot";

    private static final int CJPROF_ALL_PATH_NUM = -1;

    private static final String NODE_TYPE_ARRAY = "array";

    /**
     * indexToIdMap
     */
    private final Map<Integer, Long> indexToIdMap = new HashMap<>();

    // nodeId -> rootType.value
    private final Map<Integer, String> rootTypeMap = new HashMap<>();

    // nodeId -> arrayLength
    private final Map<Integer, Integer> arrayLengthMap = new HashMap<>();

    // rawId -> List<HeapThreadInfo>
    private final Map<String, List<HeapThreadInfo>> heapThreadMap = new HashMap<>();

    /**
     * isCjprof
     */
    @Getter
    private boolean isCjprof = false;

    /**
     * CjTraceTree info， key:tid
     */
    private final HashMap<Integer, List<CjStack>> cjTraceTreeMap = new HashMap<>();

    /**
     * snapshotIdMap， key:rawId , value: cjprofSnapshotId
     */
    private final HashMap<String, Long> snapshotIdMap = new HashMap<>();

    /**
     * Snapshot comparison view list
     */
    private final LruLinkedHashMap<String, List<JsHeapSnapshotConstructorDiffNode>> cjComparisonMap =
        new LruLinkedHashMap<>();

    /**
     * parse Ark HeapSnapshot
     *
     * @param sessionId sessionId
     * @param tid tid
     * @param rawHeapSnapshot RawHeapSnapshot
     * @param isFilterNumber isFilterNumber
     * @return successful/fail
     */
    public boolean parseArkHeapSnapshot(String sessionId, int tid, RawHeapSnapshot rawHeapSnapshot,
        boolean isFilterNumber) {
        if (rawHeapSnapshot == null) {
            LOGGER.warn("Failed to parse ark heap snapshot, cause snapshot is null.");
            return false;
        }
        long startTime = rawHeapSnapshot.getStartTime();
        CompletableFuture.runAsync(
                () -> {
                    savaTraceFile(startTime, rawHeapSnapshot, CJ_HEAP_SNAPSHOT_SUFFIX_NAME, sessionId, tid);
                }, EXECUTOR)
            .exceptionally(e -> {
                LOGGER.warn("Failed to save trace file asynchronously, error is {}.", e.getMessage());
                return null;
            });
        initRootTypeMap(rawHeapSnapshot);
        initArrayLength(rawHeapSnapshot);
        initHeapThreadDetail(rawHeapSnapshot);
        saveHeapSnapshotCacheData(rawHeapSnapshot, System.currentTimeMillis(), null, "", isFilterNumber);
        return true;
    }

    private void initHeapThreadDetail(RawHeapSnapshot rawHeapSnapshot) {
        List<HeapThreadInfo> heapThreadInfos = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);
        heapThreadMap.put(String.valueOf(rawHeapSnapshot.getStartTime()), heapThreadInfos);
    }

    /**
     * getDataTypeBySuffix
     *
     * @param suffixName String
     * @return String
     */
    protected String getDataTypeBySuffix(String suffixName) {
        if (CJ_HEAP_SNAPSHOT_SUFFIX_NAME.equals(suffixName)) {
            return UnitKey.CJ_HEAP_SNAPSHOT.getType();
        } else {
            return UnitKey.CJ_HEAP_TIMELINE.getType();
        }
    }

    private void initTraceTreeMap(RawHeapSnapshot heapSnapshot, int tid) {
        List<Object> traceTree = heapSnapshot.getTraceTree();
        if (traceTree != null && !traceTree.isEmpty()) {
            CjStack stack = new CjStack();
            stack.setName("root");
            stack.setChildren(new ArrayList<>());
            handleCjStackTree(tid, convertToObjList(traceTree.get(0)), stack);
            cjTraceTreeMap.put(tid, stack.getChildren());
        }
    }

    /**
     * 获取HeapProfileNode assignStack Info By NodeId
     *
     * @param pid pid
     * @return List<CjStack>
     */
    public List<CjStack> getStackList(int pid) {
        return cjTraceTreeMap.get(pid);
    }

    private void handleCjStackTree(int tid, List<Object> prop, CjStack parentStack) {
        int index = 0;
        while (index < prop.size()) {
            int traceTreeId = 0;
            if (prop.get(index) instanceof String) {
                try {
                    traceTreeId = Integer.parseInt((String) prop.get(index));
                } catch (NumberFormatException e) {
                    LOGGER.warn("TraceTreeId parseInt Failed: {}.");
                }
            }
            int functionInfoIndex = 0;
            if (prop.get(index + 1) instanceof String) {
                try {
                    functionInfoIndex = Integer.parseInt((String) prop.get(index + 1));
                } catch (NumberFormatException e) {
                    LOGGER.warn("FunctionInfoIndex parseInt Failed: {}.");
                }
            }
            int size = 0;
            if (prop.get(index + traceTreeSizeOffset) instanceof String) {
                try {
                    size = Integer.parseInt((String) prop.get(index + traceTreeSizeOffset));
                } catch (NumberFormatException e) {
                    LOGGER.warn("size parseInt Failed: {}.");
                }
            }
            CjStack cjStack = new CjStack();
            cjStack.setNodeId(traceTreeId);
            cjStack.setFunctionInfoIndex(functionInfoIndex);
            cjStack.setName(getStrings(getTraceFunctionInfo(functionInfoIndex,
                traceFunctionInfoNameOffset, tid), tid));
            String scriptName = getStrings(getTraceFunctionInfo(functionInfoIndex,
                traceFunctionInfoScriptNameOffset, tid), tid);
            if (StringUtils.isEmpty(scriptName)) {
                cjStack.setScriptName(scriptName);
            } else {
                String[] parts = scriptName.split("/");
                cjStack.setScriptName(parts[parts.length - 1]);
            }
            cjStack.setRootPath(scriptName);
            cjStack.setLine(
                getTraceFunctionInfo(functionInfoIndex, traceFunctionInfoLineOffset, tid));
            cjStack.setColumn(
                getTraceFunctionInfo(functionInfoIndex, traceFunctionInfoColumnOffset, tid));
            cjStack.setSize(size);
            parentStack.getChildren().add(cjStack);
            handleCjStackTree(tid, convertToObjList(prop.get(index + traceTreeChildOffset)), cjStack);
            index += traceNodeFieldCount;
        }
    }

    /**
     *  Process ark heapTimeline
     *
     * @param sessionId sessionId
     * @param tid tid
     * @param heapSnapshot RawHeapSnapshot
     * @param isImport boolean
     * @return boolean true/false
     */
    public boolean parseArkHeapTimeline(String sessionId, int tid, RawHeapSnapshot heapSnapshot, boolean isImport) {
        synchronized (LOCK) {
            heapTimelineRootMap.put(tid, new JsHeapSnapshotRootNode(ARK_HEAP_TIMELINE, heapSnapshot));
            initParams(tid);
            processSamples(tid);
            initTraceTreeMap(heapSnapshot, tid);
            if (!isImport) {
                long currentTraceId = System.currentTimeMillis();
                String suffixName = CJ_HEAP_TIMELINE_SUFFIX_NAME;
                savaTraceFile(currentTraceId, heapSnapshot, suffixName, sessionId, tid);
            }
        }
        return true;
    }

    /**
     * All heap snapshot data is imported to the database and cached data is cleared.
     *
     * @param sessionId String
     * @return result boolean
     */
    public boolean handleAllSnapshot(String sessionId) {
        // 处理snapshot缓存数据后，即不再从缓存中查询数据
        isInsertHeapSnapshot = true;
        if (CollectionUtils.isNotEmpty(snapshotList)) {
            List<HeapSnapshot> snapshotData = new ArrayList<>();
            for (HeapSnapshot data : snapshotList) {
                boolean isFilterNumber = data.getIsFilterNumber() != null && data.getIsFilterNumber();
                snapshotData.add(new HeapSnapshot(data.getId(), data.getRawId(), data.getTimestamp(),
                    data.getDuration(), data.getFileSize(), data.getFileName(), data.getHash(), isFilterNumber,
                    ArkConstants.TYPE_DEFAULT));
            }
            SingletonContainer.getInstance(SnapshotMessageBuffer.class)
                .saveAndClear(CANGJIE_RAW_SNAPSHOT_DATA_PREFIX, sessionId, CmdConstants.TRACE_SUFFIX);
            LOGGER.info("JsMemoryParserRawService.handleAllSnapshot remove sessionId: {}", sessionId);
            return CjVmProfilerDao.getInstance().insertCjHeapDumps(sessionId, snapshotData);
        }
        return true;
    }

    /**
     * Get all heap snapshot data
     *
     * @param request CommonQueryRequest
     * @return heapSnapshotList List<HeapSnapshot>
     */
    public List<HeapSnapshot> getAllSnapshot(CommonQueryRequest request) {
        long startRecordTime = request.getStartRecordTime();
        long startTime = TimeUnit.NANOSECONDS.toMillis(request.getStartTime()) + startRecordTime;
        long endTime = TimeUnit.NANOSECONDS.toMillis(request.getEndTime()) + startRecordTime;
        if (isQueryCacheSnapshot()) {
            return snapshotList.stream()
                .filter(object -> object.getRawId() + TimeUnit.NANOSECONDS.toMillis(object.getDuration()) >= startTime
                    && object.getRawId() <= endTime)
                .peek(item -> {
                    item.setTimestamp(TimeUnit.MILLISECONDS.toNanos(item.getRawId() - startRecordTime));
                })
                .collect(Collectors.toList());
        }
        return CjVmProfilerDao.getInstance()
            .selectCjHeapDumps(request.getSessionId(), request.getStartTime(), request.getEndTime());
    }

    /**
     * 解析多个堆内存快照文件
     *
     * @param request 请求参数
     * @return boolean
     */
    public boolean parseCjprofHeapSnapshotFile(ArkSnapshotParseRequest request) {
        String sessionId = request.getSessionId();
        long startTime = System.currentTimeMillis();
        List<String> filePathList = request.getFilePathList();
        return parseCjprofHeapSnapshotFiles(sessionId, startTime, filePathList, true);
    }

    /**
     * Clean cj heap snapshot files via cjprof.
     */
    public void cleanCjHeapSnapshotFiles() {
        if (!isCjprof) {
            return;
        }
        List<Long> snapshotIds = getAllCjprofSnapshotIds();
        Cjprof.cleanHeapSnapshotFiles(snapshotIds);
    }

    /**
     * 释放临时文件和堆快照文件资源
     */
    public void releaseResources() {
        cleanTemporaryFiles();
        cleanCjHeapSnapshotFiles();
    }

    /**
     * parseCjprofHeapSnapshotFiles
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param filePathList filePathList
     * @param saveTraceFile saveTraceFile
     * @return boolean
     */
    public boolean parseCjprofHeapSnapshotFiles(String sessionId, long startTime, List<String> filePathList,
            boolean saveTraceFile) {
        isCjprof = true;
        if (CollectionUtils.isEmpty(filePathList)) {
            throw new ProfilerException(ProfilerError.REQUEST_PARAMETER_ERROR,
                    "Failed to parse heap snapshot files, cause: filePathList is empty.");
        }
        boolean parseSuccess = parseHeapSnapshotFilesSafely(filePathList);
        if (!parseSuccess) {
            LOGGER.warn("Failed to parse heap snapshot files via cjprof");
            return false;
        }
        LOGGER.info("Success parse heap snapshot files via cjprof");
        // 处理快照数据
        List<com.cjprof.jni.model.HeapSnapshot> cjProfHeapSnapshots = Cjprof.queryAllHeapSnapshot();
        LOGGER.info("Success queryAllHeapSnapshot via cjprof");
        long endTime = startTime;

        for (String filePath : filePathList) {
            // 在所有快照中查找与当前文件路径匹配的快照
            Optional<com.cjprof.jni.model.HeapSnapshot> matchingSnapshot = cjProfHeapSnapshots.stream()
                    .filter(snapshot -> isSamePath(filePath, snapshot.getFilePath())).findFirst();

            if (matchingSnapshot.isPresent()) {
                com.cjprof.jni.model.HeapSnapshot cjProfHeapSnapshot = matchingSnapshot.get();
                long cjprofSnapshotId = cjProfHeapSnapshot.getId();
                // 每个heap间隔1秒，持续1秒
                endTime += HEAP_SNAPSHOT_DEAL_TIME_INTERVAL;
                long rawId = endTime;
                putCjprofSnapshotId(String.valueOf(rawId), cjprofSnapshotId);
                saveSnapshotFileData(sessionId, rawId, endTime += HEAP_SNAPSHOT_DEAL_TIME_INTERVAL,
                        FileUtils.getFile(filePath), saveTraceFile);
                LOGGER.info("Added heap snapshot, filePath: {}, snapshotId: {}", filePath, rawId);
            } else {
                LOGGER.warn("No matching heap snapshot found for filePath: {}", filePath);
            }
        }

        // 更新时间范围
        parseFileTimeRange = new Time(startTime, TimeUnit.MILLISECONDS.toNanos(endTime - startTime));
        setInsertHeapSnapshot(false);
        return true;
    }

    /**
     * 安全解析堆快照文件（带超时和异常处理）
     * 
     * @param filePaths 文件路径数组
     * @return 解析是否成功
     */
    private boolean parseHeapSnapshotFilesSafely(List<String> filePaths) {
        boolean parseSuccess;
        try {
            Cjprof.initialize();
            CompletableFuture<Boolean> future =
                CompletableFuture.supplyAsync(() -> Cjprof.parseHeapSnapshotFiles(filePaths), EXECUTOR);
            parseSuccess = future.get(WS_REPLY_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOGGER.warn("Failed to parse heap snapshot files, cause: timeout. {}", e.getMessage());
            throw new ProfilerException(ProfilerError.FILE_PARSING_ERROR,
                    "Failed to parse heap snapshot files, cause: timeout");
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.warn("Failed to parse heap snapshot files, cause: {}. {}", e.getClass().getSimpleName(),
                    e.getMessage());
            parseSuccess = false;
        }
        if (!parseSuccess) {
            LOGGER.warn("Failed to parse heap snapshot files via cjprof");
        }
        return parseSuccess;
    }

    private void saveSnapshotFileData(String sessionId, long startTime, long endTime, File file,
            boolean saveTraceFile) {
        String hash = "";
        try {
            hash = HashUtil.hashWithSha256(file.toPath());
        } catch (IOException e) {
            LOGGER.warn("Generate hash error.", e);
        }
        if (saveTraceFile) {
            JsTraceFileService.getInstance().saveTraceFile(sessionId, ArkConstants.TYPE_CJPROF_HEAP_SNAPSHOT,
                    file.getPath(), startTime, file.getPath());
        }
        int suffixDotIndex = file.getName().lastIndexOf('.');
        if (suffixDotIndex <= 0) {
            throw new ProfilerException(ProfilerError.FILE_PARSING_ERROR,
                    "Failed to save snapshot file data, cause: file Name format error");
        }
        saveHeapSnapshotCacheData(startTime, endTime, file.getName().substring(0, suffixDotIndex), hash);
    }

    private void saveHeapSnapshotCacheData(long startTime, long endTime, String fileName, String hash) {
        // 保存快照数据
        List<ConstructorNode> constructorNodes =
            getAndFilterConstructorNodes(getCjprofSnapshotId(String.valueOf(startTime)));

        JsHeapSnapshotRootNode arkHeapSnapshot = new JsHeapSnapshotRootNode(fileName,
                CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorNodes(constructorNodes, indexToIdMap));
        // startTime == rawId == snapshotID
        heapSnapshotRootMap.put(String.valueOf(startTime), arkHeapSnapshot);
        // 保存快照基本信息
        long fileSize = arkHeapSnapshot.getConstructorNodes().stream()
                .mapToLong(JsHeapSnapshotConstructorNode::getShallowSize).sum() >> 10;
        long duration = TimeUnit.MILLISECONDS.toNanos(endTime - startTime);
        snapshotList.add(new HeapSnapshot((long) (snapshotList.size() + 1), startTime, startTime, duration, fileSize,
                fileName, hash, false, ArkConstants.TYPE_CJPROF_HEAP_SNAPSHOT));
    }

    /**
     * 获取并过滤className为空或空字符串的构造节点
     * 
     * @param cjprofSnapshotId 快照ID
     * @return 过滤后的构造节点列表
     */
    private List<ConstructorNode> getAndFilterConstructorNodes(long cjprofSnapshotId) {
        // 获取构造节点
        List<ConstructorNode> constructorNodes = Cjprof.getConstructorNodesBySnapshotID(cjprofSnapshotId);
        if (constructorNodes == null) {
            return Collections.emptyList();
        }

        // 过滤掉className为空或空字符串的节点
        return constructorNodes.stream().filter(node -> node != null && StringUtils.isNotBlank(node.getClassName()))
                .collect(Collectors.toList());
    }

    private Long getCjprofSnapshotId(String rawId) {
        return snapshotIdMap.get(rawId);
    }

    private void putCjprofSnapshotId(String rawId, Long cjprofSnapshotId) {
        snapshotIdMap.put(rawId, cjprofSnapshotId);
    }

    private List<Long> getAllCjprofSnapshotIds() {
        return new ArrayList<>(snapshotIdMap.values());
    }

    /**
     * getDiffView
     *
     * @param request request
     * @return List<JsHeapSnapshotDiffNode>
     */
    public List<JsHeapSnapshotDiffNode> getDiffView(ArkSnapshotComparisonRequest request) {
        if (isCjprof) {
            return getCjprofDiffView(request);
        } else {
            return super.getDiffView(request);
        }
    }

    /**
     * getCjprofDiffView
     *
     * @param request request
     * @return List<JsHeapSnapshotDiffNode>
     */
    private List<JsHeapSnapshotDiffNode> getCjprofDiffView(ArkSnapshotComparisonRequest request) {
        String baseSnapshotId = request.getBaseRawId();
        String targetSnapshotId = request.getCurRawId();
        LOGGER.info("Query cjprof snapshot comparison between {} and {}.", baseSnapshotId, targetSnapshotId);
        // 若base已是LRU首元素(即将被驱逐)，先刷新到末尾防止恢复target时被踢出
        if (!this.heapSnapshotRootMap.containsKey(baseSnapshotId)
                || this.heapSnapshotRootMap.keySet().stream().findFirst().get().equals(baseSnapshotId)) {
            this.heapSnapshotRootMap.remove(baseSnapshotId);
            this.recoverCjprofLruData(request.getSessionId(), baseSnapshotId);
        }

        if (!this.heapSnapshotRootMap.containsKey(targetSnapshotId)) {
            this.recoverCjprofLruData(request.getSessionId(), targetSnapshotId);
        }
        List<JsHeapSnapshotConstructorDiffNode> diffNodes = this.getCjprofDiffNodes(baseSnapshotId, targetSnapshotId);
        return diffNodes.stream()
                .filter(diffNode -> diffNode instanceof JsHeapSnapshotDiffNode)
                .map(diffNode -> (JsHeapSnapshotDiffNode) diffNode)
                .collect(Collectors.toList());
    }


    /**
     * queryCountOfResults
     *
     * @param request request
     * @return int
     */
    public int queryCountOfResults(ArkHeapNodeSearchRequest request) {
        if (isCjprof) {
            return queryCjprofCountOfResults(request);
        } else {
            return super.queryCountOfResults(request);
        }
    }

    /**
     * queryCjprofCountOfResults
     *
     * @param request request
     * @return int
     */
    private synchronized int queryCjprofCountOfResults(ArkHeapNodeSearchRequest request) {
        return switch (ArkMemoryTypeEnum.getTypeByValue(request.getType())) {
            case SNAPSHOT -> queryCjprofSnapshotCountOfResults(request);
            case COMPARISON -> queryCjprofComparisonCountOfResults(request);
            default -> throw new ProfilerException(ProfilerError.INVALID_TYPE, "Invalid search type");
        };
    }

    /**
     * queryResultByIndex
     *
     * @param request request
     * @return Optional<MemoryObject>
     */
    public Optional<MemoryObject> queryResultByIndex(ArkHeapNodeSearchRequest request) {
        if (isCjprof) {
            return queryCjprofResultByIndex(request);
        } else {
            return super.queryResultByIndex(request);
        }
    }

    /**
     * queryCjprofResultByIndex
     *
     * @param request request
     * @return Optional<MemoryObject>
     */
    private synchronized Optional<MemoryObject> queryCjprofResultByIndex(ArkHeapNodeSearchRequest request) {
        return switch (ArkMemoryTypeEnum.getTypeByValue(request.getType())) {
            case SNAPSHOT -> queryCjprofSnapshotNodeByIndex(request);
            case COMPARISON -> queryCjprofComparisonNodeByIndex(request);
            default -> throw new ProfilerException(ProfilerError.INVALID_TYPE, "Invalid search type");
        };
    }

    /**
     * recoverLruData
     *
     * @param sessionId sessionId
     * @param snapshotId snapshotId
     */
    protected void recoverLruData(String sessionId, String snapshotId) {
        if (isCjprof) {
            recoverCjprofLruData(sessionId, snapshotId);
        } else {
            super.recoverLruData(sessionId, snapshotId);
        }
    }

    private void recoverCjprofLruData(String sessionId, String snapshotId) {
        LOGGER.info("Recover Cjprof heap snapshot data, sessionId: {}, snapshotId: {}.", sessionId, snapshotId);
        Optional<TraceFile> traceFileOptional =
            JsTraceFileService.getInstance().querySnapshotFileInfo(sessionId, snapshotId);
        if (traceFileOptional.isEmpty()) {
            LOGGER.warn("Failed to recover Cjprof heap snapshot data, cause: trace file not found, snapshotId: {}.",
                    snapshotId);
            return;
        }
        String filePath = traceFileOptional.get().getPath();
        // 调用通用方法解析文件
        boolean parseSuccess = parseHeapSnapshotFilesSafely(Collections.singletonList(filePath));
        if (!parseSuccess) {
            LOGGER.warn("Failed to recover Cjprof heap snapshot data, cause: parse failed, snapshotId: {}.",
                    snapshotId);
            return;
        }
        // 处理快照数据
        List<com.cjprof.jni.model.HeapSnapshot> cjProfHeapSnapshots = Cjprof.queryAllHeapSnapshot();
        if (cjProfHeapSnapshots == null || cjProfHeapSnapshots.isEmpty()) {
            LOGGER.warn("Failed to recover Cjprof heap snapshot data, cause: no heap snapshot found, snapshotId: {}.",
                    snapshotId);
            return;
        }

        // 根据filePath找到对应的快照ID
        Optional<Long> optionalCjprofSnapshotId =
            cjProfHeapSnapshots.stream().filter(snapshot -> isSamePath(filePath, snapshot.getFilePath()))
                    .map(com.cjprof.jni.model.HeapSnapshot::getId).findFirst();
        if (optionalCjprofSnapshotId.isEmpty()) {
            LOGGER.warn("Failed to recover Cjprof heap snapshot data, cause: no matching snapshot found for "
                    + "filePath: {}, snapshotId: {}.", filePath, snapshotId);
            return;
        }

        long cjprofSnapshotId = optionalCjprofSnapshotId.get();
        putCjprofSnapshotId(snapshotId, cjprofSnapshotId);
        // 保存快照数据到内存
        List<ConstructorNode> constructorNodes = getAndFilterConstructorNodes(cjprofSnapshotId);
        JsHeapSnapshotRootNode jsHeapSnapshot = new JsHeapSnapshotRootNode("ArkHeapSnapshot",
                CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorNodes(constructorNodes, indexToIdMap));
        this.heapSnapshotRootMap.put(snapshotId, jsHeapSnapshot);
        LOGGER.info("Success recover Cjprof heap snapshot data, snapshotId: {}, cjprofSnapshotId: {}.", snapshotId,
                cjprofSnapshotId);
    }

    private List<ConstructorDiffNode> getAndFilterConstructorDiffNodes(Long baseCjprofId, Long targetCjprofId) {
        List<ConstructorDiffNode> diffNodes = Cjprof.querySnapshotComparison(baseCjprofId, targetCjprofId);

        if (diffNodes == null) {
            return Collections.emptyList();
        }

        // 过滤掉className为空或空字符串的节点
        return diffNodes.stream().filter(node -> node != null && StringUtils.isNotBlank(node.getClassName()))
                .collect(Collectors.toList());
    }

    private List<JsHeapSnapshotConstructorDiffNode> getCjprofDiffNodes(String baseSnapshotId, String targetSnapshotId) {
        Long baseCjprofId = getCjprofSnapshotId(baseSnapshotId);
        Long targetCjprofId = getCjprofSnapshotId(targetSnapshotId);
        if (baseCjprofId == null || targetCjprofId == null) {
            LOGGER.warn("Failed to get Cjprof diff nodes, cause: cjprofSnapshotId is null, "
                    + "baseSnapshotId: {}, targetSnapshotId: {}.", baseSnapshotId, targetSnapshotId);
            return new ArrayList<>();
        }
        LOGGER.info(
                "Query Cjprof snapshot comparison, baseSnapshotId: {}, "
                        + "targetSnapshotId: {}, baseCjprofId: {}, targetCjprofId: {}.",
                baseSnapshotId, targetSnapshotId, baseCjprofId, targetCjprofId);
        List<ConstructorDiffNode> constructorDiffNode = getAndFilterConstructorDiffNodes(baseCjprofId, targetCjprofId);
        List<JsHeapSnapshotConstructorDiffNode> comparisonList = constructorDiffNode.stream()
                .map(node -> CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorDiffNode(node,
                        new CjMemoryBaseMapper.DiffSnapshotId(baseSnapshotId, targetSnapshotId), indexToIdMap))
                .collect(Collectors.toList());
        this.cjComparisonMap.put(baseSnapshotId, comparisonList);
        LOGGER.info("Success get Cjprof diff nodes, count: {}.", comparisonList.size());
        return comparisonList;
    }

    private int queryCjprofSnapshotCountOfResults(ArkHeapNodeSearchRequest request) {
        String rawId = getCjSnapshotId(request);
        if (rawId == null || rawId.isBlank()) {
            LOGGER.warn("Failed to query Cjprof snapshot count, cause: rawId is blank.");
            return 0;
        }
        Long cjprofSnapshotId = getCjprofSnapshotId(rawId);
        if (cjprofSnapshotId == null) {
            LOGGER.warn("Failed to query Cjprof snapshot count, cause: cjprofSnapshotId is null, rawId: {}.", rawId);
            return 0;
        }
        return Cjprof.querySnapshotCountOfResults(request.getKeyword(), request.getIsIgnoreCase(), cjprofSnapshotId);
    }

    private int queryCjprofComparisonCountOfResults(ArkHeapNodeSearchRequest request) {
        List<JsHeapSnapshotConstructorDiffNode> diffNodes = cjComparisonMap.get(request.getSnapshotId());
        if (CollectionUtils.isEmpty(diffNodes)) {
            LOGGER.warn("Failed to query Cjprof comparison count, cause: diffNodes is empty, snapshotId: {}.",
                    request.getSnapshotId());
            return 0;
        }
        String targetSnapshotId = diffNodes.get(0).getJsTargetHeapSnapshotId();
        Long baseCjprofId = getCjprofSnapshotId(request.getSnapshotId());
        Long targetCjprofId = getCjprofSnapshotId(targetSnapshotId);
        if (baseCjprofId == null || targetCjprofId == null) {
            LOGGER.warn("Failed to query Cjprof comparison count, cause: cjprofSnapshotId is null, "
                    + "baseSnapshotId: {}, targetSnapshotId: {}.", request.getSnapshotId(), targetSnapshotId);
            return 0;
        }
        return Cjprof.queryComparisonCountOfResults(request.getKeyword(), request.getIsIgnoreCase(), baseCjprofId,
                targetCjprofId);
    }

    private Optional<MemoryObject> queryCjprofSnapshotNodeByIndex(ArkHeapNodeSearchRequest request) {
        String rawId = getCjSnapshotId(request);
        if (rawId == null || rawId.isBlank()) {
            LOGGER.warn("Failed to query Cjprof snapshot node by index, cause: rawId is blank.");
            return Optional.empty();
        }
        Long cjprofSnapshotId = getCjprofSnapshotId(rawId);
        if (cjprofSnapshotId == null) {
            LOGGER.warn("Failed to query Cjprof snapshot node by index, cause: cjprofSnapshotId is null, rawId: {}.",
                    rawId);
            return Optional.empty();
        }
        ConstructorNode node = Cjprof.querySnapshotNodeByIndex(request.getKeyword(), request.getIsIgnoreCase(),
                cjprofSnapshotId, request.getLength(), request.getIndex());
        if (node == null) {
            LOGGER.warn("Failed to query Cjprof snapshot node by index, cause: node is null.");
            return Optional.empty();
        }
        return Optional.ofNullable(CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorNode(node, indexToIdMap));
    }

    private Optional<MemoryObject> queryCjprofComparisonNodeByIndex(ArkHeapNodeSearchRequest request) {
        List<JsHeapSnapshotConstructorDiffNode> diffNodes = cjComparisonMap.get(request.getSnapshotId());
        if (CollectionUtils.isEmpty(diffNodes)) {
            return Optional.empty();
        }
        String targetSnapshotId = diffNodes.get(0).getJsTargetHeapSnapshotId();
        Long baseCjprofSnapshotId = getCjprofSnapshotId(request.getSnapshotId());
        Long targetCjprofSnapshotId = getCjprofSnapshotId(targetSnapshotId);
        if (baseCjprofSnapshotId == null || targetCjprofSnapshotId == null) {
            LOGGER.warn("Failed to query Cjprof comparison node by index, cause: cjprofSnapshotId is null, "
                    + "baseSnapshotId: {}, targetSnapshotId: {}.", request.getSnapshotId(), targetSnapshotId);
            return Optional.empty();
        }
        ConstructorDiffNode diffNode =
            Cjprof.queryComparisonNodeByIndex(request.getKeyword(), request.getIsIgnoreCase(), baseCjprofSnapshotId,
                    targetCjprofSnapshotId, request.getLength(), request.getIndex());
        if (diffNode == null) {
            LOGGER.warn("Failed to query Cjprof comparison node by index, cause: diffNode is null.");
            return Optional.empty();
        }
        return Optional.of(CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotDiffNode(diffNode, indexToIdMap));
    }

    /**
     * getSnapshotId
     *
     * @param request ArkHeapNodeSearchRequest
     * @return String
     */
    private String getCjSnapshotId(ArkHeapNodeSearchRequest request) {
        // 如果前端传递了snapshotId，则直接返回
        if (request.getSnapshotId() != null) {
            return request.getSnapshotId();
        }
        // 外部导入的snapshot没有入库，如果缓存存在，则从缓存查询
        if (CollectionUtils.isEmpty(snapshotList)) {
            LOGGER.warn("Failed to find Cjprof snapshot, snapshotList is empty.");
            return "";
        }

        List<HeapSnapshot> selectedSnapshots =
            snapshotList.stream().filter(object -> object.getRawId() >= request.getStartTimeAll()
                    && object.getRawId() <= request.getEndTimeAll()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(selectedSnapshots)) {
            LOGGER.warn("Failed to find Cjprof snapshot in range, {},{}.", request.getStartTimeAll(),
                    request.getEndTimeAll());
            return "";
        }
        return String.valueOf(selectedSnapshots.get(0).getRawId());
    }

    /**
     * jumpToInstanceNode
     *
     * @param request request
     * @return Optional<MemoryObject>
     */
    public Optional<MemoryObject> jumpToInstanceNode(ArkHeapNodeExpandRequest request) {
        if (isCjprof) {
            return jumpToCjprofInstanceNode(request);
        } else {
            Optional<MemoryObject> memoryObject = super.jumpToInstanceNode(request);
            memoryObject.ifPresent(this::setInstanceAttribute);
            return memoryObject;
        }
    }

    /**
     * jumpToCjprofInstanceNode
     *
     * @param request request
     * @return Optional<MemoryObject>
     */
    private Optional<MemoryObject> jumpToCjprofInstanceNode(ArkHeapNodeExpandRequest request) {
        synchronized (LOCK) {
            return this.jumpToInstanceNodeForCjprofSnapshot(request);
        }
    }

    private Optional<MemoryObject> jumpToInstanceNodeForCjprofSnapshot(ArkHeapNodeExpandRequest request) {
        Long cjprofSnapshotId = getCjprofSnapshotId(request.getRawId());
        if (cjprofSnapshotId == null) {
            LOGGER.warn("Failed to jump to instance node, cause: cjprofSnapshotId is null, rawId: {}");
            return Optional.empty();
        }
        List<ConstructorNode> constructorNodeList = Cjprof.getConstructorNodesBySnapshotID(cjprofSnapshotId);
        if (CollectionUtils.isEmpty(constructorNodeList)) {
            LOGGER.warn("Failed to jump to instance node, cause: constructorNodeList is empty, cjprofSnapshotId: {}",
                    cjprofSnapshotId);
            return Optional.empty();
        }
        for (ConstructorNode constructorNode : constructorNodeList) {
            ConstructorNode expandedConstructorNode =
                Cjprof.expandConstructorNode(cjprofSnapshotId, constructorNode.getId(), 0, Integer.MAX_VALUE);
            if (isFind(request, expandedConstructorNode)) {
                return Optional.of(PageToolsForCjprof.pageConstructorNode(request, CjMemoryBaseMapper.INSTANCE
                        .toJsHeapSnapshotConstructorNode(expandedConstructorNode, indexToIdMap)));
            }
        }
        return Optional.empty();
    }

    private boolean isFind(ArkHeapNodeExpandRequest request, ConstructorNode constructorNode) {
        List<InstanceNode> children = constructorNode.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return false;
        }
        for (int index = 0; index < children.size(); index++) {
            InstanceNode instanceNode = children.get(index);
            if (request.getNodeId().equals(instanceNode.getNodeIndex())) {
                request.setStart(index);
                return true;
            }
        }
        return false;
    }

    /**
     * expand heapNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @return myLists List<JsHeapSnapshotConstructorNode>
     */
    public MemoryObject expandHeapNode(ArkHeapNodeExpandRequest request) {
        if (isCjprof) {
            return this.expandCjprofNode(request);
        } else {
            return baseExpandHeapNode(request);
        }
    }

    /**
     * expand heapNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @return MemoryObject
     */
    public synchronized MemoryObject baseExpandHeapNode(ArkHeapNodeExpandRequest request) {
        synchronized (LOCK) {
            MemoryObject result;
            switch (request.getPathNode().size()) {
                case CONSTRUCTOR -> {
                    result = super.expandConstructor(request);
                    setInstanceAttribute(result);
                }
                case INSTANCE -> result = super.expandInstance(request, true);
                case EXPAND -> result = super.expandDetail(request, true);
                default ->
                    result = super.expandDetailAndMore(expandInstance(request, false), request.getPathNode().size(),
                        request);
            }
            return result;
        }
    }

    private MemoryObject expandCjprofNode(ArkHeapNodeExpandRequest request) {
        synchronized (LOCK) {
            List<Integer> pathNode = request.getPathNode();
            int pathSize = pathNode.size();

            return switch (pathSize) {
                case CONSTRUCTOR -> this.expandConstructor(request);
                case INSTANCE -> this.expandInstance(request, true);
                case EXPAND -> this.expandDetail(request, true);
                default -> this.expandDetailAndMore(this.expandInstance(request, false), pathSize, request);
            };
        }
    }

    /**
     * expandConstructor
     *
     * @param request ArkHeapNodeExpandRequest
     * @return MemoryObject
     */
    @Override
    protected MemoryObject expandConstructor(ArkHeapNodeExpandRequest request) {
        if (ArkMemoryTypeEnum.COMPARISON.getValue() == request.getType()) {
            return expandConstructorDiffNode(request);
        }
        JsHeapSnapshotConstructorNode constructorNode = queryConstructorNode(request);
        if (constructorNode.getChildren().isEmpty()) {
            ConstructorNode expandedConstructorNode = Cjprof.expandConstructorNode(
                snapshotIdMap.get(request.getRawId()), indexToIdMap.get(constructorNode.getId()), 0, Integer.MAX_VALUE);
            JsHeapSnapshotConstructorNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorNode(
                expandedConstructorNode, indexToIdMap);
            constructorNode.setChildren(tmp.getChildren());
            CjMemoryTransService.buildPathNodeFirstLevel(constructorNode);
        }
        return PageToolsForCjprof.pageConstructorNode(request, constructorNode);
    }

    /**
     * expandInstance
     *
     * @param request ArkHeapNodeExpandRequest
     * @param isPaged boolean
     * @return JsHeapSnapshotInstanceNode
     */
    @Override
    protected JsHeapSnapshotInstanceNode expandInstance(ArkHeapNodeExpandRequest request, boolean isPaged) {
        if (ArkMemoryTypeEnum.COMPARISON.getValue() == request.getType()) {
            return expandInstanceDiffNode(request);
        }
        JsHeapSnapshotConstructorNode constructorNode = queryConstructorNode(request);
        // 可视化界面没有走表格点击流程，需要在此计算
        if (constructorNode.getChildren().isEmpty()) {
            ConstructorNode expandedConstructorNode = Cjprof.expandConstructorNode(
                snapshotIdMap.get(request.getRawId()), indexToIdMap.get(constructorNode.getId()), 0, Integer.MAX_VALUE);
            constructorNode = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorNode(expandedConstructorNode,
                indexToIdMap);
            CjMemoryTransService.buildPathNodeFirstLevel(constructorNode);
        }
        return cjCalculateInstanceNode(constructorNode.getChildren(), request, isPaged);
    }

    /**
     * expandDetail
     *
     * @param request ArkHeapNodeExpandRequest
     * @param isPaged boolean
     * @return MemoryObject
     */
    @Override
    protected MemoryObject expandDetail(ArkHeapNodeExpandRequest request, boolean isPaged) {
        JsHeapSnapshotInstanceNode instanceNode = expandInstance(request, false);
        int nodeId = request.getPathNode().get(INSTANCE);
        if (nodeId > 0) {
            JsHeapSnapshotDetailNode detailNode = queryDetailsNode(instanceNode.getChildren(), nodeId);
            if (detailNode != null) {
                InstanceNode expandInstanceNode = Cjprof.expandInstanceNode(snapshotIdMap.get(request.getRawId()),
                    indexToIdMap.get(detailNode.getId()), 0, Integer.MAX_VALUE);
                JsHeapSnapshotDetailNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotDetailNode(
                    expandInstanceNode, indexToIdMap);
                detailNode.setChildren(tmp.getChildren());
                CjMemoryTransService.buildPathNode(detailNode);
                return isPaged ? PageToolsForCjprof.pageInstanceNode(request, detailNode) : detailNode;
            }
        }
        JsHeapSnapshotRetainerNode retainerNode = queryRetainersNode(instanceNode.getRetainerNodes(), nodeId);
        if (retainerNode != null) {
            InstanceNode expandInstanceNode = Cjprof.expandDetailNode(snapshotIdMap.get(request.getRawId()),
                indexToIdMap.get(retainerNode.getId()), true, 0, Integer.MAX_VALUE);
            JsHeapSnapshotRetainerNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotRetainerNode(
                expandInstanceNode, indexToIdMap);
            retainerNode.setChildren(tmp.getChildren());
            CjMemoryTransService.buildPathNodeForRetainer(retainerNode);
        }
        return isPaged ? PageToolsForCjprof.pageRetainerNode(request, retainerNode) : retainerNode;
    }

    /**
     * expandDetailAndMore
     *
     * @param instanceNode JsHeapSnapshotInstanceNode
     * @param pathSize int
     * @param request ArkHeapNodeExpandRequest
     * @return MemoryObject
     */
    @Override
    protected MemoryObject expandDetailAndMore(JsHeapSnapshotInstanceNode instanceNode, int pathSize,
        ArkHeapNodeExpandRequest request) {
        int nodeId = request.getPathNode().get(INSTANCE);
        JsHeapSnapshotDetailNode detailNode = null;
        JsHeapSnapshotRetainerNode retainer = null;
        if (nodeId > 0) {
            detailNode = queryDetailsNode(instanceNode.getChildren(), nodeId);
        } else {
            retainer = queryRetainersNode(instanceNode.getRetainerNodes(), nodeId);
        }
        return getMemoryObject(request, pathSize, INSTANCE, detailNode, retainer);
    }

    /**
     * expandDiffNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @return MemoryObject
     */
    protected JsHeapSnapshotDiffNode expandConstructorDiffNode(ArkHeapNodeExpandRequest request) {
        JsHeapSnapshotConstructorDiffNode jsHeapSnapshotConstructorDiffNode = queryCjprofDiffNode(request);
        ConstructorDiffNode expandConstructorDiffNode = Cjprof.expandConstructorDiffNode(
            snapshotIdMap.get(request.getRawId()),
            snapshotIdMap.get(jsHeapSnapshotConstructorDiffNode.getJsTargetHeapSnapshotId()),
            indexToIdMap.get(jsHeapSnapshotConstructorDiffNode.getId()), 0, Integer.MAX_VALUE);
        JsHeapSnapshotDiffNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotDiffNode(expandConstructorDiffNode,
            indexToIdMap);
        jsHeapSnapshotConstructorDiffNode.setChildren(tmp.getChildren());
        CjMemoryTransService.buildPathNodeFirstLevel(jsHeapSnapshotConstructorDiffNode);
        return PageToolsForCjprof.pageDiffNode(request, jsHeapSnapshotConstructorDiffNode);
    }

    private JsHeapSnapshotInstanceNode expandInstanceDiffNode(ArkHeapNodeExpandRequest request) {
        JsHeapSnapshotConstructorDiffNode jsHeapSnapshotConstructorDiffNode = queryCjprofDiffNode(request);
        JsHeapSnapshotInstanceNode instanceNode = queryInstanceNode(request, jsHeapSnapshotConstructorDiffNode);
        if (instanceNode != null && instanceNode.getChildren().isEmpty()) {
            InstanceDiffNode expandInstanceNode = Cjprof.expandInstanceDiffNode(snapshotIdMap.get(request.getRawId()),
                snapshotIdMap.get(jsHeapSnapshotConstructorDiffNode.getJsTargetHeapSnapshotId()),
                indexToIdMap.get(instanceNode.getId()), 0, Integer.MAX_VALUE);
            JsHeapSnapshotInstanceNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotInstanceNode(
                expandInstanceNode, indexToIdMap);
            instanceNode.setChildren(tmp.getChildren());
            instanceNode.setRetainerNodes(tmp.getRetainerNodes());
            CjMemoryTransService.buildPathNode(instanceNode);
        }
        return PageToolsForCjprof.pageInstanceNode(request, instanceNode);
    }

    private JsHeapSnapshotInstanceNode queryInstanceNode(ArkHeapNodeExpandRequest request,
        JsHeapSnapshotConstructorDiffNode jsHeapSnapshotConstructorDiffNode) {
        return jsHeapSnapshotConstructorDiffNode.getChildren()
            .stream()
            .filter(node -> request.getPathIndex().get(0).equals(node.getNodeIndex()))
            .findFirst()
            .orElse(null);
    }

    /**
     * calculateInstanceNode
     *
     * @param instanceNodes List<JsHeapSnapshotInstanceNode>
     * @param request ArkHeapNodeExpandRequest
     * @param isPaged boolean
     * @return JsHeapSnapshotInstanceNode
     */
    protected JsHeapSnapshotInstanceNode cjCalculateInstanceNode(List<JsHeapSnapshotInstanceNode> instanceNodes,
        ArkHeapNodeExpandRequest request, boolean isPaged) {
        JsHeapSnapshotInstanceNode instanceNode = queryInstanceNode(instanceNodes, request);
        if (instanceNode != null && instanceNode.getChildren().isEmpty()) {
            InstanceNode expandInstanceNode = Cjprof.expandInstanceNode(snapshotIdMap.get(request.getRawId()),
                indexToIdMap.get(instanceNode.getId()), 0, Integer.MAX_VALUE);
            JsHeapSnapshotInstanceNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotInstanceNode(
                expandInstanceNode, indexToIdMap);
            instanceNode.setChildren(tmp.getChildren());
            instanceNode.setRetainerNodes(tmp.getRetainerNodes());
            CjMemoryTransService.buildPathNode(instanceNode);
        }
        return isPaged ? PageToolsForCjprof.pageInstanceNode(request, instanceNode) : instanceNode;
    }

    /**
     * queryDiffNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @return JsHeapSnapshotConstructorDiffNode
     */
    private JsHeapSnapshotConstructorDiffNode queryCjprofDiffNode(ArkHeapNodeExpandRequest request) {
        List<JsHeapSnapshotConstructorDiffNode> nodes = cjComparisonMap.get(request.getRawId());
        int diffNodeId = request.getPathNode().get(0);
        return nodes.stream().filter(node -> node.getId() == diffNodeId).findFirst().orElse(null);
    }

    /**
     * getMemoryObject
     *
     * @param request ArkHeapNodeExpandRequest
     * @param pathSize int
     * @param levelParam int
     * @param detailNodeParam JsHeapSnapshotDetailNode
     * @param retainer JsHeapSnapshotRetainerNode
     * @return MemoryObject
     */
    @Nullable
    protected MemoryObject getMemoryObject(ArkHeapNodeExpandRequest request, int pathSize, int levelParam,
        JsHeapSnapshotDetailNode detailNodeParam, JsHeapSnapshotRetainerNode retainer) {
        List<Integer> pathNode = request.getPathNode();
        int level = levelParam;
        JsHeapSnapshotDetailNode detailNode = detailNodeParam;
        JsHeapSnapshotRetainerNode retainerNode = retainer;
        while (level <= pathSize - 1) {
            if (detailNode != null && level < pathSize - 1) {
                JsHeapSnapshotDetailNode detailNodeTmp = detailNode;
                if (detailNode.getChildren() != null && !detailNode.getChildren().isEmpty()) {
                    detailNode = queryDetailsNode(detailNode.getChildren(), pathNode.get(++level));
                    retainerNode = updateRetainer(retainerNode, detailNode, detailNodeTmp, pathNode, level);
                    continue;
                }
                retainerNode = queryRetainersNode(detailNodeTmp.getRetainerNodes(), pathNode.get(level++));
                detailNode = null;
            } else if (detailNode != null && level == pathSize - 1) {
                InstanceNode expandInstanceNode = Cjprof.expandInstanceNode(snapshotIdMap.get(request.getRawId()),
                    indexToIdMap.get(detailNode.getId()), 0, Integer.MAX_VALUE);
                JsHeapSnapshotDetailNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotDetailNode(
                    expandInstanceNode, indexToIdMap);
                detailNode.setChildren(tmp.getChildren());
                CjMemoryTransService.buildPathNode(detailNode);
                return PageToolsForCjprof.pageInstanceNode(request, detailNode);
            } else if (retainerNode != null && level < pathSize - 1) {
                retainerNode = queryRetainersNode(retainerNode.getChildren(), pathNode.get(++level));
            } else if (retainerNode != null && level == pathSize - 1) {
                InstanceNode expandInstanceNode = Cjprof.expandDetailNode(snapshotIdMap.get(request.getRawId()),
                    indexToIdMap.get(retainerNode.getId()), true, 0, Integer.MAX_VALUE);
                JsHeapSnapshotRetainerNode tmp = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotRetainerNode(
                    expandInstanceNode, indexToIdMap);
                retainerNode.setChildren(tmp.getChildren());
                CjMemoryTransService.buildPathNodeForRetainer(retainerNode);
                return PageToolsForCjprof.pageRetainerNode(request, retainerNode);
            } else {
                LOGGER.warn("Failed to calculate js allocation detail data in getMemoryObject");
                break;
            }
        }
        return null;
    }

    /**
     * cjExpandGcRootPath
     *
     * @param request request
     * @return List<JsHeapSnapshotInstanceNode>
     */
    public List<JsHeapSnapshotInstanceNode> cjprofExpandGcRootPath(ArkGcRootPathExpandRequest request) {
        // 比较页签处理rawId
        if (ArkMemoryTypeEnum.COMPARISON.getValue() == request.getType()) {
            if (request.getCountDelta() > 0) {
                request.setRawId(request.getBaseRawId());
            } else {
                request.setRawId(request.getTargetRawId());
            }
        }
        if (request.getPathNum() == ALL_GC_ROOT_PATH) {
            request.setPathNum(CJPROF_ALL_PATH_NUM);
        }
        // 节点到根节点的列表
        List<List<InstanceNode>> gcRootPathList = Cjprof.getNodeRootpaths(snapshotIdMap.get(request.getRawId()),
            indexToIdMap.get(request.getNodeId()), request.getPathNum());
        return covertInstanceToTree(gcRootPathList, request.getCountDelta());
    }

    private List<JsHeapSnapshotInstanceNode> covertInstanceToTree(List<List<InstanceNode>> gcRootPathList,
        int countDelta) {
        if (CollectionUtils.isEmpty(gcRootPathList)) {
            LOGGER.warn("No garbage collection roots found for the given instance node.");
            return Collections.emptyList();
        }
        List<JsHeapSnapshotInstanceNode> resultList = new ArrayList<>();
        for (List<InstanceNode> gcRootPath : gcRootPathList) {
            JsHeapSnapshotInstanceNode instanceNode = buildSingleTree(gcRootPath);
            instanceNode.setCountDelta(countDelta);
            resultList.add(instanceNode);
        }

        return resultList;
    }

    private JsHeapSnapshotInstanceNode buildSingleTree(List<InstanceNode> gcRootPath) {
        JsHeapSnapshotInstanceNode instanceNode = CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotInstanceNode(
            gcRootPath.get(0), indexToIdMap);
        instanceNode.setDistance(gcRootPath.size());

        // 2. 第二层：放入instanceNode.retainerNodes
        if (gcRootPath.size() > 1) {
            JsHeapSnapshotRetainerNode secondLayer = new JsHeapSnapshotRetainerNode(
                CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotRetainerNode(gcRootPath.get(1), indexToIdMap));
            secondLayer.setDistance(gcRootPath.size() - 1);
            instanceNode.getRetainerNodes().add(secondLayer);

            // 3. 第三层及以后：每一层都是前一层的 children
            // 用 current 指针记录当前最深层的节点
            JsHeapSnapshotRetainerNode current = secondLayer;
            for (int i = 2; i < gcRootPath.size(); i++) {
                JsHeapSnapshotRetainerNode nextNode = new JsHeapSnapshotRetainerNode(
                    CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotRetainerNode(gcRootPath.get(i), indexToIdMap));
                nextNode.setDistance(current.getDistance() - 1);
                current.getChildren().add(nextNode); // 放入上层的 children 中
                current = nextNode;       // 指针下移，实现深度嵌套
            }
        }
        return instanceNode;
    }

    private void initRootTypeMap(RawHeapSnapshot heapSnapshot) {
        rootTypeMap.clear();
        CjMemoryDataService.parseRootTypeMap(rootTypeMap, heapSnapshot);
    }

    private void initArrayLength(RawHeapSnapshot heapSnapshot) {
        arrayLengthMap.clear();
        CjMemoryDataService.parseArrayLengthMap(arrayLengthMap, heapSnapshot);
    }

    private void setInstanceAttribute(MemoryObject memoryObject) {
        if (memoryObject instanceof JsHeapSnapshotConstructorNode constructorNode) {
            constructorNode.getChildren().forEach(child -> {
                int id = child.getId();
                child.setRootType(rootTypeMap.getOrDefault(id, RootTypeEnum.NOT_ROOT.getValue()));
                if (Objects.equals(child.getType(), NODE_TYPE_ARRAY)) {
                    child.setArrayLength(arrayLengthMap.getOrDefault(id, 0));
                }
            });
        }
    }

    /**
     * 获取线程信息列表
     *
     * @param sessionId 会话ID
     * @param snapshotId 快照ID
     * @return 线程信息列表
     */
    public List<HeapThreadInfo> getHeapThreadInfo(String sessionId, String snapshotId) {
        if (isCjprof) {
            return getCjHeapThreadInfo(snapshotId);
        }
        LOGGER.info("Query heap thread info, sessionId: {}, snapshotId: {}", sessionId, snapshotId);
        List<HeapThreadInfo> heapThreadInfos = heapThreadMap.get(snapshotId);
        if (!CollectionUtils.isEmpty(heapThreadInfos)) {
            heapThreadInfos.forEach(this::filterStackFrameByCondition);
            return heapThreadInfos;
        }
        return new ArrayList<>();
    }

    /**
     * getCjHeapThreadInfo
     *
     * @param snapshotId snapshotId
     * @return List<HeapThreadInfo>
     */
    private List<HeapThreadInfo> getCjHeapThreadInfo(String snapshotId) {
        LOGGER.info("Query CJ heap thread info, snapshotId: {}.", snapshotId);
        Long cjprofSnapshotId = getCjprofSnapshotId(snapshotId);
        if (cjprofSnapshotId == null) {
            LOGGER.warn("Failed to get CJ heap thread info, cause: cjprofSnapshotId is null, snapshotId: {}.",
                    snapshotId);
            return new ArrayList<>();
        }
        List<ThreadInfo> threadInfos = Cjprof.getThreadInfos(cjprofSnapshotId);
        if (threadInfos == null) {
            LOGGER.warn("Failed to get CJ heap thread info, cause: threadInfos is null, snapshotId: {}.", snapshotId);
            return new ArrayList<>();
        }
        List<HeapThreadInfo> res = CjMemoryBaseMapper.INSTANCE.toHeapThreadInfos(threadInfos, indexToIdMap);
        return filterEmptyStackFrame(res);
    }

    /**
     * 过滤掉 methodName 为空的且localObject为空的StackFrame
     *
     * @param heapThreadInfoList 线程信息列表
     * @return 过滤后的线程信息列表
     */
    private List<HeapThreadInfo> filterEmptyStackFrame(List<HeapThreadInfo> heapThreadInfoList) {
        if (CollectionUtils.isEmpty(heapThreadInfoList)) {
            return heapThreadInfoList;
        }
        return heapThreadInfoList.stream().map(heapThreadInfo -> {
            if (heapThreadInfo == null || heapThreadInfo.getStackFrameInfoList() == null) {
                return heapThreadInfo;
            }
            List<StackFrame> filteredList = heapThreadInfo.getStackFrameInfoList().stream()
                    .filter(frame -> !(StringUtils.isEmpty(frame.getMethodName())
                            && CollectionUtils.isEmpty(frame.getLocalObjectList())))
                    .collect(Collectors.toList());

            heapThreadInfo.setStackFrameInfoList(filteredList);
            return heapThreadInfo;
        }).collect(Collectors.toList());
    }

    private void filterStackFrameByCondition(HeapThreadInfo heapThreadInfo) {
        List<StackFrame> stackFrameList = heapThreadInfo.getStackFrameInfoList();
        if (CollectionUtils.isEmpty(stackFrameList)) {
            return;
        }

        List<StackFrame> filteredList = new ArrayList<>();
        boolean hasObject = false;

        for (StackFrame stackFrame : stackFrameList) {
            boolean isEmptyName = StringUtils.isEmpty(stackFrame.getMethodName());
            boolean hasCurrentObject = !CollectionUtils.isEmpty(stackFrame.getLocalObjectList());

            // name 不为空，直接保留
            if (!isEmptyName) {
                filteredList.add(stackFrame);
                hasObject = hasObject || hasCurrentObject;
                continue;
            }

            // name 为空：当前有object且之前没有object -> 保留
            if (hasCurrentObject && !hasObject) {
                filteredList.add(stackFrame);
            }

            hasObject = hasObject || hasCurrentObject;
        }

        heapThreadInfo.setStackFrameInfoList(filteredList);
    }

    private static boolean isSamePath(String path1, String path2) {
        if (path1 == null || path2 == null) {
            return Objects.equals(path1, path2);
        }

        Path p1 = Paths.get(path1).toAbsolutePath().normalize();
        Path p2 = Paths.get(path2).toAbsolutePath().normalize();

        return p1.equals(p2);
    }
}
