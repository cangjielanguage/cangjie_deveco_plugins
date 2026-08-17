/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto.response.cjthread;

import com.huawei.cjprofiler.model.vo.CjLaneSliceVo;

import java.util.List;

/**
 * CjLaneSliceData
 *
 * @since 2025/04/22
 */
public record CjLaneSliceData(List<CjLaneSliceVo> cjLaneSliceInfo) {}
