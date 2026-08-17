/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 局部对象信息
 *
 * @since 2026-04-15
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LocalObject {
    /**
     * 类型名
     */
    private String typeName;

    /**
     * 对象ID
     */
    private int objectId;

    /**
     * Shallow Size
     */
    private int shallowSize;

    /**
     * Retained Size
     */
    private int retainedSize;
}
