/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.idea.constants;

/**
 * ComponentPath
 *
 * @since 2024-04-12
 */
public enum CangjieComponentPath {
    CANGJIE("cangjie");

    private final String path;

    CangjieComponentPath(String path) {
        this.path = path;
    }

    /**
     * component path
     *
     * @return component path
     */
    public String value() {
        return path;
    }
}
