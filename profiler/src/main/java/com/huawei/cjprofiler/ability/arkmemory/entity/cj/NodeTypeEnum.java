/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj;

import lombok.Getter;

/**
 * 该记录为节点类型枚举
 *
 * @since 2025-02-10
 */
@Getter
public enum NodeTypeEnum {
    /**
     * Array
     */
    ARRAY(1),

    /**
     * STRING
     */
    STRING(2),

    /**
     * OBJECT
     */
    OBJECT(3);

    private final int value;

    NodeTypeEnum(int value) {
        this.value = value;
    }
}
