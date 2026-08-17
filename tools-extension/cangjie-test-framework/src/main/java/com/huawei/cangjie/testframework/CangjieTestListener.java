/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework;

import static com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestStatus.ASYNC_DESCRIBE_ERR_MSG;
import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.getMaxTime;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.console.CangjieLocalTestLocationProvider;
import com.huawei.cangjie.testframework.console.CangjieOhosTestLocationProvider;
import com.huawei.cangjie.testframework.console.CangjieTestConsoleProperties;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestIdentifier;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestResult;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestStatus;
import com.huawei.deveco.ohos.testframework.run.testrunner.StackTraceHyperLinkFilter;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestRunnerListener;
import com.huawei.deveco.ohos.testframework.utils.OhosTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.ServiceMessageBuilder;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

/**
 * CangjieTestListener
 *
 * @since 2025/02/20
 */
public class CangjieTestListener implements TestRunnerListener {
    private static final Logger LOG = Logger.getInstance(CangjieTestListener.class);
    private static final String ENTERED_THE_MATRIX = "enteredTheMatrix";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";
    private static final String TEST_RUNNING_FAILED = "Test %s running failed: ";
    private static final String SEPARATOR = System.lineSeparator();

    private final OpenHarmonyLaunchAppInfo launchAppInfo;
    private final SMTRunnerConsoleView console;
    private final SMTestRunnerResultsForm form;
    @NotNull
    private final OpenHarmonyLaunchStatus launchStatus;
    @NotNull
    private final OpenHarmonyConsolePrinter consolePrinter;
    private final String moduleName;
    private final String protocolId;
    private SMTestProxy testFile;
    private String filePath;
    private String fileName;
    private final List<SMTestProxy> fileTests = new ArrayList<>();
    private final List<String> suites = new ArrayList<>();
    private CangjieTestRunConfiguration.TestingType testingType = CangjieTestRunConfiguration.TestingType.UNDEFINED;
    private String curPackageName;
    private Map<String, List<String>> packageNameMap;
    private String testSuiteName = null;
    private SMTestProxy testIt;
    private SMTestProxy.SMRootTestProxy root;
    private final Stack<SMTestProxy> currentSuiteStack = new Stack<>();
    private final Map<SMTestProxy, Long> suiteStartTimeMap = new HashMap<>();
    private final Set<SMTestProxy> failTestProxySet = new HashSet<>();
    private final OhosTraceHelper mTraceHelper;
    private long rootStartTime = 0L;
    private final Map<SMTestProxy, Long> fileStartTimeMap = new HashMap<>();
    private long suiteStartTime = 0L;
    private long suiteEndTime = 0L;

    /**
     * OpenHarmonyTestListener
     *
     * @param launchStatus OpenHarmonyLaunchStatus
     * @param consolePrinter OpenHarmonyConsolePrinter
     * @param launchAppInfo OpenHarmonyLaunchAppInfo
     * @param configuration OpenHarmonyTestRunConfiguration
     * @param traceHelper OhosTraceHelper
     */
    public CangjieTestListener(@NotNull OpenHarmonyLaunchStatus launchStatus,
                               @NotNull OpenHarmonyConsolePrinter consolePrinter,
                               OpenHarmonyLaunchAppInfo launchAppInfo,
                               CangjieTestRunConfiguration configuration, OhosTraceHelper traceHelper) {
        OhosTestUtils.setStartTestTime(System.currentTimeMillis());
        this.launchStatus = launchStatus;
        this.consolePrinter = consolePrinter;
        this.launchAppInfo = launchAppInfo;
        this.mTraceHelper = traceHelper;
        moduleName = launchAppInfo.getModule().getModuleName();
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            protocolId = CangjieLocalTestLocationProvider.PROTOCOL_ID;
        } else {
            protocolId = CangjieOhosTestLocationProvider.PROTOCOL_ID;
        }
        CangjieTestConsoleProperties consoleProperties = new CangjieTestConsoleProperties(configuration,
                new DefaultRunExecutor(), protocolId);
        ConsoleView consoleView = configuration.getConsoleView();
        if (consoleView instanceof SMTRunnerConsoleView) {
            console = (SMTRunnerConsoleView) consoleView;
        } else {
            console = new SMTRunnerConsoleView(consoleProperties);
        }
        form = console.getResultsViewer();
        packageNameMap = configuration.getPackageNameMap();
        root = form.getTestsRootNode();
        rootStartTime = System.currentTimeMillis();
        root.setStarted();
        if (configuration.getTestingType() == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            testingType = CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE;
            Set<String> filePaths = packageNameMap.keySet();
            for (String filePath : filePaths) {
                if (packageNameMap.get(filePath) == null || packageNameMap.get(filePath).isEmpty()) {
                    continue;
                }
                SMTestProxy curTestFile = getSmTestProxy(filePath);
                fileTests.add(curTestFile);
            }
            return;
        }
        filePath = configuration.getFilePath();
        fileName = new File(configuration.getFilePath().trim()).getName();
        testFile = getSmTestProxy(configuration.getFilePath());
        suites.addAll(List.of(configuration.getClassName().split(Constant.COMMA)));
    }

    @NotNull
    private SMTestProxy getSmTestProxy(String filePath) {
        String finalPath;
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            finalPath = filePath;
        } else {
            finalPath = this.filePath;
        }
        String locationUrl = protocolId + Constant.COLON_SLASH_SLASH + moduleName + Constant.URL_SPLITS + finalPath
                + Constant.VERTICAL_LINE;
        String curFileName = new File(filePath.trim()).getName();
        SMTestProxy curTestFile = new SMTestProxy(curFileName, false, locationUrl, false);
        // 更新file节点finish信息
        curTestFile.setStarted();
        curTestFile.setParent(root);
        curTestFile.setLocator(CangjieTestConsoleProperties.getTestLocatorInstance(protocolId));
        root.addChild(curTestFile);
        form.onSuiteStarted(curTestFile);
        return curTestFile;
    }

    @Override
    public void testRunStopped(long elapsedTime) {
        launchStatus.terminateLaunch(OpenHarmonyHintBundle.message("test.run.stopped") + SEPARATOR, true);
    }

    @Override
    public void testRunEnded(long duration, Map<String, String> runMetrics) {
        if (!launchStatus.isLaunchTerminated() && failTestProxySet.contains(root)) {
            root.setTestFailed(StringUtils.EMPTY, StringUtils.EMPTY, true);
        }
        while (!launchStatus.isLaunchTerminated() && !currentSuiteStack.isEmpty()) {
            SMTestProxy suit = currentSuiteStack.peek();
            if (suit == null || suit.isFinal()) {
                currentSuiteStack.pop();
                continue;
            }
            testRunFailed("Test run failed." + System.lineSeparator());
            currentSuiteStack.pop();
        }
        finishTestFile(null);
        root.setFinished();
        root.setDuration(duration);
        collectTimeInfo();
        endTestProcess(launchStatus);
    }

    private void collectTimeInfo() {
        long endTime = System.currentTimeMillis();
        LOG.info(String.format(Locale.ROOT, "instrument_test_continuous_test_time --> %s",
                (endTime - OhosTestUtils.getStartTestTime())));
        LOG.info(String.format(Locale.ROOT, "instrument_test_continuous_time --> %s",
                (endTime - OhosTestUtils.getStartTime())));
    }

    private void endTestProcess(OpenHarmonyLaunchStatus launchStatus) {
        ProcessHandler processHandler = launchStatus.getProcessHandler();
        String message = OpenHarmonyHintBundle.message("test.finished");
        processHandler.notifyTextAvailable(message + System.lineSeparator(), ProcessOutputTypes.STDOUT);
        if (!processHandler.isProcessTerminating() && !processHandler.isProcessTerminated()) {
            processHandler.destroyProcess();
        }
    }

    @Override
    public void testRunFailed(String errorMessage) {
        SMTestProxy currentSuite = currentSuiteStack.peek();
        currentSuite.addStdErr(errorMessage);
        OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.RUN_FAILED);
        String suiteName = currentSuite.getName();
        launchStatus.terminateLaunch(String.format(TEST_RUNNING_FAILED, suiteName) + errorMessage, true);
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
        consolePrinter.stdout(SEPARATOR + OpenHarmonyHintBundle.message("started.running.test") + SEPARATOR);
        ServiceMessageBuilder builder = new ServiceMessageBuilder(ENTERED_THE_MATRIX);
        consolePrinter.stdout(builder.toString());
    }

    @Override
    public void testStarted(OhosTestIdentifier test) {
        Optional<String> optPkg = getTestFilePathBySuiteName(testSuiteName);
        String packageName = "";
        if (optPkg.isPresent()) {
            packageName = optPkg.get();
        }
        String locationUrl = protocolId + Constant.COLON_SLASH_SLASH + moduleName + Constant.URL_SPLITS
                + packageName + Constant.HASH_TAGS + test.getTestSuiteName() + Constant.DOT + test.getTestName();
        testIt = new SMTestProxy(test.getTestName(), false, locationUrl, true);
        setPrinter(testIt);
        bindToParent(testIt, currentSuiteStack.peek());
        form.onTestStarted(testIt);
    }

    private void finishTestFile(SMTestProxy nextTestFile) {
        if (testFile == null) {
            testFile = nextTestFile;
            return;
        }
        boolean isSameFile = Objects.equals(testFile, nextTestFile);
        if (isSameFile) {
            return;
        }
        if (testFile.getChildren().isEmpty()) {
            String ignoreComment = OhosTestStatus.NO_TEST_RESULTS_MSG + " found in " + testFile.getName();
            testFile.setTestIgnored(ignoreComment, StringUtils.EMPTY);
            testFile.getParent().getChildren().remove(testFile);
        }
        long time = suiteEndTime - fileStartTimeMap.getOrDefault(testFile, 0L);
        time = getMaxTime(testFile, time);
        testFile.setDuration(time);
        setFailIfNecessary(testFile);
        testFile.setFinished();
        testFile = nextTestFile;
    }

    private void setFailIfNecessary(SMTestProxy smTestProxy) {
        if (failTestProxySet.contains(smTestProxy)
                && !Objects.equals(smTestProxy.getMagnitudeInfo(), TestStateInfo.Magnitude.ERROR_INDEX)) {
            smTestProxy.setTestFailed(StringUtils.EMPTY, StringUtils.EMPTY, true);
            failTestProxySet.add(smTestProxy.getParent());
        }
    }

    private SMTestProxy getFileTestFromFileName(String filePath) {
        for (SMTestProxy fileTest : fileTests) {
            String locationUrl = fileTest.getLocationUrl();
            if (locationUrl == null) {
                continue;
            }
            String testFilePath = locationUrl.substring(locationUrl.indexOf(Constant.URL_SPLITS)
                    + Constant.URL_SPLITS.length(), locationUrl.length() - Constant.VERTICAL_LINE.length());
            if (testFilePath.equals(filePath)) {
                return fileTest;
            }
        }
        String locationUrl = protocolId + Constant.COLON_SLASH_SLASH + moduleName
                + Constant.COLON + fileName;
        return new SMTestProxy(fileName, false, locationUrl, false);
    }

    private void bindToParent(@NotNull SMTestProxy son, @NotNull SMTestProxy parent) {
        son.setParent(parent);
        son.setStarted();
        son.setLocator(CangjieTestConsoleProperties.getTestLocatorInstance(protocolId));
        parent.addChild(son);
    }

    @Override
    public void testEnded(OhosTestIdentifier testIdentifier, long testDuration, Map<String, String> testMetrics) {
        if (testIt == null || !testIdentifier.getTestName().equals(testIt.getName())) {
            return;
        }
        testIt.setDuration(testIdentifier.getTestDuration());
        testIt.setFinished();
        form.onTestFinished(testIt);
    }

    @Override
    public void testAssumptionFailure(OhosTestIdentifier testIdentifier, String traceInfo) {
        ServiceMessageBuilder serviceMessageBuilder = ServiceMessageBuilder.testIgnored(testIdentifier.getTestName());
        serviceMessageBuilder.addAttribute(MESSAGE, OpenHarmonyHintBundle.message("assumption.failed"));
        serviceMessageBuilder.addAttribute(DETAILS, traceInfo);
        consolePrinter.stdout(serviceMessageBuilder.toString());
    }

    @Override
    public void testFailed(OhosTestIdentifier test, String stackTrace) {
        testIt.setTestFailed(StringUtils.EMPTY, stackTrace, true);
        form.onTestFailed(testIt);
        failTestProxySet.add(testIt.getParent());
    }

    private void setPrinter(SMTestProxy smTestProxy) {
        Project project = launchAppInfo.getProject();
        TestProxyPrinterProvider provider = new TestProxyPrinterProvider(console,
                (nodeType, nodeName, nodeArguments) -> new StackTraceHyperLinkFilter(project, project.getBasePath()));
        Printer printer = provider.getPrinterByType(Constant.ACE_TEST_IT, smTestProxy.getName(), null);
        if (printer != null) {
            smTestProxy.setPreferredPrinter(printer);
        }
    }

    @Override
    public void testIgnored(OhosTestIdentifier testIdentifier) {
        String skipTestClassName = testIdentifier.getTestSuiteName();
        String skipTestCaseName = testIdentifier.getTestName();
        // check is skipped running test
        if (testIt != null && skipTestClassName.equals(testSuiteName) && skipTestCaseName.equals(testIt.getName())) {
            testIt.getParent().getChildren().remove(testIt);
            return;
        }
        ServiceMessageBuilder serviceMessageBuilder = ServiceMessageBuilder.testIgnored(testIdentifier.getTestName());
        consolePrinter.stdout(serviceMessageBuilder.toString());
    }

    @Override
    public void suiteStarted(OhosTestResult result) {
        boolean isSameSuite = StringUtils.equals(result.getSuiteName(), testSuiteName);
        if (isSameSuite && isContainsSuite(testSuiteName)) {
            return;
        }
        testSuiteName = result.getSuiteName();
        suiteStartTime = result.getSuiteStartTime();
        Optional<String> optPkg = getTestFilePathBySuiteName(testSuiteName);
        String packageName = "";
        if (optPkg.isPresent()) {
            packageName = optPkg.get();
        }
        String locationUrl = protocolId + Constant.COLON_SLASH_SLASH + moduleName + Constant.URL_SPLITS
                + packageName + Constant.HASH_TAGS + testSuiteName;
        boolean isNestedSuiteExist = !currentSuiteStack.isEmpty();
        SMTestProxy currentSuite = new SMTestProxy(testSuiteName, false, locationUrl, isNestedSuiteExist);
        suiteStartTimeMap.putIfAbsent(currentSuite, suiteStartTime);
        if (isNestedSuiteExist) {
            bindToParent(currentSuite, currentSuiteStack.peek());
            form.onSuiteStarted(currentSuite);
        } else {
            SMTestProxy belongFile = getBelongFile();
            if (belongFile == null) {
                return;
            }
            fileStartTimeMap.putIfAbsent(belongFile, suiteStartTime);
            bindToParent(currentSuite, belongFile);
            form.onSuiteStarted(currentSuite);
            finishTestFile(belongFile);
        }
        currentSuiteStack.push(currentSuite);
    }

    @Nullable
    private SMTestProxy getBelongFile() {
        if (testingType != CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            return testFile;
        }
        Optional<String> suiteFilePath = getTestFilePathBySuiteName(testSuiteName);
        return suiteFilePath.map(this::getFileTestFromFileName).orElseGet(() -> testFile != null ? testFile : null);
    }

    private boolean isContainsSuite(String suiteName) {
        for (SMTestProxy testProxy : currentSuiteStack) {
            if (testProxy.getName().equals(suiteName)) {
                return true;
            }
        }
        return false;
    }

    private Optional<String> getTestFilePathBySuiteName(String suiteName) {
        if (!StringUtils.isEmpty(this.curPackageName)) {
            return Optional.of(this.curPackageName);
        }
        for (Map.Entry<String, List<String>> entry : packageNameMap.entrySet()) {
            String key = entry.getKey();
            List<String> list = entry.getValue();
            if (list.contains(suiteName)) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    @Override
    public void suiteEnded(OhosTestResult result) {
        if (StringUtils.isEmpty(testSuiteName) || currentSuiteStack.isEmpty()
                || !result.getSuiteName().equals(testSuiteName)) {
            return;
        }
        SMTestProxy currentSuite = currentSuiteStack.pop();
        if (result.isAsyncDescribe()) {
            currentSuite.setTestFailed(StringUtils.EMPTY, ASYNC_DESCRIBE_ERR_MSG, true);
            failTestProxySet.add(currentSuite.getParent());
        }
        if (currentSuite.getChildren().isEmpty()) {
            if (!result.isAsyncDescribe()) {
                currentSuite.getParent().getChildren().remove(currentSuite);
            }
        }
        suiteEndTime = System.currentTimeMillis();
        currentSuite.setDuration(result.getTestDuration());
        setFailIfNecessary(currentSuite);
        currentSuite.setFinished();
        form.onSuiteFinished(currentSuite);
        suiteStartTime = 0L;
    }

    /**
     * set package name
     *
     * @param packageName packageName
     */
    public void setPackageName(String packageName) {
        this.curPackageName = packageName;
    }
}
