/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import com.huawei.cangjie.testframework.CangjieTestListener;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.tasks.LaunchTask;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;

import com.intellij.openapi.application.ApplicationManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * CangjieTestLaunchTask
 *
 * @since 2025/02/20
 */
public class CangjieOhosTestLaunchTask implements LaunchTask {
    private static final String ID = "Cangjie Instrument Test Launch";

    private static final String LAUNCHING_INSTRUMENTATION_RUNNER = "Launching instrumentation runner";

    private final String oHosTestRunner;

    private final Devices device;

    @NotNull
    private final Map<String, String> oHosTestRunnerArguments;

    private final CangjieTestRunConfiguration configuration;

    private final OpenHarmonyConsolePrinter consolePrinter;

    private final OpenHarmonyLaunchStatus launchStatus;

    private final OpenHarmonyLaunchAppInfo launchAppInfo;

    private OhosTraceHelper mTraceHelper;

    public CangjieOhosTestLaunchTask(CangjieTestLaunchParameters cangjieTestLaunchParameters) {
        oHosTestRunner = cangjieTestLaunchParameters.getRunner();
        device = cangjieTestLaunchParameters.getDevice();
        oHosTestRunnerArguments = cangjieTestLaunchParameters.getArguments();
        configuration = cangjieTestLaunchParameters.getConfiguration();
        consolePrinter = cangjieTestLaunchParameters.getConsolePrinter();
        launchStatus = cangjieTestLaunchParameters.getLaunchStatus();
        launchAppInfo = cangjieTestLaunchParameters.getLaunchAppInfo();
    }

    @Override
    @NotNull
    public String getDescription() {
        return LAUNCHING_INSTRUMENTATION_RUNNER;
    }

    @Override
    public int getDuration() {
        return 2;
    }

    @Override
    public boolean perform() {
        consolePrinter.stdout(OpenHarmonyHintBundle.message("running.tests") + System.lineSeparator());
        CangjieOhosTestRunner runner = new CangjieOhosTestRunner(configuration, launchAppInfo, oHosTestRunner, device,
                consolePrinter, launchStatus);
        runner.setTraceHelper(mTraceHelper);
        setRunnerData(runner);
        for (Map.Entry<String, String> entry : oHosTestRunnerArguments.entrySet()) {
            runner.addInstrumentationArg(entry.getKey(), entry.getValue());
        }
        List<String> instrumentCommands = runner.getInstrumentCommands();
        if (CollectionUtils.isEmpty(instrumentCommands)) {
            mTraceHelper.traceRun(OhosTraceHelper.TraceAction.RUN_OHOS_TEST.getActionName(),
                    OhosTraceHelper.TraceStage.ERROR.getStageName());
            launchStatus.terminateLaunch(StringUtils.EMPTY, true);
            return true;
        }
        for (String command : instrumentCommands) {
            consolePrinter.stdout("$ hdc shell " + command);
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            runner.run(new CangjieTestListener(launchStatus, consolePrinter, launchAppInfo, configuration,
                    mTraceHelper));
        });
        return true;
    }

    private void setRunnerData(CangjieOhosTestRunner runner) {
        CangjieTestRunConfiguration.TestingType testingType = configuration.getTestingType();
        runner.setClassName(configuration.getClassName());
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            runner.setTestPackageName(configuration.getPackageName());
            return;
        }
        runner.setMethodName(configuration.getMethodName());
    }

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    @Override
    public void processError() {
    }

    public void setTraceHelper(OhosTraceHelper mTraceHelper) {
        this.mTraceHelper = mTraceHelper;
    }
}
