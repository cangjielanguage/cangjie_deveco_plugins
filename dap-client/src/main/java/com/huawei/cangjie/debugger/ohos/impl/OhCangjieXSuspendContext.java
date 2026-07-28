/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.intellij.ex.DapXExecutionStack;
import com.huawei.bitfun.intellij.treemodel.DapExecutionStackNode;
import com.huawei.bitfun.protocol.extend.DapThreadsResponse;
import com.huawei.bitfun.protocol.extend.StoppedEventArguments;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.impl.CangjieXSuspendContext;

/**
 * OH cangjie suspend context implementation
 *
 * @since 2022-12-5
 */
public class OhCangjieXSuspendContext extends CangjieXSuspendContext {
    public OhCangjieXSuspendContext(StoppedEventArguments stoppedEvent, CangjieXDebugProcess dapProcess) {
        super(stoppedEvent, dapProcess);
    }

    @Override
    protected DapXExecutionStack<CangjieXDebugProcess, ?> createExecutionStack(DapThreadsResponse.Thread thread) {
        return new OhCangjieXExecutionStack(new DapExecutionStackNode(thread, suspendContextNode), dapProcess);
    }
}
