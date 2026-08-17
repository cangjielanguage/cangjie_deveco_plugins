/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.dao.mapper;

import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapStats;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.HeapUsages;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * cangjie heap profiler mapper
 *
 * @since 2025/02/28/11:02
 */
public interface CjVmProfilerMapper {
    /**
     * selectCjHeapUsages
     *
     * @param tid tid
     * @param startTime long
     * @param endTime long
     * @return list List<HeapUsages>
     */
    List<HeapUsages> selectCjHeapUsages(@Param("tid") int tid, @Param("startTime") long startTime,
                                        @Param("endTime") long endTime);

    /**
     * selectCjHeapStats
     *
     * @param tid tid
     * @param startTime long
     * @param endTime long
     * @return list List<HeapStats>
     */
    List<HeapStats> selectCjHeapStats(@Param("tid") int tid, @Param("startTime") long startTime,
                                        @Param("endTime") long endTime);

    /**
     * selectCjHeapDumps
     *
     * @param startTime long
     * @param endTime long
     * @return list List<HeapSnapshot>
     */
    List<HeapSnapshot> selectCjHeapDumps(@Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * insertCjHeapUsages
     *
     * @param tid tid
     * @param heapUsageList List<HeapUsages>
     * @return result boolean
     */
    boolean insertCjHeapUsages(@Param("tid") int tid, @Param("list") List<HeapUsages> heapUsageList);

    /**
     * insertCjHeapStats
     *
     * @param tid tid
     * @param heapStatList List<HeapStats>
     * @return result boolean
     */
    boolean insertCjHeapStats(@Param("tid") int tid, @Param("list") List<HeapStats> heapStatList);

    /**
     * insertCjHeapDumps
     *
     * @param heapSnapshotList List<HeapSnapshot>
     * @return result boolean
     */
    boolean insertCjHeapDumps(@Param("list") List<HeapSnapshot> heapSnapshotList);
}
