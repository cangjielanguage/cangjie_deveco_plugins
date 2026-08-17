/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

/**
 * ExecutorInfo
 *
 * @since 2015-11-6
 */
public class ExecutorInfo {
    @Getter
    @NotNull
    private final String id;

    @Getter
    @NotNull
    private final String actionName;

    public ExecutorInfo(@NotNull String id, @NotNull String actionName) {
        this.id = id;
        this.actionName = actionName;
    }
}
