/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.po.cjthread;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CjThreadSliceInfoPo
 *
 * @since 2025/4/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjThreadSliceInfoPo {
    private String state;

    private int cjThreadId;

    private long startTime;

    private long endTime;

    private long duration;

    // 代码跳转使用
    private int threadId;

    // 代码跳转使用
    private int processId;
}
