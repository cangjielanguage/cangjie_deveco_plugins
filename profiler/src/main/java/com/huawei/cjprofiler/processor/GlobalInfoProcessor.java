/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.ability.global.CJPluginInfo;
import com.huawei.cjprofiler.service.GlobalInfoService;
import com.huawei.deveco.insight.ohos.ability.global.CountryInfo;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;

import com.alibaba.fastjson2.JSONObject;

/**
 * GlobalInfoProcessor
 *
 * @since 2025-04-07
 */
@RequestMapping(path = "cangjieProfiler.globalInfo")
public class GlobalInfoProcessor {
    /**
     * queryCountryCode
     *
     * @param params params
     * @return Response<?>
     */
    @RequestMapping(path = "globalInfo.getCountryCode")
    public static Response<?> queryCountryCode(JSONObject params) {
        return Response.success(new CountryInfo(GlobalInfoService.getInstance().getCountryCode()));
    }

    /**
     * queryCJPluginInfo
     *
     * @param params params
     * @return Response<?>
     */
    @RequestMapping(path = "globalInfo.getCJPluginInfo")
    public static Response<?> queryCJPluginInfo(JSONObject params) {
        return Response.success(new CJPluginInfo(GlobalInfoService.getInstance().hasCJPluginApplied()));
    }
}