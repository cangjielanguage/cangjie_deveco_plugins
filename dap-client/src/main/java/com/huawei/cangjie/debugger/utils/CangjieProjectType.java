/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

/**
 * cangjie project type
 *
 * @since 2024-05-20
 */
public enum CangjieProjectType {
    CANGJIE("Cangjie"),

    ARKTS_CANGJIE("ArkTsCangjie"),

    NOT_CANGJIE("NotCangjie");

    private final String projectType;

    /**
     * cangjie project type
     *
     * @param projectType project type
     */
    CangjieProjectType(String projectType) {
        this.projectType = projectType;
    }

    /**
     * get cangjie project type
     *
     * @param projectType  project type
     * @return cangjie project type
     */
    public static CangjieProjectType getCangjieProjectType(String projectType) {
        for (CangjieProjectType cangjieProjectType : CangjieProjectType.values()) {
            if (cangjieProjectType.projectType.equals(projectType)) {
                return cangjieProjectType;
            }
        }
        return NOT_CANGJIE;
    }
}

