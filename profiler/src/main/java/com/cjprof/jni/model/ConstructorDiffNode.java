/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.cjprof.jni.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 构造节点差异类
 *
 * @since 2026-04-14
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConstructorDiffNode extends ConstructorNode {
    private int addedCount;

    private int removedCount;

    private long countDelta;

    private int addedSize;

    private int removedSize;

    private long sizeDelta;

    private int baseTotalSize;

    private int targetTotalSize;

    private List<Boolean> childAddedStates;
}