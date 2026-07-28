/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.service.CjHiPerfService;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.model.dto.request.hiperf.HiPerfCallStackIcicleRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.hiperf.HiPerfMetaDataRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.hiperf.HiPerfNodeStateRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.hiperf.HiPerfThreadCallStackRequest;
import com.huawei.deveco.insight.ohos.model.dto.response.hiperf.HiPerfThreadCallStack;
import com.huawei.deveco.insight.ohos.model.vo.HiPerfMetadata;
import com.huawei.deveco.insight.ohos.model.vo.HiPerfNode;
import com.huawei.deveco.insight.ohos.model.vo.hiperf.HiPerfNodeStateVo;
import com.huawei.deveco.insight.ohos.processor.HiPerfProcessor;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.deveco.insight.ohos.service.HiPerfService;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;

import com.alibaba.fastjson2.JSONObject;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * CjHiPerfProcessor
 *
 * @since 2023-1-13
 */
@RequestMapping(path = "cangjieProfiler.handleCjHiPerf")
public class CjHiPerfProcessor {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjHiPerfProcessor.class);

    /**
     * 框选父泳道查询所有子泳道数据
     *
     * @param params 入参
     * @return Response<?>
     */
    @RequestMapping(path = "dfx.queryCjHiPerfCallStack")
    public static Response<?> queryCjHiPerfCallStack(JSONObject params) {
        HiPerfThreadCallStackRequest request = JsonUtil.parseObject(params, HiPerfThreadCallStackRequest.class);
        ValidateUtil.validate(request);
        long startRecordTime = TimeUnit.MILLISECONDS.toNanos(request.getStartRecordTime());
        long startTime = request.getStartTimestamp() + startRecordTime;
        long endTime = request.getEndTimestamp() + startRecordTime;
        Optional<HiPerfService> hiPerfServiceOptional = HiPerfProcessor.getInstance()
            .getHiPerfService(request.getSessionId());
        if (hiPerfServiceOptional.isEmpty()) {
            return Response.failure(ProfilerError.PARSE_HIPERF_ERROR);
        }
        HiPerfService hiPerfService = hiPerfServiceOptional.get();
        HiPerfMetadata metadata = hiPerfService.getFileMetadata();
        // The query parameter is a tid array.So the result is also an array.
        List<HiPerfNode> hiPerfNodeList = new ArrayList<>();
        LOGGER.info("Start query hiPerf thread callStack.");
        for (int tid : metadata.getTid()) {
            HiPerfNode threadNode = hiPerfService.searchData(startTime, endTime, tid);
            // 空类跳过
            if (threadNode.getThreadName() == null) {
                continue;
            }
            threadNode.setThreadName(threadNode.getThreadName() + " [" + tid + "]");
            Optional<HiPerfNode> hiPerfNodeOptional = CjHiPerfService.searchCangJieData(threadNode);
            hiPerfNodeOptional.ifPresent(hiPerfNodeList::add);
        }
        if (hiPerfNodeList.isEmpty()) {
            hiPerfNodeList.add(new HiPerfNode());
        }
        HiPerfThreadCallStack hiPerfThreadCallStack = new HiPerfThreadCallStack(hiPerfNodeList, metadata.getTid());
        return Response.success(hiPerfThreadCallStack);
    }

    /**
     * queryCjHiPerfNodeState
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "dfx.queryCjHiPerfNodeState")
    public static Response<?> queryCjHiPerfNodeState(JSONObject params) {
        HiPerfNodeStateRequest request = JsonUtil.parseObject(params, HiPerfNodeStateRequest.class);
        ValidateUtil.validate(request);
        Optional<HiPerfService> hiPerfService = HiPerfProcessor.getInstance().getHiPerfService(request.getSessionId());
        if (hiPerfService.isEmpty()) {
            return Response.failure(ProfilerError.PARSE_HIPERF_ERROR);
        }
        List<HiPerfNodeStateVo> hiPerfNodeStateVoList = hiPerfService.get().queryHiPerfNodeState(request);
        return Response.success(CjHiPerfService.selectCangJieStateVo(hiPerfNodeStateVoList));
    }

    /**
     * queryHiPerfThreadCallStack
     *
     * @param params params
     * @return Response<?>
     */
    @RequestMapping(path = "dfx.queryCjHiPerfThreadCallStack")
    public static Response<?> queryCjHiPerfThreadCallStack(JSONObject params) {
        String sessionId = params.getString("sessionId");
        String tidListStr = params.getString("tidList");
        List<Integer> tidList = JsonUtil.parseArray(tidListStr, Integer.class);
        if (sessionId == null || tidList == null) {
            LOGGER.warn("Failed to queryHiPerfThreadCallStack, invalid parameters, sessionId: {}, tidList: {}",
                sessionId, tidListStr);
            return Response.failure(ProfilerError.REQUEST_PARAMETER_ERROR);
        }
        long startTime = params.getLong("startTimestamp");
        long endTime = params.getLong("endTimestamp");
        long startRecordTime = TimeUnit.MILLISECONDS.toNanos(params.getLong("startRecordTime"));
        startTime = startTime + startRecordTime;
        endTime = endTime + startRecordTime;
        Optional<HiPerfService> hiPerfServiceOptional = HiPerfProcessor.getInstance().getHiPerfService(sessionId);
        if (hiPerfServiceOptional.isEmpty()) {
            return Response.failure(ProfilerError.PARSE_HIPERF_ERROR);
        }
        // The query parameter is a tid array.So the result is also an array.
        List<HiPerfNode> hiPerfNodeList = new ArrayList<>();
        for (Integer tid : tidList) {
            LOGGER.info("Query hiPerf thread callStack, tid: {}.", tid);
            HiPerfNode threadNode = hiPerfServiceOptional.get().searchData(startTime, endTime, tid);
            Optional<HiPerfNode> hiPerfNodeOptional = CjHiPerfService.searchCangJieData(threadNode);
            hiPerfNodeOptional.ifPresent(hiPerfNodeList::add);
        }
        return Response.success(new HiPerfThreadCallStack(hiPerfNodeList, tidList));
    }

    /**
     * queryCjHiPerfIcicleCallStack Native/Cj CallStack泳道冰锥图数据
     *
     * @param params params
     * @return Response<?>
     */
    @RequestMapping(path = "dfx.queryCjHiPerfIcicleCallStack")
    public static Response<?> queryCjHiPerfIcicleCallStack(JSONObject params) {
        HiPerfCallStackIcicleRequest request = JsonUtil.parseObject(params, HiPerfCallStackIcicleRequest.class);
        ValidateUtil.validate(request);
        Optional<HiPerfService> hiPerfServiceOptional = HiPerfProcessor.getInstance()
            .getHiPerfService(request.getSessionId());
        if (hiPerfServiceOptional.isEmpty()) {
            return Response.failure(ProfilerError.PARSE_HIPERF_ERROR);
        }
        List<HiPerfNode> hiPerfNodeList = hiPerfServiceOptional.get().searchIcicle(request);
        List<HiPerfNode> cjHiPerfNodeList = CjHiPerfService.searchIcicle(hiPerfNodeList);
        return Response.success(new HiPerfThreadCallStack(cjHiPerfNodeList, List.of(request.getTid())));
    }

    /**
     * queryCjHiPerfMetadata
     *
     * @param params params
     * @return Response<?>
     */
    @RequestMapping(path = "dfx.queryCjHiPerfMetadata")
    public static Response<?> queryCjHiPerfMetadata(JSONObject params) {
        HiPerfMetaDataRequest request = JsonUtil.parseObject(params, HiPerfMetaDataRequest.class);
        ValidateUtil.validate(request);
        Optional<HiPerfService> hiPerfServiceOptional = HiPerfProcessor.getInstance()
            .getHiPerfService(request.getSessionId());
        if (hiPerfServiceOptional.isEmpty()) {
            return Response.failure(ProfilerError.PARSE_HIPERF_ERROR);
        }
        HiPerfMetadata metadata = hiPerfServiceOptional.get().getFileMetadata();
        if (CollectionUtils.isEmpty(metadata.getCangJieTid())) {
            return Response.success(null);
        }
        com.huawei.deveco.insight.ohos.model.dto.response.hiperf.HiPerfMetadata hiPerfMetadata
            = com.huawei.deveco.insight.ohos.model.dto.response.hiperf.HiPerfMetadata.builder()
            .totalTime(metadata.getEndTs() - metadata.getStartTs())
            .pid(metadata.getPid())
            .threadCount(metadata.getTid().size())
            .tidList(metadata.getCangJieTid())
            .threadNameList(metadata.getCangJieThreadName())
            .build();
        HiPerfService.sortPerfMetadata(hiPerfMetadata, request.getProcessId());
        return Response.success(hiPerfMetadata);
    }
}
