/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.idea.bundle;

import com.intellij.DynamicBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Tool class for obtaining internationalization resources.
 *
 * @since 2024-3-13
 */
public class CangjieIdeaMessageBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE_NAME = "messages.CangjieSdkMgmtMessage";

    private static CangjieIdeaMessageBundle instance = null;

    private CangjieIdeaMessageBundle() {
        super(BUNDLE_NAME);
    }

    /**
     * Gets singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized CangjieIdeaMessageBundle getSingletonInstance() {
        if (instance == null) {
            instance = new CangjieIdeaMessageBundle();
        }
        return instance;
    }

    /**
     * message
     *
     * @param key key
     * @param params params
     * @return value
     */
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
        @NotNull String... params) {
        return getSingletonInstance().getMessage(key, params);
    }
}
