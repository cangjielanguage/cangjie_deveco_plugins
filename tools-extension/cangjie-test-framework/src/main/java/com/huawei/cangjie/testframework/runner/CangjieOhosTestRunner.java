/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.getAnonymousString;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_WARNING_OUTPUT;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.result.CangjieTestResultParser;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.hdclib.ohos.hdc.Hilog;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestResultParserModel;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestRunnerListener;
import com.huawei.deveco.ohos.testframework.utils.OhosTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyLogBundle;
import com.huawei.deveco.ohos.testframework.utils.ValidateResultEnum;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;

import com.intellij.openapi.diagnostic.Logger;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * CangjieTestRunner
 *
 * @since 2025/02/20
 */
public class CangjieOhosTestRunner {
    private static final Logger LOG = Logger.getInstance(CangjieOhosTestRunner.class);

    private static final String REMOTE_HARMONY_TEST = "RemoteHarmonyTest";

    private static final int TIMEOUT = 3 * 1000;

    private static final int DEFAULT_DEBUG_TIME_OUT = 600000;

    private static final String POUND_SIGN = "#";

    private static final String DS_STORE = ".DS_Store";

    private final Pattern processCommandPattern = Pattern.compile("^[\\.\\-\\_ 0-9a-zA-Z]+$");

    private Map<String, String> argMap;

    private Devices device;

    private CangjieTestResultParser parser;

    private String runnerName;

    private String className;

    private String methodName;

    private ValidateResultEnum validateResult = null;

    private OhosTraceHelper mTraceHelper;

    private final OpenHarmonyLaunchAppInfo appInfo;

    private final OpenHarmonyConsolePrinter printer;

    private final CangjieTestRunConfiguration configuration;

    private final OpenHarmonyLaunchStatus launchStatus;

    private final List<String> instrumentCommands = new ArrayList<>();

    public CangjieOhosTestRunner(CangjieTestRunConfiguration configuration,
                                 OpenHarmonyLaunchAppInfo appInfo, String runnerName, Devices device,
                                 OpenHarmonyConsolePrinter printer, OpenHarmonyLaunchStatus launchStatus) {
        this.appInfo = appInfo;
        this.printer = printer;
        this.configuration = configuration;
        this.launchStatus = launchStatus;
        argMap = new Hashtable<>();
        this.runnerName = runnerName;
        this.device = device;
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
                new TestResultParserModel(runnerName == null ? appInfo.getBundleName() : runnerName, device,
                        launchStatus, appInfo, printer);
        parser = new CangjieTestResultParser(listeners, parserModel);
        runCallbackMode();
    }

    private void runCallbackMode() {
        LOG.info("runCallbackMode");
        List<String> targetInstrumentCommands = getInstrumentCommands();
        String bundleName = appInfo.getBundleName();
        for (String command : targetInstrumentCommands) {
            Hilog.info(REMOTE_HARMONY_TEST,
                    String.format(Locale.ROOT, OpenHarmonyLogBundle.message("running"), command.replace(bundleName,
                            getAnonymousString(bundleName)), getAnonymousString(device.getName())));
            device.getClient().sendShellCommand(command, 0, parser);
            if (appInfo.isDebug()) {
                closeFreeze();
            }
        }
    }

    /**
     * aa test命令执行时间较长时，会导致后续连不上设备，此时执行冻结屏蔽操作
     *
     */
    private void closeFreeze() {
        String uid = "";
        String bundleName = appInfo.getBundleName();
        try {
            uid = device.getClient().getUidByBundleName(bundleName, TIMEOUT);
            device.getClient().closeFreeze(uid, bundleName, TIMEOUT);
        } catch (TimeoutException e) {
            LOG.error("get uid fail");
        }
    }

    public List<String> getInstrumentCommands() {
        if (!instrumentCommands.isEmpty()) {
            return instrumentCommands;
        }
        setWakeupCommand();
        List<String> hdcArgs = new ArrayList<>();
        hdcArgs.add("aa test -b");
        String bundleName = appInfo.getBundleName();
        hdcArgs.add(bundleName);
        String productName = "default";
        HvigorProductV2 product =
                ProductManager.getInstance().getCurrentProduct(CommonProjectUtil.getProjectModel(appInfo.getProject()));
        if (product != null) {
            productName = product.getName();
        }
        // find target name
        String targetName = "default";
        if (!ModuleType.HAR.toString().equalsIgnoreCase(appInfo.getModule().getModuleType())) {
            OhosTarget currentTarget = TargetManager.getInstance().getCurrentTarget(appInfo.getModule());
            if (currentTarget != null) {
                targetName = currentTarget.getName();
            }
        }
        String buildPath = Path.of(appInfo.getModule().getModulePath(), Constant.BUILD, productName,
                Constant.OUTPUTS, targetName).toString();
        if (appInfo.getModule().isHarLibrary()) {
            buildPath = Path.of(appInfo.getModule().getModulePath(), Constant.BUILD, productName,
                    Constant.OUTPUTS, Constant.OHOS_TEST).toString();
        }
        String testHapName = OhosTestUtils.getHapOrHspName(buildPath);
        if (!processCommandPattern.matcher(testHapName).find()) {
            printer.stderr(String.format(Locale.ENGLISH, OpenHarmonyHintBundle.message("invalid.hap.file.name")));
            OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.HAP_FILE_NAME_INVALID);
            return Collections.EMPTY_LIST;
        }
        hdcArgs.add("-m");
        hdcArgs.add(configuration.getOhosTestModuleName());
        hdcArgs.add("-s unittest");
        hdcArgs.add(findRunnerPath());
        String testName = className;
        testName = initTestTargetParams(testName);
        // 校验失败返回空的参数信息
        if (testName == null) {
            return Collections.EMPTY_LIST;
        }
        hdcArgs.add("-s class " + testName);
        if (appInfo.isDebug()) {
            hdcArgs.add("-D");
        }
        if (appInfo.getConfiguration() instanceof CangjieTestRunConfiguration testRunConfiguration) {
            if (testRunConfiguration.getIsRunWithCoverage()) {
                hdcArgs.add("-s coverage true");
            }
        }
        instrumentCommands.add(String.join(" ", hdcArgs));
        return instrumentCommands;
    }

    /**
     * set unlock screen command
     */
    public void setWakeupCommand() {
        String abilityName = configuration.getAbilityName();
        if (StringUtils.isEmpty(abilityName)) {
            return;
        }
        List<String> hdcArgs = new ArrayList<>();
        hdcArgs.add("aa start -b");
        hdcArgs.add(appInfo.getBundleName());
        hdcArgs.add("-a");
        hdcArgs.add(abilityName);
        hdcArgs.add("-m");
        hdcArgs.add(configuration.getOhosTestModuleName());
        instrumentCommands.add(String.join(" ", hdcArgs));
    }

    /**
     * 获取OpenHarmonyTestRunner路径，默认暂为OpenHarmonyTestRunner
     *
     * @return OpenHarmonyTestRunner路径
     */
    private String findRunnerPath() {
        return Constant.OPEN_HARMONY_TEST_RUNNER;
    }

    /**
     * 构造测试用例运行参数,
     * 测试套运行返回： className
     * 单个测试用例：   className#methodName
     * 整个测试包执行： className,className
     *
     * @param testName testName
     * @return str 测试参数
     */
    private String initTestTargetParams(String testName) {
        if (validateResult != null) {
            appInfo.getConsoleView().print(validateResult.getErrorMsg(), LOG_WARNING_OUTPUT);
        }

        String result = testName;
        if (className != null && !StringUtils.isEmpty(methodName)) {
            result = className + POUND_SIGN + methodName;
        }
        return result;
    }

    /**
     * setMethodName
     *
     * @param methodName methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * setClassName
     *
     * @param classNameForRunner String
     */
    public void setClassName(String classNameForRunner) {
        this.className = classNameForRunner;
    }

    public void setTraceHelper(OhosTraceHelper mTraceHelper) {
        this.mTraceHelper = mTraceHelper;
    }

    /**
     * setTestPackageName
     *
     * @param packageName packageName
     */
    public void setTestPackageName(String packageName) {
        addInstrumentationArg(Constant.PACKAGE, packageName);
    }

    /**
     * addInstrumentationArg
     *
     * @param name name
     * @param value value
     */
    public void addInstrumentationArg(String name, String value) {
        if (name != null && value != null) {
            argMap.put(name, value);
        } else {
            throw new IllegalArgumentException(OpenHarmonyHintBundle.message("name.or.value.arguments.cannot.null"));
        }
    }
}
