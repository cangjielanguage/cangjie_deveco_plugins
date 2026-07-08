/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import java.text.Normalizer;
import java.util.Optional;

/**
 * StringUtil
 *
 * @since 2020-10-08
 */
public class StringUtil {
    /**
     * empty string
     */
    public static final String EMPTY = "";

    /**
     * 判断字符串是否为空
     *
     * @param str 待判断的字符串
     * @return 字符串为空返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 待判断的字符串
     * @return 字符串不为空返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 将char数组转化为String
     *
     * @param chars 待转换的char数组
     * @return string
     */
    public static String chars2Str(char[] chars) {
        StringBuilder sb = new StringBuilder(chars.length);
        for (char ch : chars) {
            sb.append(ch);
        }

        return sb.toString();
    }

    /**
     * 标准化路径字符串
     *
     * @param input input
     * @return normalized String
     */
    public static Optional<String> normalize(String input) {
        return input == null ? Optional.empty() : Normalizer.normalize(input, Normalizer.Form.NFKC).describeConstable();
    }
}
