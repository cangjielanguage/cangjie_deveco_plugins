/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.resourcehandler;

import com.huawei.cjprofiler.common.constant.CangJieConstant;
import com.huawei.cjprofiler.model.dto.JcefRequest;
import com.huawei.cjprofiler.resourcehandler.controller.RequestDispatcher;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.resourcehandler.DispatchExtPoint;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;

/**
 * JCEF消息路由adapter
 *
 * @since 2025/11/11
 */
public class CangjieProfilerDispatchImp implements DispatchExtPoint {
    @Override
    public boolean handleRequest(CefBrowser browser, CefFrame frame, long queryId, String request,
        CefQueryCallback callback) {
        CangjieMappingRegistyManager.getInstance().loadRequestMapping();
        JcefRequest jcefRequest = JsonUtil.parseObject(request, JcefRequest.class);
        ValidateUtil.validate(jcefRequest);

        String requestKey = jcefRequest.getKey();
        if (!requestKey.contains(CangJieConstant.CANGJIE_PROFILER_DISPATCH_KEY)) {
            return false;
        }
        String requestMethod = jcefRequest.getData().getMethod();
        JSONObject params = jcefRequest.getData().getParams();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Response<?> response = RequestDispatcher.getInstance().dispatch(requestKey, requestMethod, params);
            if (response.isNeedCallback()) {
                callback.success(new Gson().toJson(response));
            }
        });
        return true;
    }
}
