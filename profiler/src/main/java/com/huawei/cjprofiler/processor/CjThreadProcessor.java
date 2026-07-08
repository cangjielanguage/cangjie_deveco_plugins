/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import com.huawei.cjprofiler.dao.CjThreadDao;
import com.huawei.cjprofiler.model.dto.request.CjThreadSliceRequest;
import com.huawei.cjprofiler.model.dto.request.cjthread.CjThreadListRequest;
import com.huawei.cjprofiler.model.dto.request.cjthread.CjThreadMetadataRequest;
import com.huawei.cjprofiler.model.dto.request.cjthread.CjThreadSliceInfoRequest;
import com.huawei.cjprofiler.model.dto.response.cjthread.CjLaneSliceData;
import com.huawei.cjprofiler.model.dto.response.cjthread.CjThreadInfoData;
import com.huawei.cjprofiler.model.dto.response.cjthread.CjThreadSliceInfoListData;
import com.huawei.cjprofiler.model.dto.response.cjthread.CjThreadStateMetadata;
import com.huawei.cjprofiler.model.vo.CjLaneSliceVo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadInfoVo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadSliceInfoVo;
import com.huawei.cjprofiler.service.CjThreadService;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.annotations.ParsedJson;

import java.util.List;

/**
 * cangjie多线程 业务实现类
 *
 * @since 2025/04/22
 */
@RequestMapping(path = "cangjieProfiler.cjThread")
public class CjThreadProcessor {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjThreadProcessor.class);

    /**
     * 查询仓颉协程状态信息
     *
     * @param request CjThreadMetadataRequest
     * @return Response.success(metadata list)
     */
    @RequestMapping(path = "cjThread.queryCjStateMetadata")
    public static Response<?> queryCjStateMetadata(@ParsedJson CjThreadMetadataRequest request) {
        if (!CjThreadDao.getInstance().registerMapper(request.getSessionId())) {
            LOGGER.warn("Failed to register CjThreadMapper");
            return Response.failure(ProfilerError.SQL_ERROR);
        }
        List<String> metadataList = CjThreadService.getInstance()
            .queryCjStateMetadata(request.getSessionId(), request.getStartTime(), request.getEndTime());
        return Response.success(new CjThreadStateMetadata(metadataList));
    }

    /**
     * 查询某一个Thread的调度状态
     *
     * @param request CjThreadSliceInfoRequest
     * @return Response.success(List < CjThreadSliceInfo >)
     */
    @RequestMapping(path = "cjThread.queryCjThreadSliceInfo")
    public static Response<?> queryCjThreadSliceInfo(@ParsedJson CjThreadSliceInfoRequest request) {
        List<CjThreadSliceInfoVo> cjThreadStateList = CjThreadService.getInstance()
            .queryCjThreadSliceInfo(request.getSessionId(), request.getCjThreadId(), request.getStartTime(),
                request.getEndTime());
        return Response.success(new CjThreadSliceInfoListData(cjThreadStateList));
    }

    /**
     * 查询有多少个仓颉协程，Thread id，name
     *
     * @param request CjThreadListRequest
     * @return Response.success(List < CjThreadInfoVo >)
     */
    @RequestMapping(path = "cjThread.queryCjThreadList")
    public static Response<?> queryCjThreadList(@ParsedJson CjThreadListRequest request) {
        List<CjThreadInfoVo> cjThreadInfoList = CjThreadService.getInstance()
            .queryCjThreadList(request.getSessionId(), request.getStartTime(), request.getEndTime());
        return Response.success(new CjThreadInfoData(cjThreadInfoList));
    }

    /**
     * 查询cjThread各泳道图像
     *
     * @param request CjThreadSliceRequest
     * @return Response.success(List < CjLaneSliceData >)
     */
    @RequestMapping(path = "cjThread.queryCjThreadLaneList")
    public static Response<?> queryCjThreadLaneList(@ParsedJson CjThreadSliceRequest request) {
        List<CjLaneSliceVo> laneSliceInfo = CjThreadService.getInstance()
            .queryCjThreadLaneList(request.getSessionId(), request.getStateName(), request.getStartTime(),
                request.getEndTime(), request.getProcessIdList());
        return Response.success(new CjLaneSliceData(laneSliceInfo));
    }
}
