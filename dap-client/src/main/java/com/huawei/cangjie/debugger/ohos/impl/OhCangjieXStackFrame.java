/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.intellij.ex.DapXScope;
import com.huawei.bitfun.intellij.ex.DapXValue;
import com.huawei.bitfun.intellij.treemodel.DapEvaluatedValueNode;
import com.huawei.bitfun.intellij.treemodel.DapScopeNode;
import com.huawei.bitfun.intellij.treemodel.DapStackFrameNode;
import com.huawei.bitfun.intellij.utils.FinishedEvaluation;
import com.huawei.bitfun.protocol.extend.EvaluateResponse;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.impl.CangjieXStackFrame;

import org.eclipse.lsp4j.debug.Scope;

/**
 * OH cangjie stack frame implementation
 *
 * @since 2022-12-5
 */
public class OhCangjieXStackFrame extends CangjieXStackFrame {
    public OhCangjieXStackFrame(DapStackFrameNode stackFrameNode, CangjieXDebugProcess dapProcess) {
        super(stackFrameNode, dapProcess);
    }

    @Override
    protected DapXScope<?, ?> createXScope(Scope scope) {
        return new OhCangjieXScope(new DapScopeNode(scope, stackFrameNode), dapProcess);
    }

    @Override
    protected DapXValue<CangjieXDebugProcess, ?, ?> createXValue(String expression, EvaluateResponse response) {
        FinishedEvaluation evaluation = new FinishedEvaluation(expression, response);
        DapEvaluatedValueNode evaluateValueNode = new DapEvaluatedValueNode(evaluation, stackFrameNode);
        return new OhCangjieXValue(EitherPair.right(evaluateValueNode), dapProcess);
    }
}
