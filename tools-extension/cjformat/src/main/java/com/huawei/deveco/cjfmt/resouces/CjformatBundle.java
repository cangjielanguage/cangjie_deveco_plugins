/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.resouces;

import com.intellij.DynamicBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * The type cjfmt  bundle.
 *
 * @since 2024 -11-17
 */
public class CjformatBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE_NAME = "messages.cjformatBundle";

    private static CjformatBundle instance = null;

    /**
     * Instantiates a new Cangjie project mgmt bundle.
     */
    private CjformatBundle() {
        super(BUNDLE_NAME);
    }

    /**
     * Gets singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized CjformatBundle getSingletonInstance() {
        if (instance == null) {
            instance = new CjformatBundle();
        }
        return instance;
    }
}
