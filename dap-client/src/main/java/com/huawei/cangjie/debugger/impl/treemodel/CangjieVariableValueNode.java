/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl.treemodel;

import com.huawei.bitfun.intellij.treemodel.DapTreeNode;
import com.huawei.bitfun.intellij.treemodel.DapVariableValueNode;
import com.huawei.bitfun.protocol.extend.ExtraFieldParseException;
import com.huawei.bitfun.protocol.extend.Variable;

import org.eclipse.lsp4j.debug.Source;

/**
 * cangjie variable node
 *
 * @since 2024-11-18
 */
public class CangjieVariableValueNode extends DapVariableValueNode {
    private int declaredLine;

    private Source declaredSource;

    /**
     * constructor
     *
     * @param variable variable
     * @param parentNode parentNode
     */
    public CangjieVariableValueNode(Variable variable, DapTreeNode<?, ?> parentNode) {
        super(variable, parentNode);
        try {
            declaredLine = variable.getInt("declaredLine");
        } catch (ExtraFieldParseException e) {
            declaredLine = -1;
        }
        try {
            declaredSource = variable.getType("declaredSource", Source.class);
        } catch (ExtraFieldParseException e) {
            declaredSource = null;
        }
    }

    public int getDeclaredLine() {
        return declaredLine;
    }

    public Source getDeclaredSource() {
        return declaredSource;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
