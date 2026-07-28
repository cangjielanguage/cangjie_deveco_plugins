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
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * cangjie 持久化配置
 *
 * @since 2025-11-17
 */
@State(name = "CangjieSettings",
        storages = @Storage(value = "cangjie_settings.xml",
                roamingType = RoamingType.PER_OS)
)
public class CangjieSettings implements PersistentStateComponent<CangjieSettings.State> {
    private State state = new State();

    /**
     * 自定义包名状态
     */
    public static class State {
        private boolean enabled = false;

        private String packageName = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    /**
     * 获取配置文件实例
     *
     * @return 配置文件实例
     */
    public static CangjieSettings getInstance() {
        return ApplicationManager.getApplication().getService(CangjieSettings.class);
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

    public boolean isEnabled() {
        return state.enabled;
    }

    public String getPackageName() {
        return state.packageName;
    }
}