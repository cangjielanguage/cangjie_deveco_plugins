/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.dts2cj;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.deveco.common.buildsupport.view.DevEcoAbstractViewManager;

import com.intellij.build.progress.BuildProgress;
import com.intellij.build.progress.BuildProgressDescriptor;
import com.intellij.build.progress.BuildRootProgressImpl;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The type Dts 2 cj view manager.
 *
 * @since 2025-01-19
 */
public class Dts2cjViewManager extends DevEcoAbstractViewManager {
    /**
     * Instantiates a new Dts 2 cj view manager.
     *
     * @param project the project
     */
    public Dts2cjViewManager(Project project) {
        super(project);
    }

    @NotNull
    @Override
    public String getViewName() {
        return message("dts2cj.title.exec.output");
    }

    /**
     * Create build progress build progress.
     *
     * @param project the project
     * @return the build progress
     */
    @ApiStatus.Experimental
    public static BuildProgress<BuildProgressDescriptor> createBuildProgress(@NotNull Project project) {
        return new BuildRootProgressImpl(project.getService(Dts2cjViewManager.class));
    }
}
