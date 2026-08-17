/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * cangjie stack
 *
 * @since 2025/03/04/18:54
 */
@Data
@NoArgsConstructor
public class CjStack {
    /**
     * NodeID
     */
    private Integer nodeId;

    /**
     * FunctionInfo index
     */
    private Integer functionInfoIndex;

    /**
     * FunctionId
     */
    private Integer functionId;

    /**
     * Function name
     */
    private String name;

    /**
     * Url
     */
    private String scriptName;

    /**
     * Root path
     */
    private String rootPath;

    /**
     * Number of the row where the function is located
     */
    private Integer line;

    /**
     * Number of the col where the function is located
     */
    private Integer column;

    /**
     * function size
     */
    private Integer size;

    private List<CjStack> children = new ArrayList<>();
}
