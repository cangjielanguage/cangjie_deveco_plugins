/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.deveco.insight.ohos.utils.DocumentUtils;

import com.google.common.reflect.ClassPath;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 类路径工具类.
 *
 * @since 2025-11-11
 */
@Slf4j
public class ClassPathUtil {
    private static final String JAR_URL_SEPARATOR = "!/";

    /**
     * 查找 指定路径下 包含指定的任一注解的类，仅限于profiler内部使用.
     *
     * @param annotations 注解集合
     * @param rootResource 资源路径
     * @param rootPackage 包路径
     * @return 包含任一注解的类
     */
    public static List<Class<?>> findClassWithAnyAnnotation(Collection<Class<? extends Annotation>> annotations,
        String rootResource, String rootPackage) {
        List<Class<?>> projectClasses = findProjectClasses(rootResource, rootPackage);
        return projectClasses.stream()
            .filter(clazz -> annotations.stream().anyMatch(clazz::isAnnotationPresent))
            .collect(Collectors.toList());
    }

    private static List<Class<?>> findProjectClasses(String rootResource, String rootPackage) {
        try {
            // 根据项目的根路径创建URLClassLoader，考虑测试用例场景下并非以jar包形式存在的情况及多个jar包可能有相同目录
            Enumeration<URL> rootUrls = ClassPathUtil.class.getClassLoader().getResources(rootResource);
            List<Class<?>> projectClassList = new ArrayList<>();
            while (rootUrls.hasMoreElements()) {
                URL url = rootUrls.nextElement();
                projectClassList.addAll(findProjectClasses(rootResource, rootPackage, url));
            }
            return projectClassList;
        } catch (IOException e) {
            LOGGER.warn("Not found url for: {}.", rootResource);
            return Collections.emptyList();
        }
    }

    private static List<Class<?>> findProjectClasses(String rootResource, String rootPackage, URL rootUrl) {
        if (rootUrl == null) {
            LOGGER.warn("Not found url for: {}.", rootResource);
            return Collections.emptyList();
        }
        URL scanUrl = rootUrl;
        String classUrlPath = rootUrl.getPath();
        int jarUrlIndex = classUrlPath.lastIndexOf(JAR_URL_SEPARATOR);
        Set<ClassPath.ClassInfo> classInfoList;
        URLClassLoader classLoader = null;
        try {
            if (jarUrlIndex >= 0) {
                scanUrl = URI.create(classUrlPath.substring(0, jarUrlIndex)).toURL();
            }
            classLoader = URLClassLoader.newInstance(new URL[] {scanUrl},
                Thread.currentThread().getContextClassLoader());
            classInfoList = ClassPath.from(classLoader).getTopLevelClassesRecursive(rootPackage);
        } catch (IOException e) {
            LOGGER.warn("Failed to search project classes.", e);
            return Collections.emptyList();
        } finally {
            DocumentUtils.closeAllIo(classLoader);
        }

        List<Class<?>> projectClassList = new ArrayList<>();
        for (ClassPath.ClassInfo classInfo : classInfoList) {
            try {
                projectClassList.add(Class.forName(classInfo.getName()));
            } catch (NoClassDefFoundError | ClassNotFoundException error) {
                LOGGER.warn("No class: {}.", classInfo.getName());
            }
        }
        return projectClassList;
    }
}
