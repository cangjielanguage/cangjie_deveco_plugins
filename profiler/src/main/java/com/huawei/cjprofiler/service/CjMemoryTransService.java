/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotConstructorNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotInstanceNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotRetainerNode;

import java.util.ArrayList;
import java.util.List;

/**
 * CjMemoryTransService
 *
 * @since 2026-04-14
 */
public class CjMemoryTransService {
    /**
     * buildPathNodeFirstLevel
     *
     * @param constructorNode constructorNode
     */
    public static void buildPathNodeFirstLevel(JsHeapSnapshotConstructorNode constructorNode) {
        for (JsHeapSnapshotInstanceNode instanceNode : constructorNode.getChildren()) {
            List<Integer> pathNodeList = new ArrayList<>();
            pathNodeList.add(constructorNode.getId());
            pathNodeList.add(instanceNode.getId());
            instanceNode.setPathNode(pathNodeList);
            instanceNode.setPathIndex(new ArrayList<>());
            instanceNode.getPathIndex().add(instanceNode.getNodeIndex());
        }
    }

    /**
     * buildPathNode
     *
     * @param instanceNode instanceNode
     */
    public static void buildPathNode(JsHeapSnapshotInstanceNode instanceNode) {
        for (JsHeapSnapshotInstanceNode childInstanceNode : instanceNode.getChildren()) {
            List<Integer> pathNodeList = new ArrayList<>(instanceNode.getPathNode());
            List<Integer> pathIndexList = new ArrayList<>(instanceNode.getPathIndex());
            pathNodeList.add(childInstanceNode.getNodeIndex());
            pathIndexList.add(childInstanceNode.getNodeIndex());
            childInstanceNode.setPathNode(pathNodeList);
            childInstanceNode.setPathIndex(pathIndexList);
        }

        for (JsHeapSnapshotRetainerNode retainerNode : instanceNode.getRetainerNodes()) {
            List<Integer> pathNodeList = new ArrayList<>(instanceNode.getPathNode());
            List<Integer> pathIndexList = new ArrayList<>(instanceNode.getPathIndex());
            pathNodeList.add(retainerNode.getNodeIndex());
            pathIndexList.add(retainerNode.getNodeIndex());
            retainerNode.setPathNode(pathNodeList);
            retainerNode.setPathIndex(pathIndexList);
        }
    }

    /**
     * buildPathNodeForRetainer
     *
     * @param retainerNode retainerNode
     */
    public static void buildPathNodeForRetainer(JsHeapSnapshotRetainerNode retainerNode) {
        for (JsHeapSnapshotRetainerNode childInstanceNode : retainerNode.getChildren()) {
            List<Integer> pathNodeList = new ArrayList<>(retainerNode.getPathNode());
            List<Integer> pathIndexList = new ArrayList<>(retainerNode.getPathIndex());
            pathNodeList.add(childInstanceNode.getNodeIndex());
            pathIndexList.add(childInstanceNode.getNodeIndex());
            childInstanceNode.setPathNode(pathNodeList);
            childInstanceNode.setPathIndex(pathIndexList);
        }
    }
}
