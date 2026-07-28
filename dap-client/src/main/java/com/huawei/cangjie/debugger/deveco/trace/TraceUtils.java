/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.deveco.trace;

import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.deveco.common.trace.HarmonyCustomTopic;
import com.huawei.deveco.common.trace.TraceDataBean;
import com.huawei.deveco.common.trace.TraceUtil;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;

/**
 * trace record
 *
 * @since 2024-10-29
 */
public class TraceUtils {
    private static final Logger LOGGER = Logger.getInstance(TraceUtils.class);
    private static final String SID = "10014";

    /**
     * log a trace
     *
     * @param propertiesJson trace properties json object
     * @param action trace action
     */
    public static void trace(JsonObject propertiesJson, String action) {
        String message = String.format("Cangjie debug trace: %s, with properties: %s", action, propertiesJson);
        LogUtils.printCangjieLogInfo(LOGGER, message);
        ApplicationInfo info = ApplicationInfo.getInstance();
        if (info != null) {
            String details = null;
            if (propertiesJson != null) {
                details = propertiesJson.toString();
            }
            TraceDataBean data = new TraceDataBean(SID, info.getFullVersion(), action, details);
            TraceUtil.trace(data, HarmonyCustomTopic.topic);
        }
    }
}
