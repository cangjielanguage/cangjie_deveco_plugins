/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.listener;

import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.build.ohos.api.CompileBuildListener;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;

import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * The type Cangjie run build listener.
 *
 * @since 2026 -04-23
 */
public class CangjieRunBuildListener implements CompileBuildListener {
    @Override
    public void buildStart(@NotNull Project project) {
        if (CommonProjectUtil.isSyncFinished(project)) {
            FileUtils.resetProfileFlag(project);
        }
    }

    @Override
    public void buildEnd(@NotNull Project project, boolean b) {
    }
}