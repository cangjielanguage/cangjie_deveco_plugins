/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.schema;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * cangjie module build profile.
 *
 * @since 2025-03-14
 */
public class CjBuildProfileProvider {
    /**
     * get the path of cangjie schema file.
     *
     * @param cangjieSchemaPath the schema file name
     * @return the path of schema file.
     */
    @Nullable
    protected static VirtualFile getSchemaVirtualFile(@NotNull Path cangjieSchemaPath) {
        if (Files.exists(cangjieSchemaPath)) {
            return ReadAction.compute(() -> LocalFileSystem.getInstance().findFileByNioFile(cangjieSchemaPath));
        }
        return null;
    }
}
