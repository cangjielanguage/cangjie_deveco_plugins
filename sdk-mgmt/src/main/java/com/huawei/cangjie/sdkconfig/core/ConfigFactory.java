/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkconfig.core;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Config Factory
 *
 * @since 2025-07-03
 */
public final class ConfigFactory {
    private static final Map<Class<?>, Object> CONFIG_CACHE = new HashMap<>();

    private ConfigFactory() {}

    /**
     * create bean
     *
     * @param configInterface config interface
     * @param isHarmony is harmony
     * @param apiVersion api version
     * @return T clazz
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> create(Class<T> configInterface, boolean isHarmony, String apiVersion) {
        if (!configInterface.isInterface()) {
            return Optional.empty();
        }

        return Optional.of((T) CONFIG_CACHE.computeIfAbsent(configInterface, clazz ->
                Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[]{clazz},
                        new SdkConfigInvocationHandler(isHarmony, apiVersion)
                )
        ));
    }
}