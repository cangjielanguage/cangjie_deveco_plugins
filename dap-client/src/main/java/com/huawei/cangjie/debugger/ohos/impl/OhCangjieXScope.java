/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.intellij.ex.DapXValue;
import com.huawei.bitfun.intellij.treemodel.DapEvaluatedValueNode;
import com.huawei.bitfun.intellij.treemodel.DapScopeNode;
import com.huawei.bitfun.protocol.extend.Variable;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.impl.CangjieXScope;
import com.huawei.cangjie.debugger.impl.treemodel.CangjieVariableValueNode;

/**
 * OH cangjie scope implementation
 *
 * @since 2022-12-5
 */
public class OhCangjieXScope extends CangjieXScope {
    /**
     * constructor
     *
     * @param scopeNode scope
     * @param dapProcess dapProcess
     */
    public OhCangjieXScope(DapScopeNode scopeNode, CangjieXDebugProcess dapProcess) {
        super(scopeNode, dapProcess);
    }

    @Override
    public DapXValue<CangjieXDebugProcess, ?, ?> createXValue(Variable variable) {
        CangjieVariableValueNode node = new CangjieVariableValueNode(variable, scopeNode);
        EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> either = EitherPair.left(node);
        return new OhCangjieXValue(either, dapProcess);
    }
}
