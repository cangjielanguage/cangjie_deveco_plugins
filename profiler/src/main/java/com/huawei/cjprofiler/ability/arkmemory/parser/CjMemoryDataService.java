/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.parser;

import com.huawei.cjprofiler.common.enums.RootTypeEnum;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.CjprofExtension;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.LocalObject;
import com.huawei.cjprofiler.model.po.cjprof.HeapThreadInfo;
import com.huawei.cjprofiler.model.po.cjprof.StackFrame;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CjMemoryDataService
 *
 * @since 2026-04-14
 */
@Slf4j
public class CjMemoryDataService {
    /**
     * parseRootTypeMap
     *
     * @param rootTypeMap rootTypeMap
     * @param rawHeapSnapshot rawHeapSnapshot
     */
    public static void parseRootTypeMap(Map<Integer, String> rootTypeMap, RawHeapSnapshot rawHeapSnapshot) {
        int[][] flattenedData = rawHeapSnapshot.getCjprofExtension().getRootDataByTypes();
        // 1. 安全校验
        if (flattenedData == null || rootTypeMap == null) {
            return;
        }

        // 2. 遍历二维数组的行 (Row)
        // 按照之前的逻辑，我们从下标 1 开始遍历到 3
        for (int i = 1; i < flattenedData.length; i++) {
            int[] keys = flattenedData[i];

            if (keys == null || keys.length == 0) {
                continue;
            }

            // 3. 根据当前行下标 i，转换出对应的 root 类型
            String typeString = RootTypeEnum.getValueByKey((byte) i);

            // 4. 遍历该行中的所有 Key，存入 Map
            for (int key : keys) {
                rootTypeMap.put(key, typeString);
            }
        }
    }

    /**
     * parseArrayLengthMap 解析数组长度映射
     * 此时 nodeArrayLengths 格式定义为步长 2: [objectId, arrayLength]
     *
     * @param arrayLengthMap arrayLengthMap
     * @param rawHeapSnapshot rawHeapSnapshot
     */
    public static void parseArrayLengthMap(Map<Integer, Integer> arrayLengthMap, RawHeapSnapshot rawHeapSnapshot) {
        if (rawHeapSnapshot == null || rawHeapSnapshot.getCjprofExtension() == null) {
            return;
        }

        CjprofExtension ext = rawHeapSnapshot.getCjprofExtension();
        int[] idAndLengths = ext.getNodeArrayLengths();
        if (idAndLengths == null || idAndLengths.length == 0) {
            return;
        }

        // 步长为 2 遍历: [objectId, arrayLength]
        for (int i = 0; i < idAndLengths.length; i += 2) {
            int objectId = idAndLengths[i];
            int arrayLength = idAndLengths[i + 1];

            arrayLengthMap.put(objectId, arrayLength);
        }
    }

    /**
     * parseHeapThreadDetail
     *
     * @param rawHeapSnapshot rawHeapSnapshot
     * @return List<HeapThreadInfo>
     */
    public static List<HeapThreadInfo> parseHeapThreadDetail(RawHeapSnapshot rawHeapSnapshot) {
        if (rawHeapSnapshot == null || rawHeapSnapshot.getCjprofExtension() == null) {
            return new ArrayList<>();
        }
        CjprofExtension ext = rawHeapSnapshot.getCjprofExtension();

        List<String> strings = rawHeapSnapshot.getStrings();
        int[] nodes = rawHeapSnapshot.getNodes();

        // stackframe [thread_id, thread_name_idx, methodIdx, fileIdx, lineNumber]
        Map<Integer, int[]> stackFramesMap = ext.getStackFramesMap();
        if (stackFramesMap == null || stackFramesMap.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, HeapThreadInfo> threadInfoMap = new HashMap<>();
        Map<Integer, StackFrame> stackFrameObjectMap = new HashMap<>();
        buildStackFrameAndThreadInfo(stackFramesMap, threadInfoMap, stackFrameObjectMap, strings);

        int[] threadSummary = ext.getThreadSummary();
        if (threadSummary == null || threadSummary.length == 0) {
            return new ArrayList<>(threadInfoMap.values());
        }
        // 步长为 3 遍历: [nodeId, stack_frame_id,nodeIndex]
        for (int i = 0; i < threadSummary.length; i += 3) {
            if (i + 2 >= threadSummary.length) {
                LOGGER.warn("ThreadSummary index out of bounds, i: {}, length: {}.", i, threadSummary.length);
                break;
            }
            int nodeId = threadSummary[i];
            int stackFrameId = threadSummary[i + 1];
            int nodeIndex = threadSummary[i + 2];
            long calculatedIndex = (long) nodeIndex * 8 + 3;

            if (nodeIndex < 0 || calculatedIndex >= nodes.length) {
                LOGGER.warn("Invalid nodeIndex: {}, nodes.length: {}.", nodeIndex, nodes.length);
                continue;
            }
            int nameIndex = nodes[nodeIndex * 8 + 1];
            int shallowSize = nodes[nodeIndex * 8 + 3];

            if (nameIndex < 0 || nameIndex >= strings.size()) {
                LOGGER.warn("Invalid nameIndex: {}, strings.size: {}.", nameIndex, strings.size());
                continue;
            }
            if (shallowSize <= 0) {
                continue;
            }
            String objectName = strings.get(nameIndex);
            LocalObject localObject =
                LocalObject.builder().objectId(nodeId).typeName(objectName).shallowSize(shallowSize).build();

            StackFrame stackFrame = stackFrameObjectMap.get(stackFrameId);
            if (stackFrame != null) {
                stackFrame.getLocalObjectList().add(localObject);
            }
        }

        // 对 threadInfo 里的 stackFrameList 排序，根据 id 从小到大
        for (HeapThreadInfo heapThreadInfo : threadInfoMap.values()) {
            List<StackFrame> stackFrameList = heapThreadInfo.getStackFrameInfoList();
            if (!CollectionUtils.isEmpty(stackFrameList)) {
                stackFrameList.sort(Comparator.comparingInt(StackFrame::getId));
            }
        }

        return new ArrayList<>(threadInfoMap.values());
    }

    private static void buildStackFrameAndThreadInfo(Map<Integer, int[]> stackFramesMap,
            Map<Integer, HeapThreadInfo> threadInfoMap, Map<Integer, StackFrame> stackFrameObjectMap,
            List<String> strings) {
        // 构建 stackFrame 和 threadInfo
        for (Map.Entry<Integer, int[]> entry : stackFramesMap.entrySet()) {
            int stackFrameId = entry.getKey();
            int[] value = entry.getValue();

            if (value == null || value.length < 5) {
                LOGGER.warn("Invalid stackFrame value, stackFrameId: {}, value: {}, length: {}.", stackFrameId, value,
                        value == null ? 0 : value.length);
                continue;
            }

            int threadId = value[0];
            int threadNameId = value[1];
            int methodNameId = value[2];
            int fileNameId = value[3];
            int lineNumber = value[4];

            // 创建或获取 HeapThreadInfo
            HeapThreadInfo heapThreadInfo = threadInfoMap.get(threadId);
            if (heapThreadInfo == null) {
                String threadName = "";
                if (threadNameId < 0 || threadNameId >= strings.size()) {
                    LOGGER.warn("Invalid threadNameId: {}, strings.size: {}.", threadNameId, strings.size());
                } else {
                    threadName = strings.get(threadNameId);
                }
                heapThreadInfo = HeapThreadInfo.builder().threadId(threadId).threadName(threadName)
                        .stackFrameInfoList(new ArrayList<>()).build();
                threadInfoMap.put(threadId, heapThreadInfo);
            }

            // 创建 StackFrame
            String fileName = "";
            if (fileNameId < 0 || fileNameId >= strings.size()) {
                LOGGER.warn("Invalid fileNameId: {}, strings.size: {}.", fileNameId, strings.size());
            } else {
                fileName = strings.get(fileNameId);
            }
            String methodName = "";
            if (methodNameId < 0 || methodNameId >= strings.size()) {
                LOGGER.warn("Invalid methodNameId: {}, strings.size: {}.", methodNameId, strings.size());
            } else {
                methodName = strings.get(methodNameId);
            }
            StackFrame stackFrame = StackFrame.builder().id(stackFrameId).lineNumber(lineNumber).fileName(fileName)
                    .methodName(methodName).localObjectList(new ArrayList<>()).build();
            stackFrameObjectMap.put(stackFrameId, stackFrame);
            heapThreadInfo.getStackFrameInfoList().add(stackFrame);
        }
    }
}
