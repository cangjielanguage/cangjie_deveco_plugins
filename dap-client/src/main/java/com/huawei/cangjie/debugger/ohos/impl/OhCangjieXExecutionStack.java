/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.intellij.ex.DapXStackFrame;
import com.huawei.bitfun.intellij.treemodel.DapExecutionStackNode;
import com.huawei.bitfun.intellij.treemodel.DapStackFrameNode;
import com.huawei.bitfun.protocol.extend.StackFrame;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.impl.CangjieXExecutionStack;

/**
 * OH cangjie execution stack implementation
 *
 * @since 2022-12-5
 */
public class OhCangjieXExecutionStack extends CangjieXExecutionStack {
    /**
     * constructor
     *
     * @param threadNode node
     * @param dapProcess dapProcess
     */
    public OhCangjieXExecutionStack(DapExecutionStackNode threadNode, CangjieXDebugProcess dapProcess) {
        super(threadNode, dapProcess);
    }

    @Override
    public DapXStackFrame<?, ?> createXStackFrame(StackFrame stackFrame) {
        stackFrame.setLanguage("Cangjie");
        return new OhCangjieXStackFrame(new DapStackFrameNode(stackFrame, threadNode), dapProcess);
    }
}
