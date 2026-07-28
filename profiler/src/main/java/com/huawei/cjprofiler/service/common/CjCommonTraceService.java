/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service.common;

import com.huawei.cjprofiler.service.launch.CjLaunchTraceService;
import com.huawei.deveco.insight.ohos.ability.database.TraceDatabase;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.Response;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.singleton.SingletonContainer;
import com.huawei.dap.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;

/**
 * CjCommonTraceService
 *
 * @since 2025-6-25
 */
public class CjCommonTraceService {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjCommonTraceService.class);

    private static volatile CjCommonTraceService cjCommonTraceService = null;

    /**
     * constructor
     */
    private CjCommonTraceService() {
    }

    /**
     * CjCommonTraceService instance
     *
     * @return CjCommonTraceService
     */
    public static CjCommonTraceService getInstance() {
        if (cjCommonTraceService == null) {
            synchronized (CjCommonTraceService.class) {
                if (cjCommonTraceService == null) {
                    cjCommonTraceService = new CjCommonTraceService();
                }
            }
        }
        return cjCommonTraceService;
    }

    /**
     * Delete trace session
     *
     * @param params parameters
     * @return Response<?>
     */
    public Response<?> deleteTraceSession(JSONObject params) {
        String sessionId = params.getString("sessionId");
        if (StringUtils.isBlank(sessionId)) {
            LOGGER.warn("Failed to deleteTraceSession, sessionId is empty");
            return Response.failure(ProfilerError.REQUEST_PARAMETER_ERROR);
        }
        if (!TraceDatabase.getInstance().releaseDatabase(sessionId)) {
            // 如果session录制失败，删除会话时断开连接会失败
            LOGGER.warn("Failed to disconnect from database in deleteTraceSession");
            return Response.failure(ProfilerError.SERVER_INTERNAL_ERROR);
        }
        // Launch Trace清理session缓存，防止二次导入没有数据
        SingletonContainer.getInstance(CjLaunchTraceService.class).cleanSessionCache(sessionId);
        return Response.success();
    }
}
