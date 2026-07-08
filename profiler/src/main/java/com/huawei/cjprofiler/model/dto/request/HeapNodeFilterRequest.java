/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HeapNodeFilterRequest
 *
 * @since 2026-04-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeapNodeFilterRequest {
    private String sessionId;

    private String rawId;

    private String curRawId;

    private List<String> rootTypeList;
}
