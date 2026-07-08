/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.register;

import com.huawei.bitfun.intellij.register.CommonsStartupActivity;

import com.intellij.openapi.project.Project;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

import org.jetbrains.annotations.NotNull;

/**
 * CangjieStartupActivity
 *
 * @since 2022-11-30
 */
public class CangjieStartupActivity extends CommonsStartupActivity {
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        return super.execute(project, continuation);
    }
}
