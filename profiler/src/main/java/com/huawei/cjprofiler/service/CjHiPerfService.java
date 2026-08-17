/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.deveco.insight.ohos.common.constant.CangJieConstant;
import com.huawei.deveco.insight.ohos.model.vo.HiPerfNode;
import com.huawei.deveco.insight.ohos.model.vo.hiperf.HiPerfNodeStateVo;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * HiPerfHandler
 *
 * @since 2023-1-13
 */
public class CjHiPerfService {
    /**
     * selectCangJieStateVo
     *
     * @param stateVoList List<HiPerfNodeStateVo>
     * @return List<HiPerfNodeStateVo>
     */
    public static List<HiPerfNodeStateVo> selectCangJieStateVo(List<HiPerfNodeStateVo> stateVoList) {
        List<HiPerfNodeStateVo> result = new ArrayList<>();
        Long startTime = stateVoList.get(0).getStartTime();
        for (HiPerfNodeStateVo hiPerfNodeStateVo : stateVoList) {
            if (Objects.equals(hiPerfNodeStateVo.getCategory(), CangJieConstant.CANG_JIE)) {
                addIdleState(result, startTime, hiPerfNodeStateVo.getStartTime());
                result.add(hiPerfNodeStateVo);
                startTime = hiPerfNodeStateVo.getStartTime() + hiPerfNodeStateVo.getDuration();
            }
        }
        HiPerfNodeStateVo lastStateVo = stateVoList.get(stateVoList.size() - 1);
        if (!Objects.equals(lastStateVo.getCategory(), CangJieConstant.CANG_JIE)) {
            addIdleState(result, startTime, lastStateVo.getStartTime() + lastStateVo.getDuration());
        }
        return result;
    }

    private static void addIdleState(List<HiPerfNodeStateVo> stateVoList, Long startTime, Long endTime) {
        stateVoList.add(HiPerfNodeStateVo.builder()
            .startTime(startTime)
            .endTime(endTime)
            .duration(endTime - startTime)
            .name("idle")
            .category("system")
            .build());
    }

    /**
     * Constructing a subtree of cangJie
     *
     * @param  threadNode HiPerfNode
     * @return result HiPerfNode
     */
    public static Optional<HiPerfNode> searchCangJieData(HiPerfNode threadNode) {
        List<HiPerfNode> hiPerfNodeList = new ArrayList<>();
        dfs(threadNode, hiPerfNodeList);
        if (CollectionUtils.isNotEmpty(hiPerfNodeList)) {
            threadNode.setChildren(hiPerfNodeList);
            return Optional.of(threadNode);
        }
        return Optional.empty();
    }

    private static void dfs(HiPerfNode node, List<HiPerfNode> hiPerfNodeList) {
        if (Objects.equals(node.getCategory(), CangJieConstant.CANG_JIE)) {
            hiPerfNodeList.add(node);
            return;
        }
        for (HiPerfNode child : node.getChildren()) {
            dfs(child, hiPerfNodeList);
        }
    }

    /**
     * Constructing a callstack subtree based on the query time and tid
     *
     * @param threadNodes List<HiPerfNode>
     * @return result boolean
     */
    public static List<HiPerfNode> searchIcicle(List<HiPerfNode> threadNodes) {
        HiPerfNode curNode = threadNodes.get(0);
        threadNodes.clear();
        Optional<HiPerfNode> hiPerfNodeOptional = searchCangJieData(curNode);
        if (hiPerfNodeOptional.isEmpty()) {
            return threadNodes;
        }
        HiPerfNode rootNode = hiPerfNodeOptional.get();
        if (CollectionUtils.isEmpty(rootNode.getChildren())) {
            return threadNodes;
        }
        rootNode.setChildren(setCangJieDepth(rootNode.getChildren()));
        threadNodes.add(rootNode);
        return threadNodes;
    }

    private static List<HiPerfNode> setCangJieDepth(List<HiPerfNode> hiperfNodeList) {
        // 按照 startTime 排序
        hiperfNodeList.sort(Comparator.comparingLong(HiPerfNode::getStartBootTime));
        for (HiPerfNode node : hiperfNodeList) {
            processNode(node, 0, hiperfNodeList);
        }
        return hiperfNodeList;
    }

    private static void processNode(HiPerfNode node, int parentDepth, List<HiPerfNode> hiperfNodeList) {
        boolean hasOverlap = false;
        for (HiPerfNode otherNode : hiperfNodeList) {
            if (otherNode != node && otherNode.getDepth() == parentDepth) {
                if (node.getStartBootTime() < otherNode.getEndBootTime()
                    && node.getEndBootTime() > otherNode.getStartBootTime()) {
                    hasOverlap = true;
                    break;
                }
            }
        }
        if (hasOverlap) {
            // 如果有重叠，尝试放到下一层
            processNode(node, parentDepth + 1, hiperfNodeList);
        } else {
            // 设置当前节点的深度
            node.setDepth(parentDepth);
            // 处理子节点
            for (HiPerfNode child : node.getChildren()) {
                processNode(child, parentDepth + 1, hiperfNodeList);
            }
        }
    }
}
