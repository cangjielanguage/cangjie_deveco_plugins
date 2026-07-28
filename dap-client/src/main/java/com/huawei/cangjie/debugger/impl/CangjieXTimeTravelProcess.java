/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.DapToServerService;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.timetravel.TimeTravelProcess;
import com.huawei.bitfun.intellij.timetravel.TimelineWebViewTab;
import com.huawei.bitfun.protocol.extend.ttd.CacheDataConfigArguments;
import com.huawei.cangjie.debugger.utils.FeatureEnableUtils;
import com.huawei.cangjie.debugger.utils.TimeTravelUtils;

import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * CppXTimeTravelProcess
 *
 * @since 2023-05-23
 */
public class CangjieXTimeTravelProcess<P extends DapXDebugProcess<F, T>, F extends DapFromServerService,
    T extends DapToServerService<?>> extends TimeTravelProcess<P, F, T> {
    private final Project project;

    private final boolean isUserSupportTimeTravel;

    /**
     * CppXTimeTravelProcess
     *
     * @param dapProcess dapProcess
     */
    public CangjieXTimeTravelProcess(@NotNull P dapProcess) {
        super(dapProcess);
        if (FeatureEnableUtils.IS_TIMELINE_VIEW_ENABLED) {
            timelineWebViewTab = new TimelineWebViewTab(dapProcess);
        }
        this.project = dapProcess.getProject();
        this.isUserSupportTimeTravel = TimeTravelUtils.getTimeTravel(project);
    }

    @Override
    protected boolean isUserSupportTimeTravel() {
        return isUserSupportTimeTravel;
    }

    @Override
    protected boolean isCodeSupportTimelineView() {
        return FeatureEnableUtils.IS_TIMELINE_VIEW_ENABLED;
    }

    @Override
    protected CacheDataConfigArguments getCacheDataConfigArgs() {
        CacheDataConfigArguments cacheDataConfigArgs = new CacheDataConfigArguments();
        cacheDataConfigArgs.setStartCacheData(isUserSupportTimeTravel());
        cacheDataConfigArgs.setSaveHistoryCache(true);
        if (FeatureEnableUtils.IS_SUPPORT_AUTO_SAVE) {
            cacheDataConfigArgs.setThreadCount(TimeTravelUtils.getIsAllThread(project) ? 0 : 1);
            cacheDataConfigArgs.setStackFrameCount(TimeTravelUtils.getFramesSize(project));
            cacheDataConfigArgs.setScopeType(TimeTravelUtils.getScopes(project));
            cacheDataConfigArgs.setVariablePageSize(TimeTravelUtils.getVariablesPageSize());
            cacheDataConfigArgs.setVariableChildrenLayers(TimeTravelUtils.getVariablesDepth(project) - 1);
            cacheDataConfigArgs.setVariableChildrenCount(TimeTravelUtils.getVariableChildrenCount(project));
        } else {
            cacheDataConfigArgs.setThreadCount(TimeTravelUtils.IS_ALL_THREADS_DEFAULT ? 0 : 1);
            cacheDataConfigArgs.setStackFrameCount(TimeTravelUtils.FRAMES_SIZE_DEFAULT);
            cacheDataConfigArgs.setScopeType(TimeTravelUtils.SCOPES_DEFAULT);
            cacheDataConfigArgs.setVariablePageSize(TimeTravelUtils.VARIABLE_PAGE_SIZE_DEFAULT);
            cacheDataConfigArgs.setVariableChildrenLayers(TimeTravelUtils.VARIABLES_DEPTH_DEFAULT - 1);
            cacheDataConfigArgs.setVariableChildrenCount(TimeTravelUtils.VARIABLE_CHILDREN_COUNT_DEFAULT);
        }
        return cacheDataConfigArgs;
    }
}
