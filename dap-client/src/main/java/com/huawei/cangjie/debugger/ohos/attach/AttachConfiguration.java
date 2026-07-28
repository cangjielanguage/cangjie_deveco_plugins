/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.attach;

import static com.huawei.cangjie.debugger.ohos.OhConstants.DEBUGGER_NAME_CANGJIE;

import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.ohos.debugcommon.debugger.DebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.OpenHarmonyDebuggerContext;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * Runtime configuration
 *
 * @since 2022-12-5
 */
public class AttachConfiguration extends OpenHarmonyRunConfiguration {
    private static final long serialVersionUID = 4634309982799563700L;

    private transient DebugClient myClient;

    private DebuggerState debuggerState;

    /**
     * init
     *
     * @param project project
     * @param factory factory
     */
    public AttachConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    /**
     * getClient
     *
     * @return myClient myClient
     */
    public DebugClient getClient() {
        return myClient;
    }

    /**
     * setClient
     *
     * @param client client
     */
    public void setClient(DebugClient client) {
        this.myClient = client;
    }

    /**
     * check is exclude compile before launch option
     *
     * @return boolean is exclude compile
     */
    @Override
    public boolean isExcludeCompileBeforeLaunchOption() {
        return true;
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        AttachConfiguration obj = this;
        return new LaunchAttachState(env, obj);
    }

    /**
     * get harmony debugger context
     *
     * @return OpenHarmonyDebuggerContext
     */
    @Override
    public OpenHarmonyDebuggerContext getHarmonyDebuggerContext() {
        return new OpenHarmonyDebuggerContext(DEBUGGER_NAME_CANGJIE);
    }

    public DebuggerState getDebuggerState() {
        return debuggerState;
    }

    public void setDebuggerState(DebuggerState debuggerState) {
        this.debuggerState = debuggerState;
    }

    @Override
    public String toString() {
        return "AttachConfiguration{}";
    }
}
