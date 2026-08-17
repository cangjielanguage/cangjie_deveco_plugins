/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console.editor;

import com.intellij.lang.Language;

import org.jetbrains.annotations.NotNull;

/**
 * lldb language
 *
 * @since 2022-10-19
 */
public class CangjieLldbLanguage extends Language {
    /**
     * INSTANCE LLDBLanguage
     */
    public static final CangjieLldbLanguage INSTANCE = new CangjieLldbLanguage();

    /**
     * LLDB
     */
    public static final String NAME = "LLDB_CANGJIE";

    private static final long serialVersionUID = -582395164129129633L;

    public CangjieLldbLanguage() {
        super(NAME);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return NAME;
    }
}
