/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.cjprofiler.dao.CjThreadDao;
import com.huawei.cjprofiler.model.po.cjthread.CjThreadSliceInfoPo;
import com.huawei.cjprofiler.model.vo.CjLaneSliceVo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadInfoVo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadSliceInfoVo;
import com.huawei.deveco.insight.ohos.model.vo.concurrency.TaskThreadDetailVo;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * cangjie多线程 业务实现类
 *
 * @since 2025/04/22
 */
public class CjThreadService {
    private static volatile CjThreadService cjThreadService = null;

    /**
     * FrameService instance
     *
     * @return FrameService
     */
    public static CjThreadService getInstance() {
        if (cjThreadService == null) {
            synchronized (CjThreadService.class) {
                if (cjThreadService == null) {
                    cjThreadService = new CjThreadService();
                }
            }
        }
        return cjThreadService;
    }

    /**
     * query state metadata
     *
     * @param sessionId session id
     * @param startTime startTime
     * @param endTime endTime
     * @return stateMetaData list
     */
    public List<String> queryCjStateMetadata(String sessionId, Long startTime, Long endTime) {
        List<String> stateMetaDataPo = CjThreadDao.getInstance().queryCjThreadState(sessionId, startTime, endTime);
        List<String> stateMetaData = new ArrayList<>();
        for (CjThreadState state : CjThreadState.getStateList()) {
            if (stateMetaDataPo.contains(state.getValue())) {
                stateMetaData.add(state.getKey());
            }
        }
        if (CollectionUtils.isNotEmpty(stateMetaData)) {
            stateMetaData.add(0, CjThreadState.TOTAL.getKey());
        }
        return stateMetaData;
    }

    /**
     * query CjThread Slice Info
     *
     * @param sessionId session id
     * @param cjThreadId cjThreadId
     * @param startTime startTime
     * @param endTime endTime
     * @return CjThreadInfoVo list
     */
    public List<CjThreadSliceInfoVo> queryCjThreadSliceInfo(String sessionId, Integer cjThreadId, Long startTime,
                                                            Long endTime) {
        List<CjThreadSliceInfoPo> cjThreadSliceInfoVoList = CjThreadDao.getInstance()
            .queryCjThreadSliceInfo(sessionId, cjThreadId, startTime, endTime);
        return parseCjThreadSliceInfoVoList(cjThreadSliceInfoVoList);
    }

    private List<CjThreadSliceInfoVo> parseCjThreadSliceInfoVoList(List<CjThreadSliceInfoPo> cjThreadSliceInfoVoList) {
        if (CollectionUtils.isEmpty(cjThreadSliceInfoVoList)) {
            return new ArrayList<>();
        }
        List<CjThreadSliceInfoVo> cjThreadSliceInfoList = new ArrayList<>();
        for (CjThreadSliceInfoPo cjThreadSliceInfoVo : cjThreadSliceInfoVoList) {
            TaskThreadDetailVo threadInfo = new TaskThreadDetailVo();
            threadInfo.setThreadId(cjThreadSliceInfoVo.getThreadId());
            threadInfo.setProcessId(cjThreadSliceInfoVo.getProcessId());
            cjThreadSliceInfoList.add(CjThreadSliceInfoVo.builder()
                .state(CjThreadState.getKeyByValue(cjThreadSliceInfoVo.getState()))
                .startTime(cjThreadSliceInfoVo.getStartTime())
                .endTime(cjThreadSliceInfoVo.getEndTime())
                .duration(cjThreadSliceInfoVo.getDuration())
                .threadId(cjThreadSliceInfoVo.getCjThreadId())
                .relateThreadDetail(threadInfo)
                .build());
        }
        return cjThreadSliceInfoList;
    }

    /**
     * query CjThread List
     *
     * @param sessionId session id
     * @param startTime startTime
     * @param endTime endTime
     * @return CjThreadInfoVo list
     */
    public List<CjThreadInfoVo> queryCjThreadList(String sessionId, Long startTime, Long endTime) {
        return CjThreadDao.getInstance().queryCjThreadList(sessionId, startTime, endTime);
    }

    /**
     * query CjThread Lane List
     *
     * @param sessionId session id
     * @param stateName stateName
     * @param startTime startTime
     * @param endTime endTime
     * @param processIdList processIdList
     * @return LaneSliceVo list
     */
    public List<CjLaneSliceVo> queryCjThreadLaneList(String sessionId, String stateName, Long startTime, Long endTime,
                                                     List<Long> processIdList) {
        if (CollectionUtils.isEmpty(processIdList)) {
            return new ArrayList<>();
        }
        List<CjThreadSliceInfoPo> threadSliceInfoPoList;
        if (CjThreadState.TOTAL.getKey().equals(stateName)) {
            threadSliceInfoPoList = CjThreadDao.getInstance()
                    .queryCjThreadTotalLaneList(sessionId, startTime, endTime, processIdList);
        } else {
            String state = CjThreadState.getValueByKey(stateName);
            threadSliceInfoPoList = CjThreadDao.getInstance()
                .queryCjThreadLaneList(sessionId, state, startTime, endTime, processIdList);
        }
        List<Long> start = threadSliceInfoPoList.stream().map(CjThreadSliceInfoPo::getStartTime).toList();
        List<Long> end = threadSliceInfoPoList.stream().map(CjThreadSliceInfoPo::getEndTime).sorted().toList();
        return getCjThreadLaneSliceInfoVos(start, end, threadSliceInfoPoList.size());
    }

    /**
     * getCjThreadLaneSliceInfoVos
     *
     * @param start start
     * @param end end
     * @param size size
     * @return List<CjLaneSliceVo>
     */
    private List<CjLaneSliceVo> getCjThreadLaneSliceInfoVos(List<Long> start, List<Long> end, int size) {
        // 开始对measure在当前时间戳内的执行时间次数做统计
        // 对measure task开始时间、结束时间从小到大做顺序排序后，再将其顺序合并
        // 合并的同时如果是startTime则当前时间戳次数+1，endTime则-1
        List<CjLaneSliceVo> measureSliceInfoVos = new ArrayList<>();
        int value = 0;
        int startThreadValue = 0;
        int endThreadValue = 0;
        while (startThreadValue < size || endThreadValue < size) {
            CjLaneSliceVo measureSliceInfoVo = new CjLaneSliceVo();
            if (startThreadValue == size) {
                measureSliceInfoVo.setTimeStamp(end.get(endThreadValue));
                measureSliceInfoVo.setValue(--value);
                endThreadValue++;
            } else if (endThreadValue == size) {
                measureSliceInfoVo.setTimeStamp(start.get(startThreadValue));
                measureSliceInfoVo.setValue(++value);
                startThreadValue++;
            } else if (start.get(startThreadValue) < end.get(endThreadValue)) {
                measureSliceInfoVo.setTimeStamp(start.get(startThreadValue));
                measureSliceInfoVo.setValue(++value);
                startThreadValue++;
            } else {
                measureSliceInfoVo.setTimeStamp(end.get(endThreadValue));
                measureSliceInfoVo.setValue(--value);
                endThreadValue++;
            }
            measureSliceInfoVos.add(measureSliceInfoVo);
        }
        return measureSliceInfoVos;
    }
}
