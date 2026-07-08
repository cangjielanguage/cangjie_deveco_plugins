/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.parser;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.EdgeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.StackFrame;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.StackTrace;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.StartThread;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.TraceNode;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.BaseTreeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.ObjectArrayDump;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.ObjectInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.PrimitiveArrayDump;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.RootLocalInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.StructArrayDump;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.TreeInfo;
import com.huawei.cjprofiler.ability.parseprof.SubRecordType;
import com.huawei.cjprofiler.common.enums.RootTypeEnum;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.CjprofExtension;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapSnapshotHeader;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapSnapshotMetaInfo;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.deveco.insight.ohos.service.ark.RecordServiceBase.ParserStreamContext;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.StringUtil;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * HeapSnapshotParser
 *
 * @since 2021-05-19
 */
public class CjMemoryParser {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjMemoryParser.class);

    private static final List<String> STRING_ARRAY = new ArrayList<>();

    private static final Map<Integer, Integer> STRING_ID_TRANSFOR_MAP = new HashMap<>();

    private static final Map<Integer, Integer> LOAD_CLASS_MAP = new HashMap<>();

    private static final Map<Integer, StackFrame> STACK_FRAME_MAP = new HashMap<>();

    private static final Map<Long, StackTrace> STACK_TRACE_MAP = new HashMap<>();

    private static final Map<Long, StartThread> START_THREAD_MAP = new HashMap<>();

    private static final Map<Integer, ObjectInfo> OBJECT_MAP = new LinkedHashMap<>();

    private static final Map<Integer, Integer> CLASS_SIZE_MAP = new HashMap<>();

    private static final Set<Integer> ROOT_SET = new HashSet<>();
    private static final Map<Integer, RootLocalInfo> ROOT_LOCAL = new HashMap<>();

    private static final String PRIMITIVE_ARRAY_NAME = "Primitive Array";

    private static final int TRACE_NODE_FIELD_COUNT = 5;

    private static final int TRACE_NODE_SIZE_OFFSET = 3;

    private static List<EdgeInfo> edgeInfoList = new ArrayList<>();

    private static List<NodeInfo> nodeInfoList = new ArrayList<>();

    private static List<String> traceNodeSizeList = new ArrayList<>();

    private static List<Integer> traceFunctionInfos = new ArrayList<>();

    private static List<Object> traceTree = new ArrayList<>();

    private static List<Integer> samples = new ArrayList<>();

    private static final List<String> NODE_FIELDS = Arrays.asList("type", "name", "id", "self_size", "edge_count",
        "trace_node_id", "detachedness", "native_size");

    private static final List<Object> NODE_TYPES = Arrays.asList(
        Arrays.asList("hidden", "array", "string", "object", "code", "closure", "regexp", "number", "native",
            "synthetic", "concatenated string", "slicedstring", "symbol", "bigint"), "string", "number", "number",
        "number", "number", "number");

    private static final List<String> EDGE_FIELDS = Arrays.asList("type", "name_or_index", "to_node");

    private static final List<Object> EDGE_TYPES = Arrays.asList(new ArrayList<>(
            Arrays.asList("context", "element", "property", "internal", "hidden", "shortcut", "weak", "invisible")),
        "string_or_number", "node");

    private static final List<String> TRACE_FUNCTION_INFO_FIELDS = Arrays.asList("function_id", "name", "script_name",
        "script_id", "line", "column");

    private static final List<String> TRACE_NODE_FIELDS = Arrays.asList("id", "function_info_index", "count", "size",
        "children");

    private static final List<String> SAMPLE_FIELDS = Arrays.asList("timestamp_us", "last_assigned_id");

    private static final List<String> LOCATION_FIELDS = Arrays.asList("object_index", "script_id", "line", "column");

    private static long startTime = 0L;

    private int left = 0;

    private int right = 0;

    private boolean hasNodeType = false;

    private boolean isFirstElement = true;

    private int dataVersion = 0;

    private List<List<Integer>> rootTypeIdList = new ArrayList<>(RootTypeEnum.ROOT_TYPE_NUM);

    private List<Integer> arrayLengthList = new ArrayList<>();

    private List<Integer> threadSummary = new ArrayList<>();

    private static List<Integer> getList(JsonParser jsonParser) throws IOException {
        List<Integer> list = new ArrayList<>();
        jsonParser.nextToken();
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            int value = jsonParser.getIntValue();
            list.add(value);
            jsonToken = jsonParser.nextToken();
        }
        return list;
    }

    /**
     * 解析ParserStreamContext，返回RawHeapSnapshot结果
     *
     * @param context 提供输入输出流的context对象
     * @return 返回RawHeapSnapshot对象
     */
    @Nullable
    public RawHeapSnapshot parseJson(@NotNull ParserStreamContext context) {
        JsonFactory jsonFactory = new JsonFactory();
        RawHeapSnapshot rawHeapSnapshot;
        try (JsonParser jsonParser = jsonFactory.createParser(context.getInputStream())) {
            rawHeapSnapshot = readRawCjHeapSnapshot(jsonParser);
        } catch (IOException e) {
            LOGGER.warn("ParseCjJson failed, IOException occurred, cause: {}.", e.getMessage());
            throw new IllegalStateException("Parse cj json failed, IOException occurred");
        } finally {
            clearAllCollections();
            context.closeStream();
        }
        return rawHeapSnapshot;
    }

    /**
     * Parse rawHeapSnapshot
     *
     * @param jsonParser JsonParser
     * @return RawHeapSnapshot
     * @throws IOException IOException
     */
    private RawHeapSnapshot readRawCjHeapSnapshot(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected content to be an object");
        }

        // 基本类型数组名称
        STRING_ARRAY.add(PRIMITIVE_ARRAY_NAME);
        RawHeapSnapshot rawHeapSnapshot = parseRawJson(jsonParser);

        TreeInfo treeInfo = CjBuildTreeService.getInstance()
            .buildMemTree(OBJECT_MAP, CLASS_SIZE_MAP, LOAD_CLASS_MAP, ROOT_SET);
        buildRawHeapSnapshot(treeInfo, rawHeapSnapshot);
        CjBuildTreeService.getInstance().clearAllCollection();
        return rawHeapSnapshot;
    }

    private RawHeapSnapshot parseRawJson(JsonParser jsonParser) throws IOException {
        RawHeapSnapshot rawHeapSnapshot = new RawHeapSnapshot();
        initRootTypeList();
        handleProperty(jsonParser);
        return rawHeapSnapshot;
    }

    private void initRootTypeList() {
        for (int i = 0; i < RootTypeEnum.ROOT_TYPE_NUM; i++) {
            rootTypeIdList.add(new ArrayList<>());
        }
    }

    private Map<Integer, int[]> buildStackFramesMap() {
        Map<Integer, int[]> stackFramesMap = new HashMap<>();

        // 遍历所有 StartThread
        for (StartThread startThread : START_THREAD_MAP.values()) {
            long startTraceIdx = startThread.getStackTraceIdx();
            if (!STACK_TRACE_MAP.containsKey(startTraceIdx)) {
                LOGGER.warn("StackTrace not found for startTraceIdx: {}.", startTraceIdx);
                continue;
            }
            List<String> frameIds = STACK_TRACE_MAP.get(startTraceIdx).getFrames();
            if (CollectionUtils.isEmpty(frameIds)) {
                LOGGER.warn("FrameIds is empty for startTraceIdx: {}.", startTraceIdx);
                continue;
            }
            for (String frameId : frameIds) {
                if (StringUtil.isEmpty(frameId)) {
                    LOGGER.warn("Empty frameId found in frameIds.");
                    continue;
                }
                int frameIdInt;
                try {
                    frameIdInt = Integer.parseInt(frameId);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to parse frameId: {}, cause: {}.", frameId, e.getMessage());
                    continue;
                }
                StackFrame stackFrame = STACK_FRAME_MAP.get(frameIdInt);
                if (stackFrame == null) {
                    LOGGER.warn("StackFrame not found for frameId: {}.", frameIdInt);
                    continue;
                }
                int methodIdx = stackFrame.getName();
                int fileIdx = stackFrame.getFileName();
                int lineNumber = (int) stackFrame.getLineNum();

                // stackFrameId -> [thread_id, thread_name_idx, methodIdx, fileIdx, lineNumber]
                stackFramesMap.put(stackFrame.getId(),
                        new int[]{startThread.getId(), startThread.getName(), methodIdx, fileIdx, lineNumber});
            }
        }

        // 遍历所有 RootLocal
        for (RootLocalInfo rootLocal : ROOT_LOCAL.values()) {
            Integer nodeIndex = CjBuildTreeService.getInstance().getNodeIndex(rootLocal.getId());
            if (nodeIndex == null) {
                LOGGER.warn("NodeIndex not found for rootLocal id: {}.", rootLocal.getId());
                continue;
            }
            StartThread thread = START_THREAD_MAP.get(rootLocal.getThreadIdx());
            if (thread == null) {
                LOGGER.warn("StartThread not found for threadIdx: {}.", rootLocal.getThreadIdx());
                continue;
            }
            StackFrame stackFrame = STACK_FRAME_MAP.get(rootLocal.getFrameId());
            if (stackFrame == null) {
                stackFramesMap.put(rootLocal.getFrameId(), new int[]{thread.getId(), thread.getName(), -1, -1, -1});
            }
            // [nodeId, stack_frame_id, nodeIndex]
            threadSummary.addAll(Arrays.asList(rootLocal.getId(), rootLocal.getFrameId(), nodeIndex));
        }
        return stackFramesMap;
    }

    private void handleProperty(JsonParser jsonParser) throws IOException {
        // 进循环前，假定当前指针在外部大对象的 START_OBJECT
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String property = jsonParser.getCurrentName();
            LOGGER.info("Parser property:{}.", property);
            // 移动到属性的值 (Value)
            jsonParser.nextToken();
            switch (property) {
                case "HEADER":
                    readHeader(jsonParser);
                    break;
                case "head":
                    readHeadInfo(jsonParser);
                    break;
                case "OBJECTS":
                    readHeapDumpList(jsonParser, true);
                    break;
                case "CLASS", "STRUCTCLASS":
                    readHeapDumpList(jsonParser, false);
                    break;
                case "STARTTHREAD":
                    readStartThreadList(jsonParser);
                    break;
                case "STACKTRACE":
                    readStackTraceList(jsonParser);
                    break;
                case "STACKFRAME":
                    readStackFrameList(jsonParser);
                    break;
                case "CLASSLOAD", "STRUCTCLASSLOAD":
                    readLoadClassList(jsonParser);
                    break;
                case "STRING":
                    readStringRecordList(jsonParser);
                    break;
                case "samples":
                    samples(jsonParser);
                    break;
                case "VERSION":
                    readVersion(jsonParser);
                    break;
                default:
                    LOGGER.info("Unknown property [{}], skip!", property);
                    jsonParser.skipChildren();
                    break;
            }
        }
    }

    private void buildRawHeapSnapshot(TreeInfo treeInfo, RawHeapSnapshot rawHeapSnapshot) {
        LOGGER.info("start buildRawHeapSnapshot");
        nodeInfoList = treeInfo.getNodeInfoList();
        edgeInfoList = treeInfo.getEdgeInfoList();
        int[] nodes = convertListToIntArray(nodeInfoList);
        int[] edges = convertListToIntArray(edgeInfoList);
        rawHeapSnapshot.setSnapshot(getHeapSnapshotHeader());
        rawHeapSnapshot.setNodes(nodes);
        rawHeapSnapshot.setEdges(edges);
        rawHeapSnapshot.setTraceFunctionInfos(new ArrayList<>(traceFunctionInfos));
        List<Object> wrappedTraceTree = new ArrayList<>();
        wrappedTraceTree.add(new ArrayList<>(traceTree));
        rawHeapSnapshot.setTraceTree(wrappedTraceTree);
        rawHeapSnapshot.setSamples(new ArrayList<>(samples));
        rawHeapSnapshot.setLocations(new int[] {});
        rawHeapSnapshot.setStrings(new ArrayList<>(STRING_ARRAY));
        rawHeapSnapshot.setStartTime(startTime);
        rawHeapSnapshot.setCjprofExtension(buildCjprofExtension());
    }

    private CjprofExtension buildCjprofExtension() {
        CjprofExtension cjprofExtension = new CjprofExtension();
        cjprofExtension.setRootDataByTypes(flattenList());
        cjprofExtension.setNodeArrayLengths(arrayLengthList.stream().mapToInt(Integer::intValue).toArray());
        cjprofExtension.setStackFramesMap(buildStackFramesMap());
        cjprofExtension.setThreadSummary(this.threadSummary.stream().mapToInt(Integer::intValue).toArray());
        return cjprofExtension;
    }

    private int[][] flattenList() {
        int[][] result = new int[RootTypeEnum.ROOT_TYPE_NUM][];
        for (int i = 1; i < RootTypeEnum.ROOT_TYPE_NUM; i++) {
            result[i] = rootTypeIdList.get(i).stream().mapToInt(Integer::intValue).toArray();
        }
        return result;
    }

    private HeapSnapshotHeader getHeapSnapshotHeader() {
        HeapSnapshotHeader heapSnapshotHeader = new HeapSnapshotHeader();
        heapSnapshotHeader.setNodeCount(nodeInfoList.size());
        heapSnapshotHeader.setEdgeCount(edgeInfoList.size());
        HeapSnapshotMetaInfo meta = getHeapSnapshotMetaInfo();
        heapSnapshotHeader.setMeta(meta);
        return heapSnapshotHeader;
    }

    private HeapSnapshotMetaInfo getHeapSnapshotMetaInfo() {
        HeapSnapshotMetaInfo meta = new HeapSnapshotMetaInfo();
        meta.setNodeFields(NODE_FIELDS);
        meta.setNodeTypes(NODE_TYPES);
        meta.setEdgeFields(EDGE_FIELDS);
        meta.setEdgeTypes(EDGE_TYPES);
        meta.setTraceFunctionInfoFields(TRACE_FUNCTION_INFO_FIELDS);
        meta.setTraceNodeFields(TRACE_NODE_FIELDS);
        meta.setSampleFields(SAMPLE_FIELDS);
        meta.setLocationFields(LOCATION_FIELDS);
        return meta;
    }

    private int[] convertListToIntArray(List<? extends BaseTreeInfo> edgeInfoList) {
        if (edgeInfoList.isEmpty()) {
            return new int[] {};
        }
        int totalFields = edgeInfoList.iterator().next().toIntArray().length;
        int[] array = new int[edgeInfoList.size() * totalFields];
        int index = 0;
        for (BaseTreeInfo treeInfo : edgeInfoList) {
            int[] values = treeInfo.toIntArray();
            for (int value : values) {
                array[index++] = value;
            }
        }
        return array;
    }

    private void readHeader(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // ID 类型所占字节数，当前固定为 8，跳过idSize
            jsonParser.nextToken();
            // 时间戳的高 32 位（从 1970 年 1 月 1 日到当前的微秒数）
            int timeHigh = jsonParser.getIntValue();
            jsonParser.nextToken();
            // 时间戳的低 32 位（从 1970 年 1 月 1 日到当前的微秒数）
            int timeLow = jsonParser.getIntValue();

            startTime = convertToMilliseconds(timeHigh, timeLow);
            jsonToken = jsonParser.nextToken();
        }
    }

    private long convertToMilliseconds(int left, int right) {
        // 将高 32 位左移 32 位，然后与低 32 位进行按位或操作，得到完整的 64 位微秒数
        long microseconds = ((long) left << 32) | (right & 0xFFFFFFFFL);
        // 将微秒数转换为毫秒数
        return microseconds / 1000;
    }

    private void readHeapDumpList(JsonParser jsonParser, boolean isObjects) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是子数组的 START_ARRAY
            if (jsonToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected START_ARRAY for sub-array");
            }

            readSubRecord(jsonParser, isObjects);
            jsonToken = jsonParser.currentToken();

            // 检查子数组是否以 END_ARRAY 结束
            if (jsonToken != JsonToken.END_ARRAY) {
                throw new IllegalStateException("Expected END_ARRAY for sub-array");
            }
        }
    }

    private void readStartThreadList(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();

        long idx = jsonParser.getLongValue();

        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        long stackTraceIdx = jsonParser.getLongValue();

        jsonParser.nextToken();
        int nameId = jsonParser.getIntValue();

        jsonToken = jsonParser.nextToken();
        // 检查子数组是否以 END_ARRAY 结束
        if (jsonToken != JsonToken.END_ARRAY) {
            throw new IllegalStateException("Expected END_ARRAY for sub-array");
        }
        START_THREAD_MAP.put(idx, StartThread.builder()
            .idx(idx)
            .id(id)
            .stackTraceIdx(stackTraceIdx)
            .name(STRING_ID_TRANSFOR_MAP.get(nameId))
            .build());
    }

    private void readStackTraceList(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        long idx = jsonParser.getLongValue();

        jsonParser.nextToken();
        long thread = jsonParser.getLongValue();

        jsonParser.nextToken();
        long frameNum = jsonParser.getLongValue();

        jsonParser.nextToken();

        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        jsonToken = jsonParser.nextToken();
        List<String> frames = new ArrayList<>();
        while (jsonToken != JsonToken.END_ARRAY) {
            frames.add(jsonParser.getText());
            jsonToken = jsonParser.nextToken();
        }
        jsonToken = jsonParser.nextToken();
        // 检查子数组是否以 END_ARRAY 结束
        if (jsonToken != JsonToken.END_ARRAY) {
            throw new IllegalStateException("Expected END_ARRAY for sub-array");
        }
        STACK_TRACE_MAP.put(idx,
            StackTrace.builder().idx(idx).thread(thread).frameNum(frameNum).frames(frames).build());
    }

    private void readStackFrameList(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是子数组的 START_ARRAY
            if (jsonToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected START_ARRAY for sub-array");
            }

            jsonParser.nextToken();
            int id = jsonParser.getIntValue();

            jsonParser.nextToken();
            int nameId = jsonParser.getIntValue();

            jsonParser.nextToken();
            int fileNameId = jsonParser.getIntValue();

            jsonParser.nextToken();
            long lineNum = jsonParser.getLongValue();

            jsonToken = jsonParser.nextToken();
            // 检查子数组是否以 END_ARRAY 结束
            if (jsonToken != JsonToken.END_ARRAY) {
                throw new IllegalStateException("Expected END_ARRAY for sub-array");
            }
            jsonToken = jsonParser.nextToken();
            STACK_FRAME_MAP.put(id, StackFrame.builder()
                .id(id)
                .name(STRING_ID_TRANSFOR_MAP.get(nameId))
                .fileName(STRING_ID_TRANSFOR_MAP.get(fileNameId))
                .lineNum(lineNum)
                .build());
        }
    }

    private void readLoadClassList(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是子数组的 START_ARRAY
            if (jsonToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected START_ARRAY for sub-array");
            }
            // 读取子数组中的id（long 类型）
            jsonParser.nextToken();
            int id = jsonParser.getIntValue();
            // 读取子数组中的name
            jsonParser.nextToken();
            int nameId = jsonParser.getIntValue();
            jsonToken = jsonParser.nextToken();
            // 检查子数组是否以 END_ARRAY 结束
            if (jsonToken != JsonToken.END_ARRAY) {
                throw new IllegalStateException("Expected END_ARRAY for sub-array");
            }
            jsonToken = jsonParser.nextToken();
            LOAD_CLASS_MAP.put(id, STRING_ID_TRANSFOR_MAP.get(nameId));
        }
    }

    private void readStringRecordList(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是子数组的 START_ARRAY
            if (jsonToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected START_ARRAY for sub-array");
            }
            // 读取子数组中的第一个元素（id）
            jsonParser.nextToken();
            int id = jsonParser.getIntValue();
            // 读取子数组中的第二个元素（string 类型）
            jsonParser.nextToken();
            String stringValue = jsonParser.getText();
            jsonToken = jsonParser.nextToken();
            // 检查子数组是否以 END_ARRAY 结束
            if (jsonToken != JsonToken.END_ARRAY) {
                throw new IllegalStateException("Expected END_ARRAY for sub-array");
            }
            jsonToken = jsonParser.nextToken();
            // 将String重新编排
            int arrayIndex = STRING_ARRAY.size();
            STRING_ARRAY.add(stringValue);
            STRING_ID_TRANSFOR_MAP.put(id, arrayIndex);
        }
    }

    private void readSubRecord(JsonParser jsonParser, boolean isObjects) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.currentToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是子数组的 START_ARRAY
            if (jsonToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected START_ARRAY for sub-array");
            }
            // 读取子数组中的第一个元素（id）
            jsonParser.nextToken();
            int tag = jsonParser.getIntValue();
            Optional<SubRecordType> subRecordTypeOptional = SubRecordType.fromType(tag);
            if (subRecordTypeOptional.isEmpty()) {
                // 未知 tag，跳过该子数组剩下的所有内容
                LOGGER.warn("Unknown tag {}, skipping remaining elements in this sub-array...", tag);

                // 循环直到找到当前子数组的结束标记 END_ARRAY
                // jsonParser.getParsingContext().getParent() 可以确保我们在正确的嵌套层级
                int depth = 1;
                while (depth > 0 && (jsonToken = jsonParser.nextToken()) != null) {
                    if (jsonToken == JsonToken.START_ARRAY || jsonToken == JsonToken.START_OBJECT) {
                        depth++;
                    } else if (jsonToken == JsonToken.END_ARRAY || jsonToken == JsonToken.END_OBJECT) {
                        depth--;
                    } else {
                        continue;
                    }
                }
            } else {
                // 已知 tag，正常处理
                handleNodeType(jsonParser, isObjects);
                handleSubRecordType(jsonParser, subRecordTypeOptional.get());

                // 让指针移动到子数组的结束标记
                jsonToken = jsonParser.nextToken();
            }
            // 检查子数组是否以 END_ARRAY 结束
            if (jsonToken != JsonToken.END_ARRAY) {
                throw new IllegalStateException("Expected END_ARRAY for sub-array");
            }
            jsonToken = jsonParser.nextToken();
        }
    }

    private void handleSubRecordType(JsonParser jsonParser, SubRecordType subRecordType)
        throws IOException {
        switch (subRecordType) {
            case ROOT_UNKNOWN:
                readRootUnknown(jsonParser);
                break;
            case ROOT_GLOBAL:
                readRootGlobal(jsonParser);
                break;
            case ROOT_LOCAL:
                readRootLocal(jsonParser);
                break;
            case CLASS_DUMP:
                readClassDump(jsonParser);
                break;
            case INSTANCE_DUMP, PINNED_INSTANCE, LARGE_INSTANCE, UNMOVABLE_INSTANCE:
                readInstanceDump(jsonParser);
                break;
            case OBJECT_ARRAY_DUMP, LARGE_OBJECT_ARRAY, UNMOVABLE_OBJECT_ARRAY:
                readObjectArrayDump(jsonParser);
                break;
            case STRUCT_ARRAY_DUMP, LARGE_STRUCT_ARRAY, UNMOVABLE_STRUCT_ARRAY_DUMP:
                readStructArrayDump(jsonParser);
                break;
            case PRIMITIVE_ARRAY_DUMP, LARGE_PRIMITIVE_ARRAY, UNMOVABLE_PRIMITIVE_ARRAY:
                readPrimitiveArrayDump(jsonParser);
                break;
        }
    }

    private void handleNodeType(JsonParser jsonParser, boolean isObjects) throws IOException {
        if (needCheckNodeType(isObjects)) {
            isFirstElement = false;
            hasNodeType = checkHasTag(jsonParser);
        }
        if (needReadNodeType(isObjects)) {
            jsonParser.nextToken();
            int typeKindId = jsonParser.getIntValue();
            TypeKindEnum.getTypeNameByValue(typeKindId);
        }
    }

    private boolean needReadNodeType(boolean isObjects) {
        return isObjects && hasNodeType;
    }

    private boolean needCheckNodeType(boolean isObjects) {
        return isObjects && !hasNodeType && isFirstElement;
    }

    private boolean checkHasTag(JsonParser jsonParser) throws IOException {
        if (dataVersion > 0) {
            return true;
        }
        int elementCount = 1; // 已读取ID

        // 计数直到内部数组结束
        while (jsonParser.nextToken() != JsonToken.END_ARRAY && elementCount <= 3) {
            elementCount++;
        }

        // ID=1的规则：总长3表示有tag，总长2表示无tag
        boolean res = elementCount == 3;
        // 重置解析位置到ID之后
        jsonParser.clearCurrentToken();
        jsonParser.nextToken();
        jsonParser.nextToken();
        return res;
    }

    private void readRootUnknown(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();
        ROOT_SET.add(id);
        rootTypeIdList.get(RootTypeEnum.UNKNOWN.getKey()).add(id);
    }

    private void readRootGlobal(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();
        ROOT_SET.add(id);
        rootTypeIdList.get(RootTypeEnum.GLOBAL.getKey()).add(id);
    }

    private void readRootLocal(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();
        ROOT_SET.add(id);

        jsonParser.nextToken();
        int threadId = jsonParser.getIntValue();

        jsonParser.nextToken();
        int frameId = jsonParser.getIntValue();
        ROOT_LOCAL.put(id, RootLocalInfo.builder().threadIdx(threadId).frameId(frameId).id(id).build());
        rootTypeIdList.get(RootTypeEnum.LOCAL.getKey()).add(id);
    }

    private void readClassDump(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        int size = jsonParser.getIntValue();

        CLASS_SIZE_MAP.put(id, size);
    }

    private void readInstanceDump(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        int cls = jsonParser.getIntValue();

        // 跳过num
        jsonParser.nextToken();

        List<Integer> values = getList(jsonParser);
        ObjectInfo objectInfo = new ObjectInfo(id, cls, values);
        OBJECT_MAP.put(id, objectInfo);
    }

    private void readObjectArrayDump(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        int num = 0;
        try {
            num = Math.toIntExact(jsonParser.getLongValue());
        } catch (ArithmeticException e) {
            LOGGER.warn("Parse cjsnapshot, nodeId:{}, integer overflow", id);
        }

        jsonParser.nextToken();
        int cls = jsonParser.getIntValue();

        List<Integer> elements = getList(jsonParser);
        arrayLengthList.add(id);
        arrayLengthList.add(num);
        OBJECT_MAP.put(id, new ObjectArrayDump(id, cls, elements));
    }

    private void readStructArrayDump(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        int num = jsonParser.getIntValue();

        jsonParser.nextToken();
        int componentNum = jsonParser.getIntValue();

        jsonParser.nextToken();
        int cls = jsonParser.getIntValue();

        List<Integer> elements = getList(jsonParser);
        arrayLengthList.add(id);
        arrayLengthList.add(num + componentNum);
        OBJECT_MAP.put(id, new StructArrayDump(id, cls, elements, num));
    }

    private void readPrimitiveArrayDump(JsonParser jsonParser) throws IOException {
        jsonParser.nextToken();
        int id = jsonParser.getIntValue();

        jsonParser.nextToken();
        int num = jsonParser.getIntValue();

        jsonParser.nextToken();
        int type = jsonParser.getIntValue();

        PrimitiveArrayDump primitiveArrayDump = new PrimitiveArrayDump(id, num, type);
        arrayLengthList.add(id);
        arrayLengthList.add(num);
        OBJECT_MAP.put(id, primitiveArrayDump);
    }

    private void samples(JsonParser jsonParser) throws IOException {
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        JsonToken jsonToken = jsonParser.nextToken();
        // 遍历数组中的每个元素
        while (jsonToken != JsonToken.END_ARRAY) {
            // 检查是否是对象的 START_OBJECT
            if (jsonToken != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected START_OBJECT for sample object");
            }
            // 读取对象的第一个元素(size)
            jsonParser.nextToken();
            jsonParser.nextToken();
            // 读取对象的第二个元素(nodeId)
            jsonParser.nextToken();
            int nodeId = jsonParser.nextIntValue(0);
            samples.add(nodeId);
            // 读取对象的第三个元素(ordinal)
            jsonParser.nextToken();
            int ordinal = jsonParser.nextIntValue(0);
            samples.add(ordinal);

            // 检查对象是否以 END_ARRAY 结束
            jsonToken = jsonParser.nextToken();
            if (jsonToken != JsonToken.END_OBJECT) {
                throw new IllegalStateException("Expected END_OBJECT for sample object");
            }
            jsonToken = jsonParser.nextToken();
        }
    }

    private void readVersion(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_ARRAY 开始
        if (jsonParser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected START_ARRAY");
        }
        jsonParser.nextToken();
        // 读取version字段
        dataVersion = jsonParser.getIntValue();
        jsonParser.nextToken();
    }

    private void readHeadInfo(JsonParser jsonParser) throws IOException {
        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected START_ARRAY OR START_OBJECT");
        }

        traceTree = readTraceTree(jsonParser);
    }

    private List<Object> readTraceTree(JsonParser jsonParser) throws IOException {
        left = 0;
        right = 0;
        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected content to be an object");
        }
        return addObject(jsonParser);
    }

    private List<Object> addObject(JsonParser jsonParser) throws IOException {
        List<Object> child = new ArrayList<>(); // when meet an '[' or '{', new child
        while (true) {
            JsonToken currentToken = jsonParser.currentToken();
            if (currentToken == JsonToken.START_ARRAY) {
                left++;
                readStartArray(jsonParser, child);
            } else if (currentToken == JsonToken.END_ARRAY) {
                right++;
                return child;
            } else if (Objects.equals(jsonParser.getText(), "callFrame")) {
                jsonParser.nextToken();
                TraceNode traceNode = readTraceNode(jsonParser);
                child.add(traceNode.getId());
                child.add(traceNode.getFunctionInfoIndex());
            } else if (currentToken == JsonToken.START_OBJECT) {
                left++;
            } else if (currentToken == JsonToken.END_OBJECT) {
                right++;
            } else {
                jsonParser.nextToken();
                continue;
            }
            if (left == right) {
                return child;
            }
            jsonParser.nextToken();
        }
    }

    private void readStartArray(JsonParser jsonParser, List<Object> child) throws IOException {
        jsonParser.nextToken();
        List<Object> nextChildren = addObject(jsonParser);
        // 获取functionInfoIndex
        int curFunctionIndex = 0;
        if (child.get(child.size() - 1) instanceof String) {
            try {
                curFunctionIndex = Integer.parseInt((String) child.get(child.size() - 1));
            } catch (NumberFormatException e) {
                LOGGER.warn("FunctionInfoIndex parseInt Failed: {}.", child.get(child.size() - 1));
            }
        }
        // count
        child.add(nextChildren.size() / TRACE_NODE_FIELD_COUNT);
        // size
        String selfSize = traceNodeSizeList.get(curFunctionIndex);
        String totalSize = calcTraceNodeTotalSize(selfSize, nextChildren);
        child.add(totalSize);
        child.add(nextChildren);
    }

    private String calcTraceNodeTotalSize(String selfSize, List<Object> nextChildren) {
        if (nextChildren == null || nextChildren.isEmpty()) {
            return selfSize;
        }
        int totalSize = 0;
        int childCount = nextChildren.size() / TRACE_NODE_FIELD_COUNT;
        for (int i = 0; i < childCount; i++) {
            int curSizeIndex = i * TRACE_NODE_FIELD_COUNT + TRACE_NODE_SIZE_OFFSET;
            Object curChildSize = nextChildren.get(curSizeIndex);
            if (curChildSize instanceof String) {
                try {
                    totalSize += Integer.parseInt((String) curChildSize);
                } catch (NumberFormatException e) {
                    LOGGER.warn("curChildSize parseInt Failed: {}.", curChildSize);
                }
            }
        }
        try {
            totalSize += Integer.parseInt(selfSize);
        } catch (NumberFormatException e) {
            LOGGER.warn("invalid selfSize.");
        }
        return String.valueOf(totalSize);
    }

    private TraceNode readTraceNode(JsonParser jsonParser) throws IOException {
        // 检查是否以 START_OBJECT 开始
        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected START_OBJECT");
        }

        // functionName
        jsonParser.nextToken();
        String functionName = jsonParser.nextTextValue();
        int functionNameStrId = STRING_ARRAY.size();
        STRING_ARRAY.add(functionName);

        // scriptName
        jsonParser.nextToken();
        String scriptName = jsonParser.nextTextValue();
        int scriptNameStrId = STRING_ARRAY.size();
        STRING_ARRAY.add(scriptName);

        // lineNumber
        jsonParser.nextToken();
        int lineNumber = jsonParser.nextIntValue(-1);

        // columnNumber
        jsonParser.nextToken();
        int columnNumber = jsonParser.nextIntValue(-1);

        jsonParser.nextToken();
        // 检查callFrame是否以 END_OBJECT 结束
        if (jsonParser.currentToken() != JsonToken.END_OBJECT) {
            throw new IllegalStateException("Expected END_OBJECT for callFrame");
        }

        // 添加函数信息到TraceFunctionInfos
        traceFunctionInfos.add(-1);
        traceFunctionInfos.add(functionNameStrId);
        traceFunctionInfos.add(scriptNameStrId);
        traceFunctionInfos.add(-1);
        traceFunctionInfos.add(lineNumber);
        traceFunctionInfos.add(columnNumber);

        // selfSize
        jsonParser.nextToken();
        int size = jsonParser.nextIntValue(-1);
        // 存到列表里
        traceNodeSizeList.add(String.valueOf(size));

        // id
        jsonParser.nextToken();
        int id = jsonParser.nextIntValue(-1);

        TraceNode traceNode = new TraceNode();
        traceNode.setFunctionInfoIndex(String.valueOf(traceNodeSizeList.size() - 1));
        traceNode.setId(String.valueOf(id));

        return traceNode;
    }

    private void clearAllCollections() {
        STRING_ARRAY.clear();
        STRING_ID_TRANSFOR_MAP.clear();
        LOAD_CLASS_MAP.clear();
        STACK_FRAME_MAP.clear();
        STACK_TRACE_MAP.clear();
        START_THREAD_MAP.clear();
        OBJECT_MAP.clear();
        CLASS_SIZE_MAP.clear();
        ROOT_LOCAL.clear();
        edgeInfoList.clear();
        nodeInfoList.clear();
        traceNodeSizeList.clear();
        traceFunctionInfos.clear();
        traceTree.clear();
        samples.clear();
        rootTypeIdList.clear();
        threadSummary.clear();
    }
}
