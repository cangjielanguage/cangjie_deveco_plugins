/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.dao;

import com.huawei.cjprofiler.dao.mapper.CjThreadMapper;
import com.huawei.cjprofiler.model.po.cjthread.CjThreadSliceInfoPo;
import com.huawei.cjprofiler.model.vo.cjthread.CjThreadInfoVo;
import com.huawei.deveco.insight.ohos.model.vo.concurrency.ProcessListInfoVo;

import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * cangjie多线程 业务数据库实现
 *
 * @since 2025/04/22
 */
public class CjThreadDao extends AbstractDao {
    private static volatile CjThreadDao cjThreadDao = null;

    private CjThreadDao() {
    }

    /**
     * FrameDao getInstance
     *
     * @return FrameDao
     */
    public static CjThreadDao getInstance() {
        if (cjThreadDao == null) {
            synchronized (CjThreadDao.class) {
                if (cjThreadDao == null) {
                    cjThreadDao = new CjThreadDao();
                }
            }
        }
        return cjThreadDao;
    }

    @Override
    Class<?> relatedMapper() {
        return CjThreadMapper.class;
    }

    /**
     * query state metadata
     *
     * @param sessionId session id
     * @param startTime startTime
     * @param endTime   endTime
     * @return metadata list
     */
    public List<String> queryCjThreadState(String sessionId, Long startTime, Long endTime) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class)
                    .queryCjThreadState(getAbsoluteOffsetBySessionId(sessionId), startTime, endTime);
        }
    }

    /**
     * query CjThread Slice Info By Id
     *
     * @param sessionId  session id
     * @param cjThreadId cjThread Id
     * @param startTime  startTime
     * @param endTime    endTime
     * @return metadata list
     */
    public List<CjThreadSliceInfoPo> queryCjThreadSliceInfo(String sessionId, Integer cjThreadId, Long startTime,
                                                            Long endTime) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class)
                    .queryCjThreadSliceInfo(cjThreadId, getAbsoluteOffsetBySessionId(sessionId), startTime, endTime);
        }
    }

    /**
     * query CjThread List
     *
     * @param sessionId session id
     * @param startTime startTime
     * @param endTime   endTime
     * @return CjThreadInfoVo list
     */
    public List<CjThreadInfoVo> queryCjThreadList(String sessionId, Long startTime, Long endTime) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class)
                    .queryCjThreadList(getAbsoluteOffsetBySessionId(sessionId), startTime, endTime);
        }
    }

    /**
     * query CjThread Lane List
     *
     * @param sessionId     session id
     * @param state         state
     * @param startTime     startTime
     * @param endTime       endTime
     * @param processIdList processIdList
     * @return CjThreadLaneList list
     */
    public List<CjThreadSliceInfoPo> queryCjThreadLaneList(String sessionId, String state, Long startTime,
                                                           Long endTime, List<Long> processIdList) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class)
                    .queryCjThreadLaneList(getAbsoluteOffsetBySessionId(sessionId),
                            state, startTime, endTime, processIdList);
        }
    }

    /**
     * query CjThread Total Lane List
     *
     * @param sessionId     session id
     * @param startTime     startTime
     * @param endTime       endTime
     * @param processIdList processIdList
     * @return CjThreadLaneList list
     */
    public List<CjThreadSliceInfoPo> queryCjThreadTotalLaneList(String sessionId, Long startTime,
                                                                Long endTime, List<Long> processIdList) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class)
                    .queryCjThreadTotalLaneList(getAbsoluteOffsetBySessionId(sessionId),
                            startTime, endTime, processIdList);
        }
    }

    /**
     * 查询CjThread全量processList
     *
     * @param sessionId session id
     * @param startTime start time
     * @param endTime end time
     * @return List<ProcessListInfoVo>
     */
    public List<ProcessListInfoVo> queryCjThreadProcessList(String sessionId, Long startTime, Long endTime) {
        try (SqlSession sqlSession = obtainSqlSession(sessionId)) {
            return sqlSession.getMapper(CjThreadMapper.class).queryCjThreadProcessList(startTime, endTime,
                    getAbsoluteOffsetBySessionId(sessionId));
        }
    }
}
