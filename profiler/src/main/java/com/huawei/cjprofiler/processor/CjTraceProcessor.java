/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.service.common.CjCommonTraceService;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;

import com.alibaba.fastjson2.JSONObject;

/**
 * CjTraceProcessor
 *
 * @since 2025-6-25
 */
@RequestMapping(path = "cangjieProfiler.cjCommon")
public class CjTraceProcessor {
    /**
     * Delete trace-related session
     *
     * @param params parameters
     * @return Response<?>
     */
    @RequestMapping(path = "trace.deleteCjSession")
    public static Response<?> deleteTraceSession(JSONObject params) {
        return CjCommonTraceService.getInstance().deleteTraceSession(params);
    }
}
