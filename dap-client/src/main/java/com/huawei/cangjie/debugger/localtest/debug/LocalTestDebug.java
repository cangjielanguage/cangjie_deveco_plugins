/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.localtest.debug;

import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.start.XDebugSessionStartUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.localtest.CangjieLaunchRequestArgs;
import com.huawei.cangjie.debugger.localtest.LocalTestParam;
import com.huawei.cangjie.debugger.utils.LogUtils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

import java.util.concurrent.CompletableFuture;

/**
 * local test debug
 *
 * @since 2025-9-2
 */
public abstract class LocalTestDebug {
    private static final Logger LOGGER = Logger.getInstance(LocalTestDebug.class);

    /**
     * create xdebug process
     *
     * @param project project
     * @param xDebugSession xdebug session
     * @param launchArgs    launch args
     * @param callback callback
     * @return DapXDebugProcess
     */
    protected abstract DapXDebugProcess<?, ?> createXDebugProcess(Project project,
        XDebugSession xDebugSession, Object launchArgs, CompletableFuture<Boolean> callback);

    /**
     * debug
     *
     * @param localTestParam local test param
     * @param callback callback
     */
    public void debug(LocalTestParam localTestParam, CompletableFuture<Boolean> callback) {
        Project project = localTestParam.getProject();
        if (project == null) {
            LogUtils.printCangjieLogInfo(LOGGER, "CangjieLocalTest: no project");
        }
        String basePath = project.getBasePath();
        if (basePath == null) {
            LogUtils.printCangjieLogInfo(LOGGER, "CangjieLocalTest: project base path is null");
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                closeExistingDebugSessions(project);
                CangjieLaunchRequestArgs launchParam = getLaunchParam(localTestParam);
                XDebugSession session = XDebugSessionStartUtils.createXDebugSessionAndInit(
                        localTestParam.getProject(),
                        xDebugSession -> createXDebugProcess(project, xDebugSession, launchParam, callback)
                );

                RunContentDescriptor descriptor = session.getRunContentDescriptor();
                if (descriptor.getAttachedContent() != null) {
                    descriptor.getAttachedContent().setDisplayName(DebugSessionConfig.DISPLAY_NAME);
                    descriptor.getAttachedContent().setIcon(DebugSessionConfig.ICON);
                }
            } catch (ExecutionException e) {
                LogUtils.printCangjieLogInfo(LOGGER, "CangjieLocalTest: debug start failed");
            }
        });
    }

    /**
     * close existing debug sessions
     *
     * @param project project
     */
    private void closeExistingDebugSessions(Project project) {
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession[] sessions = debuggerManager.getDebugSessions();

        for (XDebugSession session : sessions) {
            if (session.getDebugProcess() instanceof CangjieXDebugProcess) {
                session.stop();
            }
        }
    }

    /**
     * get launch param
     *
     * @param localTestParam local test param
     * @return LaunchRequestArgs
     */
    private CangjieLaunchRequestArgs getLaunchParam(LocalTestParam localTestParam) {
        return CangjieLaunchRequestArgs
                .builder()
                .name("(cjdb) Launch")
                .program(localTestParam.getProgram())
                .request("launch")
                .type("cangjieDebug")
                .externalConsole(false)
                .env(localTestParam.getEnvs())
                .stopAtEntry(false)
                .args(localTestParam.getArgs())
                .build();
    }
}
