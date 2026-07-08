/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

/**
 * CangjieModulePathType
 *
 * @since 2025/09/01
 */
public enum CangjieModulePathType {
    MAIN("main"),
    OHOS_TEST("ohosTest"),
    LOCAL_TEST("test");

    private final String path;

    CangjieModulePathType(String path) {
        this.path = path;
    }
}
