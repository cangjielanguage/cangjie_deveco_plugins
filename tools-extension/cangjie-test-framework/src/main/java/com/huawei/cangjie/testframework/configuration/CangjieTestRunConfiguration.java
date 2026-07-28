/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.configuration;

import static com.huawei.cangjie.testframework.runner.CangjieTestProgramRunner.EXECUTOR_ID;
import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.isHarModule;

import com.huawei.cangjie.testframework.console.CangjieLocalTestLocationProvider;
import com.huawei.cangjie.testframework.console.CangjieOhosTestLocationProvider;
import com.huawei.cangjie.testframework.console.CangjieTestConsoleProperties;
import com.huawei.cangjie.testframework.dialog.CangjieTestRunConfigurationEditor;
import com.huawei.cangjie.testframework.dialog.CangjieTestRunParameters;
import com.huawei.cangjie.testframework.runprofilestate.LocalTestRunProfileState;
import com.huawei.cangjie.testframework.utils.TestFrameworkBundle;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.debug.OpenHarmonyAutoDebugger;
import com.huawei.deveco.debugger.ohos.deployment.DeviceItem;
import com.huawei.deveco.debugger.ohos.deployment.DeviceSelector;
import com.huawei.deveco.debugger.ohos.deployment.OpenHarmonyDeviceItem;
import com.huawei.deveco.debugger.ohos.run.OpenHarmonyParameters;
import com.huawei.deveco.debugger.ohos.run.app.info.LaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.debugger.ohos.util.DebuggerUtil;
import com.huawei.deveco.debugger.ohos.util.ProjectUtil;
import com.huawei.deveco.hdclib.ohos.client.Client;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.hdclib.ohos.hdc.HarmonyDebugConnector;
import com.huawei.deveco.ohos.debugcommon.DebugClientListener;
import com.huawei.deveco.ohos.debugcommon.module.ModuleWrapper;
import com.huawei.deveco.ohos.testframework.run.configuration.ConfigurationError;
import com.huawei.deveco.ohos.testframework.run.configuration.OpenHarmonyHarRunState;
import com.huawei.deveco.ohos.testframework.run.history.HistoryHvigorProductV2;
import com.huawei.deveco.ohos.testframework.run.history.HistoryModuleModel;
import com.huawei.deveco.ohos.testframework.run.history.HistoryOhosTarget;
import com.huawei.deveco.ohos.testframework.run.history.HistoryProjectModel;
import com.huawei.deveco.ohos.testframework.utils.OhosTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyLogBundle;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyPanelBundle;
import com.huawei.deveco.ohos.testframework.utils.PsiUtils;
import com.huawei.deveco.projectmodel.hos.v2.impl.HosProductV2;
import com.huawei.deveco.projectmodel.hos.v2.impl.HosProjectModelV2;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.history.actions.AbstractImportTestsAction;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.DefaultDebugProcessHandler;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * CangjieTestRunConfiguration
 *
 * @since 2025/02/20
 */
public class CangjieTestRunConfiguration extends OpenHarmonyRunConfiguration implements Externalizable,
        SMRunnerConsolePropertiesProvider {
    private static final Logger LOG = Logger.getInstance(CangjieTestRunConfiguration.class);

    private static final long serialVersionUID = 2991929520121409508L;

    private static final String UNNAMED = "Unnamed";

    private static final String ALL_TESTS_IN = "All Tests in '";

    private static final String RIGHT_BRACKET = ")'";

    private static final String LEFT_BRACKET = " (";

    private static final String OHOS_TEST = "OhosTest";

    private static final String LOCAL_TEST = "LocalTest";

    private static final String PACKAGE_NAME = "PACKAGE_NAME";

    private static final String WHOLE_PACKAGE_NAME = "WHOLE_PACKAGE_NAME";

    private static final String FILE_PATH = "FILE_PATH";

    private static final String CLASS_NAME = "CLASS_NAME";

    private static final String METHOD_NAME = "METHOD_NAME";

    private static final String VALUE = "value";

    private static final String HISTORY_PROJECT_MODEL = "HISTORY_PROJECT_MODEL";

    private static final String HISTORY_MODULE_MODEL = "HISTORY_MODULE_MODEL";

    private static final String HISTORY_OHOS_TARGET = "HISTORY_OHOS_TARGET";

    private static final String HISTORY_HVIGOR_PRODUCT_V2 = "HISTORY_HVIGOR_PRODUCT_V2";

    private static final String NAME = "name";

    private static final String ABILITY_INFOS = "abilityInfos";

    private static final String HAP_MODULE_INFOS = "hapModuleInfos";

    private TestPathType testPathType = TestPathType.OHOS_TEST_PATH;

    private TestingType testingType = TestingType.TEST_ALL_IN_PACKAGE;

    private String packageName = "";

    private String className = "";

    private String methodName = "";

    private String filePath = "";

    private String wholePackageName = "";

    private int testType;

    private Project project;

    private Boolean isKeepDataSelected = false;

    private String timeout = Constant.DEFAULT_TIME_OUT;

    private String testArgs = "";

    private boolean isRunWithCoverage = false;

    private ConsoleView consoleView;

    private ProcessHandler processHandler;

    private HistoryModuleModel historyModuleModel;

    private HistoryProjectModel historyProjectModel;

    private HistoryOhosTarget historyOhosTarget;

    private HistoryHvigorProductV2 historyHvigorProductV2;

    private boolean isOnlyOhosTestPackage = false;

    private Map<String, List<String>> packageNameMap = new HashMap<>();

    private List<String> dependenciesPaths = new ArrayList<>();

    private String ohosTestModuleName;

    public CangjieTestRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
        super.setTest(true);
        this.project = project;
    }

    @Override
    public String getActionName() {
        String actionName = getName();
        // Omit when longer than 20 chars.
        return actionName.length() <= 20 ? actionName : actionName.substring(0, 20) + Constant.ELLIPSIS;
    }

    @Override
    public boolean isGeneratedName() {
        String name = getName();
        if ((testingType == TestingType.TEST_CLASS || testingType == TestingType.TEST_METHOD)
                && (filePath == null || filePath.isEmpty())) {
            return name.startsWith(UNNAMED);
        }
        if (testingType == TestingType.TEST_METHOD && (methodName == null)) {
            return name.startsWith(UNNAMED);
        }
        return Objects.equals(name, suggestedName());
    }

    @Nullable
    @Override
    public String suggestedName() {
        if (testingType == TestingType.TEST_ALL_IN_PACKAGE) {
            if (getModule() != null) {
                return ALL_TESTS_IN + packageName + LEFT_BRACKET + getModule().getModuleName() + RIGHT_BRACKET;
            }
            return ExecutionBundle.message("test.in.scope.presentable.text", packageName);
        } else if (testingType == TestingType.TEST_FILE) {
            return ProgramRunnerUtil.shortenName(getShortTestFileName(filePath), 0);
        } else if (testingType == TestingType.TEST_CLASS) {
            return className;
        } else if (testingType == TestingType.TEST_METHOD) {
            return methodName + Constant.PARENTHESES_PAIR;
        } else {
            return OpenHarmonyPanelBundle.message("ohosTest.all.tests.scope.presentable.text");
        }
    }

    private String getShortTestFileName(@Nullable String fqName) {
        return fqName == null ? "" : StringUtil.getShortName(fqName.replaceAll("\\\\", "/"), '/');
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        CangjieTestRunConfigurationEditor<CangjieTestRunConfiguration> editor
                = new CangjieTestRunConfigurationEditor<>(project, this);
        editor.setConfigurationSpecificEditor(new CangjieTestRunParameters(project, this, editor));
        return editor;
    }

    @Override
    @Nullable
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        OhosTestUtils.setStartTime(System.currentTimeMillis());
        // set necessary environment info when configuration comes from test history
        if (env.getRunProfile() instanceof AbstractImportTestsAction.ImportRunProfile) {
            LOG.info(OpenHarmonyLogBundle.message("configuration.comes.from.test.history"));
            setNecessaryEnvInfo();
        } else {
            getNecessaryEnvInfo();
        }
        setIsRunWithCoverage(EXECUTOR_ID.equals(executor.getId()));
        dependenciesPaths.clear();
        ProjectModel projectModel = ProjectModelManager.getInstance().getTargetProjectModel(env.getProject());
        List<OhosModuleModel> selectModuleList = ProjectUtil.getSelectMultiModules(projectModel, this);
        selectModuleList.forEach(module -> dependenciesPaths.add(module.getModulePath()));
        if (testPathType == TestPathType.LOCAL_TEST_PATH) {
            return getLocalTestRunProfileState(executor, env);
        }
        return getOhosTestRunProfileState(executor, env);
    }

    @Nullable
    private RunProfileState getOhosTestRunProfileState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        DeviceItem deviceItem = DeviceSelector.getSelectedDeviceItem();
        if (deviceItem == null) {
            DebuggerUtil.notifyError(OpenHarmonyHintBundle.message("select.device"), project);
            return null;
        }
        if (Constant.PREVIEWER_DEBUG.equals(deviceItem.getDeviceType())) {
            DebuggerUtil.notifyError(OpenHarmonyPanelBundle.message("previewer.not.support"), project);
            return null;
        }
        if (StringUtils.isEmpty(getDebugType())) {
            setDebugType(OpenHarmonyAutoDebugger.DISPLAY_NAME);
        }
        if (!isHarModule(getModule())) {
            return super.getState(executor, env);
        }

        boolean isDebug = executor instanceof DefaultDebugExecutor;
        // 防止hdc重启后添加的监听器被清空，因此需要重新添加监听器,测试框架也会执行到此处
        if (isDebug) {
            HarmonyDebugConnector.addJsDebugClientChangeListener(DebugClientListener.getInstance());
        }
        LaunchAppInfo launchAppInfo = LaunchAppInfo.getLaunchAppInfo(env.getProject());
        if (launchAppInfo instanceof OpenHarmonyLaunchAppInfo openHarmonyLaunchAppInfo) {
            openHarmonyLaunchAppInfo.setRunStartTime(System.currentTimeMillis());
        }

        ProcessHandler curProcessHandler = new DefaultDebugProcessHandler();
        createAndAttach(curProcessHandler, env.getProject(), executor);
        DebuggerUtil.detachExistingProcess(env);
        ModuleWrapper moduleWrapper = new ModuleWrapper(getModule());
        OpenHarmonyParameters parameters =
                constructParameters(env, moduleWrapper, consoleView, curProcessHandler, deviceItem);
        return new OpenHarmonyHarRunState(parameters);
    }

    @NotNull
    private RunProfileState getLocalTestRunProfileState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        ProcessHandler curProcessHandler = new DefaultDebugProcessHandler();
        createAndAttach(curProcessHandler, env.getProject(), executor);
        LaunchAppInfo launchAppInfo = LaunchAppInfo.getLaunchAppInfo(env.getProject());
        return new LocalTestRunProfileState(this, env, curProcessHandler, launchAppInfo);
    }

    private void getNecessaryEnvInfo() {
        OhosModuleModel ohosModuleModel = getModule();
        this.setHistoryModuleModel(HistoryModuleModel.getHistoryModuleModel(ohosModuleModel));
        ProjectModel projectModel = ohosModuleModel.getProjectModel();
        this.setHistoryProjectModel(HistoryProjectModel.getHistoryProjectModel(projectModel));
        OhosTarget ohosTarget = TargetManager.getInstance().getCurrentTarget(ohosModuleModel);
        if (ohosTarget == null) {
            ohosTarget = ohosModuleModel.getTargetMap().get(Constant.DEFAULT);
        }
        this.setHistoryOhosTarget(HistoryOhosTarget.getHistoryOhosTarget(ohosTarget));
        HvigorProductV2 hvigorProductV2 = ProductManager.getInstance().getCurrentProduct(projectModel);
        this.setHistoryHvigorProductV2(HistoryHvigorProductV2.getHistoryHvigorProductV2(ohosModuleModel,
                ohosTarget, hvigorProductV2));
    }

    private OpenHarmonyParameters constructParameters(ExecutionEnvironment env, ModuleWrapper module,
                                                      ConsoleView consoleView, ProcessHandler processHandler,
                                                      DeviceItem deviceItem) {
        OpenHarmonyParameters params = new OpenHarmonyParameters();
        params.environment = env;
        params.module = module;
        params.isDeployMultiHap = false;
        params.isReinstall = false;
        params.configuration = this;
        params.consoleView = consoleView;
        params.processHandler = processHandler;
        params.isTest = true;
        params.deviceItem = deviceItem;
        return params;
    }

    private void setNecessaryEnvInfo() {
        // build virtual hvigorProductV2
        HvigorProductV2 hvigorProductV2 = HistoryHvigorProductV2.getHvigorProductV2(historyHvigorProductV2);
        if (!(hvigorProductV2 instanceof HosProductV2 hosProductV2)) {
            return;
        }
        HashMap<String, HosProductV2> productMap = new HashMap<>();
        String productName = StringUtils.isEmpty(hosProductV2.getName()) ? Constant.DEFAULT : hosProductV2.getName();
        productMap.put(productName, hosProductV2);

        // build virtual projectModel
        ProjectModel projectModel = HistoryProjectModel.getProjectModel(historyProjectModel);
        if (!(projectModel instanceof HosProjectModelV2 hosProjectModelV2)) {
            return;
        }
        hosProjectModelV2.setProject(project);
        hosProjectModelV2.setProjectPath(project.getBasePath());
        hosProjectModelV2.setProductMap(productMap);

        // build virtual moduleModel
        ModuleModel moduleModel = HistoryModuleModel.getModuleModel(historyModuleModel);
        if (!(moduleModel instanceof OhosModuleModel ohosModuleModel)) {
            return;
        }

        // build virtual ohosTarget
        OhosTarget ohosTarget = HistoryOhosTarget.getOhosTarget(historyOhosTarget, ohosModuleModel);
        HashMap<String, OhosTarget> targetMap = new HashMap<>();
        targetMap.put(Constant.DEFAULT, ohosTarget);

        // build virtual others
        ohosModuleModel.setTargetMap(targetMap);
        ohosModuleModel.setProjectModel(hosProjectModelV2);
        setModule(ohosModuleModel);
    }

    /**
     * 创建测试框架控制台
     *
     * @param handler handler
     * @param project project
     * @param executor executor
     * @return ConsoleView
     */
    @Override
    protected ConsoleView createAndAttach(@NotNull ProcessHandler handler, Project project, Executor executor) {
        this.processHandler = handler;
        String protocolId = CangjieOhosTestLocationProvider.PROTOCOL_ID;
        if (testPathType == TestPathType.LOCAL_TEST_PATH) {
            protocolId = CangjieLocalTestLocationProvider.PROTOCOL_ID;
        }
        CangjieTestConsoleProperties properties = new CangjieTestConsoleProperties(this, executor, protocolId);
        // consoleView toolbar 工具栏action 按钮默认状态设置
        TestConsoleProperties.HIDE_PASSED_TESTS.set(properties, false);
        // 默认开启单击跳转至source
        TestConsoleProperties.SCROLL_TO_SOURCE.set(properties, true);
        try {
            if (testPathType == TestPathType.LOCAL_TEST_PATH) {
                consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(LOCAL_TEST, handler, properties);
            } else {
                consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(OHOS_TEST, handler, properties);
            }
            Disposer.register(project, consoleView);
        } catch (ExecutionException e) {
            LOG.warn(OpenHarmonyLogBundle.message("get.console.view.failed"));
        }
        return consoleView;
    }

    /**
     * 校验configuration
     *
     * @throws RuntimeConfigurationException runtime configuration exception
     */
    @Override
    public final void checkConfiguration() throws RuntimeConfigurationException {
        List<ConfigurationError> configurationErrorList = validate();
        if (configurationErrorList.isEmpty()) {
            return;
        }
        String message = configurationErrorList.get(0).getMessage();
        Runnable quickfix = configurationErrorList.get(0).getQuickfix();
        String severity = configurationErrorList.get(0).getSeverity().toString();
        for (ConfigurationError error : configurationErrorList) {
            if (error.getSeverity().equals(ConfigurationError.Severity.FATAL)) {
                message = error.getMessage();
                quickfix = error.getQuickfix();
                severity = error.getSeverity().toString();
                break;
            }
        }

        switch (severity) {
            case "FATAL":
                throw new RuntimeConfigurationError(message, quickfix);
            case "WARNING":
                throw new RuntimeConfigurationWarning(message, quickfix);
            case "INFO":
            default:
                break;
        }
    }

    /**
     * 校验configuration
     *
     * @return errorList
     */
    public List<ConfigurationError> validate() {
        List<ConfigurationError> errors = new ArrayList<>();
        if (timeout.length() > 6 || !timeout.matches(Constant.TIMEOUT_REGEX) || !(Integer.parseInt(timeout)
                <= 60 * 60 * 24)) {
            errors.add(ConfigurationError.fatal(OpenHarmonyHintBundle.message("timeout.number.error")));
            return errors;
        }
        if (getModule() == null) {
            errors.add(ConfigurationError.fatal(OpenHarmonyHintBundle.message("module.is.null.message")));
            return errors;
        }
        String modulePath = getModule().getModulePath();
        if (testingType == TestingType.TEST_ALL_IN_PACKAGE) {
            validatePackage(modulePath, errors);
            return errors;
        }
        validFile(modulePath, errors);
        if (testingType == TestingType.TEST_FILE) {
            return errors;
        }
        validateClass(errors);
        if (testingType == TestingType.TEST_CLASS) {
            return errors;
        }
        validateMethod(errors);
        return errors;
    }

    private void validatePackage(String modulePath, List<ConfigurationError> errors) {
        if (StringUtils.isEmpty(packageName)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("package.path.empty")));
            return;
        }
        if (StringUtils.isEmpty(PsiUtils.replaceBackSlashOfSlash(wholePackageName))) {
            errors.add(ConfigurationError.fatal(OpenHarmonyHintBundle.message("package.not.specified")));
            return;
        }
        if (!PsiUtils.replaceBackSlashOfSlash(wholePackageName).contains(modulePath)) {
            errors.add(ConfigurationError.fatal(OpenHarmonyHintBundle.message("package.not.in.current.module")));
            return;
        }
        if (testPathType == TestPathType.MAIN_PATH && !isUnderCjCodePath(modulePath, wholePackageName)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("package.not.exist.main.path")));
            return;
        }
        if (testPathType == TestPathType.OHOS_TEST_PATH && !isUnderCjCodePath(modulePath, wholePackageName)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("package.not.exist.test.path")));
        }
        if (testPathType == TestPathType.LOCAL_TEST_PATH && !isUnderCjCodePath(modulePath, wholePackageName)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("package.not.exist.test.path")));
        }
    }

    private void validFile(String modulePath, List<ConfigurationError> errors) {
        if (StringUtils.isEmpty(filePath)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("file.path.empty")));
            return;
        }
        if (!PsiUtils.replaceBackSlashOfSlash(filePath).contains(modulePath)) {
            errors.add(ConfigurationError.fatal(OpenHarmonyHintBundle.message("file.not.in.current.module")));
            return;
        }
        if (!filePath.endsWith(Constant.CJ_TEST_FILE_SUFFIX)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("test.file.invalid")));
            return;
        }
        if (testPathType == TestPathType.MAIN_PATH && !isUnderCjCodePath(modulePath, filePath)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("file.not.exist.main.path")));
            return;
        }
        if (testPathType == TestPathType.OHOS_TEST_PATH && !isUnderCjCodePath(modulePath, filePath)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("file.not.exist.ohos.test.path")));
        }
        if (testPathType == TestPathType.LOCAL_TEST_PATH && !isUnderCjCodePath(modulePath, filePath)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("file.not.exist.local.test.path")));
        }
    }

    private boolean isUnderCjCodePath(String modulePath, String path) {
        // main path
        if (testPathType == TestPathType.MAIN_PATH) {
            String parentPath = Path.of(modulePath, Constant.SRC, Constant.MAIN, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
            return TestUtil.isSameOrSubPath(parentPath, path);
        }
        // ohosTest path
        if (testPathType == TestPathType.OHOS_TEST_PATH) {
            String parentPath = Path.of(modulePath, Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
            return TestUtil.isSameOrSubPath(parentPath, path);
        }
        // local test path
        String parentPath = Path.of(modulePath, Constant.SRC, Constant.TEST, Constant.CANGJIE)
                .toAbsolutePath().normalize().toString();
        return TestUtil.isSameOrSubPath(parentPath, path);
    }

    private void validateClass(List<ConfigurationError> errors) {
        if (StringUtils.isEmpty(className)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("class.name.empty")));
            return;
        }
        if (testPathType == TestPathType.LOCAL_TEST_PATH) {
            return;
        }
        List<String> registerClassName = TestUtil.getRegisterClassNamesInPackage(project, filePath);
        if (registerClassName == null || !registerClassName.contains(className)) {
            errors.add(ConfigurationError.fatal(
                    TestFrameworkBundle.message("run.target.class.not.found.in.register.file")));
        }
    }

    private void validateMethod(List<ConfigurationError> errors) {
        if (StringUtils.isEmpty(methodName)) {
            errors.add(ConfigurationError.fatal(TestFrameworkBundle.message("method.name.empty")));
            return;
        }
        Set<String> methodNames = TestUtil.getClassNamesByFile(project, filePath, className);
        if (methodNames == null || !methodNames.contains(methodName)) {
            errors.add(ConfigurationError.fatal(
                    TestFrameworkBundle.message("run.target.method.not.found.in.parent.class")));
        }
    }

    /**
     * readExternal
     *
     * @param element element
     * @throws InvalidDataException invalid data exception
     */
    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        readHistoryExternal(element);
        testType = CangjieTestConfigurationProducer.TESTING_CANGJIE;
        Element testingTypeEle = element.getChild(Constant.TESTING_TYPE);
        if (testingTypeEle != null) {
            try {
                testingType = TestingType.fromCode(Integer.parseInt(readExternalCompatibly(testingTypeEle)));
            } catch (NumberFormatException e) {
                testingType = TestingType.TEST_ALL_IN_PACKAGE;
                LOG.warn(OpenHarmonyLogBundle.message("invalid.testing.type"));
            }
        }
        Element keepDataElement = element.getChild(Constant.KEEP_DATA);
        if (keepDataElement != null) {
            isKeepDataSelected = readExternalCompatibly(keepDataElement).equals(Constant.STRING_TRUE);
        }
        Element timeoutElement = element.getChild(Constant.TIMEOUT);
        if (timeoutElement != null) {
            timeout = readExternalCompatibly(timeoutElement);
        }
        Element testPathTypeEle = element.getChild(Constant.TEST_PATH_TYPE);
        if (testPathTypeEle != null) {
            try {
                testPathType = TestPathType.fromCode(Integer.parseInt(readExternalCompatibly(testPathTypeEle)));
            } catch (NumberFormatException e) {
                testPathType = TestPathType.OHOS_TEST_PATH;
                LOG.warn("Read cache invalid test path type.");
            }
        }
        Element onlyOhosTestPackageElement = element.getChild(Constant.IS_ONLY_OHOS_TEST_PACKAGE);
        if (onlyOhosTestPackageElement != null) {
            isOnlyOhosTestPackage = readExternalCompatibly(onlyOhosTestPackageElement).equals(Constant.STRING_TRUE);
        }
        // optional arg testArgs should not block serialization, get text when it is not null
        Element testArgsElement = element.getChild(Constant.TEST_ARGS);
        testArgs = testArgsElement == null ? StringUtils.EMPTY : readExternalCompatibly(testArgsElement);
        Element moduleNameElement = element.getChild(Constant.MODULE_NAME);
        if (moduleNameElement != null) {
            setModuleName(readExternalCompatibly(moduleNameElement));
        }
        setConfigurationFromTestingType(element);
    }

    private void readHistoryExternal(Element element) {
        Element historyModuleModelElement = element.getChild(HISTORY_MODULE_MODEL);
        if (historyModuleModelElement != null) {
            historyModuleModel = JSONObject.parseObject(readExternalCompatibly(historyModuleModelElement),
                    HistoryModuleModel.class);
        }
        Element historyProjectModelElement = element.getChild(HISTORY_PROJECT_MODEL);
        if (historyProjectModelElement != null) {
            historyProjectModel = JSONObject.parseObject(readExternalCompatibly(historyProjectModelElement),
                    HistoryProjectModel.class);
        }
        Element historyOhosTargetElement = element.getChild(HISTORY_OHOS_TARGET);
        if (historyOhosTargetElement != null) {
            historyOhosTarget = JSONObject.parseObject(readExternalCompatibly(historyOhosTargetElement),
                    HistoryOhosTarget.class);
        }
        Element historyHvigorProductV2Element = element.getChild(HISTORY_HVIGOR_PRODUCT_V2);
        if (historyHvigorProductV2Element != null) {
            historyHvigorProductV2 = JSONObject.parseObject(readExternalCompatibly(historyHvigorProductV2Element),
                    HistoryHvigorProductV2.class);
        }
    }

    private void setConfigurationFromTestingType(@NotNull Element element) {
        Element packageNameEle = element.getChild(PACKAGE_NAME);
        if (packageNameEle != null) {
            packageName = readExternalCompatibly(packageNameEle);
            if (!StringUtils.isEmpty(packageName)) {
                packageNameMap.put(packageName, new ArrayList<>());
            }
        }
        if (testingType == TestingType.TEST_ALL_IN_PACKAGE) {
            Element wholePackageNameEle = element.getChild(WHOLE_PACKAGE_NAME);
            if (wholePackageNameEle != null) {
                wholePackageName = readExternalCompatibly(wholePackageNameEle);
            }
        }
        Element filePathEle = element.getChild(FILE_PATH);
        if (filePathEle != null) {
            filePath = readExternalCompatibly(filePathEle);
        }
        if (testingType == TestingType.TEST_FILE) {
            return;
        }
        Element classNameEle = element.getChild(CLASS_NAME);
        if (classNameEle != null) {
            className = readExternalCompatibly(classNameEle);
        }
        if (testingType == TestingType.TEST_CLASS) {
            return;
        }
        Element methodNameEle = element.getChild(METHOD_NAME);
        if (methodNameEle != null) {
            methodName = readExternalCompatibly(methodNameEle);
        }
    }

    private String readExternalCompatibly(Element element) {
        if (element == null) {
            return StringUtils.EMPTY;
        }
        return element.getAttribute(VALUE) == null ? element.getText() : element.getAttributeValue(VALUE);
    }

    /**
     * writeExternal
     *
     * @param element element
     * @throws WriteExternalException write external exception
     */
    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeHistoryExternal(element);
        element.addContent(new Element(Constant.TEST_BAR_TYPE).setAttribute(VALUE, String.valueOf(testType)));
        element.addContent(new Element(Constant.TESTING_TYPE).setAttribute(VALUE, String.valueOf(testingType.code)));
        element.addContent(new Element(Constant.KEEP_DATA).setAttribute(VALUE, String.valueOf(isKeepDataSelected)));
        element.addContent(new Element(Constant.TIMEOUT).setAttribute(VALUE, String.valueOf(timeout)));
        element.addContent(new Element(Constant.TEST_ARGS).setAttribute(VALUE, String.valueOf(testArgs)));
        element.addContent(new Element(Constant.TEST_PATH_TYPE).setAttribute(VALUE, String.valueOf(testPathType.code)));
        element.addContent((new Element(Constant.MODULE_NAME)).setAttribute(VALUE, String.valueOf(getModuleName())));
        element.addContent((new Element(Constant.IS_ONLY_OHOS_TEST_PACKAGE))
                .setAttribute(VALUE, String.valueOf(isOnlyOhosTestPackage)));
        element.addContent(new Element(PACKAGE_NAME).setAttribute(VALUE, String.valueOf(packageName)));
        if (testingType == TestingType.TEST_ALL_IN_PACKAGE) {
            element.addContent(new Element(WHOLE_PACKAGE_NAME).setAttribute(VALUE, String.valueOf(wholePackageName)));
        }
        element.addContent(new Element(FILE_PATH).setAttribute(VALUE, String.valueOf(filePath)));
        if (testingType == TestingType.TEST_FILE) {
            return;
        }
        element.addContent(new Element(CLASS_NAME).setAttribute(VALUE, String.valueOf(className)));
        if (testingType == TestingType.TEST_CLASS) {
            return;
        }
        element.addContent(new Element(METHOD_NAME).setAttribute(VALUE, String.valueOf(methodName)));
    }

    private void writeHistoryExternal(Element element) {
        element.addContent(new Element(HISTORY_PROJECT_MODEL)
                .setAttribute(VALUE, JSONObject.toJSONString(historyProjectModel)));
        element.addContent(new Element(HISTORY_MODULE_MODEL)
                .setAttribute(VALUE, JSONObject.toJSONString(historyModuleModel)));
        element.addContent(new Element(HISTORY_OHOS_TARGET)
                .setAttribute(VALUE, JSONObject.toJSONString(historyOhosTarget)));
        LaunchAppInfo launchAppInfo = LaunchAppInfo.getLaunchAppInfo(project);
        if (historyHvigorProductV2 != null
                && StringUtils.isEmpty(historyHvigorProductV2.getBundleName()) && launchAppInfo != null) {
            historyHvigorProductV2.setBundleName(launchAppInfo.getBundleName());
        }
        element.addContent(new Element(HISTORY_HVIGOR_PRODUCT_V2)
                .setAttribute(VALUE, JSONObject.toJSONString(historyHvigorProductV2)));
    }

    /**
     * get current configuration root path
     *
     * @return current configuration root path
     */
    public String getCurConfigRootPath() {
        if (this.testPathType == TestPathType.MAIN_PATH) {
            return Path.of(getModule().getModulePath(), Constant.SRC, Constant.MAIN, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
        }
        if (this.testPathType == TestPathType.OHOS_TEST_PATH) {
            return Path.of(getModule().getModulePath(), Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
        }
        return Path.of(getModule().getModulePath(), Constant.SRC, Constant.TEST, Constant.CANGJIE)
                .toAbsolutePath().normalize().toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) {
    }

    @Override
    public void readExternal(ObjectInput in) {
    }

    @Override
    @NotNull
    public SMTRunnerConsoleProperties createTestConsoleProperties(@NotNull Executor executor) {
        String protocolId = CangjieOhosTestLocationProvider.PROTOCOL_ID;
        if (testPathType == TestPathType.LOCAL_TEST_PATH) {
            protocolId = CangjieLocalTestLocationProvider.PROTOCOL_ID;
        }
        return new CangjieTestConsoleProperties(this, executor, protocolId);
    }

    public Boolean getKeepDataSelected() {
        return isKeepDataSelected;
    }

    public void setKeepDataSelected(Boolean canKeepDataSelected) {
        isKeepDataSelected = canKeepDataSelected;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public Map<String, List<String>> getPackageNameMap() {
        return packageNameMap;
    }

    public void setPackageNameMap(Map<String, List<String>> packageNameMap) {
        this.packageNameMap = packageNameMap;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public String getClassName() {
        return className;
    }

    public String getWholePackageName() {
        return wholePackageName;
    }

    public void setWholePackageName(String wholePackageName) {
        this.wholePackageName = wholePackageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public TestingType getTestingType() {
        return testingType;
    }

    public void setTestingType(TestingType testingType) {
        this.testingType = testingType;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getAbilityName() {
        return isOnlyOhosTestPackage || isHarModule(getModule()) ? getTestAbilityName() : super.getAbilityName();
    }

    private String getTestAbilityName() {
        LOG.info("isOnlyOhosTestPackage getTestAbilityName");
        DeviceItem deviceItem = LaunchAppInfo.getLaunchAppInfo(project).getDeviceItem();
        if (!(deviceItem instanceof OpenHarmonyDeviceItem openHarmonyDeviceItem)) {
            return StringUtils.EMPTY;
        }
        Devices device = openHarmonyDeviceItem.getOpenHarmonyDevice();
        if (Objects.isNull(device)) {
            return StringUtils.EMPTY;
        }
        Client client = device.getClient();
        try {
            String bundleName = ProjectModelManager.getInstance().getTargetProjectModel(project).getBundleName();
            JSONObject dumpJson = JSONObject.parseObject(client.getDumpJson(bundleName, 1000));
            JSONArray hapModuleInfos = dumpJson.getJSONArray(HAP_MODULE_INFOS);
            if (hapModuleInfos == null || hapModuleInfos.isEmpty()) {
                return StringUtils.EMPTY;
            }
            for (int i = 0; i < hapModuleInfos.size(); i++) {
                JSONArray abilityInfos = hapModuleInfos.getJSONObject(i).getJSONArray(ABILITY_INFOS);
                if (abilityInfos != null && !abilityInfos.isEmpty()) {
                    return abilityInfos.getJSONObject(0).getString(NAME);
                }
            }
        } catch (JSONException e) {
            LOG.warn(OpenHarmonyLogBundle.message("dumpJson.error"));
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    public int getTestType() {
        return testType;
    }

    public boolean getIsRunWithCoverage() {
        return isRunWithCoverage;
    }

    public void setIsRunWithCoverage(boolean isRunWithCoverage) {
        this.isRunWithCoverage = isRunWithCoverage;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    public void setProcessHandler(ProcessHandler processHandler) {
        this.processHandler = processHandler;
    }

    public String getTestArgs() {
        return testArgs;
    }

    public void setTestArgs(String testArgs) {
        this.testArgs = testArgs;
    }

    public HistoryModuleModel getHistoryModuleModel() {
        return historyModuleModel;
    }

    public void setHistoryModuleModel(HistoryModuleModel historyModuleModel) {
        this.historyModuleModel = historyModuleModel;
    }

    public HistoryProjectModel getHistoryProjectModel() {
        return historyProjectModel;
    }

    public void setHistoryProjectModel(HistoryProjectModel historyProjectModel) {
        this.historyProjectModel = historyProjectModel;
    }

    public HistoryOhosTarget getHistoryOhosTarget() {
        return historyOhosTarget;
    }

    public void setHistoryOhosTarget(HistoryOhosTarget historyOhosTarget) {
        this.historyOhosTarget = historyOhosTarget;
    }

    public HistoryHvigorProductV2 getHistoryHvigorProductV2() {
        return historyHvigorProductV2;
    }

    public void setHistoryHvigorProductV2(HistoryHvigorProductV2 historyHvigorProductV2) {
        this.historyHvigorProductV2 = historyHvigorProductV2;
    }

    public Boolean getOnlyOhosTestPackage() {
        return isOnlyOhosTestPackage;
    }

    public void setOnlyOhosTestPackage(Boolean onlyOhosTestPackage) {
        isOnlyOhosTestPackage = onlyOhosTestPackage;
    }

    public List<String> getDependenciesPaths() {
        return dependenciesPaths;
    }

    public void setDependenciesPaths(List<String> dependenciesPaths) {
        this.dependenciesPaths = dependenciesPaths;
    }

    public String getOhosTestModuleName() {
        return ohosTestModuleName;
    }

    public void setOhosTestModuleName(String ohosTestModuleName) {
        this.ohosTestModuleName = ohosTestModuleName;
    }

    public TestPathType getTestPathType() {
        return testPathType;
    }

    public void setTestPathType(TestPathType testPathType) {
        this.testPathType = testPathType;
    }

    /**
     * get testing type
     *
     * @return testing type
     */
    public String getTestingTypeString() {
        return switch (testingType) {
            case TEST_ALL_IN_PACKAGE -> TraceCangjieTestType.PACKAGE.getTestType();
            case TEST_FILE -> TraceCangjieTestType.FILE.getTestType();
            case TEST_CLASS -> TraceCangjieTestType.CLASS.getTestType();
            case TEST_METHOD -> TraceCangjieTestType.METHOD.getTestType();
            default -> TraceCangjieTestType.BLANK.getTestType();
        };
    }

    /**
     * TestPathType
     */
    public enum TestPathType {
        UNDEFINED(-1),
        MAIN_PATH(0),
        OHOS_TEST_PATH(1),
        LOCAL_TEST_PATH(2);

        private final int code;

        /**
         * get type
         *
         * @param code code
         * @return type
         */
        public static TestPathType fromCode(int code) {
            for (TestPathType type : TestPathType.values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return UNDEFINED;
        }

        TestPathType(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    /**
     * TestingType
     */
    public enum TestingType {
        UNDEFINED(-1),
        TEST_ALL_IN_PACKAGE(0),
        TEST_FILE(1),
        TEST_CLASS(2),
        TEST_METHOD(3);

        private final int code;

        /**
         * get type
         *
         * @param code code
         * @return type
         */
        public static TestingType fromCode(int code) {
            for (TestingType type : TestingType.values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return UNDEFINED;
        }

        TestingType(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        @Override
        public String toString() {
            return String.valueOf(this.code);
        }
    }
}
