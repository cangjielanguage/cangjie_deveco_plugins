/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.start;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.connect.DapConnectionLauncher;
import com.huawei.bitfun.request.handler.RunInTerminalHandler;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.CommonUtils;
import com.huawei.bitfun.utils.ConnectionMode;
import com.huawei.bitfun.utils.ThreadManager;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.protocol.CangjieIDebugProtocolServer;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.cangjie.debugger.utils.PtyUtils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;

import com.pty4j.PtyProcess;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.lsp4j.debug.RunInTerminalRequestArguments;
import org.eclipse.lsp4j.debug.RunInTerminalResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * utility class for DAP connection
 *
 * @since 2022-10-19
 */
public class CangjieDapConnectionUtils {
    private static final Logger LOGGER = Logger.getInstance(CangjieDapConnectionUtils.class);

    /**
     * create cangjie dap server connection launcher on dynamic port
     *
     * @param serverPath server path
     * @param dynamicLibPath lldb dynamic library location
     * @param otherDynamicLibPath other dynamic library location
     * @param cangjieSdkPath cangjie sdk location
     * @param ptyProcessFuture pty process future
     * @return launcher
     * @throws IOException e
     */
    public static DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService>
    createDapConnectionLauncher(String serverPath, String dynamicLibPath, String otherDynamicLibPath,
        String cangjieSdkPath, CompletableFuture<PtyProcess> ptyProcessFuture) throws IOException {
        DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService> launcher =
            new DapConnectionLauncher<>();
        if (dynamicLibPath == null || dynamicLibPath.isEmpty()) {
            throw CodeCheckByPassUtils.createRuntimeException("dynamicLibPath path is not available");
        }
        ConnectionMode mode;
        if (SystemUtils.IS_OS_WINDOWS) {
            mode = ConnectionMode.SOCKET;
            launcher.configureProcessBuilder(processBuilder -> {
                String oldPath = processBuilder.environment().get("Path");
                String newPath = dynamicLibPath + ";" + otherDynamicLibPath + ";" + oldPath;
                processBuilder.environment().put("Path", newPath);
                processBuilder.environment().put("CANGJIE_HOME", cangjieSdkPath);
            });
        } else {
            // was std in old code, but lose messages in new code sometimes, change to socket for now
            mode = ConnectionMode.SOCKET;
            launcher.configureProcessBuilder(processBuilder -> {
                String oldPath = processBuilder.environment().get("DYLD_LIBRARY_PATH");
                String newPath = dynamicLibPath + ":" + otherDynamicLibPath + ":" + oldPath;
                processBuilder.environment().put("DYLD_LIBRARY_PATH", newPath);
                processBuilder.environment().put("CANGJIE_HOME", cangjieSdkPath);
            });
        }
        launcher.connectionMode(mode);
        launcher.addServerExecArgs("--debuggertype=lldbapi");
        launcher.serverExecPath(serverPath);
        setDapServerLogSwitch(launcher);
        configureLauncherCommonLogic(launcher, ptyProcessFuture);
        switch (mode) {
            case SOCKET:
                int port = CommonUtils.getAvailableLocalPort();
                launcher.port(port);
                launcher.addServerExecArgs("--port=" + port);
                break;
            case STD:
                launcher.addServerExecArgs("--link=std");
                break;
            default:
                throw CodeCheckByPassUtils.createRuntimeException("wrong args");
        }
        LogUtils.printCangjieLogInfo(LOGGER, "DAP Server start command: " + launcher.getStartCommand());
        return launcher;
    }

    private static void setDapServerLogSwitch(
        DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService> launcher) {
        String enableLog = System.getProperty("dap.log.enable");
        if ("true".equals(enableLog)) {
            launcher.addServerExecArgs("--logpath=" + PathManager.getLogPath());
        } else {
            launcher.addServerExecArgs("--logEnable=0");
        }
    }

    private static void configureLauncherCommonLogic(
        DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService> launcher,
        CompletableFuture<PtyProcess> ptyProcessFuture) {
        ThreadManager threadManager = new ThreadManager();
        DapFromServerService dapFromServerService =
                new DapFromServerService(threadManager.createSingleThreadExecutor("event-callback-pool"));
        if (ptyProcessFuture != null) {
            dapFromServerService.setRunInTerminalHandler(getRunInTerminalHandler(ptyProcessFuture));
        }
        launcher.threadManager(threadManager)
            .toRemoteLsp4jInterfaceClass(CangjieIDebugProtocolServer.class)
                .fromRemoteService(dapFromServerService)
            .interfaceWrapperFunction(protocolServer -> new CangjieDapToServerService(protocolServer,
                threadManager.createCachedThreadExecutor("response-callback-pool")));
    }

    private static RunInTerminalHandler getRunInTerminalHandler(CompletableFuture<PtyProcess> ptyProcessFuture) {
        return new RunInTerminalHandler() {
            /**
             * runInTerminal request handler
             *
             * @param arguments runInTerminalRequestArguments
             * @param future future
             */
            public void handle(RunInTerminalRequestArguments arguments,
                               CompletableFuture<RunInTerminalResponse> future) {
                RunInTerminalResponse response = new RunInTerminalResponse();
                try {
                    PtyProcess process = PtyUtils.createHiddenPtyProcess();
                    ptyProcessFuture.complete(process);
                    int pid = Math.toIntExact(process.pid());
                    response.setShellProcessId(pid);
                } catch (IOException e) {
                    ptyProcessFuture.completeExceptionally(e);
                }
                future.complete(response);
            }
        };
    }
}
