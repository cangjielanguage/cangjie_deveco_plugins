/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console;

import com.huawei.bitfun.intellij.utils.IntellijThreadUtils;
import com.huawei.cangjie.debugger.console.editor.CangjieLldbLanguage;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * ConsolePrinter
 *
 * @since 2022-10-19
 */
public class ConsolePrinter {
    private volatile LanguageConsoleView myLldbConsoleView;

    private final String defaultPrompt;

    /**
     * create LanguageConsoleImpl
     *
     * @param project project
     * @param displayName displayName such as: gdb,lldb,lldbapi
     */
    public ConsolePrinter(@NotNull Project project, String displayName) {
        this.myLldbConsoleView = new LanguageConsoleImpl(project, CangjieLldbLanguage.NAME,
            CangjieLldbLanguage.INSTANCE);
        defaultPrompt = '(' + displayName + ')';
        init();
    }

    /**
     * Initialization parameters
     *
     */
    private void init() {
        // add consoleEditor registerLineExtensionPainter for Display completion information
        myLldbConsoleView.getConsoleEditor().registerLineExtensionPainter(
                painter -> CommandCompletion.INSTANCE.getLineExtensionInfos());
        // add document listener for user input
        LanguageConsoleView consoleView = myLldbConsoleView;
        consoleView.getConsoleEditor().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                DocumentListener.super.documentChanged(event);
                // through '\t' to completion command or show prompt command list on the editor
                CharSequence charSequence = event.getNewFragment();
                if (charSequence.length() == 1 && charSequence.charAt(0) == '\t') {
                    CommandCompletion.INSTANCE.doCompletion(myLldbConsoleView.getConsoleEditor());
                }
            }
        }, consoleView);
        myLldbConsoleView.setEditable(true);
        // set Title header on the left side of the view
        myLldbConsoleView.setPrompt(defaultPrompt);
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    public LanguageConsoleView getMyLldbConsoleView() {
        return myLldbConsoleView;
    }

    /**
     * print info in error format
     *
     * @param str string to be print
     */
    public void printErrorInfo(@NotNull String str) {
        IntellijThreadUtils.invokeLaterIfNeeded(() -> myLldbConsoleView.print(str + System.lineSeparator(),
            ConsoleViewContentType.LOG_ERROR_OUTPUT));
    }

    /**
     * print info in debug format
     *
     * @param str string to be print
     */
    public void printDebugInfo(@NotNull String str) {
        IntellijThreadUtils.invokeLaterIfNeeded(() -> myLldbConsoleView.print(str + System.lineSeparator(),
            ConsoleViewContentType.LOG_DEBUG_OUTPUT));
    }

    /**
     * print info in info format
     *
     * @param str string to be print
     */
    public void printLogInfo(@NotNull String str) {
        IntellijThreadUtils.invokeLaterIfNeeded(() -> myLldbConsoleView.print(str + System.lineSeparator(),
            ConsoleViewContentType.LOG_INFO_OUTPUT));
    }

    @Override
    public String toString() {
        return "ConsolePrinter{" + "myLldbConsoleView=" + myLldbConsoleView + '}';
    }
}
