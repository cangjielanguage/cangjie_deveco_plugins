/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CjLaneSliceVo
 *
 * @since 2025/4/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjLaneSliceVo {
    Long timeStamp;

    Integer value;
}
