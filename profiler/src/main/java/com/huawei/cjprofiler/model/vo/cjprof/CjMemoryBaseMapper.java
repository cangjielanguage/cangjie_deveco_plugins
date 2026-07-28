/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.vo.cjprof;

import com.huawei.cjprofiler.model.po.cjprof.HeapThreadInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.LocalObject;
import com.huawei.cjprofiler.model.po.cjprof.StackFrame;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotConstructorNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDetailNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDiffNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotInstanceNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotRetainerNode;

import com.cjprof.jni.model.BaseNode;
import com.cjprof.jni.model.ConstructorDiffNode;
import com.cjprof.jni.model.ConstructorNode;
import com.cjprof.jni.model.Frame;
import com.cjprof.jni.model.InstanceDiffNode;
import com.cjprof.jni.model.InstanceNode;
import com.cjprof.jni.model.JsHeapSnapshotConstructorDiffNode;
import com.cjprof.jni.model.ThreadInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * CjMemoryBaseMapper
 *
 * @since 2026-04-14
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {java.util.Collections.class})
public interface CjMemoryBaseMapper {
    /**
     * INSTANCE
     */
    CjMemoryBaseMapper INSTANCE = Mappers.getMapper(CjMemoryBaseMapper.class);

    /**
     * toJsHeapSnapshotConstructorNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotConstructorNode
     */
    @Mapping(source = "className", target = "name")
    @Mapping(source = "className", target = "className")
    @Mapping(source = ".", target = "id", qualifiedByName = "saveId")
    @Mapping(source = "children", target = "children", qualifiedByName = "BaseInstanceMapping")
    @Mapping(source = "shallowSizePercent", target = "shallowSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "retainedSizePercent", target = "retainedSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "totalInstanceCountPercent", target = "totalInstanceCountPercent",
        qualifiedByName = "doubleToIntPercent")
    @Mapping(target = "pathNode", expression = "java(Collections.singletonList((int)source.getNodeIndex()))")
    JsHeapSnapshotConstructorNode toJsHeapSnapshotConstructorNode(ConstructorNode source,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * baseToJsHeapSnapshotDiffNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotDiffNode
     */
    @InheritConfiguration(name = "toJsHeapSnapshotConstructorNode")
    JsHeapSnapshotDiffNode baseToJsHeapSnapshotDiffNode(ConstructorDiffNode source,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotDiffNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotDiffNode
     */
    default JsHeapSnapshotDiffNode toJsHeapSnapshotDiffNode(ConstructorDiffNode source,
        Map<Integer, Long> indexToIdMap) {
        JsHeapSnapshotDiffNode node = baseToJsHeapSnapshotDiffNode(source, indexToIdMap);

        updateAddedRemoved(node, source);

        return node;
    }

    /**
     * toJsHeapSnapshotConstructorDiffNode
     *
     * @param source source
     * @param diffSnapshotId diffSnapshotId
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotConstructorDiffNode
     */
    @Mapping(source = "source.className", target = "name")
    @Mapping(source = "source.className", target = "className")
    @Mapping(source = "source.", target = "id", qualifiedByName = "saveId")
    @Mapping(source = "source.children", target = "children", qualifiedByName = "BaseInstanceMapping")
    @Mapping(source = "source.shallowSizePercent", target = "shallowSizePercent",
        qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "source.retainedSizePercent", target = "retainedSizePercent",
        qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "source.totalInstanceCountPercent", target = "totalInstanceCountPercent",
        qualifiedByName = "doubleToIntPercent")
    @Mapping(target = "pathNode", expression = "java(Collections.singletonList((int)source.getNodeIndex()))")
    @Mapping(target = "jsBaseHeapSnapshotId", source = "diffSnapshotId.jsBaseHeapSnapshotId")
    @Mapping(target = "jsTargetHeapSnapshotId", source = "diffSnapshotId.jsTargetHeapSnapshotId")
    JsHeapSnapshotConstructorDiffNode toJsHeapSnapshotConstructorDiffNode(ConstructorDiffNode source,
        DiffSnapshotId diffSnapshotId, @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotInstanceNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotInstanceNode
     */
    @Named("BaseInstanceMapping")
    @Mapping(source = "className", target = "name")
    @Mapping(source = "type", target = "type", qualifiedByName = "parseType")
    @Mapping(source = "rootType", target = "rootType", qualifiedByName = "parseType")
    @Mapping(source = ".", target = "id", qualifiedByName = "saveId")
    @Mapping(source = "shallowSizePercent", target = "shallowSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "shallowSizePercent", target = "totalShallowSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "retainedSizePercent", target = "retainedSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "retainedSizePercent", target = "totalRetainedSizePercent",
        qualifiedByName = "doubleToIntPercent")
    @Mapping(target = "fromCjprof", constant = "true")
    JsHeapSnapshotInstanceNode toJsHeapSnapshotInstanceNode(InstanceNode source,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotDetailNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotDetailNode
     */
    @InheritConfiguration(name = "toJsHeapSnapshotInstanceNode")
    JsHeapSnapshotDetailNode toJsHeapSnapshotDetailNode(InstanceNode source, @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotRetainerNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotRetainerNode
     */
    @Mapping(source = "className", target = "name")
    @Mapping(source = ".", target = "id", qualifiedByName = "saveId")
    @Mapping(source = "shallowSizePercent", target = "shallowSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "shallowSizePercent", target = "totalShallowSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "retainedSizePercent", target = "retainedSizePercent", qualifiedByName = "doubleToIntPercent")
    @Mapping(source = "retainedSizePercent", target = "totalRetainedSizePercent",
        qualifiedByName = "doubleToIntPercent")
    // InstanceNode.retainerCount == JsHeapSnapshotRetainerNode.childrenCount
    @Mapping(source = "retainerCount", target = "childrenCount")
    JsHeapSnapshotRetainerNode toJsHeapSnapshotRetainerNode(InstanceNode source,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotInstanceNode
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return JsHeapSnapshotInstanceNode
     */
    @InheritConfiguration(name = "toJsHeapSnapshotInstanceNode")
    @Mapping(source = "added", target = "isAddedRemoved")
    JsHeapSnapshotInstanceNode toJsHeapSnapshotInstanceNode(InstanceDiffNode source,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * toLocalObject
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return LocalObject
     */
    @Named("toLocalObject")
    @Mapping(source = "className", target = "typeName")
    @Mapping(source = "source.", target = "objectId", qualifiedByName = "saveId")
    LocalObject toLocalObject(InstanceNode source, @Context Map<Integer, Long> indexToIdMap);

    /**
     * toJsHeapSnapshotConstructorNodes
     *
     * @param sourceList sourceList
     * @param indexToIdMap indexToIdMap
     * @return List<JsHeapSnapshotConstructorNode>
     */
    List<JsHeapSnapshotConstructorNode> toJsHeapSnapshotConstructorNodes(List<ConstructorNode> sourceList,
        @Context Map<Integer, Long> indexToIdMap);

    /**
     * toStackFrame
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return HeapThreadInfo.StackFrame
     */
    @Named("toStackFrame")
    @Mapping(source = "funcName", target = "methodName")
    @Mapping(source = "line", target = "lineNumber")
    @Mapping(source = "locals", target = "localObjectList", qualifiedByName = "toLocalObject")
    StackFrame toStackFrame(Frame source, @Context Map<Integer, Long> indexToIdMap);

    /**
     * toHeapThreadInfo
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return HeapThreadInfo
     */
    @Mapping(source = "name", target = "threadName")
    @Mapping(source = "id", target = "threadId")
    @Mapping(source = "frames", target = "stackFrameInfoList", qualifiedByName = "toStackFrame")
    HeapThreadInfo toHeapThreadInfo(ThreadInfo source, @Context Map<Integer, Long> indexToIdMap);

    /**
     * toHeapThreadInfos
     *
     * @param sources sources
     * @param indexToIdMap indexToIdMap
     * @return List<HeapThreadInfo>
     */
    List<HeapThreadInfo> toHeapThreadInfos(List<ThreadInfo> sources, @Context Map<Integer, Long> indexToIdMap);

    /**
     * copyJsHeapSnapshotConstructorNode
     *
     * @param source source
     * @return JsHeapSnapshotConstructorNode
     */
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "childrenCount", ignore = true)
    JsHeapSnapshotConstructorNode copyJsHeapSnapshotConstructorNode(JsHeapSnapshotConstructorNode source);

    /**
     * copyJsHeapSnapshotDiffNode
     *
     * @param source source
     * @return JsHeapSnapshotConstructorDiffNode
     */
    @InheritConfiguration(name = "copyJsHeapSnapshotConstructorNode")
    JsHeapSnapshotConstructorDiffNode copyJsHeapSnapshotDiffNode(JsHeapSnapshotDiffNode source);

    // 只要写了这个方法，MapStruct 就会用它来处理所有 Stack<Integer> 字段

    /**
     * mapStack
     *
     * @param source source
     * @return Stack<Integer>
     */
    default Stack<Integer> mapStack(Stack<Integer> source) {
        if (source == null) {
            return new Stack<>();
        }
        Stack<Integer> target = new Stack<>();
        target.addAll(source); // 使用 addAll 规避构造函数问题
        return target;
    }

    /**
     * doubleToIntPercent  cjprof百分比传值逻辑：实际85.11% -> 传值为85.11
     *
     * @param sizePercent sizePercent
     * @return int
     */
    @Named("doubleToIntPercent")
    default int doubleToIntPercent(double sizePercent) {
        return (int) sizePercent;
    }

    /**
     * parseType
     *
     * @param s s
     * @return String
     */
    @Named("parseType")
    default String parseType(String s) {
        int start = s.indexOf('[');
        int end = s.indexOf(']');
        String result = "";

        // 检查是否都存在，且顺序正确
        if (start != -1 && end > start) {
            result = s.substring(start + 1, end);
        }
        return result;
    }

    /**
     * updateAddedRemoved
     *
     * @param diffNode diffNode
     * @param source source
     */
    default void updateAddedRemoved(JsHeapSnapshotDiffNode diffNode, ConstructorDiffNode source) {
        if (diffNode == null || diffNode.getChildren() == null) {
            return;
        }

        List<Boolean> childAddedStates = source.getChildAddedStates();
        int currentIndex = 0;

        for (JsHeapSnapshotInstanceNode instanceNode : diffNode.getChildren()) {
            boolean isAdded = childAddedStates.get(currentIndex);

            instanceNode.setAddedRemoved(isAdded);
            instanceNode.setAddedCount(isAdded ? 1 : 0);
            instanceNode.setRemovedCount(isAdded ? 0 : 1);
            instanceNode.setCountDelta(instanceNode.getAddedCount() - instanceNode.getRemovedCount());

            currentIndex++;
        }
    }

    /**
     * toJsHeapSnapshotConstructorDiffNode
     *
     * @param source source
     * @param diffSnapshotId diffSnapshotId
     * @param indexToIdMap indexToIdMap
     * @return List<JsHeapSnapshotConstructorDiffNode>
     */
    default List<JsHeapSnapshotConstructorDiffNode> toJsHeapSnapshotConstructorDiffNode(
        List<ConstructorDiffNode> source, DiffSnapshotId diffSnapshotId, @Context Map<Integer, Long> indexToIdMap) {
        return source.stream()
            .map(node -> CjMemoryBaseMapper.INSTANCE.toJsHeapSnapshotConstructorDiffNode(node, diffSnapshotId,
                indexToIdMap))
            .toList();
    }

    /**
     * saveId
     *
     * @param source source
     * @param indexToIdMap indexToIdMap
     * @return int
     */
    @Named("saveId")
    default int saveId(BaseNode source, @Context Map<Integer, Long> indexToIdMap) {
        indexToIdMap.put(source.getNodeIndex(), source.getId());
        return source.getNodeIndex();
    }

    /**
     * DiffSnapshotId
     *
     * @since 2026-04-14
     */
    @Data
    @AllArgsConstructor
    class DiffSnapshotId {
        private String jsBaseHeapSnapshotId;

        private String jsTargetHeapSnapshotId;
    }
}
