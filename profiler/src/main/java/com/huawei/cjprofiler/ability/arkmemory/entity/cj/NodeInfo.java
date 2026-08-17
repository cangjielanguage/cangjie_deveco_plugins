/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.BaseTreeInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 该记录为仓颉节点信息
 *
 * @since 2025-02-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NodeInfo implements BaseTreeInfo {
    private int type;

    private int name;

    private int id;

    private int selfSize;

    private int edgeCount;

    private int traceNodeId;

    private int detachedness;

    private int nativeSize;

    @Override
    public int[] toIntArray() {
        return new int[] {
            type, name, id, selfSize, edgeCount, traceNodeId, detachedness, nativeSize
        };
    }
}
