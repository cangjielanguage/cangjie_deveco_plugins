/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.EdgeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 树的节点和边信息
 *
 * @since 2025-02-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TreeInfo {
    List<EdgeInfo> edgeInfoList = new ArrayList<>();

    List<NodeInfo> nodeInfoList = new ArrayList<>();
}
