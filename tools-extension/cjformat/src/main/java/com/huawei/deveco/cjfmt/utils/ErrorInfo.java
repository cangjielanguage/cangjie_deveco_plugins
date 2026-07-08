/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.utils;

import lombok.Getter;

/**
 * Error Info enum
 *
 * @since 2024-03-13
 */
@Getter
public enum ErrorInfo {
    SDK_ERROR("Please check Cangjie SDK path.");

    private final String value;

    ErrorInfo(String value) {
        this.value = value;
    }
}
