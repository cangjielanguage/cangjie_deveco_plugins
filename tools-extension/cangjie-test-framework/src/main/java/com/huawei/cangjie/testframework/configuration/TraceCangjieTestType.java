/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.configuration;

/**
 * TraceCangjieTestType
 *
 * @since 2025/02/20
 */
public enum TraceCangjieTestType {
    PACKAGE("package"), FILE("file"), CLASS("class"), METHOD("method"), BLANK("");

    private final String testType;

    TraceCangjieTestType(String testType) {
        this.testType = testType;
    }

    public String getTestType() {
        return testType;
    }
}
