/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common.constant;

import java.util.regex.Pattern;

/**
 * LaunchTraceConstants
 *
 * @since 2025-04-25
 */
public class LaunchTraceConstants {
    /**
     * life cycle duration time max value
     */
    public static final Long MAX_LIFE_CYCLE_DURATION = -1L;

    /**
     * main phase number of launch
     */
    public static final Integer LAUNCH_MAIN_PHASE_NUM = 6;

    /**
     * app frame association rs frame key feature information
     */
    public static final String REGULAR_STRING_FRAME_TRACE_RELATE = "\\[\\d{1,19}\\,\\d{1,19}\\]?";

    /**
     * pattern of app frame association rs frame key feature information
     */
    public static final Pattern PATTERN_FRAME_TRACE_RELATE = Pattern.compile(REGULAR_STRING_FRAME_TRACE_RELATE);

    /**
     * launch trace of Process Creating
     */
    public static final String TRACE_START_ABILITY = "H:virtual int OHOS::AAFwk::AbilityManagerService::StartAbility(";

    /**
     * launch trace of Process Creating
     */
    public static final String TRACE_START_ABILITY_LOCKED = "H:int OHOS::AAFwk::MissionListManager::StartAbilityLocked";

    /**
     * launch trace of Process Creating
     */
    public static final String TRACE_LOAD_ABILITY = "virtual void OHOS::AppExecFwk::AppMgrServiceInner::LoadAbility";

    /**
     * launch trace of application launching
     */
    public static final String TRACE_ATTACH_APPLICATION =
        "H:virtual void OHOS::AppExecFwk::AppMgrServiceInner::AttachApplication";

    /**
     * launch trace of Load Extension
     */
    public static final String TRACE_LOAD_EXTENSION = "H:void OHOS::AppExecFwk::MainThread::LoadAllExtensions";

    /**
     * launch trace of Handle Launch Application
     */
    public static final String TRACE_HANDLE_LAUNCH = "H:void OHOS::AppExecFwk::MainThread::HandleLaunchApplication";

    /**
     * launch trace of ui ability launching
     */
    public static final String TRACE_LAUNCH_ABILITY = "H:void OHOS::AppExecFwk::MainThread::HandleLaunchAbility";

    /**
     * launch trace of ui ability on foreground
     */
    public static final String TRACE_FOREGROUND_ABILITY = "HandleAbilityTransaction";

    /**
     * launch trace of receive vsync
     */
    public static final String TRACE_RECEIVE_VSYNC = "H:ReceiveVsync";

    /**
     * launch trace of ui ability rendering
     */
    public static final String TRACE_APP_RENDERING = "transactionFlag:[";

    /**
     * launch trace of ui ability rendering
     */
    public static final String TRACE_APP_RENDERING_SEND_COMMANDS = "SendCommands";

    /**
     * launch trace of render service rendering
     */
    public static final String TRACE_RS_RENDERING = "H:RSMainThread::ProcessCommandUni";

    /**
     * launch trace of ability transaction
     */
    public static final String TRACE_ABILITY_TRANSACTION =
        "H:bool OHOS::AbilityRuntime::UIAbilityImpl::AbilityTransaction";

    /**
     * launch trace of cangjie runtime
     */
    public static final String TRACE_CANGJIE_RUNTIME = "Initialize cangjie runtime and namespace";

    /**
     * launch trace of load cangjie app library
     */
    public static final String TRACE_LOAD_CANGJIE_APP_LIBRARY = "OHOS::AbilityRuntime::CJRuntime::LoadCJAppLibrary";

    /**
     * launch trace of UIAbilityImpl start
     */
    public static final String TRACE_UI_START =
            "H:void OHOS::AbilityRuntime::UIAbilityImpl::Start(const AAFwk::Want &, sptr<AAFwk::SessionInfo>)";

    /**
     * process name of render service
     */
    public static final String PROCESS_RENDER_SERVICE = "render_service";

    /**
     * launch life cycle name of Process Creating
     */
    public static final String LIFE_CYCLE_NAME_PROCESS_CREATING = "Process Creating";

    /**
     * launch life cycle name of application launching
     */
    public static final String LIFE_CYCLE_NAME_APPLICATION_LAUNCHING = "Application Launching";

    /**
     * launch life cycle name of Runtime Initialization
     */
    public static final String LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION = "Runtime Initialization";

    /**
     * launch life cycle name of Load Extension
     */
    public static final String LIFE_CYCLE_NAME_LOAD_EXTENSION = "Load Extension";

    /**
     * launch life cycle name of Module Information Acquisition
     */
    public static final String LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION = "Module Information Acquisition";

    /**
     * launch life cycle name of ui ability launching
     */
    public static final String LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING = "UI Ability Launching";

    /**
     * launch life cycle name of .so File Loading
     */
    public static final String LIFE_CYCLE_NAME_LOADING_SO_FILE = ".so File Loading";

    /**
     * launch life cycle name of UI Ability Initialization
     */
    public static final String LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION = "UI Ability Initialization";

    /**
     * launch life cycle name of onCreate Execution
     */
    public static final String LIFE_CYCLE_NAME_ON_CREATE_EXECUTION = "onCreate Execution";

    /**
     * launch life cycle name of ui ability on foreground
     */
    public static final String LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND = "UI Ability OnForeground";

    /**
     * launch life cycle name of Ability Lifecycle Callback
     */
    public static final String LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK = "Ability Lifecycle Callback";

    /**
     * launch life cycle name of loadContent&loadPage
     */
    public static final String LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE = "Content and Page Loading";

    /**
     * launch life cycle name of ui ability rendering
     */
    public static final String LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING = "First Frame - App Phase";

    /**
     * launch life cycle name of first frame rendering
     */
    public static final String LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING = "First Frame - Render Phase";

    /**
     * launch life cycle name of START_ABILITY
     */
    public static final String LIFE_CYCLE_NAME_START = "Start";

    /**
     * launch life cycle name of ABILITY_ONBACKGROUND
     */
    public static final String LIFE_CYCLE_NAME_BACKGROUND = "Background";

    /**
     * launch life cycle name of TERMINATE_ABILITY
     */
    public static final String LIFE_CYCLE_NAME_TERMINATE = "Terminate";

    /**
     * launch life cycle name of ABILITY_ONINACTIVE
     */
    public static final String LIFE_CYCLE_NAME_INACTIVE = "Inactive";

    /**
     * launch life cycle name of ABILITY_ONACTIVE
     */
    public static final String LIFE_CYCLE_NAME_ACTIVE = "Active";
}
