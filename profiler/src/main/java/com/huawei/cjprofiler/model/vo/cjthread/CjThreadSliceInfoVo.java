/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.vo.cjthread;

import com.huawei.deveco.insight.ohos.model.vo.concurrency.TaskThreadDetailVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CjThreadSliceInfoVo
 *
 * @since 2025/4/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjThreadSliceInfoVo {
    private String name;

    private int threadId;

    private String state;

    private Long startTime;

    private Long endTime;

    private Long duration;

    private TaskThreadDetailVo relateThreadDetail;
}