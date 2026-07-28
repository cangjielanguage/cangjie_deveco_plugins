/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.localtest.debug;

import static org.apache.commons.lang3.SystemUtils.USER_HOME_KEY;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.connect.DapConnectionLauncher;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.start.DebugStartType;
import com.huawei.bitfun.intellij.utils.IntellijThreadUtils;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.CommonUtils;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.localtest.CangjieLaunchRequestArgs;
import com.huawei.cangjie.debugger.ohos.OhFilePathUtils;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.protocol.CangjieIDebugProtocolServer;
import com.huawei.cangjie.debugger.start.CangjieDapConnectionUtils;
import com.huawei.cangjie.debugger.trace.CangjieDebugTracer;
import com.huawei.cangjie.debugger.utils.EnvPathBuilder;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.cangjie.sdkconfig.support.SdkConfig;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;

import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.intellij.xdebugger.impl.ui.XDebugSessionTab;

import com.pty4j.PtyProcess;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * cangjie local test debug
 *
 * @since 2025-9-2
 */
public class CangjieLocalTestDebug extends LocalTestDebug {
    private static final Logger LOGGER = Logger.getInstance(CangjieLocalTestDebug.class);

    private static void handleSessionStopped(CompletableFuture<PtyProcess> ptyProcessFuture,
        CompletableFuture<Boolean> callback, XDebugSession xDebugSession, Project project) {
        if (ptyProcessFuture.isDone()) {
            ptyProcessFuture.thenAccept(Process::destroyForcibly);
        }
        callback.complete(true);
        if (xDebugSession instanceof XDebugSessionImpl debugSession) {
            XDebugSessionTab sessionTab = debugSession.getSessionTab();
            if (sessionTab == null) {
                return;
            }
            RunContentDescriptor runContentDescriptor = sessionTab.getRunContentDescriptor();
            if (runContentDescriptor == null) {
                return;
            }
            IntellijThreadUtils.invokeAndWait(() -> {
                RunContentManager.getInstance(project).removeRunContent(
                        DefaultDebugExecutor.getDebugExecutorInstance(),
                        runContentDescriptor);
            });
        }
    }

    /**
     * create xdebug process
     *
     * @param project project
     * @param xDebugSession xdebug session
     * @param launchArgs    launch args
     * @param callback callback
     * @return DapXDebugProcess
     */
    @Override
    protected DapXDebugProcess<?, ?> createXDebugProcess(Project project, XDebugSession xDebugSession,
                                                         Object launchArgs, CompletableFuture<Boolean> callback) {
        CangjieLaunchRequestArgs params = CommonUtils.convertObjToType(launchArgs, CangjieLaunchRequestArgs.class);
        DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService>
                connectionLauncher;
        String cangjieHomePath = getCangjieHomePath();
        CompletableFuture<PtyProcess> ptyProcessFuture = new CompletableFuture<>();
        try {
            String serverPath = OhFilePathUtils.getServerInstallPath();
            connectionLauncher = CangjieDapConnectionUtils.createDapConnectionLauncher(serverPath,
                    getDynamicLibPath(), getOtherDynamicLibPath(), cangjieHomePath, ptyProcessFuture);
        } catch (IOException e) {
            throw CodeCheckByPassUtils.createRuntimeException(ExceptionUtils.getNonNullMsg(e));
        }
        DebugStartType startType;
        if ("launch".equals(params.getRequest())) {
            startType = DebugStartType.LAUNCH;
        } else {
            startType = DebugStartType.ATTACH;
        }
        handleEnv(params.getEnv(), cangjieHomePath);
        xDebugSession.addSessionListener(new XDebugSessionListener() {
            @Override
            public void sessionStopped() {
                handleSessionStopped(ptyProcessFuture, callback, xDebugSession, project);
            }
        });
        CangjieXDebugProcess cangjieXDebugProcess = new CangjieXDebugProcess(xDebugSession, params,
                startType, connectionLauncher, new CangjieDebugTracer());
        cangjieXDebugProcess.setDebugStartType(DebugStartType.LAUNCH);
        return cangjieXDebugProcess;
    }

    /**
     * handle env, append env path
     *
     * @param env env
     * @param cangjieHomePath cangjie home path
     */
    private void handleEnv(Map<String, String> env, String cangjieHomePath) {
        env.put("CANGJIE_HOME", cangjieHomePath);

        Path binPath = Path.of(cangjieHomePath, "bin");
        Path toolsBinPath = Path.of(cangjieHomePath, "tools", "bin");
        Path toolsLibPath = Path.of(cangjieHomePath, "tools", "lib");
        Path cjpmBinPath = Path.of(System.getProperty(USER_HOME_KEY), ".cjpm", "bin");

        if (SystemUtils.IS_OS_WINDOWS) {
            Path runtimePath = Path.of(cangjieHomePath, "runtime", "lib", "windows_x86_64_cjnative");
            String newPath = EnvPathBuilder.buildEnvPath(env.get("Path"),
                    runtimePath, binPath, toolsBinPath, toolsLibPath, cjpmBinPath);
            env.put("Path", newPath);
        } else {
            String newPath = EnvPathBuilder.buildEnvPath(env.get("PATH"),
                    binPath, toolsBinPath, cjpmBinPath);
            env.put("PATH", newPath);

            String runtimeSuffix = EnvPathBuilder.getMacRuntimePathSuffix();
            Path runtimePath = Path.of(cangjieHomePath, "runtime", "lib", runtimeSuffix);
            String newDyldPath = EnvPathBuilder.buildEnvPath(env.get("DYLD_LIBRARY_PATH"),
                    runtimePath, toolsLibPath);
            env.put("DYLD_LIBRARY_PATH", newDyldPath);
        }
    }

    /**
     * return sdkPath
     *
     * @return java.lang.String sdkPath
     */
    @Nullable
    private String getDynamicLibPath() {
        try {
            SdkConfig config = getSdkConfig();
            File dynamicLibFile;
            if (SystemUtils.IS_OS_WINDOWS) {
                dynamicLibFile = Path.of(config.getBuildToolsBinPath()).toFile();
            } else {
                dynamicLibFile = Path.of(config.getBuildToolsThirdPartyLibPath()).toFile();
            }
            if (dynamicLibFile.exists()) {
                return dynamicLibFile.getCanonicalPath();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Dynamic lib directory is not exist");
        }
        return CodeCheckByPassUtils.getNull();
    }

    /**
     * get other libPath
     *
     * @return other libPath
     */
    @Nullable
    private String getOtherDynamicLibPath() {
        try {
            SdkConfig config = getSdkConfig();
            if (SystemUtils.IS_OS_WINDOWS) {
                StringBuilder filePath = new StringBuilder();
                File liblldbPath = Path.of(config.getBuildToolsThirdPartyLibPath()).toFile();
                if (liblldbPath.exists()) {
                    filePath.append(liblldbPath.getCanonicalPath()).append(';');
                }
                File runtimePath = Path.of(config.getBuildToolsWinRuntimePath()).toFile();
                if (runtimePath.exists()) {
                    filePath.append(runtimePath.getCanonicalPath()).append(';');
                }
                return filePath.toString();
            } else {
                StringBuilder filePath = new StringBuilder();
                File runtimePath = Path.of(config.getBuildToolsMacX86RuntimePath())
                        .toFile();
                if (runtimePath.exists()) {
                    filePath.append(runtimePath.getCanonicalPath()).append(':');
                }
                File toolsLib = Path.of(config.getBuildToolsLibPath()).toFile();
                if (toolsLib.exists()) {
                    filePath.append(toolsLib.getCanonicalPath());
                }
                return filePath.toString();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Other lib directory is not exist");
        }
        return null;
    }

    /**
     * get CANGJIE_HOME path
     *
     * @return CANGJIE_HOME path
     */
    private String getCangjieHomePath() {
        try {
            SdkConfig config = getSdkConfig();
            File dynamicLibFile = Path.of(config.getBuildToolsRootPath()).toFile();
            if (dynamicLibFile.exists()) {
                return dynamicLibFile.getCanonicalPath();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Cangjie home directory is not exist");
        }
        return CodeCheckByPassUtils.getNull();
    }

    /**
     * get sdk config
     *
     * @return sdk config
     */
    private SdkConfig getSdkConfig() {
        int apiVersion = 20;
        boolean isHarmony = true;
        return new CangjieIdeaSdkInfoHandler()
                .getLocalSdks(isHarmony, apiVersion)
                .get(CangjieComponentPath.CANGJIE.value())
                .getSdkConfig();
    }
}
