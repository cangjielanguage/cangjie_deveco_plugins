/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.dialog.optimization;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * The type Profdata file.
 *
 * @since 2026-05-13
 */
public class ProfdataFile extends LanguageFileType {
    /**
     * The constant INSTANCE.
     */
    public static final ProfdataFile INSTANCE = new ProfdataFile();

    private ProfdataFile() {
        super(Language.ANY);
    }

    @NotNull
    @Override
    public String getName() {
        return "Profdata File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Optimization profile data file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "profdata";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}