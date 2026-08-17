/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto.request;

import com.huawei.deveco.insight.ohos.utils.annotations.NotBlank;
import com.huawei.deveco.insight.ohos.utils.annotations.NotNull;

import lombok.Data;

import java.util.List;

/**
 * CjThreadSliceRequest
 *
 * @since 2025/04/22
 */
@Data
public class CjThreadSliceRequest {
    @NotBlank
    private String sessionId;

    @NotNull
    private Long startTime;

    @NotNull
    private Long endTime;

    @NotBlank
    private String stateName;

    @NotNull
    private List<Long> processIdList;
}
