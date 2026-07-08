/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.resourcehandler;

import com.huawei.deveco.insight.ohos.resourcehandler.controller.HandlerMethod;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.MappingRegistration;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.MappingRegistry;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.cjprofiler.utils.ClassPathUtil;

import com.intellij.openapi.util.text.Strings;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 请求映射注册表管理.
 *
 * @since 2025-11-11
 */
@Slf4j
public class CangjieMappingRegistyManager {
    private static final String PATH_JOINER = "/";

    private static final String PROCESSOR_ROOT_PACKAGE = "com.huawei.cjprofiler.processor";

    private static final String PROCESSOR_ROOT_RESOURCE = "com/huawei/cjprofiler/processor";

    private static volatile CangjieMappingRegistyManager instance = null;

    private final MappingRegistry mappingRegistry = new MappingRegistry();

    private boolean isLoaded;

    private CangjieMappingRegistyManager() {
    }

    /**
     * 获取MappingRegistryManager实例.
     *
     * @return MappingRegistryManager实例
     */
    public static CangjieMappingRegistyManager getInstance() {
        if (instance == null) {
            synchronized (CangjieMappingRegistyManager.class) {
                if (instance == null) {
                    instance = new CangjieMappingRegistyManager();
                }
            }
        }
        return instance;
    }

    /**
     * 加载请求映射关系
     */
    public synchronized void loadRequestMapping() {
        if (!this.isLoaded) {
            LOGGER.info("Begin loading the processor classes from the Profiler.");
            List<Class<?>> controllerClassList = ClassPathUtil.findClassWithAnyAnnotation(
                Collections.singleton(RequestMapping.class), PROCESSOR_ROOT_RESOURCE, PROCESSOR_ROOT_PACKAGE);
            controllerClassList.forEach(this::loadRequestMappingForClass);
            this.isLoaded = true;
            LOGGER.info("Finished loading the processor classes from the Profiler.");
        }
    }

    private void loadRequestMappingForClass(Class<?> controllerClass) {
        RequestMapping classRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
        String parentPath = classRequestMapping.path();
        if (StringUtils.isEmpty(parentPath)) {
            LOGGER.warn("Class has no path. Class: {}.", controllerClass);
        } else {
            for (Method method : controllerClass.getMethods()) {
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                if (methodRequestMapping != null) {
                    if (StringUtils.isEmpty(methodRequestMapping.path())) {
                        LOGGER.warn("Method has no path. Class: {}, method: {}.", controllerClass, method.getName());
                    } else {
                        String completePath = parentPath + PATH_JOINER + methodRequestMapping.path();
                        if (this.mappingRegistry.containsMapping(completePath)) {
                            LOGGER.warn("Duplicate request mapping for: {}.", completePath);
                        } else {
                            MappingRegistration mappingRegistration = new MappingRegistration(
                                new HandlerMethod(controllerClass, method),
                                List.of(parentPath, methodRequestMapping.path()));
                            this.mappingRegistry.registerMapping(completePath, mappingRegistration);
                            LOGGER.debug("Register request mapping: {}.", completePath);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据完整路径获取处理方法.
     *
     * @param pathList 多级path列表
     * @return 处理方法
     */
    public Optional<HandlerMethod> getHandlerMethod(List<String> pathList) {
        String completePath = Strings.join(pathList, PATH_JOINER);
        return this.mappingRegistry.getHandlerMethod(completePath);
    }
}
