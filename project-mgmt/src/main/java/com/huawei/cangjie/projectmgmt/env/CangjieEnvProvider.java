/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.env;

import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;

import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.Map;

/**
 * CangjieEnvProvider
 *
 * @since 2025/04/30
 */
public interface CangjieEnvProvider {
    /**
     * env provider extension point
     */
    ExtensionPointName<CangjieEnvProvider> ENV_PROVIDER_EXTENSION_LIST =
        ExtensionPointName.create("com.huawei.cangjie.envProvider");

    /**
     * collect need env
     *
     * @param projectModel projectModel
     * @return env map
     */
    Map<String, String> collectNeedEnv(ProjectModel projectModel);
}