/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.resources;

import com.intellij.DynamicBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * The type Cangjie project mgmt bundle.
 *
 * @since 2024 -11-17
 */
public class CangjieProjectMgmtBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE_NAME = "messages.CangjieProjectMgmtBundle";

    private static CangjieProjectMgmtBundle instance = null;

    /**
     * Instantiates a new Cangjie project mgmt bundle.
     */
    private CangjieProjectMgmtBundle() {
        super(BUNDLE_NAME);
    }

    /**
     * Gets singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized CangjieProjectMgmtBundle getSingletonInstance() {
        if (instance == null) {
            instance = new CangjieProjectMgmtBundle();
        }
        return instance;
    }

    /**
     * get bundle message
     *
     * @param key key of message
     * @param params params
     * @return value of message
     */
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
        @NotNull String... params) {
        return getSingletonInstance().getMessage(key, (Object) params);
    }
}
