/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint.utils;

import java.io.File;

/**
 * cjlint constant class
 *
 * @since 2024-04-02
 */
public class CodeCheckConstantUtil {
    /**
     * Regex
     */
    public static final String REGEX = "^([A-Z_]{1,10}_\\d{1,5})";

    /**
     * cangjie engine name
     */
    public static final String ENGINE_NAME = "cjlint";

    /**
     * cangjie language
     */
    public static final String LANGUAGE_CANGJIE = "cj";

    /**
     * file separator
     */
    public static final String SEPARATOR = File.separator;
}
