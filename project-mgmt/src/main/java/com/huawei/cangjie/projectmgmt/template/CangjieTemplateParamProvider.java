/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.template;

import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.Map;

/**
 * The interface Cangjie param provider.
 *
 * @since 2025 -05-04
 */
public interface CangjieTemplateParamProvider {
    /**
     * env provider extension point
     */
    ExtensionPointName<CangjieTemplateParamProvider> TEMPLATE_PARAM_PROVIDER_EXTENSION_LIST =
        ExtensionPointName.create("com.huawei.cangjie.template.paramProvider");

    /**
     * collect need params
     *
     * @return params map
     */
    Map<String, Object> collectTemplateParams();
}
