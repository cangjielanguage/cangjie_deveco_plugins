/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump;

/**
 * 定义接口，包含将对象属性转换为整数数组的方法
 *
 * @since 2025-02-10
 */
public interface BaseTreeInfo {
    /**
     * 返回int数组
     *
     * @return array
     */
    int[] toIntArray();
}
