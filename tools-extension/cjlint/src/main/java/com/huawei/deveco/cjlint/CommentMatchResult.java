/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint;

import com.intellij.psi.PsiElement;

import lombok.Data;

import org.jetbrains.annotations.Nullable;

/**
 * comment match result
 *
 * @since 2024-04-02
 */
@Data
public class CommentMatchResult {
    private boolean isMatched;

    @Nullable
    private PsiElement ignorePsiElement;

    private String commentText;
}
