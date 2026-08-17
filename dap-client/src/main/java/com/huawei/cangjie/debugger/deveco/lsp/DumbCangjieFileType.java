/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.deveco.lsp;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * used when lsp is not installed
 *
 * @since 2022-11-21
 */
public class DumbCangjieFileType implements FileType {
    /**
     * instance
     */
    public static final DumbCangjieFileType INSTANCE = new DumbCangjieFileType();

    @Override
    @NonNls
    @NotNull
    public String getName() {
        return "DUMB_CANGJIE_FILE";
    }

    @Override
    @NlsContexts.Label
    @NotNull
    public String getDescription() {
        return "DUMB_CANGJIE_FILE";
    }

    @Override
    @NlsSafe
    @NotNull
    public String getDefaultExtension() {
        return "cj";
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
