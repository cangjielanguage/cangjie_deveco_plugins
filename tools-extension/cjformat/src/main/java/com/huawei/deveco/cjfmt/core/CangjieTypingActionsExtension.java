/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.core;

import static com.huawei.deveco.cjfmt.utils.FormatUtils.checkFile;

import com.intellij.codeInsight.editorActions.TypingActionsExtension;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * CangjieTypingActionsExtension
 *
 * @since 2024-06-12
 */
public class CangjieTypingActionsExtension implements TypingActionsExtension {
    @Override
    public boolean isSuitableContext(@NotNull Project project, @NotNull Editor editor) {
        Document document = editor.getDocument();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        PsiFile file = documentManager.getPsiFile(document);
        return file != null && checkFile(file.getVirtualFile());
    }
}
