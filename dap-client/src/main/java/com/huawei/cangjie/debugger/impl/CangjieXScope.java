/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.intellij.ex.DapXScope;
import com.huawei.bitfun.intellij.ex.DapXValue;
import com.huawei.bitfun.intellij.treemodel.DapEvaluatedValueNode;
import com.huawei.bitfun.intellij.treemodel.DapScopeNode;
import com.huawei.bitfun.protocol.extend.Variable;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.cangjie.debugger.impl.treemodel.CangjieVariableValueNode;

/**
 * Cangjie Scope
 *
 * @since 2022-12-15
 */
public class CangjieXScope extends DapXScope<CangjieXDebugProcess, DapScopeNode> {
    /**
     * constructor
     *
     * @param dapScopeNode scope
     * @param dapProcess dapProcess
     */
    public CangjieXScope(DapScopeNode dapScopeNode, CangjieXDebugProcess dapProcess) {
        super(dapScopeNode, dapProcess);
    }

    @Override
    public DapXValue<CangjieXDebugProcess, ?, ?> createXValue(Variable variable) {
        CangjieVariableValueNode node = new CangjieVariableValueNode(variable, scopeNode);
        EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> either = EitherPair.left(node);
        return new CangjieXValue(either, dapProcess);
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
        return "CangjieXScope{}";
    }
}
