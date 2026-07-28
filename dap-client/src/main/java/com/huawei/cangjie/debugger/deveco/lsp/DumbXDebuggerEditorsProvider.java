/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.deveco.lsp;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * used when LSP plugin is not installed
 *
 * @since 2022-11-21
 */
public class DumbXDebuggerEditorsProvider extends XDebuggerEditorsProvider {
    /**
     * instance
     */
    public static final DumbXDebuggerEditorsProvider INSTANCE = new DumbXDebuggerEditorsProvider();

    @Override
    @NotNull
    public FileType getFileType() {
        return DumbCangjieFileType.INSTANCE;
    }

    @Override
    @NotNull
    public Document createDocument(@NotNull Project project, @NotNull XExpression expression,
            @Nullable XSourcePosition sourcePosition, @NotNull EvaluationMode mode) {
        String text = expression.getExpression();
        VirtualFile virtualFile = new LightVirtualFile("temp.cj", text.trim());
        FileViewProvider fileViewProvider =
                PsiManagerEx.getInstanceEx(project).getFileManager().createFileViewProvider(virtualFile, true);
        return fileViewProvider.getDocument();
    }
}
