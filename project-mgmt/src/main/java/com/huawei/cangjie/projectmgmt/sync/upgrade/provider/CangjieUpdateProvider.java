/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.provider;

import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.openapi.extensions.ExtensionPointName;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * The interface Cangjie upgrade provider.
 *
 * @since 2024 -11-05
 */
public interface CangjieUpdateProvider {
    /**
     * The constant UPGRADE_PROVIDER_EXTENSION_LIST.
     */
    ExtensionPointName<CangjieUpdateProvider> UPDATE_PROVIDER_EXTENSION_LIST =
        ExtensionPointName.create("com.huawei.cangjie.updateProvider");

    /**
     * Is need upgrade boolean.
     *
     * @param projectModel the project model
     * @param syncRequest the sync request
     * @return the boolean
     */
    boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest);

    /**
     * Gets upgrade message.
     *
     * @param projectModel the project model
     * @param syncRequest the sync request
     * @return the upgrade message
     */
    @NotNull
    Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest);

    /**
     * Do upgrade boolean.
     *
     * @param projectModel the project model
     * @param syncRequest the sync request
     * @return the boolean
     */
    boolean doUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest);
}
