/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint;

import lombok.Data;

/**
 * json defect info
 *
 * @since 2023-01-14
 */
@Data
public class DefectInfo {
    private String defectLevel;
    private String file;
    private String description;
    private String defectType;
    private String language;
    private int line;
    private int column;
}