/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runprofilestate;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.deveco.debugger.ohos.run.LaunchTaskRunner;
import com.huawei.deveco.debugger.ohos.run.app.info.LaunchAppInfo;
import com.huawei.deveco.ohos.debugcommon.module.ModuleWrapper;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LocalTestRunProfileState
 *
 * @since 2025/08/28
 */
public class LocalTestRunProfileState implements RunProfileState {
    private final CangjieTestRunConfiguration configuration;
    private final ExecutionEnvironment env;
    private final ProcessHandler processHandler;
    private final LaunchAppInfo launchAppInfo;

    public LocalTestRunProfileState(@NotNull CangjieTestRunConfiguration configuration,
                                    @NotNull ExecutionEnvironment executionEnvironment,
                                    @NotNull ProcessHandler processHandler,
                                    @NotNull LaunchAppInfo launchAppInfo) {
        this.configuration = configuration;
        this.env = executionEnvironment;
        this.processHandler = processHandler;
        this.launchAppInfo = launchAppInfo;
    }

    @Override
    @Nullable
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> programRunner) {
        ConsoleView consoleView = this.configuration.getConsoleView();
        if (consoleView == null) {
            return null;
        }
        launchAppInfo.setIsTest(true);
        Project project = this.configuration.getProject();
        boolean isDebug = executor instanceof DefaultDebugExecutor;
        ModuleWrapper moduleWrapper = new ModuleWrapper(this.configuration.getModule());
        launchAppInfo.init(moduleWrapper, false, false, this.configuration, project);
        launchAppInfo.setExecutor(executor);
        launchAppInfo.setRunner(programRunner);
        launchAppInfo.setIsDebug(isDebug);
        launchAppInfo.setConsoleView(consoleView);
        if (!launchAppInfo.canRun()) {
            return null;
        }
        Task.Backgroundable taskRunner = new LaunchTaskRunner(env, launchAppInfo, processHandler);
        ProgressManager.getInstance().run(taskRunner);
        return new DefaultExecutionResult(consoleView, processHandler);
    }
}
