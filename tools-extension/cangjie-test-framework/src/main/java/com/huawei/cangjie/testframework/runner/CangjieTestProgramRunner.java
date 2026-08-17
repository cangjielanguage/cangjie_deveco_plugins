/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * CangjieTestProgramRunner
 *
 * @since 2025/02/20
 */
public class CangjieTestProgramRunner implements ProgramRunner<RunnerSettings> {
    public static final @NonNls String EXECUTOR_ID = "Coverage";

    @Override
    @NotNull
    @NonNls
    public String getRunnerId() {
        return "CangjieTestProgramRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        if (!(runProfile instanceof CangjieTestRunConfiguration)) {
            return false;
        }
        return (executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) || executorId.equals(EXECUTOR_ID));
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        ExecutionManager.getInstance(executionEnvironment.getProject())
                .startRunProfile(executionEnvironment, state -> doExecute(state, executionEnvironment));
    }

    /**
     * doExecute
     *
     * @param state state
     * @param env env
     * @return RunContentDescriptor
     * @throws ExecutionException ExecutionException
     */
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
            throws ExecutionException {
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = env.getRunnerAndConfigurationSettings();

        if (runnerAndConfigurationSettings != null && runnerAndConfigurationSettings.isActivateToolWindowBeforeRun()) {
            runnerAndConfigurationSettings.setActivateToolWindowBeforeRun(true);
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        ExecutionResult result = state.execute(env.getExecutor(), this);

        return DefaultProgramRunnerKt.showRunContent(result, env);
    }
}
