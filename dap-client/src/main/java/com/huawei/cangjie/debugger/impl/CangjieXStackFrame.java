/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.intellij.ex.DapXScope;
import com.huawei.bitfun.intellij.ex.DapXStackFrame;
import com.huawei.bitfun.intellij.ex.DapXValue;
import com.huawei.bitfun.intellij.treemodel.DapEvaluatedValueNode;
import com.huawei.bitfun.intellij.treemodel.DapScopeNode;
import com.huawei.bitfun.intellij.treemodel.DapStackFrameNode;
import com.huawei.bitfun.intellij.utils.FinishedEvaluation;
import com.huawei.bitfun.protocol.extend.EvaluateResponse;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.cangjie.debugger.ohos.impl.OhCangjieXValue;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;

import org.eclipse.lsp4j.debug.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Cangjie XStackFrame implementation
 *
 * @since 2022-10-19
 */
public class CangjieXStackFrame extends DapXStackFrame<CangjieXDebugProcess, DapStackFrameNode> {
    public CangjieXStackFrame(DapStackFrameNode dapStackFrameNode, CangjieXDebugProcess dapProcess) {
        super(dapStackFrameNode, dapProcess);
    }

    @Override
    @Nullable
    public String getDefaultUnfoldScope() {
        return "Locals";
    }

    @Override
    protected DapXValue<CangjieXDebugProcess, ?, ?> createXValue(String expression, EvaluateResponse evaluateResponse) {
        FinishedEvaluation evaluation = new FinishedEvaluation(expression, evaluateResponse);
        DapEvaluatedValueNode evaluateValueNode = new DapEvaluatedValueNode(evaluation, stackFrameNode);
        return new OhCangjieXValue(EitherPair.right(evaluateValueNode), dapProcess);
    }

    @Override
    protected DapXScope<?, ?> createXScope(Scope scope) {
        return new CangjieXScope(new DapScopeNode(scope, stackFrameNode), dapProcess);
    }

    @Override
    @Nullable
    public XDebuggerEvaluator getEvaluator() {
        return new CangjieDapXDebuggerEvaluator();
    }

    /**
     * Dap XDebuggerEvaluator
     */
    private class CangjieDapXDebuggerEvaluator extends DapXDebuggerEvaluator {
        @Override
        public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback,
            @Nullable XSourcePosition expressionPosition) {
            super.evaluate(expression, callback, expressionPosition);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
