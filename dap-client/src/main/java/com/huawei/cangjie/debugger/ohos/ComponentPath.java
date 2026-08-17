/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

/**
 * component path
 *
 * @since 2024-1-15
 */
public enum ComponentPath {
    CANGJIE("cangjie");

    private final String path;

    private ComponentPath(String path) {
        this.path = path;
    }

    public String value() {
        return this.path;
    }
}
