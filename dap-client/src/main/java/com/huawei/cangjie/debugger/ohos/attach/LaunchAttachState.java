/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.attach;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.ohos.CangjieOpenHarmonyDebugger;
import com.huawei.cangjie.debugger.ohos.OhConnectDebuggerTask;
import com.huawei.cangjie.debugger.utils.DummyConsoleView;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.LaunchInfo;
import com.huawei.deveco.ohos.debugcommon.debugger.ConnectDebuggerTask;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.OpenHarmonyDebugger;
import com.huawei.deveco.ohos.debugcommon.debugger.OpenHarmonyDebuggerContext;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.xdebugger.DefaultDebugProcessHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Used to start the process, usually return the corresponding process processor and console
 *
 * @since 2022-12-5
 */
public class LaunchAttachState implements RunProfileState {
    private final ExecutionEnvironment myExecEnv;

    private final AttachConfiguration myAttachConfig;

    public LaunchAttachState(ExecutionEnvironment execEnv, AttachConfiguration attachConfig) {
        this.myExecEnv = execEnv;
        this.myAttachConfig = attachConfig;
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> programRunner) {
        OpenHarmonyDebuggerContext debuggerContext = myAttachConfig.getHarmonyDebuggerContext();
        if (debuggerContext.getHarmonyDebugger().isEmpty() || debuggerContext.getDebuggerType() == null) {
            return null;
        }
        CangjieOpenHarmonyDebugger debugger = getOpenHarmonyDebugger();
        OhosTarget target = myAttachConfig.getDebuggerState().getTarget();
        if (target == null || debugger == null || !(debuggerContext.getDebuggerState(
            debugger.getDisplayName()) instanceof NativeDebuggerState)) {
            return CodeCheckByPassUtils.getNull();
        }
        ProcessHandler processHandler = new DefaultDebugProcessHandler();
        ConsoleView consoleView = new DummyConsoleView();
        DebugClient client = myAttachConfig.getClient();
        String packageName = client.getClientData().getPackageName();
        Devices devices = client.getDevice();
        ConnectDebuggerTask connectDebuggerTask = new OhConnectDebuggerTask(devices, packageName, myExecEnv,
            myAttachConfig.getDebuggerState(), LaunchType.ATTACH_DEBUGGER_TO_PROCESS);
        OpenHarmonyConsolePrinter consolePrinter = new OpenHarmonyConsolePrinter(processHandler);
        OpenHarmonyLaunchStatus launchStatus = new OpenHarmonyLaunchStatus(processHandler);
        LaunchInfo launchInfo = new LaunchInfo(executor, programRunner, myExecEnv, consoleView);
        connectDebuggerTask.perform(launchInfo, consolePrinter, launchStatus, myAttachConfig.getClient());
        return null;
    }

    @Nullable
    private CangjieOpenHarmonyDebugger getOpenHarmonyDebugger() {
        List<OpenHarmonyDebugger> openHarmonyDebuggers = OpenHarmonyDebugger.EP_NAME.getExtensionList();
        for (OpenHarmonyDebugger openHarmonyDebugger : openHarmonyDebuggers) {
            if (openHarmonyDebugger instanceof CangjieOpenHarmonyDebugger) {
                return (CangjieOpenHarmonyDebugger) openHarmonyDebugger;
            }
        }
        return CodeCheckByPassUtils.getNull();
    }

    @Override
    public String toString() {
        return "LaunchAttachState{}";
    }
}
