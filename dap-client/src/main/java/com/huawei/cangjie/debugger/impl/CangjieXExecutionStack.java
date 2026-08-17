/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.intellij.ex.DapXExecutionStack;
import com.huawei.bitfun.intellij.ex.DapXStackFrame;
import com.huawei.bitfun.intellij.treemodel.DapExecutionStackNode;
import com.huawei.bitfun.intellij.treemodel.DapStackFrameNode;
import com.huawei.bitfun.protocol.extend.StackFrame;

/**
 * Cangjie executionStack
 *
 * @since 2022-12-15
 */
public class CangjieXExecutionStack extends DapXExecutionStack<CangjieXDebugProcess, DapExecutionStackNode> {
    /**
     * constructor
     *
     * @param dapExecutionStackNode node
     * @param dapProcess dapProcess
     */
    public CangjieXExecutionStack(DapExecutionStackNode dapExecutionStackNode, CangjieXDebugProcess dapProcess) {
        super(dapExecutionStackNode, dapProcess);
    }

    @Override
    public DapXStackFrame<?, ?> createXStackFrame(StackFrame stackFrame) {
        stackFrame.setLanguage("Cangjie");
        return new CangjieXStackFrame(new DapStackFrameNode(stackFrame, threadNode), dapProcess);
    }
}
