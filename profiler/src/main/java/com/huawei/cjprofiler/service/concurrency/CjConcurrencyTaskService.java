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
import com.huawei.deveco.insight.ohos.ability.concurrency.ConcurrencyHelper;
import com.huawei.deveco.insight.ohos.common.constant.ConcurrencyConstants;
import com.huawei.deveco.insight.ohos.dao.ConcurrencyDao;
import com.huawei.deveco.insight.ohos.model.dto.request.concurrency.QueryConcurrencyProcessList;
import com.huawei.deveco.insight.ohos.model.vo.concurrency.ProcessListInfoVo;
import com.huawei.deveco.insight.ohos.service.concurrency.ArkConcurrencyService;

import java.util.ArrayList;
import java.util.List;

/**
 * CjConcurrencyTaskService
 *
 * @since 2025-6-25
 */
public class CjConcurrencyTaskService {
    private static volatile CjConcurrencyTaskService instance = null;

    /**
     * constructor
     */
    private CjConcurrencyTaskService() {
    }

    /**
     * CommonTraceService instance
     *
     * @return CommonTraceService
     */
    public static CjConcurrencyTaskService getInstance() {
        if (instance == null) {
            synchronized (ArkConcurrencyService.class) {
                if (instance == null) {
                    instance = new CjConcurrencyTaskService();
                }
            }
        }
        return instance;
    }

    /**
     * 查询 FFRT/TaskPool/NAPI/ArkTS的processList
     *
     * @param request request
     * @return process list
     */
    public List<ProcessListInfoVo> getConcurrencyProcessList(QueryConcurrencyProcessList request) {
        return switch (request.getType()) {
            case ConcurrencyConstants.CONCURRENCY_FFRT -> ConcurrencyDao.getInstance()
                .queryFfrtProcessList(request.getSessionId(), request.getStartTime(), request.getEndTime());
            case ConcurrencyConstants.CONCURRENCY_NAPI -> ConcurrencyDao.getInstance()
                .queryNapiProcessList(request.getSessionId(), request.getStartTime(), request.getEndTime());
            case ConcurrencyConstants.CONCURRENCY_ARK_TS ->
                ConcurrencyHelper.arkProcessInfoPoToVo(ConcurrencyDao.getInstance()
                .queryArkTSProcessList(request.getSessionId(), request.getStartTime(), request.getEndTime()));
            case ConcurrencyConstants.CONCURRENCY_TASKPOOL -> ConcurrencyDao.getInstance()
                .queryTaskPoolProcessList(request.getSessionId(), request.getStartTime(), request.getEndTime());
            case CjConcurrencyConstants.CONCURRENCY_CJ_THREAD -> CjThreadDao.getInstance()
                .queryCjThreadProcessList(request.getSessionId(), request.getStartTime(), request.getEndTime());
            default -> new ArrayList<>();
        };
    }
}
