/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.protocol;

import com.huawei.bitfun.protocol.GenericIDebugProtocolServer;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandArgs;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandResponse;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Cangjie to server remote interface
 *
 * @since 2022-10-19
 */
public interface CangjieIDebugProtocolServer extends GenericIDebugProtocolServer {
    /**
     * debug In Console request
     *
     * @param args debugInConsole arguments
     * @return output by lldb/gdb
     */
    @JsonRequest
    default CompletableFuture<ExecuteDebuggerCommandResponse> debugInConsole(ExecuteDebuggerCommandArgs args) {
        throw new UnsupportedOperationException();
    }
}
