/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.component;

import com.huawei.cangjie.sdkmanager.idea.api.CangjieComponent;

import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.Map;

/**
 * The interface Cangjie component provider.
 *
 * @since 2025-05-04
 */
public interface CangjieComponentProvider {
    /**
     * The constant COMPONENT_PROVIDER_EXTENSION_LIST.
     */
    ExtensionPointName<CangjieComponentProvider> COMPONENT_PROVIDER_EXTENSION_LIST =
        ExtensionPointName.create("com.huawei.cangjie.sdkmanager.compontentProvider");

    /**
     * Collect cangjie component map.
     *
     * @param isHarmony the is harmony
     * @param apiVersion the api version
     * @return the map
     */
    Map<String, CangjieComponent> collectCangjieComponent(boolean isHarmony, int apiVersion);
}
