/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.protocol;

import com.huawei.bitfun.DapToServerService;
import com.huawei.bitfun.request.requester.CompletableFutureRequester;
import com.huawei.bitfun.request.requester.Requester;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandArgs;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Cangjie remote interface wrapper
 *
 * @since 2022-10-19
 */
public class CangjieDapToServerService extends DapToServerService<CangjieIDebugProtocolServer> {
    private final Requester<ExecuteDebuggerCommandArgs, ExecuteDebuggerCommandResponse> executeDebuggerCommandRequester;

    /**
     * constructor
     *
     * @param remoteServer server
     * @param asyncCallbackExecutor thread pool
     */
    public CangjieDapToServerService(CangjieIDebugProtocolServer remoteServer, ExecutorService asyncCallbackExecutor) {
        super(remoteServer, asyncCallbackExecutor);
        executeDebuggerCommandRequester =
            new CompletableFutureRequester<>("ExecuteDebuggerCommand", asyncCallbackExecutor) {
            @Override
            protected CompletableFuture<ExecuteDebuggerCommandResponse> getFuture(
                ExecuteDebuggerCommandArgs executeDebuggerCommandArgs) {
                return createFutureInner(remoteServer.debugInConsole(executeDebuggerCommandArgs), this.name);
            }
        };
    }

    public Requester<ExecuteDebuggerCommandArgs, ExecuteDebuggerCommandResponse> getExecuteDebuggerCommandRequester() {
        return executeDebuggerCommandRequester;
    }
}
