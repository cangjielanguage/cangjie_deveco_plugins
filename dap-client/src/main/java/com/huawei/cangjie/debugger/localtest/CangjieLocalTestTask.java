/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.localtest;

import com.huawei.cangjie.debugger.localtest.debug.CangjieLocalTestDebug;

import java.util.concurrent.CompletableFuture;

/**
 * cangjie local test task
 *
 * @since 2025-08-31
 */
public class CangjieLocalTestTask {
    /**
     * debug
     *
     * @param localTestParam local test param
     * @param callback callback
     */
    public static void debug(LocalTestParam localTestParam, CompletableFuture<Boolean> callback) {
        CangjieLocalTestDebug cangjieLocalTestDebug = new CangjieLocalTestDebug();
        cangjieLocalTestDebug.debug(localTestParam, callback);
    }
}
