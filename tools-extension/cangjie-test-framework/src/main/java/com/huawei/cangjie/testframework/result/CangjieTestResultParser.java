/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.result;

import static com.huawei.cangjie.testframework.utils.Constant.EXECUTE_CLASS_END;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.XmlUtil.createSAXParser;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.XmlUtil.stripBom;

import com.huawei.cangjie.testframework.CangjieTestListener;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.hdclib.ohos.client.CallBackReceiver;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestResult;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestResultParserModel;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestRunnerListener;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * CangjieTestResultParser
 *
 * @since 2025/02/20
 */
@Slf4j
public class CangjieTestResultParser extends CallBackReceiver {
    private static final Logger LOG = Logger.getInstance(CangjieTestResultParser.class);

    private static final String SEPARATOR = "\n";

    private static final String UNKNOWN_ERROR = "Unknown error";

    private static final String UNKNOWN_FAILURE = "Unknown failure";

    private static final Pattern RESULT_CODE_PATTERN =
            Pattern.compile(Constant.TEST_FINISHED_RESULT_CODE + "\\s*(-?\\d+)");

    /**
     * hdc发送的报文buffer为64k
     */
    private static final int MAX_BUFFER_MEMORY = 64 * 1024 * 8;

    private final String testRunName;

    private final Collection<TestRunnerListener> testRunnerListeners;

    private String onError = null;

    private int numTestsRun = 0;

    private int numTestsExpected = 0;

    private long tedtStartTime = 0L;

    private long suiteStartTime = 0L;

    private String lastStream = "";

    private OpenHarmonyLaunchStatus launchStatus;

    private OpenHarmonyLaunchAppInfo appInfo;

    private CangjieTestRunConfiguration ohConfiguration;

    private OpenHarmonyConsolePrinter printer;

    private String coveragePathInDevice = "";

    private Devices device;

    private int nextXmlReportContentLength = 0;

    private String outputReportXmlPath = "";

    private boolean isUseParseXmlFile = false;

    private StringBuilder sb = new StringBuilder();

    private final boolean isCoverage;

    public CangjieTestResultParser(Collection<TestRunnerListener> listeners, TestResultParserModel parserModel) {
        testRunName = parserModel.getRunName();
        this.testRunnerListeners = new ArrayList<>(listeners);
        this.launchStatus = parserModel.getLaunchStatus();
        this.device = parserModel.getDevice();
        this.appInfo = parserModel.getAppInfo();
        this.printer = parserModel.getPrinter();
        if (appInfo != null && appInfo.getConfiguration() instanceof CangjieTestRunConfiguration configuration) {
            ohConfiguration = configuration;
        }
        isCoverage = ohConfiguration.getIsRunWithCoverage();
    }

    @Override
    public void processData(String stream) {
        if (stream == null) {
            return;
        }
        String streamIn = stream;
        if (streamIn.length() > MAX_BUFFER_MEMORY) {
            streamIn = streamIn.substring(0, MAX_BUFFER_MEMORY);
        }

        // 处理数据流截断的场景
        streamIn = lastStream + streamIn;
        String[] lines = streamIn.split(SEPARATOR);

        int length = lines.length;
        if (length > 1 && !streamIn.endsWith(SEPARATOR)) {
            lastStream = lines[length - 1];
            length = length - 1;
        } else {
            lastStream = StringUtils.EMPTY;
        }
        for (int index = 0; index < length; index++) {
            String line = lines[index];
            if (nextXmlReportContentLength == 0) {
                parse(line);
                continue;
            }
            if (StringUtils.isNotBlank(outputReportXmlPath) && Paths.get(outputReportXmlPath).toFile().exists()) {
                try {
                    String reportXmlContent = Files.readString(Paths.get(outputReportXmlPath));
                    parse(reportXmlContent);
                    outputReportXmlPath = StringUtils.EMPTY;
                    isUseParseXmlFile = true;
                } catch (Exception e) {
                    LOG.warn("Failed to read the unittest xml file.");
                }
            }
            while (index < length && !lines[index].startsWith(EXECUTE_CLASS_END)) {
                sb.append(lines[index]).append(SEPARATOR);
                index++;
            }
            int lastIndex = index == length ? index - 1 : index;
            if (lines[lastIndex].startsWith(EXECUTE_CLASS_END)) {
                nextXmlReportContentLength = 0;
                String testXmlContent = sb.toString();
                if (StringUtils.isNotBlank(testXmlContent) && !isUseParseXmlFile) {
                    parse(testXmlContent);
                }
                parse(lines[lastIndex]);
            }
        }
    }

    private boolean pareXml(String xmlText) {
        DefaultHandler handler = new CangjieTestOutPutHandle(testRunnerListeners);
        try {
            SAXParser saxParser = createSAXParser();
            InputSource inputSource = new InputSource(new StringReader(stripBom(xmlText)));
            saxParser.parse(inputSource, handler);
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            return false;
        }
        return true;
    }

    /**
     * parse test run result
     *
     * @param resultInput String
     */
    public void parse(String resultInput) {
        // xml report
        if (pareXml(resultInput)) {
            return;
        }
        parseRunningProcess(resultInput);
    }

    private void parseRunningProcess(String resultInput) {
        if (resultInput.startsWith(Constant.TEST_RUNNER_ON_RUN)) {
            TestUtil.deleteOldResults(ohConfiguration.getModule());
        } else if (resultInput.startsWith(Constant.EXECUTE_PACKAGE_START)) {
            String packageName = StringUtil.unquoteString(
                    resultInput.substring(Constant.EXECUTE_PACKAGE_START.length())
            );
            for (TestRunnerListener listener : testRunnerListeners) {
                if (!(listener instanceof CangjieTestListener cangjieTestListener)) {
                    continue;
                }
                cangjieTestListener.setPackageName(packageName);
            }
        } else if (resultInput.startsWith(Constant.EXECUTE_CLASS_START)) {
            if (tedtStartTime == 0) {
                tedtStartTime = System.currentTimeMillis();
            }
            if (suiteStartTime == 0) {
                suiteStartTime = System.currentTimeMillis();
            }
            String className = StringUtil.unquoteString(resultInput.substring(Constant.EXECUTE_CLASS_START.length()));
            if (StringUtils.isEmpty(className)) {
                return;
            }
            OhosTestResult ohosTestResult = new OhosTestResult();
            ohosTestResult.setSuiteName(className);
            ohosTestResult.setSuiteStartTime(System.currentTimeMillis());
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.suiteStarted(ohosTestResult);
            }
        } else if (resultInput.startsWith(EXECUTE_CLASS_END)) {
            String className = StringUtil.unquoteString(resultInput.substring(EXECUTE_CLASS_END.length()));
            long currentTime = System.currentTimeMillis();
            long duration = currentTime - suiteStartTime;
            OhosTestResult result = new OhosTestResult();
            result.setSuiteName(className);
            result.setTestDuration(duration);
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.suiteEnded(result);
            }
            suiteStartTime = 0;
            nextXmlReportContentLength = 0;
        } else if (resultInput.startsWith(Constant.XML_REPORT_CONTENT_LENGTH)) {
            String length = StringUtil.unquoteString(
                    resultInput.substring(Constant.XML_REPORT_CONTENT_LENGTH.length()));
            if (StringUtils.isEmpty(length)) {
                return;
            }
            nextXmlReportContentLength = Integer.parseInt(length);
        } else if (resultInput.startsWith(Constant.GENERATE_COVERAGE)) {
            coveragePathInDevice = StringUtil.unquoteString(
                    resultInput.substring(Constant.GENERATE_COVERAGE.length()));
            generateCoverageFile();
        } else if (resultInput.startsWith(Constant.REPORT_TEST_RESULT)) {
            String testReportsPath = StringUtil.unquoteString(
                    resultInput.substring(Constant.REPORT_TEST_RESULT.length()));
            if (StringUtils.isEmpty(testReportsPath)) {
                LOG.warn("No test report path found in device");
                return;
            }
            moveTestReports(testReportsPath);
        } else if (resultInput.startsWith(Constant.TEST_FINISHED_RESULT_CODE)) {
            Matcher matcher = RESULT_CODE_PATTERN.matcher(resultInput);
            if (!matcher.find()) {
                return;
            }
            int code = 0;
            try {
                code = Integer.parseInt(matcher.group(1));
            } catch (IllegalArgumentException exception) {
                LOG.warn("get test result code failed.");
            }
            if (code != 0) {
                for (TestRunnerListener listener : testRunnerListeners) {
                    listener.testRunFailed("Test run failed with code: " + code + System.lineSeparator());
                }
            }
            long currentTime = System.currentTimeMillis();
            long duration = currentTime - suiteStartTime;
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.testRunEnded(duration, new HashMap<>());
            }
        } else {
            return;
        }
    }

    private void moveTestReports(String testReportsPath) {
        if (StringUtils.isEmpty(testReportsPath)) {
            LOG.warn("No test report path found in device");
            return;
        }
        OhosModuleModel module = ohConfiguration.getModule();
        if (module == null) {
            LOG.warn("moveTestsReport failed! module is null!");
            return;
        }
        String modulePath = ohConfiguration.getModule().getModulePath();
        Path outputReportPath = Path.of(modulePath, Constant.TEST_OUTPUT_DIR, Constant.TEST_REPORTS_OUTPUT_DIR);
        try {
            if (!outputReportPath.toFile().exists()) {
                boolean isCreated = outputReportPath.toFile().mkdirs();
                if (!isCreated) {
                    LOG.info("Failed to create the .test/reports folder.");
                }
            }
            // receive test reports to local
            String recvCommand = String.format(Locale.ENGLISH, "file recv %s %s", testReportsPath,
                    outputReportPath.toAbsolutePath().normalize());
            String recvResult = device.getClient().sendSyncCommand(recvCommand, 0);
            LOG.info(String.format(Locale.ENGLISH, "recvResult: %s", recvResult));
            // remove test reports in device
            String rmCommand = String.format(Locale.ENGLISH, "shell rm -r %s", testReportsPath);
            String rmResult = device.getClient().sendSyncCommand(rmCommand, 0);
            outputReportXmlPath = outputReportPath.resolve(Paths.get(testReportsPath).getFileName()).toString();
            LOG.info(String.format(Locale.ENGLISH, "rmResult: %s", rmResult));
        } catch (TimeoutException e) {
            LOG.warn("recv test reports time out.");
        }
    }

    private void generateCoverageFile() {
        OhosModuleModel module = ohConfiguration.getModule();
        if (!ohConfiguration.getIsRunWithCoverage()) {
            return;
        }
        if (module == null) {
            LOG.warn("generateCoverageOutput failed! module is null!");
            return;
        }
        String modulePath = module.getModulePath();
        CangjieTestRunConfiguration.TestPathType testPathType = ohConfiguration.getTestPathType();
        Path testPath = Path.of(modulePath, Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE)
                .toAbsolutePath().normalize();
        if (testPathType == CangjieTestRunConfiguration.TestPathType.MAIN_PATH) {
            testPath = Path.of(modulePath, Constant.SRC, Constant.MAIN, Constant.CANGJIE)
                    .toAbsolutePath().normalize();
        }
        if (testPathType == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            testPath = Path.of(modulePath, Constant.SRC, Constant.TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize();
        }
        if (this.ohConfiguration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            generateLocalTestCoverage(testPath, module);
            return;
        }
        generateOhosTestCoverage(testPath, module);
    }

    private void generateOhosTestCoverage(Path testPath, OhosModuleModel module) {
        Path buildCoverageOutputPath = Path.of(testPath.toString(), Constant.BUILD_GCNO_OUTPUT_DIR);
        Path testCoverageOutputPath = Path.of(testPath.toString(), Constant.TEST_GCNO_OUTPUT_DIR);
        if (StringUtils.isEmpty(coveragePathInDevice)) {
            LOG.warn("No coverage device path found in coverage");
            return;
        }
        String gcdaPathInDevice = coveragePathInDevice + testPath.toString().replaceAll("\\\\", "/");
        String recvCommand = String.format(Locale.ENGLISH, "file recv %s %s", gcdaPathInDevice,
                testCoverageOutputPath.toAbsolutePath().normalize());
        try {
            // copy hdc coverage to ide path
            String recvResult = device.getClient().sendSyncCommand(recvCommand, 0);
            LOG.info(String.format(Locale.ENGLISH, "recvResult: %s", recvResult));
            // recv copy gcda dir, so add cangjie dir
            Path realGcdaPath = Path.of(testPath.toString(), Constant.TEST_GCNO_OUTPUT_DIR, Constant.CANGJIE);
            if (realGcdaPath.toFile().exists()) {
                FileUtils.copyDirectory(realGcdaPath.toFile(), testCoverageOutputPath.toFile());
                FileUtils.deleteDirectory(realGcdaPath.toFile());
            }
            // copy gcno and gcda to execute path
            FileUtils.copyDirectory(testCoverageOutputPath.toFile(), testPath.toFile());
            FileUtils.copyDirectory(buildCoverageOutputPath.toFile(), testPath.toFile());
            generateCoverage(testPath, module, buildCoverageOutputPath, testCoverageOutputPath);
        } catch (IOException exception) {
            LOG.warn("Failed to copy gcno and gcda folder.");
        } catch (TimeoutException e) {
            LOG.warn("recv time out.");
        }
    }

    private void generateLocalTestCoverage(Path testPath, OhosModuleModel module) {
        Path buildCoverageOutputPath = Path.of(testPath.toString(), Constant.BUILD_GCNO_OUTPUT_DIR);
        Path testCoverageOutputPath = Path.of(testPath.toString(), Constant.TEST_GCNO_OUTPUT_DIR);
        try {
            // copy gcda
            TestUtil.copyFile(testPath, testCoverageOutputPath, ".gcda", true);
            // copy gcno and gcda to execute path
            FileUtils.copyDirectory(testCoverageOutputPath.toFile(), testPath.toFile());
            FileUtils.copyDirectory(buildCoverageOutputPath.toFile(), testPath.toFile());
            generateCoverage(testPath, module, buildCoverageOutputPath, testCoverageOutputPath);
        } catch (IOException exception) {
            LOG.warn("Failed to copy gcno and gcda folder.");
        }
    }

    private void generateCoverage(Path testPath,
                                  OhosModuleModel module,
                                  Path buildCoverageOutputPath,
                                  Path testCoverageOutputPath) throws IOException {
        // execute cjcov
        TestUtil.executeCjCmd(testPath, "&&" + Constant.CJCOV + " " + Constant.CJCOV_OUTPUT_PARAM + " "
                + Constant.OUTPUT + " " + Constant.CJCOV_GENERATE_HTML_PARAM, module);
        // coverage generate target dir
        Path coverageOutputPath = Path.of(testPath.toString(), Constant.OUTPUT);
        // target move dir
        String modulePath = module.getModulePath();
        Path targetBuildGcnoOutputPath = Path.of(modulePath, Constant.TEST_OUTPUT_DIR, Constant.BUILD_GCNO_OUTPUT_DIR);
        Path targetTestGcdaOutputPath = Path.of(modulePath, Constant.TEST_OUTPUT_DIR, Constant.TEST_GCDA_OUTPUT_DIR);
        Path targetTestOutputsPath = Path.of(modulePath, Constant.TEST_OUTPUT_DIR, Constant.OUTPUT);
        boolean isTargetBuildGcnoOutputCreated = targetBuildGcnoOutputPath.toFile().mkdir();
        if (!isTargetBuildGcnoOutputCreated) {
            LOG.info("Failed to create the .test/build_gcno_output folder.");
        }
        boolean isTargetTestGcdaOutputCreated = targetTestGcdaOutputPath.toFile().mkdir();
        if (!isTargetTestGcdaOutputCreated) {
            LOG.info("Failed to create the .test/test_gcno_output folder.");
        }
        boolean isTargetTestOutputsCreated = targetTestOutputsPath.toFile().mkdir();
        if (!isTargetTestOutputsCreated) {
            LOG.info("Failed to create the .test/outputs folder.");
        }
        if (buildCoverageOutputPath.toFile().exists()) {
            FileUtils.copyDirectory(buildCoverageOutputPath.toFile(), targetBuildGcnoOutputPath.toFile());
            FileUtils.deleteDirectory(buildCoverageOutputPath.toFile());
        }
        if (testCoverageOutputPath.toFile().exists()) {
            FileUtils.copyDirectory(testCoverageOutputPath.toFile(), targetTestGcdaOutputPath.toFile());
            FileUtils.deleteDirectory(testCoverageOutputPath.toFile());
        }
        if (coverageOutputPath.toFile().exists()) {
            FileUtils.copyDirectory(coverageOutputPath.toFile(), targetTestOutputsPath.toFile());
            FileUtils.deleteDirectory(coverageOutputPath.toFile());
        }
        // execute cjpm clean
        TestUtil.executeCjCmd(testPath, "&&" + Constant.CJPM + " " + Constant.CLEAN, module);

        // refresh file system
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(testPath.toString());
        if (virtualFile == null) {
            return;
        }
        virtualFile.refresh(false, false);
    }

    @Override
    public void onThrowable(Exception e) {

    }
}
