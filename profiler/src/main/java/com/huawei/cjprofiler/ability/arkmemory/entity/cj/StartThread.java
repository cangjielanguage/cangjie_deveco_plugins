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
 * 该记录主要用于存放线程信息
 *
 * @since 2025-02-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StartThread {
    // 线程序号
    private long idx;

    // 线程 ID
    private int id;

    // 线程当前的调用栈序号
    private long stackTraceIdx;

    // 线程名对应的字符串 ID
    private int name;
}
