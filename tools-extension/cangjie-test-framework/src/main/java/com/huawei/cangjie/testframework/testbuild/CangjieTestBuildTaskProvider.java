/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.testbuild;

import static com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestResultParser.SCRIPT_DIR_OF_COVERAGE_REPORT;
import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.isHarModule;
import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.isOhpmInstallCheck;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.cangjie.testframework.utils.LocalTestUtil;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.build.ohos.api.TestCoveragePlugin;
import com.huawei.deveco.build.ohos.api.TestCoveragePluginImpl;
import com.huawei.deveco.debugger.ohos.util.ResourceLoader;
import com.huawei.deveco.hdclib.ohos.hdc.Hilog;
import com.huawei.deveco.ohos.testframework.utils.ConfigBundle;
import com.huawei.deveco.ohos.testframework.utils.HvigorTaskModel;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyLogBundle;
import com.huawei.deveco.ohos.testframework.utils.TestHvigorService;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Icon;

/**
 * CangjieTestBuildTaskProvider
 *
 * @since 2025/02/20
 */
public class CangjieTestBuildTaskProvider extends BeforeRunTaskProvider<CangjieTestBuildTask>
        implements BuildCangjieTestProviderRepo {
    /**
     * Task ID
     */
    public static final Key<CangjieTestBuildTask> OBJECT_KEY = Key.create("CangjieTest.Build.Hvigor.BeforeRunTask");

    /**
     * Task name
     */
    public static final String TASK_NAME = "Build Cangjie Tests";

    private static final Logger LOG = Logger.getInstance(CangjieTestBuildTaskProvider.class);

    private static final String LOG_TAG = "CangjieTestBuildTaskProvider";

    @Override
    public Key<CangjieTestBuildTask> getId() {
        return OBJECT_KEY;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ResourceLoader.ARK_ICON_16;
    }

    @Nullable
    @Override
    public Icon getTaskIcon(CangjieTestBuildTask task) {
        return ResourceLoader.ARK_ICON_16;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean canExecuteTask(@NotNull RunConfiguration configuration, @NotNull CangjieTestBuildTask task) {
        return super.canExecuteTask(configuration, task);
    }

    @Nullable
    @Override
    public CangjieTestBuildTask createTask(@NotNull RunConfiguration runConfiguration) {
        if (runConfiguration instanceof CangjieTestRunConfiguration) {
            CangjieTestBuildTask task = new CangjieTestBuildTask(runConfiguration);
            task.setEnabled(true);
            return task;
        } else {
            return null;
        }
    }

    @Override
    public boolean executeTask(@NotNull DataContext dataContext,
                               @NotNull RunConfiguration runConfiguration,
                               @NotNull ExecutionEnvironment executionEnvironment,
                               @NotNull CangjieTestBuildTask buildTask) {
        return doExecuteTask(dataContext, runConfiguration, executionEnvironment, buildTask);
    }

    private boolean doExecuteTask(@NotNull DataContext context, @NotNull RunConfiguration runConfiguration,
                                  @NotNull ExecutionEnvironment env, @NotNull CangjieTestBuildTask task) {
        Project project = env.getProject();
        if (!isOhpmInstallCheck(project)) {
            return false;
        }
        if (!(runConfiguration instanceof CangjieTestRunConfiguration configuration)) {
            return false;
        }
        OhosModuleModel module = configuration.getModule();
        HvigorProductV2 hvigorProductV2 = ProductManager.getInstance().getCurrentProduct(module.getProjectModel());
        OhosTarget ohosTarget = TargetManager.getInstance().getCurrentTarget(module);
        boolean isAsan = configuration.isAsanEnable();
        String[] targets = new String[] {ohosTarget == null ? Constant.DEFAULT : ohosTarget.getName()};
        String product = hvigorProductV2.getName();
        HvigorTaskModel hvigorTaskModel = new HvigorTaskModel();
        hvigorTaskModel.setProject(project)
                .setAsan(isAsan)
                .setModule(module)
                .setEnv(env)
                .setAutoDependencies(configuration.isAutoDependencies())
                .setHasDependency(!configuration.getDependenciesPaths().isEmpty())
                .setProduct(product)
                .setTargets(targets);
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            return runLocalTestTask(configuration, hvigorTaskModel, module, project);
        }
        return runOhosTestTask(configuration, hvigorTaskModel, module, project);
    }

    private boolean runLocalTestTask(CangjieTestRunConfiguration configuration,
                                    HvigorTaskModel hvigorTaskModel, OhosModuleModel module, Project project) {
        if (!configuration.getIsRunWithCoverage()) {
            return LocalTestUtil.assembleLocalTest(configuration, hvigorTaskModel);
        }

        // 检查coverage插件是否安装
        TestCoveragePlugin testCoveragePlugin = new TestCoveragePluginImpl(module.getProjectModel());
        String newPluginPath = testCoveragePlugin.getCoveragePluginNodeModulesPathByProject(project);
        File coverageDirFile = new File(Paths.get(newPluginPath, SCRIPT_DIR_OF_COVERAGE_REPORT).toString());
        if (StringUtil.isEmpty(newPluginPath) || !coverageDirFile.exists()) {
            boolean isInstallCoveragePlugin = testCoveragePlugin.installCoveragePlugin(
                    ConfigBundle.info("coverage.plugin.version"));
            if (!isInstallCoveragePlugin) {
                Hilog.warn(LOG_TAG, OpenHarmonyLogBundle.message("install.coverage.plugin.error"));
                return false;
            }
        }
        return LocalTestUtil.assembleLocalTestWithCoverage(configuration, hvigorTaskModel);
    }

    private boolean runOhosTestTask(CangjieTestRunConfiguration configuration,
                                           HvigorTaskModel hvigorTaskModel, OhosModuleModel module, Project project) {
        if (!configuration.getIsRunWithCoverage()) {
            return assembleOhosTest(configuration, hvigorTaskModel);
        }

        // 检查coverage插件是否安装
        TestCoveragePlugin testCoveragePlugin = new TestCoveragePluginImpl(module.getProjectModel());
        String newPluginPath = testCoveragePlugin.getCoveragePluginNodeModulesPathByProject(project);
        File coverageDirFile = new File(Paths.get(newPluginPath, SCRIPT_DIR_OF_COVERAGE_REPORT).toString());
        if (StringUtil.isEmpty(newPluginPath) || !coverageDirFile.exists()) {
            boolean isInstallCoveragePlugin = testCoveragePlugin.installCoveragePlugin(
                    ConfigBundle.info("coverage.plugin.version"));
            if (!isInstallCoveragePlugin) {
                Hilog.warn(LOG_TAG, OpenHarmonyLogBundle.message("install.coverage.plugin.error"));
                return false;
            }
        }
        return assembleOhosTestWithCoverage(configuration, hvigorTaskModel);
    }

    private boolean assembleOhosTest(@NotNull CangjieTestRunConfiguration runConfiguration,
                                     @NotNull HvigorTaskModel hvigorTaskModel) {
        if (isHarModule(hvigorTaskModel.getModule())) {
            return assembleHar(runConfiguration, hvigorTaskModel, false);
        } else if (Constant.SHARED.equals(hvigorTaskModel.getModule().getModuleType())) {
            return TestHvigorService.assembleHsp(hvigorTaskModel)
                    && TestHvigorService.assembleOhosTestForHsp(hvigorTaskModel);
        } else if (runConfiguration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.MAIN_PATH) {
            return TestUtil.assembleHapForOhosTest(runConfiguration, hvigorTaskModel);
        } else {
            return TestUtil.assembleHapForOhosTest(runConfiguration, hvigorTaskModel);
        }
    }

    private boolean assembleOhosTestWithCoverage(@NotNull CangjieTestRunConfiguration runConfiguration,
                                                 @NotNull HvigorTaskModel hvigorTaskModel) {
        try {
            // need to clean build when run with coverage
            OhosModuleModel module = runConfiguration.getModule();
            Path buildOutputPath = Path.of(module.getModulePath(), Constant.BUILD);
            if (buildOutputPath.toFile().exists()) {
                FileUtils.deleteDirectory(buildOutputPath.toFile());
            }
            Path testOutputPath = Path.of(module.getModulePath(), Constant.TEST_OUTPUT_DIR);
            if (testOutputPath.toFile().exists()) {
                FileUtils.deleteDirectory(testOutputPath.toFile());
            }
        } catch (IOException exception) {
            LOG.warn("Failed to delete build or .test folder when run test with coverage");
        }

        if (isHarModule(hvigorTaskModel.getModule())) {
            return assembleHar(runConfiguration, hvigorTaskModel, true);
        } else if (Constant.SHARED.equals(hvigorTaskModel.getModule().getModuleType())) {
            return TestHvigorService.assembleHspCoverage(hvigorTaskModel)
                    && TestHvigorService.assembleOhosTestCoverageForHsp(hvigorTaskModel);
        } else if (runConfiguration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.MAIN_PATH) {
            return TestUtil.assembleHapCoverage(runConfiguration, hvigorTaskModel);
        } else {
            return TestUtil.assembleHapCoverage(runConfiguration, hvigorTaskModel);
        }
    }


    private boolean assembleHar(@NotNull CangjieTestRunConfiguration runConfiguration,
                                @NotNull HvigorTaskModel hvigorTaskModel, boolean isCoverage) {
        boolean isSuccess = true;
        if (hvigorTaskModel.hasDependency()) {
            // assemble hsp when har depends on hsp
            isSuccess = isCoverage
                    ? TestHvigorService.assembleHspCoverage(hvigorTaskModel)
                    : TestHvigorService.assembleHsp(hvigorTaskModel);
        }
        return isSuccess && isCoverage
                ? TestUtil.assembleOhosTestCoverageForHar(runConfiguration, hvigorTaskModel)
                : TestUtil.assembleOhosTestForHar(runConfiguration, hvigorTaskModel);
    }

    @Override
    public Key<CangjieTestBuildTask> getKey() {
        return OBJECT_KEY;
    }
}
