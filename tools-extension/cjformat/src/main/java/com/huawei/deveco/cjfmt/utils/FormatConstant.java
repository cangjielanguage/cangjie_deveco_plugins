/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.utils;

import java.util.regex.Pattern;

/**
 * Format constant class
 *
 * @since 2024-01-17
 */
public class FormatConstant {
    /**
     * language pattern
     */
    public static final Pattern AVAILABLE_LANGUAGE = Pattern.compile("^(.+)\\.(cj)$");

    /**
     * format exe
     */
    public static final String CJFMT_WINDOWS = "cjfmt.exe";

    /**
     * linux or mac format
     */
    public static final String CJFMT_MAC = "cjfmt";

    /**
     * reformat code
     */
    public static final String REFORMAT_CODE = "Reformat Code";

    /**
     * format dir
     */
    public static final String REFORMAT_DIR = "-d";
}
