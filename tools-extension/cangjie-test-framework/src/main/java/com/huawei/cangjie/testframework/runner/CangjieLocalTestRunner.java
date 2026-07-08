/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import com.huawei.cangjie.debugger.localtest.CangjieLocalTestTask;
import com.huawei.cangjie.debugger.localtest.LocalTestParam;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.result.CangjieTestResultParser;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.cangjie.testframework.utils.LocalTestUtil;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestResultParserModel;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestRunnerListener;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CangjieLocalTestRunner
 *
 * @since 2025/08/28
 */
public class CangjieLocalTestRunner {
    /**
     * executor
     */
    public static final ExecutorService EXECUTOR_SERVICE;

    private static final Logger LOG = Logger.getInstance(CangjieLocalTestRunner.class);

    private static final int TIMEOUT = 3 * 1000;

    private static final int DEFAULT_DEBUG_TIME_OUT = 600000;

    static {
        int sysCoreCount = Runtime.getRuntime().availableProcessors();
        int threadCnt = sysCoreCount > 1 ? (sysCoreCount / 2) : sysCoreCount;
        EXECUTOR_SERVICE = new ThreadPoolExecutor(threadCnt, threadCnt, 10L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private Map<String, String> argMap;

    private CangjieTestResultParser parser;

    private final String runnerName;

    private OhosTraceHelper mTraceHelper;

    private final OpenHarmonyLaunchAppInfo appInfo;

    private final OpenHarmonyConsolePrinter printer;

    private final CangjieTestRunConfiguration configuration;

    private final OpenHarmonyLaunchStatus launchStatus;

    private final Map<Pair<String, String>, String> instrumentCommands = new HashMap<>();

    public CangjieLocalTestRunner(CangjieTestRunConfiguration configuration,
                                 OpenHarmonyLaunchAppInfo appInfo, String runnerName,
                                 OpenHarmonyConsolePrinter printer, OpenHarmonyLaunchStatus launchStatus) {
        this.appInfo = appInfo;
        this.printer = printer;
        this.configuration = configuration;
        this.launchStatus = launchStatus;
        argMap = new Hashtable<>();
        this.runnerName = runnerName;
    }

    /**
     * run
     *
     * @param listeners listeners
     */
    public void run(TestRunnerListener... listeners) {
        run(Arrays.asList(listeners));
    }

    /**
     * run
     *
     * @param listeners listeners
     */
    public void run(Collection<TestRunnerListener> listeners) {
        TestResultParserModel parserModel =
                new TestResultParserModel(runnerName == null ? appInfo.getBundleName() : runnerName, null,
                        launchStatus, appInfo, printer);
        parser = new CangjieTestResultParser(listeners, parserModel);
        runCallbackMode();
    }

    private void runCallbackMode() {
        ModuleModel model = this.configuration.getModule();
        if (model == null) {
            return;
        }
        ProjectModel projectModel = model.getProjectModel();
        if (projectModel == null) {
            LOG.warn("projectModel is null");
            return;
        }
        String testReportOutputPath = Path.of(
                model.getModulePath(),
                Constant.TEST_OUTPUT_DIR,
                Constant.TEST_REPORTS_OUTPUT_DIR).toAbsolutePath().normalize().toString();
        Map<String, String> localTestNeedEnv = LocalTestUtil.getLocalTestNeedEnv(model, this.appInfo.isDebug());
        String sdkPath = TestUtil.getSdkPath(projectModel);
        Map<Pair<String, String>, String> targetInstrumentCommands = getLocalTestCommands();
        String batPath = Path.of(sdkPath, Constant.BUILD_TOOLS, Constant.ENV_SCRIPT_WIN).toString();
        if (SystemInfo.isMac) {
            batPath = Path.of(sdkPath, Constant.BUILD_TOOLS, Constant.ENV_SCRIPT_MAC).toString();
        }
        this.parser.parse(Constant.TEST_RUNNER_ON_RUN);
        for (Map.Entry<Pair<String, String>, String> item : targetInstrumentCommands.entrySet()) {
            String packageName = item.getKey().first;
            String className = item.getKey().second;
            String xmlName = "test-" + packageName + "." + className + ".xml";
            if (StringUtils.isEmpty(item.getKey().first) || StringUtils.isEmpty(className)) {
                continue;
            }
            this.parser.parse(Constant.EXECUTE_PACKAGE_START + packageName);
            this.parser.parse(Constant.EXECUTE_CLASS_START + className);
            String command = item.getValue();
            ProcessBuilder processBuilder;
            if (SystemInfo.isWindows) {
                processBuilder = new ProcessBuilder(
                        Constant.WIN_BAT, Constant.WIN_BAT_OPTION, "\"" + batPath + "\"" + "&&" + command);
            } else {
                processBuilder = new ProcessBuilder(Constant.MAC_BASH, Constant.MAC_BASH_OPTION,
                        "source " + "\"" + batPath + "\"" + "&&" + command);
            }
            // set need env
            Map<String, String> processEnv = processBuilder.environment();
            for (Map.Entry<String, String> env : localTestNeedEnv.entrySet()) {
                String key = env.getKey();
                String value = env.getValue();
                String oldPath = processEnv.get(key);
                processBuilder.environment().put(key, value + File.pathSeparator + oldPath);
            }

            // execute test
            if (!executeCommand(command, model, processBuilder, className)) {
                continue;
            }

            // read xml
            Path xmlPath = Path.of(testReportOutputPath, Constant.TEST_REPORTS_TESTS_OUTPUT_DIR, xmlName);
            if (!xmlPath.toFile().exists()) {
                LOG.warn("local test xml file does not exist.");
                this.parser.parse(Constant.EXECUTE_CLASS_END + className);
                continue;
            }
            Future<String> future = EXECUTOR_SERVICE.submit(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(xmlPath.toFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            });

            try {
                String content = future.get();
                this.parser.parse(content);
                this.parser.parse(Constant.EXECUTE_CLASS_END + className);
            } catch (ExecutionException | InterruptedException e) {
                LOG.warn("read or parse local test xml file failed.");
            }
        }
        if (this.configuration.getIsRunWithCoverage()) {
            this.parser.parse(Constant.GENERATE_COVERAGE);
        }
        this.parser.parse(Constant.TEST_FINISHED_RESULT_CODE + "0");
    }

    private boolean executeCommand(String command, ModuleModel model, ProcessBuilder processBuilder, String className) {
        if (this.appInfo.isDebug()) {
            String[] parts = command.split(" ");
            if (parts.length < 1) {
                return false;
            }
            List<String> args = Arrays.asList(parts).subList(1, parts.length);
            LocalTestParam localTestParam = new LocalTestParam(
                    model, appInfo.getProject(), parts[0], processBuilder.environment(), args);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            CangjieLocalTestTask.debug(localTestParam, future);
            boolean executed = future.join();
        } else {
            Process process;
            try {
                process = processBuilder.inheritIO().start();
                ProcessHandler processHandler = this.configuration.getProcessHandler();
                processHandler.addProcessListener(new ProcessAdapter() {
                    @Override
                    public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
                        LocalTestUtil.killProcessTree(process);
                    }

                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        if (process.isAlive()) {
                            LocalTestUtil.killProcessTree(process);
                        }
                    }
                });
            } catch (IOException e) {
                LOG.warn("local test execute failed.");
                this.parser.parse(Constant.EXECUTE_CLASS_END + className);
                return false;
            }

            Future<?> stdoutFuture = EXECUTOR_SERVICE.submit(() -> {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    reader.lines().forEach(System.out::println);
                } catch (IOException exception) {
                    LOG.warn("local test execute failed.");
                }
            });
            Future<?> stderrFuture = EXECUTOR_SERVICE.submit(() -> {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    reader.lines().forEach(System.err::println);
                } catch (IOException exception) {
                    LOG.warn("local test execute failed.");
                }
            });
            try {
                stdoutFuture.get();
                stderrFuture.get();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    LOG.warn("process exitCode: " + exitCode);
                }
            } catch (InterruptedException | ExecutionException exception) {
                LOG.warn("local test execute failed.");
                this.parser.parse(Constant.EXECUTE_CLASS_END + className);
                return false;
            } finally {
                LocalTestUtil.closeQuietly(process.getInputStream());
                LocalTestUtil.closeQuietly(process.getOutputStream());
                LocalTestUtil.closeQuietly(process.getErrorStream());
                LocalTestUtil.killProcessTree(process);
            }
        }
        return true;
    }

    /**
     * get local test commands
     *
     * @return local test commands
     */
    public Map<Pair<String, String>, String> getLocalTestCommands() {
        if (!instrumentCommands.isEmpty()) {
            return instrumentCommands;
        }
        ModuleModel model = this.configuration.getModule();
        if (model == null || model.getProjectModel() == null) {
            return instrumentCommands;
        }
        Project project = model.getProjectModel().getProject();
        if (project == null) {
            return instrumentCommands;
        }
        String testReportOutputPath = Path.of(
                model.getModulePath(),
                Constant.TEST_OUTPUT_DIR,
                Constant.TEST_REPORTS_OUTPUT_DIR).toAbsolutePath().normalize().toString();
        List<String> packageNames = this.configuration.getPackageNameMap().keySet().stream().toList();
        String binDir = "release";
        String mockDir = "";
        if (LocalTestUtil.checkIsMock(model.getProjectModel().getProject())) {
            mockDir = "mock";
        }
        // find product name
        String productName = "default";
        HvigorProductV2 product =
                ProductManager.getInstance().getCurrentProduct(CommonProjectUtil.getProjectModel(project));
        if (product != null) {
            productName = product.getName();
        }
        // find target name
        String targetName = "default";
        if (!ModuleType.HAR.toString().equalsIgnoreCase(model.getModuleType())) {
            OhosTarget currentTarget = TargetManager.getInstance().getCurrentTarget(model);
            if (currentTarget != null) {
                targetName = currentTarget.getName();
            }
        }
        Path buildPath = Path.of(model.getModulePath(),
                "build", productName, "intermediates", "cj", "build", targetName, mockDir, binDir, "unittest_bin");
        for (String packageName : packageNames) {
            if (StringUtils.isEmpty(packageName)) {
                continue;
            }
            String binaryName = packageName;
            if (SystemInfo.isWindows) {
                binaryName += ".exe";
            }
            String binaryPath = Path.of(buildPath.toString(), binaryName).toAbsolutePath().normalize().toString();
            List<String> testClass = List.of(this.configuration.getClassName().split(Constant.COMMA));
            if (this.configuration.getTestingType() == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
                testClass = this.configuration.getPackageNameMap().get(packageName);
            } else {
                this.configuration.getPackageNameMap().put(packageName, testClass);
            }
            if (testClass == null) {
                continue;
            }
            for (String testClassName : testClass) {
                List<String> hdcArgs = new ArrayList<>();
                hdcArgs.add(binaryPath);
                if (this.configuration.getTestingType() == CangjieTestRunConfiguration.TestingType.TEST_METHOD) {
                    String methodName = this.configuration.getMethodName().replaceAll("<[^>]*>", "<*>");
                    String target = testClassName + "." + methodName;
                    if (!StringUtils.isEmpty(methodName) && !methodName.equals(this.configuration.getMethodName())) {
                        target = "\"" + testClassName + "." + methodName + "\"";
                    }
                    hdcArgs.add("--filter=" + target);
                } else {
                    hdcArgs.add("--filter=" + testClassName);
                }
                hdcArgs.add("--report-path=" + testReportOutputPath);
                instrumentCommands.put(
                        Pair.create(packageName, testClassName), String.join(" ", hdcArgs)
                );
            }
        }

        return instrumentCommands;
    }

    public void setTraceHelper(OhosTraceHelper mTraceHelper) {
        this.mTraceHelper = mTraceHelper;
    }

    /**
     * addLocalTestArg
     *
     * @param name name
     * @param value value
     */
    public void addLocalTestArg(String name, String value) {
        if (name != null && value != null) {
            argMap.put(name, value);
        } else {
            throw new IllegalArgumentException(OpenHarmonyHintBundle.message("name.or.value.arguments.cannot.null"));
        }
    }
}
