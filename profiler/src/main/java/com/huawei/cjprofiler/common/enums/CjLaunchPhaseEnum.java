/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common.enums;

import lombok.Getter;

/**
 * launch泳道各阶段枚举
 *
 * @since 2025-04-25
 */@Getter
public enum CjLaunchPhaseEnum {
    /**
     * Process Creating
     */
    LIFE_CYCLE_NAME_PROCESS_CREATING("Process Creating"),

    /**
     * Application Launching
     * Application Launching包括子阶段Runtime Initialization、Load Extension和.so File Loading
     */
    LIFE_CYCLE_NAME_APPLICATION_LAUNCHING("Application Launching"),

    /**
     * UI Ability Launching
     * UI Ability Launching包括子阶段onCreate Execution
     */
    LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING("UI Ability Launching"),

    /**
     * UI Ability OnForeground
     * UI Ability OnForeground包括子阶段Ability Lifecycle Callback和Content and Page Loading
     */
    LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND("UI Ability OnForeground"),

    /**
     * First Frame - App Phase
     */
    LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING("First Frame - App Phase"),

    /**
     * First Frame - Render Phase
     */
    LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING("First Frame - Render Phase"),

    /**
     * 以下为新增的子阶段
     */
    LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION("Runtime Initialization"),

    LIFE_CYCLE_NAME_LOADING_SO_FILE(".so File Loading"),

    LIFE_CYCLE_NAME_LOAD_EXTENSION("Load Extension"),

    LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION("Module Information Acquisition"),

    LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION("UI Ability Initialization"),

    LIFE_CYCLE_NAME_ON_CREATE_EXECUTION("onCreate Execution"),

    LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK("Ability Lifecycle Callback"),

    LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE("Content and Page Loading");

    private final String value;

    CjLaunchPhaseEnum(String value) {
        this.value = value;
    }
}
