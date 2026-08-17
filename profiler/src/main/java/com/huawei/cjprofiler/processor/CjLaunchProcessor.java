/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.service.launch.CjLaunchTraceService;
import com.huawei.deveco.insight.ohos.model.dto.request.launch.LaunchLifeCycleRequest;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;
import com.huawei.deveco.insight.ohos.model.dto.response.launch.LaunchLifeCycleLane;
import com.huawei.deveco.insight.ohos.model.dto.response.launch.LaunchThreadMetaData;
import com.huawei.deveco.insight.ohos.utils.singleton.SingletonContainer;

import com.alibaba.fastjson2.JSONObject;

/**
 * LaunchProcessor
 *
 * @since 2025/06/11
 */
@RequestMapping(path = "cangjieProfiler.cjLaunch")
public class CjLaunchProcessor {
    /**
     * query launch life cycle lane
     *
     * @param params JSONObject
     * @return Response.success(new LaunchLifeCycleLane(xxx))
     */
    @RequestMapping(path = "launch.queryCjLaunchLifeCycleLane")
    public static Response<?> queryLaunchLifeCycleLane(JSONObject params) {
        LaunchLifeCycleRequest request = JsonUtil.parseObject(params, LaunchLifeCycleRequest.class);
        ValidateUtil.validate(request);
        return Response.success(new LaunchLifeCycleLane(SingletonContainer.getInstance(CjLaunchTraceService.class)
                .queryLaunchLifeCycleLane(request)));
    }

    /**
     * query launch thread metadata
     *
     * @param params JSONObject
     * @return Response.success(new LaunchThreadMetadata ( xxx))
     */
    @RequestMapping(path = "launch.queryCjLaunchThreadMetadata")
    public static Response<?> queryLaunchThreadMetadata(JSONObject params) {
        LaunchLifeCycleRequest request = JsonUtil.parseObject(params, LaunchLifeCycleRequest.class);
        ValidateUtil.validate(request);
        return Response.success(
            new LaunchThreadMetaData(SingletonContainer.getInstance(CjLaunchTraceService.class)
                .queryLaunchThreadMetadata(request)));
    }
}
