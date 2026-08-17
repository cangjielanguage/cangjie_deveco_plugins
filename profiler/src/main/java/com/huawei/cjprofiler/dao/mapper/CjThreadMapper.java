/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.dao.mapper;

import com.huawei.cjprofiler.model.po.cjthread.CjThreadSliceInfoPo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadInfoVo;
import com.huawei.deveco.insight.ohos.model.vo.concurrency.ProcessListInfoVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * cangjie多线程 数据实现接口
 *
 * @since 2024-04-15
 */
public interface CjThreadMapper {
    /**
     * query state MetaData
     *
     * @param offset offset
     * @param startTime startTime
     * @param endTime endTime
     * @return state metadata
     */
    List<String> queryCjThreadState(@Param("offset") Long offset, @Param("startTime") Long startTime,
        @Param("endTime") Long endTime);

    /**
     * query CjThread Slice Info By Id
     *
     * @param cjThreadId cjThreadId
     * @param offset offset
     * @param startTime startTime
     * @param endTime endTime
     * @return state metadata
     */
    List<CjThreadSliceInfoPo> queryCjThreadSliceInfo(@Param("cjThreadId") Integer cjThreadId,
                                                     @Param("offset") Long offset,
                                                     @Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime);

    /**
     * query CjThread List
     *
     * @param offset offset
     * @param startTime startTime
     * @param endTime endTime
     * @return CjThreadInfoVo list
     */
    List<CjThreadInfoVo> queryCjThreadList(@Param("offset") Long offset, @Param("startTime") Long startTime,
                                           @Param("endTime") Long endTime);

    /**
     * query CjThread Lane List
     *
     * @param offset offset
     * @param state state
     * @param startTime startTime
     * @param endTime endTime
     * @param processList processList
     * @return CjThreadInfoVo list
     */
    List<CjThreadSliceInfoPo> queryCjThreadLaneList(@Param("offset") Long offset, @Param("state") String state,
        @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("list") List<Long> processList);

    /**
     * query CjThread Total Lane List
     *
     * @param offset offset
     * @param startTime startTime
     * @param endTime endTime
     * @param processList processList
     * @return CjThreadInfoVo list
     */
    List<CjThreadSliceInfoPo> queryCjThreadTotalLaneList(@Param("offset") Long offset,
        @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("list") List<Long> processList);

    /**
     * 通过sessionId和开始结束时间，查询CjThread全量processList信息
     *
     * @param startTime startTime
     * @param endTime endTime
     * @param offset offset
     * @return List<ExecuteJobDetailPo>
     */
    List<ProcessListInfoVo> queryCjThreadProcessList(@Param("startTime") long startTime,
                                                     @Param("endTime") long endTime, @Param("offset") long offset);
}
