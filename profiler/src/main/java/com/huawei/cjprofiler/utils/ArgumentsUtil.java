/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;
import com.huawei.deveco.insight.ohos.utils.annotations.ParsedJson;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 用于参数校验及转换相关的工具类
 *
 * @since 2025-11-11
 */
public class ArgumentsUtil {
    /**
     * 将入参中带有@ParsedJson注解的JsonObject转换为实际的接收类型，并统一进行校验
     *
     * @param method 方法
     * @param args 参数
     */
    public static void parseJsonObject(Method method, Object[] args) {
        if (method == null || args == null) {
            throw new ProfilerException(ProfilerError.ILLEGAL_ARGUMENT);
        }
        Parameter[] parameters = method.getParameters();
        if (parameters.length != args.length) {
            throw new ProfilerException(ProfilerError.INCONSISTENT_PARAMETERS_ERROR);
        }
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(ParsedJson.class)) {
                Class<?> paramType = parameters[i].getType();
                if (args[i] instanceof JSONObject jsonObject) {
                    Object parsedObject = JsonUtil.parseObject(jsonObject, paramType);
                    ValidateUtil.validate(parsedObject);
                    args[i] = parsedObject;
                }
            }
        }
    }
}
