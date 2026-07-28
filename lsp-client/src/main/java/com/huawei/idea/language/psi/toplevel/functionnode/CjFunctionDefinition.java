/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.language.psi.toplevel.functionnode;

import com.huawei.idea.highlightersetting.CangjieSemanticTokenHighlighter;
import com.huawei.idea.language.CangjieIcons;
import com.huawei.idea.language.psi.CangjieBaseNode;
import com.huawei.idea.language.psi.CangjieGetID;
import com.huawei.idea.language.utils.CjFunctionInfoHandlers;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.contributors.semantic.SemanticToken;

import javax.swing.Icon;

/**
 * CjFunctionDefinition
 *
 * @since 2020-08-09
 */
public class CjFunctionDefinition extends CangjieBaseNode implements CangjieGetID {
    private transient FunctionDefinitionInfo functionDefinitionInfo = null;

    public CjFunctionDefinition(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * get FunctionDefinition info
     *
     * @return info
     */
    public FunctionDefinitionInfo getFunctionDefinitionInfo() {
        initFunctionInfo();
        return functionDefinitionInfo;
    }

    private void initFunctionInfo() {
        functionDefinitionInfo = new FunctionDefinitionInfo();
        new CjFunctionInfoHandlers(functionDefinitionInfo).processAllElement(this.getChildren());
    }

    @Override
    @Nullable
    public Icon getIcon(int flags) {
        return CangjieIcons.CANGJIE_FUNCTION;
    }

    @Override
    public String getName() {
        return getFunctionDefinitionInfo().getName();
    }

    @Override
    @Nullable
    public TextAttributesKey getColor() {
        return CangjieSemanticTokenHighlighter.colorOf(SemanticToken.FUNCTION);
    }
}