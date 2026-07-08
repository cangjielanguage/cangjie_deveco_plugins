/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.language.psi.toplevel.macronode;

import com.huawei.idea.highlightersetting.CangjieSemanticTokenHighlighter;
import com.huawei.idea.language.CangjieIcons;
import com.huawei.idea.language.psi.CangjieBaseNode;
import com.huawei.idea.language.psi.CangjieGetID;
import com.huawei.idea.language.psi.CangjieNamedElement;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.contributors.semantic.SemanticToken;

import javax.swing.Icon;

/**
 * TypeAlias
 *
 * @since 2022-02-15
 */
public class CjMacroInputType extends CangjieNamedElement implements CangjieGetID {
    public CjMacroInputType(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public Icon getIcon(int flags) {
        return CangjieIcons.CANGJIE_TYPEALIAS;
    }

    @Override
    public TextAttributesKey getColor() {
        return CangjieSemanticTokenHighlighter.colorOf(SemanticToken.CLASS);
    }
}
