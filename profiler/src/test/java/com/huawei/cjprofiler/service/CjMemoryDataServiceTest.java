/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.LocalObject;
import com.huawei.cjprofiler.ability.arkmemory.parser.CjMemoryDataService;
import com.huawei.cjprofiler.common.enums.RootTypeEnum;
import com.huawei.cjprofiler.model.po.cjprof.HeapThreadInfo;
import com.huawei.cjprofiler.model.po.cjprof.StackFrame;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.CjprofExtension;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CjMemoryDataServiceTest
 *
 * @since 2026-05-22
 */
public class CjMemoryDataServiceTest {
    private RawHeapSnapshot rawHeapSnapshot;

    private CjprofExtension cjprofExtension;

    private Map<Integer, String> rootTypeMap;

    private Map<Integer, Integer> arrayLengthMap;

    @BeforeEach
    public void setUp() {
        rawHeapSnapshot = new RawHeapSnapshot();
        cjprofExtension = new CjprofExtension();
        rawHeapSnapshot.setCjprofExtension(cjprofExtension);
        rootTypeMap = new HashMap<>();
        arrayLengthMap = new HashMap<>();
    }

    @Test
    public void testParseRootTypeMap() {
        // 准备测试数据
        int[][] flattenedData = new int[][] {
            {}, // 第0行不处理
            {1, 2}, // 第1行，对应RootTypeEnum的类型
            {3, 4}, // 第2行，对应RootTypeEnum的类型
            {5}     // 第3行，对应RootTypeEnum的类型
        };

        // 直接设置 CjprofExtension 的属性
        cjprofExtension.setRootDataByTypes(flattenedData);

        CjMemoryDataService.parseRootTypeMap(rootTypeMap, rawHeapSnapshot);

        // 验证结果
        Assertions.assertEquals(5, rootTypeMap.size());
        Assertions.assertEquals(RootTypeEnum.getValueByKey((byte) 1), rootTypeMap.get(1));
        Assertions.assertEquals(RootTypeEnum.getValueByKey((byte) 1), rootTypeMap.get(2));
        Assertions.assertEquals(RootTypeEnum.getValueByKey((byte) 2), rootTypeMap.get(3));
        Assertions.assertEquals(RootTypeEnum.getValueByKey((byte) 2), rootTypeMap.get(4));
        Assertions.assertEquals(RootTypeEnum.getValueByKey((byte) 3), rootTypeMap.get(5));
    }

    @Test
    public void testParseArrayLengthMap() {
        // 准备测试数据
        int[] idAndLengths = new int[] {1, 10, 2, 20, 3, 30};

        // 直接设置 CjprofExtension 的属性
        cjprofExtension.setNodeArrayLengths(idAndLengths);

        CjMemoryDataService.parseArrayLengthMap(arrayLengthMap, rawHeapSnapshot);

        // 验证结果
        Assertions.assertEquals(3, arrayLengthMap.size());
        Assertions.assertEquals(10L, (long) arrayLengthMap.get(1));
        Assertions.assertEquals(20L, (long) arrayLengthMap.get(2));
        Assertions.assertEquals(30L, (long) arrayLengthMap.get(3));
    }

    @Test
    public void testParseArrayLengthMapWithInvalidData() {
        // 测试奇数长度的数组
        int[] idAndLengths = new int[] {1, 10, 2};
        cjprofExtension.setNodeArrayLengths(idAndLengths);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            // 2. 调用会导致报错的方法
            CjMemoryDataService.parseArrayLengthMap(arrayLengthMap, rawHeapSnapshot);
        });
    }

    @Test
    public void testParseRootTypeMapWithNullData() {
        // 测试空数据
        cjprofExtension.setRootDataByTypes(null);

        CjMemoryDataService.parseRootTypeMap(rootTypeMap, rawHeapSnapshot);

        // 验证结果
        Assertions.assertEquals(0, rootTypeMap.size());
    }

    @Test
    public void testParseHeapThreadDetailWithNullRawHeapSnapshot() {
        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(null);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseHeapThreadDetailWithNullCjprofExtension() {
        rawHeapSnapshot.setCjprofExtension(null);
        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseHeapThreadDetailWithEmptyStackFramesMap() {
        cjprofExtension.setStackFramesMap(new HashMap<>());
        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseHeapThreadDetailWithEmptyThreadSummary() {
        // 设置 strings
        rawHeapSnapshot.setStrings(List.of("Thread1", "Method1", "ObjectType1", "File1"));

        // 设置 nodes：每 8 个元素为一个节点
        // [type, nameIdx, id, selfSize, edgeCount, traceNodeId, detachedness, nativeSize]
        rawHeapSnapshot.setNodes(new int[]{0, 2, 100, 1024, 5, 0, 0, 0, // node 0: ObjectType1, selfSize=1024
                0, 2, 101, 2048, 3, 0, 0, 0 // node 1: ObjectType1, selfSize=2048
        });

        // 设置 stackFramesMap: [threadId, threadNameIdx, methodIdx, fileIdx, lineNumber]
        Map<Integer, int[]> stackFramesMap = new HashMap<>();
        stackFramesMap.put(1, new int[]{1, 0, 1, 3, 100}); // frameId=1
        stackFramesMap.put(2, new int[]{1, 0, 1, 3, 101}); // frameId=2
        cjprofExtension.setStackFramesMap(stackFramesMap);

        // 设置空的 threadSummary
        cjprofExtension.setThreadSummary(new int[]{});

        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);

        // 验证：虽然有 stackFramesMap，但没有 threadSummary，所以只有线程信息，没有局部对象
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1, result.get(0).getThreadId());
        Assertions.assertEquals("Thread1", result.get(0).getThreadName());
        Assertions.assertEquals(2, result.get(0).getStackFrameInfoList().size());
    }

    @Test
    public void testParseHeapThreadDetailWithNormalData() {
        // 设置 strings
        rawHeapSnapshot.setStrings(List.of("Thread1", "Method1", "ObjectType1", "File1.java"));

        // 设置 nodes：每 8 个元素为一个节点
        // [type, nameIdx, id, selfSize, edgeCount, traceNodeId, detachedness, nativeSize]
        rawHeapSnapshot.setNodes(new int[]{0, 2, 100, 1024, 5, 0, 0, 0, // node 0: ObjectType1, selfSize=1024
                0, 2, 101, 2048, 3, 0, 0, 0 // node 1: ObjectType1, selfSize=2048
        });

        // 设置 stackFramesMap: [threadId, threadNameIdx, methodIdx, fileIdx, lineNumber]
        Map<Integer, int[]> stackFramesMap = new HashMap<>();
        stackFramesMap.put(1, new int[]{1, 0, 1, 3, 100}); // frameId=1
        stackFramesMap.put(2, new int[]{1, 0, 1, 3, 101}); // frameId=2
        cjprofExtension.setStackFramesMap(stackFramesMap);

        // 设置 threadSummary: [nodeId, stackFrameId, nodeIndex]
        // nodeIndex * 8 = 节点起始位置
        cjprofExtension.setThreadSummary(new int[]{100, 1, 0, // nodeId=100, frameId=1, nodeIndex=0
                101, 2, 1 // nodeId=101, frameId=2, nodeIndex=1
        });

        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);

        // 验证结果
        Assertions.assertEquals(1, result.size());
        HeapThreadInfo threadInfo = result.get(0);
        Assertions.assertEquals(1, threadInfo.getThreadId());
        Assertions.assertEquals("Thread1", threadInfo.getThreadName());

        // 验证栈帧按 id 排序
        List<StackFrame> frames = threadInfo.getStackFrameInfoList();
        Assertions.assertEquals(2, frames.size());
        Assertions.assertEquals(1, frames.get(0).getId());
        Assertions.assertEquals(2, frames.get(1).getId());

        // 验证局部对象
        List<LocalObject> localObjects1 = frames.get(0).getLocalObjectList();
        Assertions.assertEquals(1, localObjects1.size());
        Assertions.assertEquals(100, localObjects1.get(0).getObjectId());
        Assertions.assertEquals("ObjectType1", localObjects1.get(0).getTypeName());
        Assertions.assertEquals(1024, localObjects1.get(0).getShallowSize());

        List<LocalObject> localObjects2 = frames.get(1).getLocalObjectList();
        Assertions.assertEquals(1, localObjects2.size());
        Assertions.assertEquals(101, localObjects2.get(0).getObjectId());
        Assertions.assertEquals("ObjectType1", localObjects2.get(0).getTypeName());
        Assertions.assertEquals(2048, localObjects2.get(0).getShallowSize());
    }

    @Test
    public void testParseHeapThreadDetailFiltersZeroSize() {
        // 设置 strings
        rawHeapSnapshot.setStrings(List.of("Thread1", "Method1", "ObjectType1", "File1.java"));

        // 设置 nodes：包含 selfSize=0 的节点
        // [type, nameIdx, id, selfSize, edgeCount, traceNodeId, detachedness, nativeSize]
        rawHeapSnapshot.setNodes(new int[]{0, 2, 100, 0, 5, 0, 0, 0, // node 0: selfSize=0, 会被过滤
                0, 2, 101, 2048, 3, 0, 0, 0 // node 1: selfSize=2048, 保留
        });

        // 设置 stackFramesMap
        Map<Integer, int[]> stackFramesMap = new HashMap<>();
        stackFramesMap.put(1, new int[]{1, 0, 1, 3, 100});
        stackFramesMap.put(2, new int[]{1, 0, 1, 3, 101});
        cjprofExtension.setStackFramesMap(stackFramesMap);

        // 设置 threadSummary
        cjprofExtension.setThreadSummary(new int[]{100, 1, 0, // selfSize=0, 会被过滤
                101, 2, 1 // selfSize=2048, 保留
        });

        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);

        // 验证：selfSize=0 的对象被过滤了
        HeapThreadInfo threadInfo = result.get(0);
        Assertions.assertEquals(0, threadInfo.getStackFrameInfoList().get(0).getLocalObjectList().size());
        Assertions.assertEquals(1, threadInfo.getStackFrameInfoList().get(1).getLocalObjectList().size());
    }

    @Test
    public void testParseHeapThreadDetailWithMultipleThreads() {
        // 设置 strings
        rawHeapSnapshot.setStrings(List.of("Thread1", "Thread2", "Method1", "ObjectType1"));

        // 设置 nodes
        rawHeapSnapshot.setNodes(new int[]{0, 3, 100, 1024, 5, 0, 0, 0, // node 0
                0, 3, 200, 2048, 3, 0, 0, 0 // node 1
        });

        // 设置 stackFramesMap: thread1 和 thread2
        Map<Integer, int[]> stackFramesMap = new HashMap<>();
        stackFramesMap.put(1, new int[]{1, 0, 2, -1, 100}); // thread 1, frame 1
        stackFramesMap.put(2, new int[]{2, 1, 2, -1, 200}); // thread 2, frame 2
        cjprofExtension.setStackFramesMap(stackFramesMap);

        // 设置 threadSummary
        cjprofExtension.setThreadSummary(new int[]{100, 1, 0, 200, 2, 1});

        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);

        // 验证有 2 个线程
        Assertions.assertEquals(2, result.size());

        // 验证线程名称
        Assertions.assertTrue(result.stream().anyMatch(t -> "Thread1".equals(t.getThreadName())));
        Assertions.assertTrue(result.stream().anyMatch(t -> "Thread2".equals(t.getThreadName())));
    }

    @Test
    public void testParseHeapThreadDetailWithNegativeShallowSize() {
        // 设置 strings
        rawHeapSnapshot.setStrings(List.of("Thread1", "Method1", "ObjectType1"));

        // 设置 nodes，包含负数的 selfSize
        rawHeapSnapshot.setNodes(new int[]{0, 2, 100, -1, 5, 0, 0, 0, // node 0: negative selfSize
                0, 2, 101, 2048, 3, 0, 0, 0 // node 1: valid selfSize
        });

        Map<Integer, int[]> stackFramesMap = new HashMap<>();
        stackFramesMap.put(1, new int[]{1, 0, 1, -1, 100});
        stackFramesMap.put(2, new int[]{1, 0, 1, -1, 101});
        cjprofExtension.setStackFramesMap(stackFramesMap);

        cjprofExtension.setThreadSummary(new int[]{100, 1, 0, 101, 2, 1});

        List<HeapThreadInfo> result = CjMemoryDataService.parseHeapThreadDetail(rawHeapSnapshot);

        // 验证：负数 selfSize 被过滤
        HeapThreadInfo threadInfo = result.get(0);
        Assertions.assertEquals(2, threadInfo.getStackFrameInfoList().size());
        Assertions.assertEquals(0, threadInfo.getStackFrameInfoList().get(0).getLocalObjectList().size());
        Assertions.assertEquals(1, threadInfo.getStackFrameInfoList().get(1).getLocalObjectList().size());
    }
}
