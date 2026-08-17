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
import com.huawei.bitfun.protocol.extend.Variable;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.impl.CangjieXValue;
import com.huawei.cangjie.debugger.impl.treemodel.CangjieVariableValueNode;

import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.frame.XValueModifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * OH cangjie value
 *
 * @since 2022-12-5
 */
public class OhCangjieXValue extends CangjieXValue {
    public OhCangjieXValue(EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> nodePair,
        CangjieXDebugProcess dapProcess) {
        super(nodePair, dapProcess);
    }

    @Override
    protected DapXValue<CangjieXDebugProcess, CangjieVariableValueNode, DapEvaluatedValueNode>
        createXValue(Variable variable) {
        CangjieVariableValueNode node = new CangjieVariableValueNode(variable, getTreeNode());
        EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> pair = EitherPair.left(node);
        return new OhCangjieXValue(pair, dapProcess);
    }

    private static XValueModifier.XModificationCallback getXModificationCallback(
            @NotNull XValueModifier.XModificationCallback callback) {
        XValueModifier.XModificationCallback newCallback = new XValueModifier.XModificationCallback() {
            @Override
            public void valueModified() {
                callback.valueModified();
            }

            @Override
            public void errorOccurred(@NotNull String errorMessage) {
                callback.errorOccurred(errorMessage);
            }
        };
        return newCallback;
    }

    @Override
    public XValueModifier getModifier() {
        XValueModifier commonsModifier = super.getModifier();
        if (commonsModifier == null) {
            return CodeCheckByPassUtils.getNull();
        }
        return new XValueModifier() {
            @Override
            public void setValue(@NotNull XExpression expression, @NotNull XModificationCallback callback) {
                XModificationCallback newCallback = getXModificationCallback(callback);
                commonsModifier.setValue(expression, newCallback);
            }

            @Nullable
            @Override
            public String getInitialValueEditorText() {
                return commonsModifier.getInitialValueEditorText();
            }
        };
    }
}
