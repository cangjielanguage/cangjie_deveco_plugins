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
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Pair;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * CangjieLocalTestLaunchTask
 *
 * @since 2025/08/28
 */
public class CangjieLocalTestLaunchTask implements LaunchTask {
    private static final String ID = "Cangjie Local Test Launch";

    private static final String LAUNCHING_INSTRUMENTATION_RUNNER = "Launching local test runner";

    private final String localTestRunner;

    @NotNull
    private final Map<String, String> localTestRunnerArguments;

    private final CangjieTestRunConfiguration configuration;

    private final OpenHarmonyConsolePrinter consolePrinter;

    private final OpenHarmonyLaunchStatus launchStatus;

    private final OpenHarmonyLaunchAppInfo launchAppInfo;

    private OhosTraceHelper mTraceHelper;

    public CangjieLocalTestLaunchTask(CangjieTestLaunchParameters cangjieTestLaunchParameters) {
        localTestRunner = cangjieTestLaunchParameters.getRunner();
        localTestRunnerArguments = cangjieTestLaunchParameters.getArguments();
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
        CangjieLocalTestRunner runner = new CangjieLocalTestRunner(configuration, launchAppInfo, localTestRunner,
                consolePrinter, launchStatus);
        for (Map.Entry<String, String> entry : localTestRunnerArguments.entrySet()) {
            runner.addLocalTestArg(entry.getKey(), entry.getValue());
        }
        Map<Pair<String, String>, String> instrumentCommands = runner.getLocalTestCommands();
        if (CollectionUtils.isEmpty(Collections.singleton(instrumentCommands))) {
            launchStatus.terminateLaunch(StringUtils.EMPTY, true);
            return true;
        }
        for (Map.Entry<Pair<String, String>, String> item : instrumentCommands.entrySet()) {
            consolePrinter.stdout("$ " + item.getValue());
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            runner.run(new CangjieTestListener(launchStatus, consolePrinter, launchAppInfo, configuration,
                    mTraceHelper));
        });
        return true;
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
