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
 * 该记录存放一个仓颉线程的栈帧
 *
 * @since 2025-02-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StackFrame {
    private int id;

    private int name;

    private int fileName;

    private long lineNum;
}
