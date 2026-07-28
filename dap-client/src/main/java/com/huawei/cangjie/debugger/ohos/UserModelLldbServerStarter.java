/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import static com.huawei.cangjie.debugger.ohos.OhFilePathUtils.getLldbServer;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.ThreadUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.utils.HdcUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.dap.util.StringUtils;
import com.huawei.deveco.debugger.ohos.run.app.info.LaunchAppInfo;
import com.huawei.deveco.hdclib.ohos.client.Client;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosAbility;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * User model lldbServerStarter
 *
 * @since 2024-6-5
 */
public class UserModelLldbServerStarter extends LldbServerManager {
    private static final String LLDB_TARGET_ROOT = "/data/local/tmp/debugserver/";

    private static final Logger LOGGER = Logger.getInstance(UserModelLldbServerStarter.class);

    private String userModeDestFilePath;

    private final String packageName;

    private final Client userModeClient;

    /**
     * Instantiates a new UserMode Lldb server launcher for api11 or a later version
     *
     * @param debugClient   debugClient
     * @param debuggerState debuggerState
     * @param printer       printer
     * @param projectModel  projectModel
     * @throws TimeoutException e
     */
    public UserModelLldbServerStarter(DebugClient debugClient, NativeDebuggerState debuggerState,
                                      OpenHarmonyConsolePrinter printer, ProjectModel projectModel)
            throws TimeoutException {
        super(debugClient, debuggerState, printer, projectModel);
        packageName = debugClient.getClientData().getPackageName();
        userModeClient = debugClient.getDevice().getClient();
    }

    @Override
    public void terminateProcess() throws TimeoutException {
        Project project = getProjectModel().getProject();
        long count = project.isDisposed() ? 0
                : Arrays.stream(XDebuggerManagerImpl.getInstance(project).getDebugSessions())
                .filter(xDebugSession -> xDebugSession.getDebugProcess() instanceof CangjieXDebugProcess)
                .count();
        if (count == 0) {
            HdcUtils.sendSyncShellCommand("aa force-stop " + packageName, userModeClient);
        }
    }

    @Override
    protected void pushFilesToDevice(OhosModuleModel ohosModuleModel) throws ExecutionException {
        File localLldbServer = getLldbServer(ohosModuleModel, getOhAbi());
        if (localLldbServer == null) {
            throw new ExecutionException("get lldbServer error");
        }
        String fileName = localLldbServer.getName();
        String createTargetRootDirCommand = String.format("mkdir -p %s", LLDB_TARGET_ROOT + packageName);
        String permissionTargetRootDirCommand = String.format("chmod 757 %s", LLDB_TARGET_ROOT + packageName);

        userModeDestFilePath = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR,
                LLDB_TARGET_ROOT + packageName, fileName);
        String sendFileCommend = String.format("file send \"%s\" \"%s\"", localLldbServer, userModeDestFilePath);

        String permissionCommand = String.format("chmod 755 %s", userModeDestFilePath);

        List<String> commands = Arrays.asList(createTargetRootDirCommand, permissionTargetRootDirCommand,
                sendFileCommend, permissionCommand);
        commands.forEach(item -> {
            if (item.equals(sendFileCommend)) {
                HdcUtils.sendSyncCommand(item, userModeClient);
            } else {
                HdcUtils.sendSyncShellCommand(item, userModeClient);
            }
        });
    }

    @Override
    protected String startLldbServer() {
        ExecutorService executorService = ThreadUtils.createSingleThreadExecutor("userModel lldb-server executor");
        try {
            executorService.execute(
                    () -> {
                        String abilityName = getAbilityNameFromAppInfo();
                        if (StringUtils.isEmpty(abilityName) || isExtensionAbility(abilityName)) {
                            abilityName = getAbilityNameFromBmDump();
                            LogUtils.printCangjieLogInfo(LOGGER, "getAbilityNameFromBmDump:" + abilityName);
                        }
                        String startCmd = getStarCmd(abilityName);
                        HdcUtils.sendSyncShellCommand(startCmd, userModeClient);
                    });
        } finally {
            executorService.shutdown();
        }
        return getConnectUrl();
    }

    private boolean isExtensionAbility(String abilityName) {
        List<OhosAbility> ohosAbilities = getDebuggerState().getModuleWrapper()
                .getOhosModule().getExtensionAbilityList();
        if (ohosAbilities == null) {
            return false;
        }
        for (OhosAbility ohosAbility : ohosAbilities) {
            if (abilityName.equals(ohosAbility.getName())) {
                LogUtils.printCangjieLogInfo(LOGGER, "isExtensionAbility:" + abilityName);
                return true;
            }
        }
        return false;
    }

    private String getAbilityNameFromBmDump() {
        JsonObject dumpObject = new Gson().fromJson(
                userModeClient.getDumpJson(packageName, 1000), JsonObject.class);
        JsonArray hapModuleInfos = dumpObject.getAsJsonArray("hapModuleInfos");
        if (hapModuleInfos == null || hapModuleInfos.isEmpty()) {
            LogUtils.printCangjieLogWarn(LOGGER, "hapModuleInfos is empty");
            return CodeCheckByPassUtils.getNull();
        }
        int i = 0;
        String debuggerModuleName = getDebuggerState().getModuleWrapper().getOhosModule().getModuleName();
        if (isHspOrHarTest()) {
            return getOhosTestAbility(hapModuleInfos);
        }
        for (; i < hapModuleInfos.size(); i++) {
            String moduleName = hapModuleInfos.get(i).getAsJsonObject().get("moduleName").getAsString();
            if (debuggerModuleName.equals(moduleName)) {
                LogUtils.printCangjieLogInfo(LOGGER, "find debuggerModuleName in hapModuleNames :" + i);
                break;
            }
        }
        if (i == hapModuleInfos.size()) {
            LogUtils.printCangjieLogWarn(LOGGER, "not find debuggerModuleName in hapModuleNames");
            return CodeCheckByPassUtils.getNull();
        }
        return hapModuleInfos.get(i).getAsJsonObject().get("abilityInfos").getAsJsonArray()
                .get(0).getAsJsonObject().get("name").getAsString();
    }

    private boolean isHspOrHarTest() {
        LaunchAppInfo launchAppInfo = LaunchAppInfo.getLaunchAppInfo(getProjectModel().getProject());
        if (launchAppInfo == null || launchAppInfo.getModuleWrapper() == null
                || launchAppInfo.getModuleWrapper().getOhosModule() == null) {
            return false;
        }
        String moduleType = launchAppInfo.getModuleWrapper().getOhosModule().getModuleType();
        return launchAppInfo.isTest() && (LibraryModuleTypeEnum.SHARED_LIBRARY.getModuleType().equals(moduleType)
                || LibraryModuleTypeEnum.STATIC_LIBRARY.getModuleType().equals(moduleType));
    }

    private String getOhosTestAbility(JsonArray hapModuleInfos) {
        for (int i = 0; i < hapModuleInfos.size(); i++) {
            JsonElement element = hapModuleInfos.get(i).getAsJsonObject().get("abilityInfos");
            JsonArray abilityInfos = element != null ? element.getAsJsonArray() : null;
            if (abilityInfos != null && !abilityInfos.isEmpty()) {
                return abilityInfos.get(0).getAsJsonObject().get("name").getAsString();
            }
        }
        return CodeCheckByPassUtils.getNull();
    }

    private String getAbilityNameFromAppInfo() {
        LaunchAppInfo launchAppInfo = LaunchAppInfo.getLaunchAppInfo(getProjectModel().getProject());
        String abilityName = launchAppInfo.getAbilityName();
        LogUtils.printCangjieLogInfo(LOGGER, "getAbilityNameFromAppInfo:" + abilityName);
        return abilityName;
    }

    private String getStarCmd(String mainAbility) {
        String debugCmd = String.format(Locale.ENGLISH, "%s %s%s/%s %s \\\"%s\\\" %s \\\"%s\\\"",
                userModeDestFilePath,
                "platform --listen unix-abstract://",
                myTargetSocketDir,
                myPlatformSocketName, "--log-channels",
                getDebuggerState().getLoggingTargetChannels(), "--log-file",
                LLDB_TARGET_ROOT + packageName + "/platform.log");
        return String.format("aa process -a %s -b %s -D \"%s\";",
                mainAbility, packageName, debugCmd);
    }

    /**
     * killLldbServer
     *
     * @throws TimeoutException timeoutException
     */
    @Override
    public void killLldbServer() throws TimeoutException {
        String pkillLldbServerCmd = String.format("pkill lldb-server platform --listen unix-abstract://",
                myTargetSocketDir,
                myPlatformSocketName, "--log-channels",
                getDebuggerState().getLoggingTargetChannels(), "--log-file",
                LLDB_TARGET_ROOT + packageName + "/platform.log");
        HdcUtils.sendSyncShellCommand(pkillLldbServerCmd, userModeClient);
    }
}
