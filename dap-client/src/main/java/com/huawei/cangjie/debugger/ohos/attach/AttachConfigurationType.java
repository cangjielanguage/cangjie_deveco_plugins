/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.attach;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Attach configuration type
 *
 * @since 2022-12-5
 */
public final class AttachConfigurationType extends SimpleConfigurationType {
    private static final String MY_ATTACH_CONFIGURATION = "MyAttachConfiguration2";

    public AttachConfigurationType() {
        super(MY_ATTACH_CONFIGURATION, MY_ATTACH_CONFIGURATION, MY_ATTACH_CONFIGURATION,
            NotNullLazyValue.createValue(() -> Objects.requireNonNull(
                IconLoader.findIcon("/icons/single-device.png", AttachConfigurationType.class))));
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new AttachConfiguration(project, this);
    }
}
