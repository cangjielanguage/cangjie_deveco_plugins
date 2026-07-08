/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * env path builder
 *
 * @since 2025-09-02
 */
public class EnvPathBuilder {
    /**
     * build env path
     *
     * @param existingEnv old env
     * @param paths       new paths
     * @return envPath
     */
    public static String buildEnvPath(String existingEnv, Path... paths) {
        List<String> pathList = Arrays.stream(paths)
                .filter(Objects::nonNull)
                .map(Path::toString)
                .filter(path -> !path.trim().isEmpty())
                .collect(Collectors.toList());

        return buildEnvPath(existingEnv, pathList);
    }

    /**
     * build env path
     *
     * @param existingEnv old env
     * @param paths       new paths
     * @return env paths
     */
    public static String buildEnvPath(String existingEnv, List<String> paths) {
        List<String> allPaths = new ArrayList<>(paths);

        if (existingEnv != null && !existingEnv.trim().isEmpty()) {
            allPaths.add(existingEnv);
        }

        return allPaths.isEmpty() ? "" : String.join(File.pathSeparator, allPaths);
    }

    /**
     * get Mac runtime path suffix
     *
     * @return runtime suffix
     */
    public static String getMacRuntimePathSuffix() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm")) {
            return "darwin_aarch64_cjnative";
        } else if (arch.contains("x86") || arch.contains("amd64")) {
            return "darwin_x86_64_cjnative";
        } else {
            return "unknown_arch_cjnative";
        }
    }
}
