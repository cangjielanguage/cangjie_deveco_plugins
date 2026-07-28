/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.resourcehandler.controller;

import com.huawei.cjprofiler.resourcehandler.CangjieMappingRegistyManager;
import com.huawei.cjprofiler.utils.ArgumentsUtil;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.common.ProjectContext;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.common.constant.CommonConstants;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.HandlerMethod;
import com.huawei.deveco.insight.ohos.utils.BalloonNotification;
import com.huawei.deveco.insight.ohos.utils.ConfigBundle;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.text.StringUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.exceptions.PersistenceException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * 请求分发，用于接收前端CEF转过来的请求，借助{@link CangjieMappingRegistyManager}进行请求映射转发.
 *
 * @since 2025-11-11
 */
@Slf4j
public class RequestDispatcher {
    private static final int MAX_LOG_LENGTH = 2500;

    private static volatile RequestDispatcher requestDispatcher = null;

    private RequestDispatcher() {
    }

    /**
     * 获取RequestDispatcher实例
     *
     * @return RequestDispatcher实例
     */
    public static RequestDispatcher getInstance() {
        if (requestDispatcher == null) {
            synchronized (RequestDispatcher.class) {
                if (requestDispatcher == null) {
                    requestDispatcher = new RequestDispatcher();
                }
            }
        }
        return requestDispatcher;
    }

    /**
     * 调度请求.
     *
     * @param requestKey requestKey
     * @param requestMethod requestMethod
     * @param params   请求参数
     * @return 响应结果
     * @throws ProfilerException Profiler业务自己抛的异常
     */
    public Response<?> dispatch(String requestKey, String requestMethod, JSONObject params) throws ProfilerException {
        List<String> pathList = List.of(requestKey, requestMethod);
        Optional<HandlerMethod> optionalHandlerMethod = CangjieMappingRegistyManager.getInstance()
            .getHandlerMethod(pathList);
        if (optionalHandlerMethod.isEmpty()) {
            LOGGER.warn("No request handler for: {}.", pathList);
            return Response.nonCallback();
        }
        Method method = optionalHandlerMethod.get().getMethod();
        method.setAccessible(true);
        try {
            Object[] paramObjects = {params};
            ArgumentsUtil.parseJsonObject(method, paramObjects);
            Object result = method.invoke(null, paramObjects);
            if (result instanceof Response<?>) {
                return (Response<?>) result;
            } else {
                return Response.nonCallback();
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            logException(requestKey, requestMethod, e);
            return Response.failure(ProfilerError.SERVER_INTERNAL_ERROR);
        } catch (InvocationTargetException e) {
            // 获取原始异常
            Throwable cause = e.getCause();
            logException(requestKey, requestMethod, cause);
            if (cause instanceof ProfilerException profilerException) {
                return Response.failure(profilerException.getInsightError());
            }
            if (cause instanceof PersistenceException) {
                return Response.failure(ProfilerError.PERSISTENCE_ERROR);
            }
            if (cause instanceof OutOfMemoryError) {
                String errorMessage = ConfigBundle.getInstance().getBundleMessage("outOfMemory");
                BalloonNotification.show(errorMessage,
                    ConfigBundle.getInstance().getBundleMessage("moreInformationLink"), CommonConstants.OUT_OF_MEMORY,
                    NotificationType.ERROR, ProjectContext.getProject());
                return Response.failure(ProfilerError.SERVER_INTERNAL_ERROR);
            }
            return Response.failure(ProfilerError.SERVER_INTERNAL_ERROR);
        }
    }

    private void logException(String requestKey, String requestMethod, Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String errorMessage = sw.toString();
        if (StringUtil.isNotEmpty(errorMessage) && errorMessage.length() > MAX_LOG_LENGTH) {
            errorMessage = errorMessage.substring(0, MAX_LOG_LENGTH) + "...";
        }
        LOGGER.warn("Failed to process request. Key: {}, method: {}, error: {}", requestKey, requestMethod,
            errorMessage);
    }
}
