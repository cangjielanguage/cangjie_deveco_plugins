/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.linemarker;

import com.huawei.cangjie.testframework.utils.TestUtil;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CangjieTestRunLineMakerProvider
 *
 * @since 2025/01/16
 */
public class CangjieTestRunLineMakerProvider extends RunLineMarkerContributor {
    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiElement) {
        if (!TestUtil.isSupportTestRun(psiElement)) {
            return null;
        }
        return RunLineMarkerContributor.withExecutorActions(AllIcons.RunConfigurations.TestState.Run);
    }
}
