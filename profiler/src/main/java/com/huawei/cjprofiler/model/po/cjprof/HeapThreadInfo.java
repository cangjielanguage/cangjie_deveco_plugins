/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.po.cjprof;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仓颉堆内存线程信息
 *
 * @since 2026-04-15
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HeapThreadInfo {
    /**
     * 线程ID
     */
    private int threadId;

    /**
     * 线程名
     */
    private String threadName;

    /**
     * 栈帧列表
     */
    private List<StackFrame> stackFrameInfoList;
}
