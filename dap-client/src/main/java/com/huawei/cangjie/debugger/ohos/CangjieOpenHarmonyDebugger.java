/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import static com.huawei.cangjie.debugger.ohos.OhConstants.DEBUGGER_NAME_CANGJIE;
import static com.huawei.cangjie.debugger.ohos.OhFilePathUtils.validateCangjieDir;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.ohos.attach.AttachConfiguration;
import com.huawei.cangjie.debugger.ohos.attach.AttachConfigurationType;
import com.huawei.cangjie.debugger.ohos.impl.OhCangjieXDebugProcess;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.hdclib.ohos.devices.DebugClientData;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.debugger.ConnectDebuggerTask;
import com.huawei.deveco.ohos.debugcommon.debugger.DebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.OpenHarmonyDebugger;
import com.huawei.deveco.ohos.debugcommon.debugger.SetBreakpointState;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * CangjieOpenHarmonyDebugger
 *
 * @since 2022-12-5
 */
public class CangjieOpenHarmonyDebugger implements OpenHarmonyDebugger {
    private static final int SORT_ID = 4;

    @NotNull
    @Override
    public String getId() {
        return CangjieXDebugProcess.LANGUAGE_ID;
    }

    @Override
    public DebuggerState createState() {
        return new NativeDebuggerState();
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DEBUGGER_NAME_CANGJIE;
    }

    @Override
    public int getSortId() {
        return SORT_ID;
    }

    @Override
    @NotNull
    public ConnectDebuggerTask getConnectDebuggerTask(Devices devices, String packageName,
        ExecutionEnvironment executionEnvironment, DebuggerState debuggerState) {
        return new OhConnectDebuggerTask(devices, packageName, executionEnvironment, debuggerState);
    }

    @Override
    public void attachToClient(Project project, DebuggerState debuggerState, DebugClient debugClient) {
        if (!(debuggerState instanceof NativeDebuggerState)) {
            throw CodeCheckByPassUtils.createRuntimeException("not Cangjie debugging configuration");
        }
        DebugClientData clientData = debugClient.getClientData();
        if (clientData == null || clientData.getClientDescription() == null) {
            return;
        }
        OhosTarget target = debuggerState.getTarget();
        // if already has xdebug session, no need to attach.
        if (hasExistingXDebugSession(project, debugClient)) {
            return;
        }
        if (target == null) {
            throw CodeCheckByPassUtils.createRuntimeException("ohos target is null");
        }
        validateCangjieDir(target.getBelongModuleModel());
        RunnerAndConfigurationSettings runnerSettings = createRunnerSettings(project, debugClient, debuggerState);
        ProgramRunnerUtil.executeConfiguration(runnerSettings, DefaultDebugExecutor.getDebugExecutorInstance());
    }

    private RunnerAndConfigurationSettings createRunnerSettings(Project project, DebugClient client,
        DebuggerState debuggerState) {
        String runConfigName =
            String.format(Locale.ENGLISH, "%s Debugger (%s)", getDisplayName(), client.getClientData().getPid());
        RunnerAndConfigurationSettings runnerSettings =
            RunManager.getInstance(project).createConfiguration(runConfigName, new AttachConfigurationType());

        if (runnerSettings.getConfiguration() instanceof AttachConfiguration) {
            AttachConfiguration configuration = (AttachConfiguration) runnerSettings.getConfiguration();
            configuration.setClient(client);
            configuration.setModule(debuggerState.getModuleWrapper().getOhosModule());
            configuration.getHarmonyDebuggerContext().setDebuggerType(getDisplayName());
            configuration.setDebuggerState(debuggerState);
            configuration.setBeforeRunTasks(Collections.emptyList());
        }
        return runnerSettings;
    }

    private boolean hasExistingXDebugSession(Project project, DebugClient client) {
        for (XDebugSession session : XDebuggerManager.getInstance(project).getDebugSessions()) {
            XDebugProcess debugProcess = session.getDebugProcess();
            if (!(debugProcess instanceof OhCangjieXDebugProcess)) {
                continue;
            }
            OhCangjieXDebugProcess cangjieAppDebugProcess = (OhCangjieXDebugProcess) debugProcess;
            if (Objects.equals(cangjieAppDebugProcess.getDeviceAppPid(), client.getClientData().getPid())) {
                return true;
            }
        }
        // no debug session
        return false;
    }

    /**
     * set dual debug breakpoint
     *
     * @param project project
     * @param codeAddress Decimal address
     * @param processId pid
     * @return 0:cangjie method and set bk failed;1:set bk success;2:not cangjie method
     */
    @Override
    public int setBreakpoint(Project project, String codeAddress, int processId) {
        String pid = String.valueOf(processId);
        List<? extends OhCangjieXDebugProcess> debugProcessList = XDebuggerManager.getInstance(project)
                .getDebugProcesses(OhCangjieXDebugProcess.class);
        for (OhCangjieXDebugProcess process : debugProcessList) {
            if (process.getDeviceAppPid().equalsIgnoreCase(pid)) {
                return process.setDualDebugBreakpoint(codeAddress);
            }
        }
        // cannot find oh cangjie debug process
        return SetBreakpointState.FAILURE;
    }

    /**
     * stop CANGJIE debug session for Cangjie debug
     *
     * @param project project
     */
    @Override
    public void stopXDebugSession(Project project) {
        List<? extends OhCangjieXDebugProcess> debugProcessList =
            XDebuggerManager.getInstance(project).getDebugProcesses(OhCangjieXDebugProcess.class);
        if (!debugProcessList.isEmpty()) {
            debugProcessList.get(0).stopSession();
        }
    }
}
