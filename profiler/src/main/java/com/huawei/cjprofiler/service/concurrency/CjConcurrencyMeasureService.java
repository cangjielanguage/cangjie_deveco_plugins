/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service.concurrency;

import com.huawei.cjprofiler.common.constant.CjConcurrencyConstants;
import com.huawei.cjprofiler.dao.CjThreadDao;
import com.huawei.cjprofiler.model.po.cjthread.CjThreadSliceInfoPo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadInfoVo;
import com.huawei.cjprofiler.model.vo.concurrency.MeasureDetailVo;
import com.huawei.cjprofiler.service.CjThreadState;
import com.huawei.deveco.insight.ohos.model.dto.ConcurrencyMeasureDuration;
import com.huawei.deveco.insight.ohos.model.dto.request.concurrency.QueryConcurrencyMeasureRequest;
import com.huawei.deveco.insight.ohos.model.vo.concurrency.TaskJumpDetailVo;
import com.huawei.deveco.insight.ohos.service.concurrency.ArkConcurrencyService;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CjConcurrencyMeasureService
 *
 * @since 2025-6-25
 */
public class CjConcurrencyMeasureService {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjConcurrencyMeasureService.class);

    /**
     * cjThreadId与 detail的映射关系
     */
    private static final HashMap<Integer, ConcurrencyMeasureDuration> cjThreadDetailMap = new HashMap<>();

    /**
     * cjThreadId与 name 的映射关系
     */
    private static final Map<Integer, String> cjThreadId2NameMap = new HashMap<>();

    private static volatile CjConcurrencyMeasureService instance = null;

    /**
     * constructor
     */
    private CjConcurrencyMeasureService() {
    }

    /**
     * CommonTraceService instance
     *
     * @return CommonTraceService
     */
    public static CjConcurrencyMeasureService getInstance() {
        if (instance == null) {
            synchronized (ArkConcurrencyService.class) {
                if (instance == null) {
                    instance = new CjConcurrencyMeasureService();
                }
            }
        }
        return instance;
    }

    /**
     * 查询measure
     *
     * @param request request
     * @return measure list
     */
    public List<MeasureDetailVo> queryConcurrencyMeasureList(QueryConcurrencyMeasureRequest request) {
        String unitName = request.getUnitName();
        if (CollectionUtils.isEmpty(request.getProcessIdList())) {
            return new ArrayList<>();
        }
        String sessionId = request.getSessionId();
        Long startTime = request.getStartTime();
        Long endTime = request.getEndTime();
        if (unitName.contentEquals(CjConcurrencyConstants.CONCURRENCY_CJ_THREAD)) {
            String measureName = request.getMeasureName();
            String state = CjThreadState.getValueByKey(measureName);
            List<CjThreadInfoVo> cjThreadInfoVoList = CjThreadDao.getInstance()
                    .queryCjThreadList(sessionId, startTime, endTime);
            cjThreadInfoVoList.forEach(cjThreadInfoVo -> {
                cjThreadId2NameMap.put(cjThreadInfoVo.getCjThreadId(), cjThreadInfoVo.getCjThreadName());
            });
            List<CjThreadSliceInfoPo> cjThreadSliceInfoPoList;
            if (CjThreadState.TOTAL.getKey().equals(measureName)) {
                cjThreadSliceInfoPoList = CjThreadDao.getInstance()
                        .queryCjThreadTotalLaneList(sessionId, startTime, endTime, request.getProcessIdList());
                return classifyCjThreadTotalDetail(cjThreadSliceInfoPoList);
            } else {
                cjThreadSliceInfoPoList = CjThreadDao.getInstance()
                        .queryCjThreadLaneList(sessionId, state, startTime, endTime, request.getProcessIdList());
                return parseCjThreadStateDetail(cjThreadSliceInfoPoList);
            }
        } else {
            // 当前有 FFRT, cjThread事件
            LOGGER.warn("measure unit name is invalid, unitName: {}", unitName);
            return new ArrayList<>();
        }
    }

    private List<MeasureDetailVo> classifyCjThreadTotalDetail(List<CjThreadSliceInfoPo> cjThreadSliceInfoPoList) {
        List<MeasureDetailVo> measureDetailVoList = new ArrayList<>();
        Map<String, List<CjThreadSliceInfoPo>> stateToCjThreadSliceInfoPoMap = cjThreadSliceInfoPoList.stream()
                .collect(Collectors.groupingBy(CjThreadSliceInfoPo::getState));
        for (CjThreadState cjThreadState : CjThreadState.getStateList()) {
            String state = cjThreadState.getValue();
            if (stateToCjThreadSliceInfoPoMap.containsKey(state)) {
                measureDetailVoList.add(addSingleStateSliceInfoList(state, stateToCjThreadSliceInfoPoMap.get(state)));
            }
        }
        return measureDetailVoList;
    }

    private List<MeasureDetailVo> parseCjThreadStateDetail(List<CjThreadSliceInfoPo> measureSliceInfoPos) {
        if (CollectionUtils.isEmpty(measureSliceInfoPos)) {
            return new ArrayList<>();
        }
        for (var data : measureSliceInfoPos) {
            ConcurrencyMeasureDuration durationInfo = cjThreadDetailMap.getOrDefault(data.getCjThreadId(),
                    new ConcurrencyMeasureDuration());
            setDuration(data, durationInfo, durationInfo.getTotalCnt(), durationInfo.getTotalDuration());
            cjThreadDetailMap.put(data.getCjThreadId(), durationInfo);
        }

        List<MeasureDetailVo> measureDetail = new ArrayList<>();
        for (Integer cjThreadId : cjThreadDetailMap.keySet()) {
            ConcurrencyMeasureDuration duration = cjThreadDetailMap.getOrDefault(cjThreadId,
                    new ConcurrencyMeasureDuration());
            if (duration == null) {
                continue;
            }
            addCjThreadStateDetail(cjThreadId, measureDetail, duration);
        }
        cjThreadDetailMap.clear();
        cjThreadId2NameMap.clear();
        return measureDetail;
    }

    private MeasureDetailVo addSingleStateSliceInfoList(String state,
                                                        List<CjThreadSliceInfoPo> cjStateThreadSliceInfoPoList) {
        List<MeasureDetailVo> measureDetail = new ArrayList<>();
        long totalAvgDuration = 0L;
        long totalMinDuration = Long.MAX_VALUE;
        long totalMaxDuration = 0L;
        long totalDuration = 0L;
        long totalCnt = 0L;
        for (CjThreadSliceInfoPo cjThreadSliceInfoPo : cjStateThreadSliceInfoPoList) {
            long duration = cjThreadSliceInfoPo.getDuration();
            ConcurrencyMeasureDuration info = getConcurrencyMeasureDuration(duration);
            totalCnt += 1;
            totalDuration += duration;
            totalMinDuration = Math.min(duration, totalMinDuration);
            totalMaxDuration = Math.max(duration, totalMaxDuration);
            addCjThreadStateDetail(cjThreadSliceInfoPo.getCjThreadId(), measureDetail, info);
        }
        MeasureDetailVo updatedMeasureDetail = new MeasureDetailVo();
        updatedMeasureDetail.setType(CjConcurrencyConstants.CONCURRENCY_NAME_TYPE_STATE);
        updatedMeasureDetail.setName(CjThreadState.getKeyByValue(state));
        updatedMeasureDetail.setTotalDuration(totalDuration);
        totalAvgDuration = totalDuration / totalCnt;
        updatedMeasureDetail.setAvgDuration(totalAvgDuration);
        updatedMeasureDetail.setMinDuration(totalMinDuration);
        updatedMeasureDetail.setMaxDuration(totalMaxDuration);
        updatedMeasureDetail.setChildren(measureDetail);
        return updatedMeasureDetail;
    }

    private static ConcurrencyMeasureDuration getConcurrencyMeasureDuration(long duration) {
        return ConcurrencyMeasureDuration.builder()
                .avgDuration(duration)
                .totalDuration(duration)
                .maxDuration(duration)
                .minDuration(duration)
                .build();
    }

    private void addCjThreadStateDetail(Integer cjThreadId, List<MeasureDetailVo> measureDetail,
                                        ConcurrencyMeasureDuration info) {
        TaskJumpDetailVo jumpInfo = new TaskJumpDetailVo();
        jumpInfo.setId(cjThreadId);
        jumpInfo.setName(cjThreadId2NameMap.get(cjThreadId));
        jumpInfo.setUnitName(CjConcurrencyConstants.CONCURRENCY_CJ_THREAD);
        measureDetail.add(MeasureDetailVo.builder()
                .type(CjConcurrencyConstants.CONCURRENCY_NAME_TYPE_CJ_THREAD)
                .name(cjThreadId2NameMap.get(cjThreadId))
                .cjThreadId(cjThreadId)
                .relatedTaskDetail(jumpInfo)
                .maxDuration(info.getMaxDuration())
                .avgDuration(info.getAvgDuration())
                .minDuration(info.getMinDuration())
                .totalDuration(info.getTotalDuration())
                .build());
    }

    private void setDuration(CjThreadSliceInfoPo data, ConcurrencyMeasureDuration durationInfo, long totalCnt,
                             long totalDuration) {
        durationInfo.setTotalCnt(totalCnt + 1);
        durationInfo.setTotalDuration(totalDuration + data.getDuration());
        durationInfo.setAvgDuration(durationInfo.getTotalDuration() / durationInfo.getTotalCnt());
        durationInfo.setState(data.getState());
        if (data.getDuration() > durationInfo.getMaxDuration()) {
            durationInfo.setMaxDuration(data.getDuration());
        }
        if (data.getDuration() < durationInfo.getMinDuration()) {
            durationInfo.setMinDuration(data.getDuration());
        }
    }
}
