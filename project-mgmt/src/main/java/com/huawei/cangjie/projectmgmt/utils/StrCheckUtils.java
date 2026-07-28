/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.intellij.openapi.util.SystemInfo;

import java.util.regex.Pattern;

/**
 * StrCheckUtils
 *
 * @since 2022-9-23
 */
public class StrCheckUtils {
    private static final Pattern PATH_PATTERN_WIN = Pattern.compile("^[a-zA-Z]:[()\\\\.\\-_a-zA-Z\\d]*$");
    private static final Pattern PATH_PATTERN_LINUX = Pattern.compile("^[/().\\-_a-zA-Z\\d]*$");
    private static final Pattern PATH_PATTERN_WIN_BASE = Pattern.compile("^[a-zA-Z]:\\\\.+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]\\w{0,30}$");
    private static final Pattern BUNDLE_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*([.][a-z][a-z\\d_]*)+$");
    private static final Pattern BUNDLE_NAME_COUNT_PATTERN = Pattern.compile("^.{7,127}$");

    /**
     * Check if target path is safe
     *
     * @param path target path
     * @return boolean if target path is safe
     */
    public static boolean isSafePath(String path) {
        return SystemInfo.isWindows
                ? PATH_PATTERN_WIN.matcher(path).matches()
                : PATH_PATTERN_LINUX.matcher(path).matches();
    }

    /**
     * Check if target path is available
     *
     * @param path target path
     * @return boolean if target path is available
     */
    public static boolean isAvailableRootPath(String path) {
        if (("").equals(path) || ("/").equals(path)) {
            return false;
        }
        return !SystemInfo.isWindows || PATH_PATTERN_WIN_BASE.matcher(path).matches();
    }

    /**
     * Check if name is safe
     *
     * @param name target custom name
     * @return boolean target custom name is safe
     */
    public static boolean isSafeName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Check if BundleName is safe
     *
     * @param name target BundleName
     * @return boolean BundleName is safe
     */
    public static boolean isSafeBundleName(String name) {
        return BUNDLE_NAME_PATTERN.matcher(name).matches() && BUNDLE_NAME_COUNT_PATTERN.matcher(name).matches();
    }

    /**
     * Check if target string is end with dot
     *
     * @param str target string
     * @return boolean target string is end with dot
     */
    public static boolean isEndWithPeriods(String str) {
        return str.endsWith(".");
    }
}
