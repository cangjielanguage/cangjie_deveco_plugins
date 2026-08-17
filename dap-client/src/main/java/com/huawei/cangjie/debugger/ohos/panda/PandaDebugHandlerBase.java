/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.panda;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.deveco.ohos.debugcommon.socket.forward.ArkTsPortForwardOperation;
import com.huawei.deveco.panda.message.DevToolsServiceWrapper;
import com.huawei.deveco.panda.message.DevtoolsEventSubject;
import com.huawei.deveco.panda.websocket.protocol.events.console.MessageAdded;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.BreakpointResolved;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.Paused;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.PausedReason;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.Resumed;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.ScriptFailedToParse;
import com.huawei.deveco.panda.websocket.protocol.events.debugger.ScriptParsed;
import com.huawei.deveco.panda.websocket.protocol.types.runtime.ConsoleAPICalled;
import com.huawei.deveco.panda.websocket.protocol.types.runtime.ExecutionContextDestroyed;
import com.huawei.deveco.panda.websocket.services.exceptions.WebSocketServiceException;

import org.jetbrains.annotations.NotNull;

/**
 * When the application is started in debug mode(-D), the program continues to run after C++ debugging is complete.
 * connect to js debug server and continue
 *
 * @since 2022-12-24
 */
public abstract class PandaDebugHandlerBase {
    /**
     * panda debug name
     */
    protected static final String PANDA_DEBUGGER_NAME = "PandaDebugger";

    private static final String WEB_SOCKET_NAME = "ws://localhost:";

    /**
     * hdc fport
     */
    protected int pandaDebuggerServerPort;

    private DevToolsServiceWrapper devToolsServiceWrapper;

    /**
     * create websocket and connect to panda server
     */
    public void createSocketAndStartListener() {
        try {
            // create js devtools websocket to connect to panda server
            devToolsServiceWrapper = new DevToolsServiceWrapper(WEB_SOCKET_NAME + pandaDebuggerServerPort);
            devToolsServiceWrapper.setService(getArkTsPortForwardOperation());
            devToolsServiceWrapper.getRuntime().enable();
            devToolsServiceWrapper.getDebugger().enable();
        } catch (WebSocketServiceException e) {
            // websocket连接失败，不中断调试流程
            return;
        }
        devToolsServiceWrapper.getRuntime().runIfWaitingForDebugger();
        devToolsServiceWrapper.listenToDevtoolsEvent(new JsEventHandler());
    }

    /**
     * disconnect panda server
     */
    public void dispose() {
        if (devToolsServiceWrapper != null) {
            devToolsServiceWrapper.dispose();
        }
    }

    /**
     * ArkTsPortForwardOperation
     *
     * @return ArkTsPortForwardOperation
     */
    protected ArkTsPortForwardOperation getArkTsPortForwardOperation() {
        return CodeCheckByPassUtils.getNull();
    }

    private class JsEventHandler implements DevtoolsEventSubject {
        @Override
        public void onPaused(@NotNull Paused paused) {
            if (paused.getReason() == PausedReason.BREAK_ON_START) {
                // resume when pause type is BREAK_ON_START
                devToolsServiceWrapper.getDebugger().resume();
            }
        }

        @Override
        public void onBreakpointResolved(@NotNull BreakpointResolved breakpointResolved) {

        }

        @Override
        public void onConsoleAPICalled(@NotNull ConsoleAPICalled consoleAPICalled) {

        }

        @Override
        public void onExecutionContextDestroyed(@NotNull ExecutionContextDestroyed executionContextDestroyed) {

        }

        @Override
        public void onMessageAdded(@NotNull MessageAdded messageAdded) {

        }

        @Override
        public void onResumed(@NotNull Resumed resumed) {

        }

        @Override
        public void onScriptFailedToParse(@NotNull ScriptFailedToParse scriptFailedToParse) {

        }

        @Override
        public void onScriptParsed(@NotNull ScriptParsed scriptParsed) {

        }
    }
}
