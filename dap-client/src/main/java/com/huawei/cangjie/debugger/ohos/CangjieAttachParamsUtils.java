/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import static com.huawei.bitfun.intellij.utils.BalloonNotificationsUtils.getExceptionMessageWithCauseAndSolution;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * utility class to create attach params
 *
 * @since 2022-12-5
 */
public class CangjieAttachParamsUtils {
    private static Set<File> symbolDirs;

    /**
     * create params
     *
     * @param debugClient debugClient
     * @param connectAddress connectAddress
     * @param debuggerState debuggerState
     * @param abi abi
     * @return CangjieAttachRequestArgs
     * @throws IOException e
     */
    public static CangjieAttachRequestArgs createOhCangjieAttachRequestParams(DebugClient debugClient,
        String connectAddress, NativeDebuggerState debuggerState, Abi abi) throws IOException {
        ProjectModel projectModel = debuggerState.getModuleWrapper().getOhosModule().getProjectModel();
        Project project = projectModel.getProject();
        CangjieAttachRequestArgs attachParams = new CangjieAttachRequestArgs();
        attachParams.setName("(cjdb) Attach");
        attachParams.setRemoteAddress(connectAddress);
        attachParams.setRequest("attach");
        attachParams.setType("cangjie");
        attachParams.setAttachStep("connectDbgServer");
        attachParams.setRemote(true);
        attachParams.setStopAtEntry(false);
        attachParams.setExternalConsole(false);
        attachParams.setShowStaticGlobalVars(debuggerState.getShowStaticGlobalVars());
        attachParams.setCwd(project.getBasePath());
        attachParams.setProcessId(String.valueOf(debugClient.getClientData().getPid()));
        attachParams.setRemotePlatform("remote-ohos");
        List<String> startupCommands = new ArrayList<>();
        startupCommands.add("settings set auto-confirm true");
        startupCommands.add("settings set symbols.debug-info-symlink-paths /proc/self/cwd");
        startupCommands.add("settings set symbols.enable-background-lookup false");
        // Add user configuration commands
        startupCommands.addAll(debuggerState.getUserStartupCommands());
        attachParams.setStartupCommands(startupCommands);
        attachParams.setAfterConnectCommands(createAfterCommends(debuggerState, abi));
        List<String> postAttachCommands = new ArrayList<>();
        postAttachCommands.add("process handle -s false SIGSEGV");
        // Shield customized abnormal signals during debugging
        postAttachCommands.add("process handle -s false -p false -n false 35");
        // Add user configuration commands
        postAttachCommands.addAll(debuggerState.getUserPostAttachCommands());
        attachParams.setPostAttachCommands(postAttachCommands);
        return attachParams;
    }

    /**
     * createAfterCommends
     *
     * @param debuggerState debuggerState
     * @param abi abi
     * @return java.util.List<java.lang.String>
     * @throws IOException e
     */
    private static List<String> createAfterCommends(NativeDebuggerState debuggerState, Abi abi)
        throws IOException {
        OhosModuleModel module = debuggerState.getModuleWrapper().getOhosModule();
        SymbolFileSearcher ohSymbolFileSearcher = new SymbolFileSearcher(abi);
        List<String> afterConnectCommands = new ArrayList<>();
        symbolDirs = ohSymbolFileSearcher.getSymbolsDir(debuggerState);
        if (symbolDirs.isEmpty()) {
            throw CodeCheckByPassUtils.createRuntimeException(getExceptionMessageWithCauseAndSolution(
                String.format(Locale.ENGLISH, "Can't find any .so file in the %s", module.getModuleName()),
                "Please check whether the module is a Cangjie project and try to rebuild the project again."));
        } else {
            boolean isContainsAbiPath = false;
            for (File path : symbolDirs) {
                String canonicalPath = path.getCanonicalPath();
                if (canonicalPath.contains(abi.toString())) {
                    isContainsAbiPath = true;
                }
                afterConnectCommands.add("settings append target.exec-search-paths \"" + canonicalPath + "\"");
            }
            if (!isContainsAbiPath) {
                throw CodeCheckByPassUtils.createRuntimeException(
                    getExceptionMessageWithCauseAndSolution("No symbol directories found",
                        String.format(Locale.ENGLISH, "Please check your abiFilters config in %s, support abi is %s",
                            module.getModuleName(), abi)));
            }
        }
        return afterConnectCommands;
    }

    public static Set<File> getSymbolDirs() {
        return symbolDirs;
    }
}
