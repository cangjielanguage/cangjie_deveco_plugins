/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotConstructorNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDiffNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotInstanceNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotRetainerNode;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkHeapNodeExpandRequest;

import org.jetbrains.annotations.NotNull;

/**
 * PageToolsForCjprof
 *
 * @since 2026-04-20
 */
public class PageToolsForCjprof {
    /**
     * pageConstructorNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @param constructorNode constructorNode
     * @return JsHeapSnapshotConstructorNode
     */
    @NotNull
    public static JsHeapSnapshotConstructorNode pageConstructorNode(ArkHeapNodeExpandRequest request,
        JsHeapSnapshotConstructorNode constructorNode) {
        int startPosition = request.getStart();
        int endPosition = Math.min(request.getStart() + request.getLength(), constructorNode.getChildrenCount());
        return pageConstructorNode(constructorNode, startPosition, endPosition);
    }

    /**
     * pageConstructorNode
     *
     * @param constructorNode constructorNode
     * @param startPosition startPosition
     * @param endPosition endPosition
     * @return JsHeapSnapshotConstructorNode
     */
    @NotNull
    public static JsHeapSnapshotConstructorNode pageConstructorNode(JsHeapSnapshotConstructorNode constructorNode,
        int startPosition, int endPosition) {
        return JsHeapSnapshotConstructorNode.builder()
            .id(constructorNode.getId())
            .name(constructorNode.getName())
            .distance(constructorNode.getDistance())
            .shallowSize(constructorNode.getShallowSize())
            .retainedSize(constructorNode.getRetainedSize())
            .shallowSizePercent(constructorNode.getShallowSizePercent())
            .retainedSizePercent(constructorNode.getRetainedSizePercent())
            .pathNode(constructorNode.getPathNode())
            .childrenCount(constructorNode.getChildrenCount())
            .startPosition(startPosition)
            .endPosition(endPosition - 1)
            .children(constructorNode.getChildren().subList(startPosition, endPosition))
            .build();
    }

    /**
     * pageInstanceNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @param instanceNode JsHeapSnapshotInstanceNode
     * @return JsHeapSnapshotInstanceNode
     */
    @NotNull
    public static JsHeapSnapshotInstanceNode pageInstanceNode(ArkHeapNodeExpandRequest request,
        JsHeapSnapshotInstanceNode instanceNode) {
        int startPosition = request.getStart() > instanceNode.getChildrenCount()
            ? instanceNode.getStartPosition()
            : request.getStart();
        int endPosition = Math.min(request.getStart() + request.getLength(), instanceNode.getChildrenCount());
        int startPositionRef = request.getStart() > instanceNode.getRetainerCount()
            ? instanceNode.getStartPositionRef()
            : request.getStart();
        int endPositionRef = Math.min(request.getStart() + request.getLength(), instanceNode.getRetainerCount());
        return JsHeapSnapshotInstanceNode.builder()
            .id(instanceNode.getId())
            .name(instanceNode.getName())
            .distance(instanceNode.getDistance())
            .shallowSize(instanceNode.getShallowSize())
            .retainedSize(instanceNode.getRetainedSize())
            .shallowSizePercent(instanceNode.getShallowSizePercent())
            .retainedSizePercent(instanceNode.getRetainedSizePercent())
            .pathNode(instanceNode.getPathNode())
            .pathIndex(instanceNode.getPathIndex())
            .childrenCount(instanceNode.getChildrenCount())
            .startPosition(startPosition)
            .endPosition(endPosition > 0 ? endPosition - 1 : endPosition)
            .children(instanceNode.getChildren().subList(startPosition, endPosition))
            .retainerCount(instanceNode.getRetainerCount())
            .startPositionRef(startPositionRef)
            .endPositionRef(endPositionRef > 0 ? endPositionRef - 1 : endPositionRef)
            .retainerNodes(instanceNode.getRetainerNodes().subList(startPositionRef, endPositionRef))
            .build();
    }

    /**
     * pageRetainerNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @param retainerNode JsHeapSnapshotRetainerNode
     * @return JsHeapSnapshotRetainerNode
     */
    @NotNull
    public static JsHeapSnapshotRetainerNode pageRetainerNode(ArkHeapNodeExpandRequest request,
        JsHeapSnapshotRetainerNode retainerNode) {
        int startPosition = request.getStart();
        int endPosition = Math.min(request.getStart() + request.getLength(), retainerNode.getChildrenCount());
        return JsHeapSnapshotRetainerNode.builder()
            .id(retainerNode.getId())
            .name(retainerNode.getName())
            .distance(retainerNode.getDistance())
            .shallowSize(retainerNode.getShallowSize())
            .retainedSize(retainerNode.getRetainedSize())
            .shallowSizePercent(retainerNode.getShallowSizePercent())
            .retainedSizePercent(retainerNode.getRetainedSizePercent())
            .pathNode(retainerNode.getPathNode())
            .pathIndex(retainerNode.getPathIndex())
            .childrenCount(retainerNode.getChildrenCount())
            .startPosition(startPosition)
            .endPosition(endPosition - 1)
            .children(retainerNode.getChildren().subList(startPosition, endPosition))
            .build();
    }

    /**
     * pageDiffNode
     *
     * @param request ArkHeapNodeExpandRequest
     * @param diffNode JsHeapSnapshotDiffNode
     * @return JsHeapSnapshotDiffNode
     */
    @NotNull
    public static JsHeapSnapshotDiffNode pageDiffNode(ArkHeapNodeExpandRequest request,
        JsHeapSnapshotDiffNode diffNode) {
        int startPosition = request.getStart();
        int endPosition = Math.min(request.getStart() + request.getLength(), diffNode.getChildrenCount());
        return JsHeapSnapshotDiffNode.builder()
            .id(diffNode.getId())
            .name(diffNode.getName())
            .className(diffNode.getClassName())
            .distance(diffNode.getDistance())
            .shallowSize(diffNode.getShallowSize())
            .retainedSize(diffNode.getRetainedSize())
            .shallowSizePercent(diffNode.getShallowSizePercent())
            .retainedSizePercent(diffNode.getRetainedSizePercent())
            .addedSize(diffNode.getAddedSize())
            .addedCount(diffNode.getAddedCount())
            .removedSize(diffNode.getRemovedSize())
            .removedCount(diffNode.getRemovedCount())
            .countDelta(diffNode.getCountDelta())
            .sizeDelta(diffNode.getSizeDelta())
            .pathNode(diffNode.getPathNode())
            .childrenCount(diffNode.getChildrenCount())
            .startPosition(startPosition)
            .endPosition(endPosition - 1)
            .children(diffNode.getChildren().subList(startPosition, endPosition))
            .build();
    }
}
