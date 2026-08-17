/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.trace;

import com.huawei.bitfun.intellij.trace.DebugTracer;
import com.huawei.bitfun.intellij.trace.DebugUsageObject;
import com.huawei.cangjie.debugger.deveco.trace.TraceUtils;

import com.google.gson.JsonObject;

/**
 * Cangjie Debug Tracer Implementation
 *
 * @since 2024-10-29
 */
public class CangjieDebugTracer extends DebugTracer {
    private static final String START_DEBUGGING_ACTION = "CangjieLaunchDebugger";
    private static final String FIRST_STOPPED_ACTION = "CangjieDebugFirstStopped";
    private static final String FIRST_DISASSEMBLY_ACTION = "CangjieDebugFirstDisassembly";
    private static final String FIRST_TIME_TRAVEL_ACTION = "CangjieDebugFirstTimeTravel";
    private static final String DEBUG_FINISH_ACTION = "CangjieDebugFinish";

    private String deviceType = "";

    private String apiVersion = "";

    private String deviceImage = "";

    private String debugType = "";


    private JsonObject createBasicPropertiesJson() {
        JsonObject propertiesJson = new JsonObject();
        propertiesJson.addProperty("debugType", debugType);
        propertiesJson.addProperty("deviceType", deviceType);
        propertiesJson.addProperty("apiVersion", apiVersion);
        propertiesJson.addProperty("deviceImage", deviceImage);
        return propertiesJson;
    }

    @Override
    public void startDebugging(String errMsg) {
        JsonObject propertiesJson = createBasicPropertiesJson();
        propertiesJson.addProperty("duration", System.currentTimeMillis() - beforeDoInitTimeStamp);
        propertiesJson.addProperty("success", errMsg == null);
        if (errMsg != null) {
            propertiesJson.addProperty("errorMessage", errMsg);
        }
        TraceUtils.trace(propertiesJson, START_DEBUGGING_ACTION);
    }

    @Override
    public void firstStopped() {
        JsonObject propertiesJson = createBasicPropertiesJson();
        long startUpTime = System.currentTimeMillis() - beforeDoInitTimeStamp;
        propertiesJson.addProperty("startTime", startUpTime);
        TraceUtils.trace(propertiesJson, FIRST_STOPPED_ACTION);
    }

    @Override
    public void stopDebugging(DebugUsageObject usageObj) {
        JsonObject propertiesJson = createBasicPropertiesJson();
        if (usageObj.isDisassemblyUsed()) {
            TraceUtils.trace(propertiesJson, FIRST_DISASSEMBLY_ACTION);
        }
        if (usageObj.isTimeTravelUsed()) {
            TraceUtils.trace(propertiesJson, FIRST_TIME_TRAVEL_ACTION);
        }
        propertiesJson.addProperty("commandLineUsage", usageObj.getCommandLineUsageCount());
        TraceUtils.trace(propertiesJson, DEBUG_FINISH_ACTION);
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setDeviceImage(String deviceImage) {
        this.deviceImage = deviceImage;
    }

    public void setDebugType(String debugType) {
        this.debugType = debugType;
    }
}
