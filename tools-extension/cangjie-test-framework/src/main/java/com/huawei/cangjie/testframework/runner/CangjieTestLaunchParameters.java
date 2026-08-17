/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;

import java.util.Map;

/**
 * CangjieTestLaunchParameters
 *
 * @since 2025/02/20
 */
public class CangjieTestLaunchParameters {
    private String runner;
    private Devices device;
    private Map<String, String> arguments;
    private CangjieTestRunConfiguration configuration;
    private OpenHarmonyConsolePrinter consolePrinter;
    private OpenHarmonyLaunchStatus launchStatus;
    private OpenHarmonyLaunchAppInfo launchAppInfo;

    public CangjieTestLaunchParameters() {
        super();
    }

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public Devices getDevice() {
        return device;
    }

    public void setDevice(Devices device) {
        this.device = device;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public CangjieTestRunConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CangjieTestRunConfiguration configuration) {
        this.configuration = configuration;
    }

    public OpenHarmonyConsolePrinter getConsolePrinter() {
        return consolePrinter;
    }

    public void setConsolePrinter(OpenHarmonyConsolePrinter consolePrinter) {
        this.consolePrinter = consolePrinter;
    }

    public OpenHarmonyLaunchStatus getLaunchStatus() {
        return launchStatus;
    }

    public void setLaunchStatus(OpenHarmonyLaunchStatus launchStatus) {
        this.launchStatus = launchStatus;
    }

    public OpenHarmonyLaunchAppInfo getLaunchAppInfo() {
        return launchAppInfo;
    }

    public void setLaunchAppInfo(OpenHarmonyLaunchAppInfo launchAppInfo) {
        this.launchAppInfo = launchAppInfo;
    }
}
