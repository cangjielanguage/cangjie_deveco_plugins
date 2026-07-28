/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Tool for loading configuration files of Cangjie SDK.
 *
 * @since 2022 -02-08
 */
public enum CangjieSdkManagerPropertiesUtil {
    /**
     * Instance cangjie sdk manager properties util.
     */
    INSTANCE;

    private final Properties properties = new Properties();

    CangjieSdkManagerPropertiesUtil() {
        try (InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream("cangjie-sdk-manager.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            Logger.getInstance(CangjieSdkManagerPropertiesUtil.class)
                .warn("Failed to load cangjie-sdk-manager.properties");
        }
    }

    /**
     * getValue
     *
     * @param key key
     * @return value value
     */
    public String getValue(String key) {
        return properties.getProperty(key, "");
    }
}
