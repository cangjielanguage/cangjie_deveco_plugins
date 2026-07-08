/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.intellij.ex.DapXExecutionStack;
import com.huawei.bitfun.intellij.ex.DapXSuspendContext;
import com.huawei.bitfun.intellij.treemodel.DapExecutionStackNode;
import com.huawei.bitfun.intellij.treemodel.DapSuspendContextNode;
import com.huawei.bitfun.protocol.extend.DapThreadsResponse;
import com.huawei.bitfun.protocol.extend.StoppedEventArguments;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import java.util.Arrays;
import java.util.Collections;

/**
 * Cangjie suspend context
 *
 * @since 2022-10-19
 */
public class CangjieXSuspendContext extends DapXSuspendContext<CangjieXDebugProcess, DapSuspendContextNode> {
    public CangjieXSuspendContext(StoppedEventArguments stoppedEvent, CangjieXDebugProcess dapProcess) {
        super(new DapSuspendContextNode(stoppedEvent), dapProcess);
    }

    @Override
    protected DapXExecutionStack<CangjieXDebugProcess, ?> createExecutionStack(DapThreadsResponse.Thread thread) {
        return new CangjieXExecutionStack(new DapExecutionStackNode(thread, suspendContextNode), dapProcess);
    }

    @Override
    public void computeExecutionStacks(XSuspendContext.XExecutionStackContainer container) {
        boolean active = this.dapProcess.getTimeTravelProcess().isOpenTimeTravel()
                || this.equals(this.dapProcess.getLatestSuspendContext());
        XExecutionStack[] stacks = this.getExecutionStacks();
        if (active && stacks.length == 2) {
            container.addExecutionStack(Arrays.asList(stacks), false);
            container.addExecutionStack(Collections.emptyList(), true);
            return;
        }
        super.computeExecutionStacks(container);
    }
}
