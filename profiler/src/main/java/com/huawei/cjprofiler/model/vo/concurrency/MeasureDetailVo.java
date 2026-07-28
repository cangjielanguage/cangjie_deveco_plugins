/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.vo.concurrency;

import com.huawei.deveco.insight.ohos.model.vo.concurrency.TaskJumpDetailVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MeasureDetailVo
 *
 * @since 2024/4/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasureDetailVo {
    /**
     * Task State or Task Name
     */
    private String type;

    private String name;

    private int cjThreadId;

    private long avgDuration;

    private long minDuration;

    private long maxDuration;

    private long totalDuration;

    private TaskJumpDetailVo relatedTaskDetail;

    private List<MeasureDetailVo> children;
}
