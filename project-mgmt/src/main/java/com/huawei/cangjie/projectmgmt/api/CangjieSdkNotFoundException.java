/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.api;

/**
 * CangjieSdkNotFoundException
 *
 * @since 2023-3-18
 */
public class CangjieSdkNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CangjieSdkNotFoundException(String msg) {
        super(msg);
    }
}
