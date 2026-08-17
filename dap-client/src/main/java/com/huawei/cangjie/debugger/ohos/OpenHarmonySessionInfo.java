/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.util.Key;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * OpenHarmonySessionInfo replace SessionInfo relation version , processHandler realization ,ExecutionTarget
 *
 * @since 2022-12-1
 */
public class OpenHarmonySessionInfo {
    /**
     * OpenHarmonySessionInfo key
     */
    public static final Key<OpenHarmonySessionInfo> KEY = new Key<>("OHKEY");

    @Getter
    @NotNull
    private final ProcessHandler myProcessHandler;

    @Getter
    private final RunContentDescriptor myDescriptor;

    @Getter
    @Nullable
    private final RunConfiguration myRunConfiguration;

    @Getter
    @NotNull
    private final ExecutionTarget myExecutionTarget;

    @NotNull
    private final ExecutorInfo myExecutorInfo;

    public OpenHarmonySessionInfo(@NotNull ProcessHandler processHandler, @NotNull RunContentDescriptor descriptor,
                                  @Nullable RunConfiguration runConfiguration, @NotNull ExecutionTarget executionTarget,
                                  @NotNull ExecutorInfo executorInfo) {
        super();
        this.myProcessHandler = processHandler;
        this.myDescriptor = descriptor;
        this.myRunConfiguration = runConfiguration;
        this.myExecutionTarget = executionTarget;
        this.myExecutorInfo = executorInfo;
    }

    /**
     * get my executor id
     *
     * @return myExecutorId
     */
    public String getMyExecutorId() {
        return myExecutorInfo.getId();
    }

    /**
     * get my executor action name
     *
     * @return mExecutorActionName
     */
    public String getMyExecutorActionName() {
        return myExecutorInfo.getActionName();
    }

    @Override
    public String toString() {
        return "OpenHarmonySessionInfo{}";
    }

    /**
     * OpenHarmonySessionInfo Builder
     */
    public static class OpenHarmonySessionInfoBuilder {
        private ProcessHandler processHandler;

        private RunContentDescriptor descriptor;

        private String executorId;

        private String executorActionName;

        private RunConfiguration runConfiguration;

        private ExecutionTarget executionTarget;

        public OpenHarmonySessionInfoBuilder() {
        }

        /**
         * set processHandler
         *
         * @param processHandler processHandler
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setProcessHandler(ProcessHandler processHandler) {
            this.processHandler = processHandler;
            return this;
        }

        /**
         * set descriptor
         *
         * @param descriptor descriptor
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setDescriptor(RunContentDescriptor descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        /**
         * set executorId
         *
         * @param executorId executorId
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setExecutorId(String executorId) {
            this.executorId = executorId;
            return this;
        }

        /**
         * set executorActionName
         *
         * @param executorActionName executorActionName
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setExecutorActionName(String executorActionName) {
            this.executorActionName = executorActionName;
            return this;
        }

        /**
         * set runConfiguration
         *
         * @param runConfiguration runConfiguration
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setRunConfiguration(RunConfiguration runConfiguration) {
            this.runConfiguration = runConfiguration;
            return this;
        }

        /**
         * set executionTarget
         *
         * @param executionTarget executionTarget
         * @return OpenHarmonySessionInfoBuilder
         */
        public OpenHarmonySessionInfoBuilder setExecutionTarget(ExecutionTarget executionTarget) {
            this.executionTarget = executionTarget;
            return this;
        }

        /**
         * build
         *
         * @return OpenHarmonySessionInfo
         */
        public OpenHarmonySessionInfo build() {
            ExecutorInfo executorInfo = new ExecutorInfo(executorId, executorActionName);
            OpenHarmonySessionInfo result = new OpenHarmonySessionInfo(
                    processHandler, descriptor, runConfiguration, executionTarget, executorInfo);
            processHandler.putUserData(KEY, result);
            return result;
        }
    }
}

