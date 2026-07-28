/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint;

import static com.huawei.deveco.cjlint.utils.CodeCheckConstantUtil.REGEX;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * manager for cjlint
 *
 * @since 2024-04-02
 */
public class CjLintManager {
    /**
     * get ignore rule
     *
     * @param ruleId rule id
     * @return ignore rule
     */
    public static String getIgnoreRule(String ruleId) {
        Matcher matcher = Pattern.compile(REGEX).matcher(ruleId);
        String result = "";
        if (matcher.find()) {
            result = "!" + matcher.group().replaceAll("_", ".");
        }
        return result;
    }
}
