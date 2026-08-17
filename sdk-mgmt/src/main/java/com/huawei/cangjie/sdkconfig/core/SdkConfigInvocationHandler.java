/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkconfig.core;

import static com.huawei.cangjie.sdkconfig.configkeys.SdkKeys.BUILD_TOOLS_ROOT_PATH;
import static com.huawei.cangjie.sdkconfig.configkeys.SdkKeys.SDK_TYPE;

import com.huawei.cangjie.sdkconfig.annotations.SdkConfigKey;
import com.huawei.cangjie.sdkmanager.util.CangjieSdkUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sdk Config Invocation Handler
 *
 * @since 2025-07-03
 */
class SdkConfigInvocationHandler implements InvocationHandler {
    private String sdkRootPath = "";

    private boolean isHarmony = true;

    private String apiVersion = "-1";

    public SdkConfigInvocationHandler(boolean isHarmony, String apiVersion) {
        this.isHarmony = isHarmony;
        this.apiVersion = apiVersion;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        try {
            SdkConfigKey annotation = method.getAnnotation(SdkConfigKey.class);
            if (annotation == null) {
                throw new IllegalStateException("Method must be annotated with @ConfigKey: " + method.getName());
            }
            String configKey = annotation.value();
            String buildToolsRootPath = CangjieSdkPathConfigBundle.sdkPathConf(BUILD_TOOLS_ROOT_PATH);
            String sdkType = CangjieSdkPathConfigBundle.sdkPathConf(SDK_TYPE);
            String relativePath = CangjieSdkPathConfigBundle.sdkPathConf(configKey, buildToolsRootPath, sdkType);
            if (this.sdkRootPath.isEmpty()) {
                sdkRootPath = CangjieSdkUtil.getCangjieSdkPath(isHarmony, apiVersion);
            }
            return joinAndNormalizePath(sdkRootPath, relativePath);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private String joinAndNormalizePath(String prefix, String relativePath) {
        Path prefixPath = Paths.get(prefix == null ? "" : prefix);
        Path combinedPath = prefixPath.resolve(relativePath == null ? "" : relativePath);
        Path normalizedPath = combinedPath.normalize();
        return normalizedPath.toString();
    }
}