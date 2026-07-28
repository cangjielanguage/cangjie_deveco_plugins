/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.trace;

import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_HYBRID_HAP;
import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_HYBRID_HAR;
import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_HYBRID_PROJECT;
import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_PURE_HAP;
import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_PURE_HAR;
import static com.huawei.cangjie.projectmgmt.trace.TraceKind.CJ_CREATE_PURE_PROJECT;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_EMPTY_ABILITY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_HYBRID_ABILITY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_HYBRID_LIBRARY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_STATIC_LIBRARY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PROJECT;

import com.huawei.deveco.common.trace.HarmonyCustomTopic;
import com.huawei.deveco.common.trace.TraceDataBean;
import com.huawei.deveco.common.trace.TraceUtil;

import com.alibaba.fastjson2.JSONObject;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;

import java.util.Map;

/**
 * TraceUtils
 *
 * @since 2024/12/06
 */
public class TraceUtils {
    private static final Logger LOG = Logger.getInstance(TraceUtils.class);

    private static final String SID = "10014";

    private static final Map<Pair<String, String>, TraceKind> TEMPLATE_TRACEKIND_MAP = Map.of(
            new Pair<>(PROJECT, CANGJIE_EMPTY_ABILITY), CJ_CREATE_PURE_PROJECT,
            new Pair<>(PROJECT, CANGJIE_HYBRID_ABILITY), CJ_CREATE_HYBRID_PROJECT,
            new Pair<>(MODULE, CANGJIE_EMPTY_ABILITY), CJ_CREATE_PURE_HAP,
            new Pair<>(MODULE, CANGJIE_HYBRID_ABILITY), CJ_CREATE_HYBRID_HAP,
            new Pair<>(MODULE, CANGJIE_STATIC_LIBRARY), CJ_CREATE_PURE_HAR,
            new Pair<>(MODULE, CANGJIE_HYBRID_LIBRARY), CJ_CREATE_HYBRID_HAR
    );

    /**
     * trace by template name
     *
     * @param createType module or project
     * @param templateName templateName
     */
    public static void traceByTemplateName(String createType, String templateName) {
        TraceKind traceKind = TEMPLATE_TRACEKIND_MAP.get(new Pair<>(createType, templateName));
        if (traceKind == null) {
            return;
        }
        trace(traceKind);
    }

    /**
     * trace
     *
     * @param traceKind traceKind
     */
    public static void trace(TraceKind traceKind) {
        trace(traceKind, 1, -1, "");
    }

    /**
     * trace
     *
     * @param traceKind traceKind
     * @param count count
     * @param duration duration
     * @param properties properties
     */
    public static void trace(TraceKind traceKind, int count, long duration, String properties) {
        JSONObject detail = new JSONObject();
        detail.put("cause", traceKind.getCause());
        detail.put("count", count);
        detail.put("duration", duration);
        detail.put("properties", properties);
        TraceDataBean data = new TraceDataBean(SID, "", traceKind.getAction(), detail.toString());
        if (traceKind == TraceKind.CJ_COMPILE_HAR || traceKind == TraceKind.CJ_COMPILE_HAP
                || traceKind == TraceKind.CJ_COMPILE_APP) {
            LOG.debug("Cangjie compile_build trace: {",
                    "action: ", data.getAction(), ", detail: ", data.getDetail() + "}");
        } else {
            LOG.debug("Cangjie project trace: {",
                    "action: ", data.getAction(), ", detail: ", data.getDetail() + "}");
        }
        TraceUtil.trace(data, HarmonyCustomTopic.topic);
    }
}
