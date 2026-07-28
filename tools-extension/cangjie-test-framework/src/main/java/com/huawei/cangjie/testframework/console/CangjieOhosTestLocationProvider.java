/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.console;

import com.huawei.cangjie.testframework.utils.TestUtil;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * CangjieOhosTestLocationProvider
 *
 * @since 2025/09/01
 */
public class CangjieOhosTestLocationProvider implements SMTestLocator {
    /**
     * CangjieOhosTest
     */
    public static final String PROTOCOL_ID = "CangjieOhosTest";

    /**
     * CangjieOhosTestLocationProvider instance
     */
    public static final CangjieOhosTestLocationProvider INSTANCE = new CangjieOhosTestLocationProvider();

    @Override
    @NotNull
    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project,
                                               @NotNull GlobalSearchScope scope) {
        if (!PROTOCOL_ID.equals(protocol)) {
            return Collections.EMPTY_LIST;
        }
        PsiElement element = TestUtil.getTestLocation(protocol);
        if (element == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.singletonList(new PsiLocation<>(project, element));
    }
}
