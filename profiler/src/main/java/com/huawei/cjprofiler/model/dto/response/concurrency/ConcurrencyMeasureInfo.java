/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto.response.concurrency;

import com.huawei.cjprofiler.model.vo.concurrency.MeasureDetailVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ConcurrencyMeasureInfo
 *
 * @since 2024/4/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrencyMeasureInfo {
    private List<MeasureDetailVo> measureInfoDetail;
}
