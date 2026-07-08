/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Project optimization settings service.
 *
 * @since 2026-04-24
 */
@State(name = "ProjectOptimizationSettings", storages = @Storage("projectOptimizationSettings.xml"))
public class ProjectOptimizationSettingsService
    implements PersistentStateComponent<ProjectOptimizationSettingsService.State> {
    private State state = new State();

    /**
     * The type State.
     */
    public static class State {
        private boolean isGenerateOptimizationProfile = false;

        private String profdataFilePath = "";

        private boolean isUseRunActionSelected = true;

        private boolean isUseExistedProfdataSelected = false;

        /**
         * Is generate optimization profile boolean.
         *
         * @return the boolean
         */
        public boolean isGenerateOptimizationProfile() {
            return isGenerateOptimizationProfile;
        }

        /**
         * Sets generate optimization profile.
         *
         * @param generateOptimizationProfile the generate optimization profile
         */
        public void setGenerateOptimizationProfile(boolean generateOptimizationProfile) {
            isGenerateOptimizationProfile = generateOptimizationProfile;
        }

        /**
         * Gets profdata file path.
         *
         * @return the profdata file path
         */
        public String getProfdataFilePath() {
            return profdataFilePath;
        }

        /**
         * Sets profdata file path.
         *
         * @param profdataFilePath the profdata file path
         */
        public void setProfdataFilePath(String profdataFilePath) {
            this.profdataFilePath = profdataFilePath;
        }

        /**
         * Is use run action selected boolean.
         *
         * @return the boolean
         */
        public boolean isUseRunActionSelected() {
            return isUseRunActionSelected;
        }

        /**
         * Sets use run action selected.
         *
         * @param useRunActionSelected the use run action selected
         */
        public void setUseRunActionSelected(boolean useRunActionSelected) {
            isUseRunActionSelected = useRunActionSelected;
        }

        /**
         * Is use existed profdata selected boolean.
         *
         * @return the boolean
         */
        public boolean isUseExistedProfdataSelected() {
            return isUseExistedProfdataSelected;
        }

        /**
         * Sets use existed profdata selected.
         *
         * @param useExistedProfdataSelected the use existed profdata selected
         */
        public void setUseExistedProfdataSelected(boolean useExistedProfdataSelected) {
            isUseExistedProfdataSelected = useExistedProfdataSelected;
        }
    }

    /**
     * Gets instance.
     *
     * @param project the project
     * @return the instance
     */
    // 重点：通过传入的具体 project 获取对应的实例
    public static ProjectOptimizationSettingsService getInstance(Project project) {
        return project.getService(ProjectOptimizationSettingsService.class);
    }

    @Nullable
    @Override
    public ProjectOptimizationSettingsService.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull ProjectOptimizationSettingsService.State state) {
        this.state = state;
    }
}