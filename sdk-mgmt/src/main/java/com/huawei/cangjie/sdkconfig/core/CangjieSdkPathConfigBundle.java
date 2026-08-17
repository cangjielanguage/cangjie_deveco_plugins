/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkconfig.core;

import com.intellij.DynamicBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

/**
 * Cangjie Sdk Path Config Bundle
 *
 * @since 2025-07-03
 */
public class CangjieSdkPathConfigBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE_NAME = "cangjie-sdk-path";

    private static final CangjieSdkPathConfigBundle INSTANCE = new CangjieSdkPathConfigBundle();

    private CangjieSdkPathConfigBundle() {
        super(BUNDLE_NAME);
    }

    /**
     * message
     *
     * @param key key
     * @param params params
     * @return value
     */
    public static String sdkPathConf(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, String... params) {
        return INSTANCE.getMessage(key, params);
    }
}
