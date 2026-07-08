/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import com.huawei.cangjie.sdkmanager.domain.Progress;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * OsTypeUtil
 *
 * @since 2020-10-10
 */
public class OsTypeUtil {
    private static Boolean isRosetta = null;

    /**
     * 获取操作系统类型
     *
     * @return mac/windows/linux
     */
    public static String getOsType() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            return "mac";
        } else if (os.startsWith("Windows")) {
            return "windows";
        } else if (os.startsWith("Linux")) {
            return "linux";
        } else {
            return "other";
        }
    }

    /**
     * 匹配相应操作系统的osType
     *
     * @param osType osType
     * @return 匹配结果
     */
    public static boolean isOsMatch(String osType) {
        if (OsTypeUtil.isMac() && "darwin".equalsIgnoreCase(osType)) {
            return true;
        }

        return getOsType().equalsIgnoreCase(osType);
    }

    /**
     * 判断是否是windows操作系统
     *
     * @return 是否是windows系统
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * 判断是否是mac操作系统
     *
     * @return 是否是mac系统
     */
    public static boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    /**
     * 获取操作系统环境变量中代表path的名称
     *
     * @return path
     */
    public static String getEnvPathVariable() {
        return isWindows() ? "Path" : "PATH";
    }


    /**
     * 获取操作系统的路径分隔符
     *
     * @return separator
     */
    public static String getEnvPathSeparator() {
        return isWindows() ? ";" : ":";
    }

    /**
     * 获取CPU架构类型
     *
     * @param progress 记录日志
     * @return String
     */
    public static synchronized String getOsArch(Progress progress) {
        OsArch osArch = getArchFromJvm();
        if (osArch == OsArch.X64 && isMac()) {
            if (isRosetta == null) {
                isRosetta = isRosetta(progress);
            }
            if (isRosetta) {
                return OsArch.ARM64.value;
            }
        }

        if (OsArch.UNKNOWN.equals(osArch)) {
            return OsArch.X64.value;
        }
        return osArch.value;
    }

    private static OsArch getArchFromJvm() {
        String arch = System.getProperty("os.arch");
        if (StringUtil.isEmpty(arch)) {
            return OsArch.UNKNOWN;
        }

        if (OsArch.isIntel64(arch)) {
            return OsArch.X64;
        } else if (OsArch.isArm64(arch)) {
            return OsArch.ARM64;
        } else {
            return OsArch.UNKNOWN;
        }
    }

    /**
     * Rosetta 2预装在使用ARM芯片的Mac电脑上
     * 使用命令检查这个软件的进程oahd是否存在
     * pgrep -q oahd && echo Yes || echo No
     *
     * @param progress progress
     * @return 如果装了Rosetta就返回true
     */
    private static boolean isRosetta(Progress progress) {
        String resultYes = "Yes";
        String resultNo = "No";
        String command = String.format(Locale.ENGLISH, "pgrep -q oahd && echo %s || echo %s", resultYes, resultNo);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command).redirectErrorStream(true);
        Process process = null;
        String commandResult = "";
        try {
            process = processBuilder.start();
            try (InputStream is = process.getInputStream()) {
                commandResult = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException exception) {
            progress.warn(exception.getMessage());
            if (process != null) {
                process.destroyForcibly();
            }
        }

        return resultYes.equals(commandResult);
    }

    private enum OsArch {
        X64("x64"),
        ARM64("arm64"),
        UNKNOWN(""),
        AARCH64("aarch64"),
        X86_64("x86_64"),
        AMD64("amd64");

        final String value;

        OsArch(String value) {
            this.value = value;
        }

        /**
         * 判断是否CPU架构是否为arm64
         *
         * @param osArch CPU架构
         * @return 如果是Arm64架构就返回true，否则返回false
         */
        public static boolean isArm64(String osArch) {
            return AARCH64.value.equals(osArch) || ARM64.value.equals(osArch);
        }

        /**
         * 判断是否CPU架构是否为Intel64
         *
         * @param osArch CPU架构
         * @return 如果是Intel64架构就返回true，否则返回false
         */
        public static boolean isIntel64(String osArch) {
            return X86_64.value.equals(osArch) || AMD64.value.equals(osArch) || X64.value.equals(osArch);
        }
    }
}
