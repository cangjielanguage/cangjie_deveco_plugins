/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.processor;

import static com.huawei.deveco.insight.ohos.common.constant.ArkExecuteType.HEAP_USAGE_START;

import com.huawei.cjprofiler.dao.CjVmProfilerDao;
import com.huawei.cjprofiler.service.CjHeapUsageService;
import com.huawei.cjprofiler.service.CjRecordManager;
import com.huawei.deveco.insight.ohos.ability.device.IHarmonyDevice;
import com.huawei.deveco.insight.ohos.ability.device.OpenHarmonyDeviceManager;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.common.constant.ArkExecuteType;
import com.huawei.deveco.insight.ohos.common.constant.CommonConstants;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkAllHeapUsageRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkExecuteRequest;
import com.huawei.deveco.insight.ohos.model.dto.request.common.CommonQueryRequest;
import com.huawei.deveco.insight.ohos.model.dto.response.ark.ArkHeapUsagesDetail;
import com.huawei.deveco.insight.ohos.model.dto.response.ark.ArkHeapUsagesLane;
import com.huawei.deveco.insight.ohos.resourcehandler.controller.RequestMapping;
import com.huawei.deveco.insight.ohos.utils.JsonUtil;
import com.huawei.deveco.insight.ohos.utils.ValidateUtil;

import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * cangjie heap usage processor for handle service
 *
 * @since 2025-03-01
 */
@Slf4j
@RequestMapping(path = "cangjieProfiler.heapUsage")
public class CjHeapUsageProcessor {
    /**
     * Start heap usage
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "cj.startHeapUsage")
    public static Response<?> startHeapUsage(JSONObject params) {
        ArkExecuteRequest request = JsonUtil.parseObject(params, ArkExecuteRequest.class);
        ValidateUtil.validate(request);
        if (!CjVmProfilerDao.getInstance().registerMapper(request.getSessionId())) {
            LOGGER.warn("Failed to register CjVmProfilerMapper");
            return Response.failure(ProfilerError.SQL_ERROR);
        }
        Optional<IHarmonyDevice> device = OpenHarmonyDeviceManager.getInstance().getDeviceByKey(request.getDeviceKey());
        if (device.isEmpty() || CommonConstants.OFFLINE.equals(device.get().getStatus().getValue())) {
            LOGGER.warn("Failed to start cangjie heap usage, cause device is empty or offline.");
            return Response.failure(ProfilerError.DEVICE_EMPTY);
        }

        CjRecordManager cjRecordManager = CjRecordManager.getInstance();
        cjRecordManager.initData(ArkExecuteType.HEAP_PROFILER, request.getSessionId());
        cjRecordManager.creatCjRecordService(request.getPid());
        if (!cjRecordManager.executeCJTask(request, HEAP_USAGE_START, params)) {
            LOGGER.warn("Failed to start cangjie heap usage, cause cangjie vm is error.");
            return Response.failure(ProfilerError.PLUGIN_START_ERROR);
        }
        return Response.success();
    }

    /**
     * Stop heap usage
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "cj.stopHeapUsage")
    public static Response<?> stopHeapUsage(JSONObject params) {
        ArkExecuteRequest request = JsonUtil.parseObject(params, ArkExecuteRequest.class);
        ValidateUtil.validate(request);
        Optional<IHarmonyDevice> device = OpenHarmonyDeviceManager.getInstance().getDeviceByKey(request.getDeviceKey());
        if (device.isEmpty() || CommonConstants.OFFLINE.equals(device.get().getStatus().getValue())) {
            LOGGER.warn("Failed to stop cangjie heap usage, cause device is empty or offline.");
            return Response.failure(ProfilerError.DEVICE_EMPTY);
        }

        CjRecordManager cjRecordManager = CjRecordManager.getInstance();
        if (!cjRecordManager.executeCJTask(request, ArkExecuteType.HEAP_USAGE_STOP, params)) {
            LOGGER.warn("Failed to stop cangjie heap usage, cause cangjie vm is error.");
            return Response.failure(ProfilerError.PLUGIN_STOP_ERROR);
        }
        return Response.success();
    }

    /**
     * Query heap usage
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "cj.queryHeapUsages")
    public static Response<?> queryHeapUsage(JSONObject params) {
        CommonQueryRequest request = JsonUtil.parseObject(params, CommonQueryRequest.class);
        ValidateUtil.validate(request);
        return Response.success(new ArkHeapUsagesLane(CjHeapUsageService.getInstance().queryHeapUsage(request)));
    }

    /**
     * Query all heap usage
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "cj.queryAllHeapUsages")
    public static Response<?> queryAllHeapUsage(JSONObject params) {
        ArkAllHeapUsageRequest request = JsonUtil.parseObject(params, ArkAllHeapUsageRequest.class);
        if (!CjVmProfilerDao.getInstance().registerMapper(request.getSessionId())) {
            LOGGER.warn("Failed to register CjVmProfilerMapper");
            return Response.failure(ProfilerError.SQL_ERROR);
        }
        ValidateUtil.validate(request);
        return Response.success(new ArkHeapUsagesLane(CjHeapUsageService.getInstance().queryAllHeapUsage(request)));
    }

    /**
     * Query all heap usages summary
     *
     * @param params JSONObject
     * @return Response<?>
     */
    @RequestMapping(path = "cj.queryAllHeapUsagesDetail")
    public static Response<?> queryAllHeapUsagesDetail(JSONObject params) {
        ArkAllHeapUsageRequest request = JsonUtil.parseObject(params, ArkAllHeapUsageRequest.class);
        ValidateUtil.validate(request);
        return Response.success(
            new ArkHeapUsagesDetail(CjHeapUsageService.getInstance().queryAllHeapUsagesDetail(request)));
    }
}
