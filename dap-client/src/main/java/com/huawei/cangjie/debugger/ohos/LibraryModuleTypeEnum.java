/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

/**
 * LibraryModuleTypeEnum.
 *
 * @since 2024-6-5
 */
public enum LibraryModuleTypeEnum {
    /**
     * Shared Library
     */
    SHARED_LIBRARY("shared"),
    /**
     * Static Library
     */
    STATIC_LIBRARY("har");


    private final String moduleType;

    LibraryModuleTypeEnum(String moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * getModuleType
     *
     * @return java.lang.String
     */
    public String getModuleType() {
        return moduleType;
    }
}
