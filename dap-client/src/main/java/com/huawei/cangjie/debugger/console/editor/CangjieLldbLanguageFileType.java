/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console.editor;

import com.intellij.openapi.fileTypes.LanguageFileType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * lldb file type
 *
 * @since 2022-10-19
 */
public class CangjieLldbLanguageFileType extends LanguageFileType {
    /**
     * INSTANCE LldbFileType
     */
    public static final CangjieLldbLanguageFileType INSTANCE = new CangjieLldbLanguageFileType();

    private static final String LLDB_FILE_TYPE_COMMANDS = "CangjieLldbApiFileTypeCommands";

    private static final String LLDB_FILE_TYPE_EXTENSION = "CangjieLldbApiFileTypeExtension";

    public CangjieLldbLanguageFileType() {
        super(CangjieLldbLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return LLDB_FILE_TYPE_COMMANDS;
    }

    @NotNull
    @Override
    public String getDescription() {
        return LLDB_FILE_TYPE_COMMANDS;
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return LLDB_FILE_TYPE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}
