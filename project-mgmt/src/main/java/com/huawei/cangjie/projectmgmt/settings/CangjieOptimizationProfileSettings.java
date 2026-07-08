/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * cangjie 持久化配置
 *
 * @since 2026 -04-24
 */
@State(name = "CangjieOptimizationProfileSettings",
    storages = @Storage(value = "cangjie_optimization_profile_settings.xml"))
public class CangjieOptimizationProfileSettings
    implements PersistentStateComponent<CangjieOptimizationProfileSettings.State> {
    private State state = new State();

    /**
     * 自定义包名状态
     */
    public static class State {
        private boolean debugUseProfile = false;

        private boolean releaseUseProfile = false;

        private String optimizationPath;

        /**
         * Is debug use profile boolean.
         *
         * @return the boolean
         */
        public boolean isDebugUseProfile() {
            return debugUseProfile;
        }

        /**
         * Sets debug use profile.
         *
         * @param debugUseProfile the debug use profile
         */
        public void setDebugUseProfile(boolean debugUseProfile) {
            this.debugUseProfile = debugUseProfile;
        }

        /**
         * Is release use profile boolean.
         *
         * @return the boolean
         */
        public boolean isReleaseUseProfile() {
            return releaseUseProfile;
        }

        /**
         * Sets release use profile.
         *
         * @param releaseUseProfile the release use profile
         */
        public void setReleaseUseProfile(boolean releaseUseProfile) {
            this.releaseUseProfile = releaseUseProfile;
        }

        /**
         * Gets optimization path.
         *
         * @return the optimization path
         */
        public String getOptimizationPath() {
            return optimizationPath;
        }

        /**
         * Sets optimization path.
         *
         * @param optimizationPath the optimization path
         */
        public void setOptimizationPath(String optimizationPath) {
            this.optimizationPath = optimizationPath;
        }
    }

    /**
     * 获取配置文件实例
     *
     * @return 配置文件实例 instance
     */
    public static CangjieOptimizationProfileSettings getInstance() {
        return ApplicationManager.getApplication().getService(CangjieOptimizationProfileSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    /**
     * Is debug use profile boolean.
     *
     * @return the boolean
     */
    public boolean isDebugUseProfile() {
        return state.debugUseProfile;
    }

    /**
     * Is release use profile boolean.
     *
     * @return the boolean
     */
    public boolean isReleaseUseProfile() {
        return state.releaseUseProfile;
    }

    /**
     * Gets optimization path.
     *
     * @return the optimization path
     */
    public String getOptimizationPath() {
        return state.optimizationPath;
    }
}