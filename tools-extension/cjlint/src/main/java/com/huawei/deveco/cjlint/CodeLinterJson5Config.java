/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint;

import lombok.Data;

import java.util.List;

/**
 * code-linter.json5配置文件的JSON映射类
 *
 * @since 2025-06-28
 */
@Data
public class CodeLinterJson5Config {
    /**
     * 配置待检查的文件名单
     */
    private List<String> files;

    /**
     * 配置无需检查的文件目录，其指定的目录或文件需使用相对路径格式，相对于code-linter.json5所在工程根目录
     */
    private List<String> ignore;
}
