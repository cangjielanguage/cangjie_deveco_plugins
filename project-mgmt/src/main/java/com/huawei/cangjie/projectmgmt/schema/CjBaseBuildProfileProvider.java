/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.schema;

import static com.huawei.cangjie.projectmgmt.utils.Constants.CJ_PLUGIN_CACHE_DIR;

import com.huawei.cangjie.projectmgmt.utils.CangjieEnvUtils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 * ProjectBuildProfileProvider
 *
 * @since 2024-03-17
 */
public class CjBaseBuildProfileProvider {
    private static final Logger LOGGER = Logger.getInstance(CjBaseBuildProfileProvider.class);

    /**
     * Gets cangjie schema file.
     *
     * @param schemaFileName the schema file name
     * @param defaultSchemaSupplier the default schema supplier
     * @return the cangjie schema file
     */
    public static VirtualFile getCangjieSchemaFile(String schemaFileName, Supplier<VirtualFile> defaultSchemaSupplier) {
        if (!CangjieEnvUtils.isProjectChinaCountryCode()) {
            return defaultSchemaSupplier.get();
        }
        Path cangjieSchemaPath = Paths.get(PathManager.getConfigPath(), CJ_PLUGIN_CACHE_DIR, schemaFileName);
        VirtualFile cangjieVirtualFile = CjBuildProfileProvider.getSchemaVirtualFile(cangjieSchemaPath);
        VirtualFile virtualFile = defaultSchemaSupplier.get();
        if (cangjieVirtualFile == null && virtualFile != null) {
            copyFileToDirectory(virtualFile, cangjieSchemaPath);
            cangjieVirtualFile = CjBuildProfileProvider.getSchemaVirtualFile(cangjieSchemaPath);
        }
        return cangjieVirtualFile != null ? cangjieVirtualFile : virtualFile;
    }

    private static void copyFileToDirectory(VirtualFile virtualFile, Path targetPath) {
        if (virtualFile == null || !virtualFile.exists() || virtualFile.isDirectory()) {
            return;
        }
        try {
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath.getParent());
            }
            if (Files.exists(targetPath)) {
                return;
            }
            try (InputStream inputStream = virtualFile.getInputStream()) {
                Files.copy(inputStream, targetPath);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to copy file: " + virtualFile.getName() + ", reason: " + e.getMessage());
        }
    }
}
