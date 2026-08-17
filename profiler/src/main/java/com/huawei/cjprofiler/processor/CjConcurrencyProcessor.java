/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.service.concurrency.CjConcurrencyMeasureService;
import com.huawei.cjprofiler.service.concurrency.CjConcurrencyTaskService;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.model.dto.request.concurrency.QueryConcurrencyMeasureRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.concurrency.QueryConcurrencyProcessList;
import com.huawei.cjprofiler.model.dto.response.concurrency.ConcurrencyMeasureInfo;
import com.huawei.deveco.insight.ohos.model.dto.response.concurrency.ConcurrencyProcessListInfo;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;

import com.alibaba.fastjson2.JSONObject;

/**
 * ConcurrencyProcessor
 *
 * @since 2025/6/10
 */
@RequestMapping(path = "cangjieProfiler.cjConcurrency")
public class CjConcurrencyProcessor {
    /**
     * 查询Cj Concurrency Measure泳道框选详情
     *
     * @param params params
     * @return Concurrency Measure list
     */
    @RequestMapping(path = "lane.queryCjConcurrencyMeasureList")
    public static Response<?> queryConcurrencyMeasureList(JSONObject params) {
        QueryConcurrencyMeasureRequest request = JsonUtil.parseObject(params, QueryConcurrencyMeasureRequest.class);
        ValidateUtil.validate(request);
        return Response.success(new
            ConcurrencyMeasureInfo(CjConcurrencyMeasureService.getInstance().queryConcurrencyMeasureList(request)));
    }

    /**
     * 获取FFRT、TaskPool、NAPI、ArkTS、CjThread五个泳道的processList
     *
     * @param params params
     * @return processList
     */
    @RequestMapping(path = "concurrency.getCjConcurrencyProcessList")
    public static Response<?> getConcurrencyProcessList(JSONObject params) {
        QueryConcurrencyProcessList request = JsonUtil.parseObject(params, QueryConcurrencyProcessList.class);
        ValidateUtil.validate(request);
        return Response.success(
            new ConcurrencyProcessListInfo(CjConcurrencyTaskService.getInstance().getConcurrencyProcessList(request)));
    }
}
