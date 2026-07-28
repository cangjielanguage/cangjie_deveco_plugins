/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * dummy console view
 *
 * @since 2026-02-26
 */
public final class DummyConsoleView implements ConsoleView {
    @Override
    public void print(@NotNull String s, @NotNull ConsoleViewContentType consoleViewContentType) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void scrollTo(int i) {

    }

    @Override
    public void attachToProcess(@NotNull ProcessHandler processHandler) {

    }

    @Override
    public void setOutputPaused(boolean b) {

    }

    @Override
    public boolean isOutputPaused() {
        return false;
    }

    @Override
    public boolean hasDeferredOutput() {
        return false;
    }

    @Override
    public void performWhenNoDeferredOutput(@NotNull Runnable runnable) {

    }

    @Override
    public void setHelpId(@NotNull String s) {

    }

    @Override
    public void addMessageFilter(@NotNull Filter filter) {

    }

    @Override
    public void printHyperlink(@NotNull String s, @Nullable HyperlinkInfo hyperlinkInfo) {

    }

    @Override
    public int getContentSize() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public AnAction @NotNull [] createConsoleActions() {
        return new AnAction[0];
    }

    @Override
    public void allowHeavyFilters() {

    }

    @Override
    public @NotNull
    JComponent getComponent() {
        return null;
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return null;
    }

    @Override
    public void dispose() {

    }
}