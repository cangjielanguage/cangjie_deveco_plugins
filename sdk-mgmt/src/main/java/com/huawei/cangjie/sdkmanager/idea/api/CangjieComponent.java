/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.idea.api;

import com.huawei.cangjie.sdkconfig.support.SdkConfig;

import java.nio.file.Path;

/**
 * cangjie component
 *
 * @since 2024-04-12
 */
public class CangjieComponent {
    private Path location;

    private SdkConfig sdkConfig;

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public SdkConfig getSdkConfig() {
        return sdkConfig;
    }

    public void setSdkConfig(SdkConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
    }
}
