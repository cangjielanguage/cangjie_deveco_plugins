/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_WARNING_OUTPUT;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.cangjie.testframework.utils.LocalTestUtil;
import com.huawei.cangjie.testframework.utils.TestFrameworkBundle;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.deployment.DeviceItem;
import com.huawei.deveco.debugger.ohos.run.app.info.LaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.tasks.LaunchTask;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.ohos.testframework.psi.PsiSuiteSourcePath;
import com.huawei.deveco.ohos.testframework.tasks.OpenHarmonyTestLaunchTaskExecutor;
import com.huawei.deveco.ohos.testframework.utils.OhosTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.ohos.testframework.utils.PsiUtils;
import com.huawei.deveco.ohos.testframework.utils.TestProjectUtils;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CangjieTestLaunchTaskExecutor
 *
 * @since 2025/02/20
 */
public class CangjieTestLaunchTaskExecutor extends OpenHarmonyTestLaunchTaskExecutor {
    private static Map<Project, CangjieTestLaunchTaskExecutor> executorMap = new ConcurrentHashMap<>();

    private OpenHarmonyLaunchAppInfo ohosLaunchAppInfo;
    private CangjieTestRunConfiguration cangjieTestRunConfiguration;

    @Override
    public OpenHarmonyTestLaunchTaskExecutor getInstance(Project project) {
        // 多工程同时启动或调试时，根据project获取对应的LaunchTasksExecutor实例，防止相互影响
        // 如果map中没有对应project的LaunchTasksExecutor, 则新建一个
        if (executorMap.get(project) == null) {
            executorMap.put(project, new CangjieTestLaunchTaskExecutor());
        }
        return executorMap.get(project);
    }

    @Override
    public void perform(ExecutionEnvironment env,
                        ProgressIndicator indicator,
                        Task.Backgroundable task,
                        LaunchAppInfo launchAppInfo,
                        ProcessHandler processHandler) {
        RunConfiguration config = launchAppInfo.getConfiguration();
        if (!(config instanceof CangjieTestRunConfiguration configuration)) {
            return;
        }
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            this.env = env;
            this.project = env.getProject();
            this.launchAppInfo = launchAppInfo;
            this.consolePrinter = new OpenHarmonyConsolePrinter(processHandler);
            this.launchStatus = new OpenHarmonyLaunchStatus(processHandler);
            this.performLaunchTasks(indicator, task);
            return;
        }
        super.perform(env, indicator, task, launchAppInfo, processHandler);
    }

    @Override
    public List<LaunchTask> getTasks() {
        if (launchAppInfo instanceof OpenHarmonyLaunchAppInfo) {
            ohosLaunchAppInfo = (OpenHarmonyLaunchAppInfo) launchAppInfo;
        }
        OhosModuleModel ohosModule = launchAppInfo.getModuleWrapper().getOhosModule();
        if (OhosTestUtils.shouldWarn(ohosModule)) {
            String warning = OpenHarmonyHintBundle.message("mock.config.json5.file.detected");
            launchAppInfo.getConsoleView().print(warning, LOG_WARNING_OUTPUT);
        }
        RunConfiguration config = launchAppInfo.getConfiguration();
        List<LaunchTask> launchTasks = new ArrayList<>();
        if (!(config instanceof CangjieTestRunConfiguration configuration)) {
            return launchTasks;
        }
        cangjieTestRunConfiguration = configuration;
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            setLocalTestLaunchTask(launchTasks);
        } else {
            setOhosTestLaunchTask(launchTasks);
        }
        return launchTasks;
    }

    private void setLocalTestLaunchTask(List<LaunchTask> launchTasks) {
        if (cangjieTestRunConfiguration.getTestingType()
                == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            setAllRunClassNamesByPackage(cangjieTestRunConfiguration);
        }
        // test file need to collect test class names
        if (cangjieTestRunConfiguration.getTestingType() == CangjieTestRunConfiguration.TestingType.TEST_FILE) {
            List<String> classNames = new ArrayList<>(TestUtil.getClassNamesByFile(
                    cangjieTestRunConfiguration.getProject(),
                    cangjieTestRunConfiguration.getFilePath(), StringUtils.EMPTY));
            StringBuilder sb = new StringBuilder();
            for (String className : classNames) {
                if (StringUtils.isEmpty(className)) {
                    continue;
                }
                if (!sb.isEmpty()) {
                    sb.append(Constant.COMMA);
                }
                sb.append(className);
            }
            cangjieTestRunConfiguration.setClassName(sb.toString());
        }
        CangjieTestLaunchParameters cangjieTestLaunchParameters = initCangjieTestLaunchParameters();
        // launch task
        CangjieLocalTestLaunchTask cangjieLocalTestLaunchTask =
                new CangjieLocalTestLaunchTask(cangjieTestLaunchParameters);
        launchTasks.add(cangjieLocalTestLaunchTask);
    }

    private void setOhosTestLaunchTask(List<LaunchTask> launchTasks) {
        // deploy hap task
        CangjieOhosTestDeployHapTask cangjieOhosTestDeployHapTask = new CangjieOhosTestDeployHapTask(
                project, cangjieTestRunConfiguration, device, ohosLaunchAppInfo, consolePrinter);
        launchTasks.add(cangjieOhosTestDeployHapTask);
        // get all class name which can run(is registered)
        setAllRunClassName(cangjieTestRunConfiguration);
        if (StringUtils.isEmpty(cangjieTestRunConfiguration.getClassName())) {
            // current select test scope was not registered, return
            consolePrinter.stderr(TestFrameworkBundle.message("run.target.class.not.found.in.register.file"));
            launchStatus.terminateLaunch(StringUtils.EMPTY, true);
            return;
        }
        ReadAction.run(() -> {
            setFindPathBySuiteAndModuleNameMap(cangjieTestRunConfiguration);
        });
        CangjieTestLaunchParameters cangjieTestLaunchParameters = initCangjieTestLaunchParameters();
        // launch task
        CangjieOhosTestLaunchTask cangjieOhosTestLaunchTask =
                new CangjieOhosTestLaunchTask(cangjieTestLaunchParameters);
        launchTasks.add(cangjieOhosTestLaunchTask);
    }

    private void setFindPathBySuiteAndModuleNameMap(CangjieTestRunConfiguration configuration) {
        ProjectModel projectModel = ProjectModelManager.getInstance().getTargetProjectModel(project);
        if (projectModel == null) {
            return;
        }
        List<OhosModuleModel> moduleModels = TestProjectUtils.getAcceptedModules(projectModel);
        Set<String> filePathInList = new HashSet<>();
        String modulePath;
        HashMap<String, HashMap<String, String>> findPathBySuiteAndModuleNameMap = new HashMap<>();
        for (ModuleModel moduleModel : moduleModels) {
            if (!configuration.getModule().getModuleName().equals(moduleModel.getModuleName())) {
                continue;
            }
            filePathInList.addAll(PsiUtils.getAllFilePathInList(moduleModel.getModulePath(), project));
            modulePath = moduleModel.getModulePath();
            if (filePathInList.isEmpty() || modulePath.isEmpty()) {
                continue;
            }
            HashMap<String, String> suitePathMap = new HashMap<>();
            for (String path : filePathInList) {
                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                if (virtualFile == null) {
                    continue;
                }
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile == null) {
                    continue;
                }
                List<String> describeNames = PsiUtils.getAllDescribeNamesWithoutUnregistered(psiFile, project,
                        modulePath, false);
                for (String describeName : describeNames) {
                    suitePathMap.put(describeName, path);
                }
            }
            findPathBySuiteAndModuleNameMap.put(moduleModel.getModuleName(), suitePathMap);
        }
        PsiSuiteSourcePath.getInstance(project).setSuiteSourcePathMap(findPathBySuiteAndModuleNameMap);
    }

    private CangjieTestLaunchParameters initCangjieTestLaunchParameters() {
        CangjieTestLaunchParameters cangjieTestLaunchParameters = new CangjieTestLaunchParameters();
        cangjieTestLaunchParameters.setRunner(StringUtils.EMPTY);
        cangjieTestLaunchParameters.setDevice(device);
        cangjieTestLaunchParameters.setArguments(Collections.emptyMap());
        cangjieTestLaunchParameters.setConfiguration(cangjieTestRunConfiguration);
        cangjieTestLaunchParameters.setConsolePrinter(consolePrinter);
        cangjieTestLaunchParameters.setLaunchStatus(launchStatus);
        cangjieTestLaunchParameters.setLaunchAppInfo(ohosLaunchAppInfo);
        return cangjieTestLaunchParameters;
    }

    private void setAllRunClassName(CangjieTestRunConfiguration configuration) {
        CangjieTestRunConfiguration.TestingType testingType = configuration.getTestingType();
        List<String> registeredClassNames = new ArrayList<>();
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            registeredClassNames.addAll(setAllRunClassNamesByPackage(configuration));
        } else {
            Set<String> classNames = new HashSet<>();
            classNames.add(configuration.getClassName());
            if (testingType == CangjieTestRunConfiguration.TestingType.TEST_FILE) {
                classNames.addAll(TestUtil.getClassNamesByFile(
                        configuration.getProject(), configuration.getFilePath(), StringUtils.EMPTY));
            }
            // get all register class names
            List<String> registerInCurPkg = TestUtil.getRegisterClassNamesInPackage(
                    configuration.getProject(), configuration.getFilePath());
            // remove class name when it is not registered
            Iterator<String> iterator = classNames.iterator();
            while (iterator.hasNext()) {
                String className = iterator.next();
                if (registerInCurPkg.contains(className)) {
                    registeredClassNames.add(className);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String className : registeredClassNames) {
            if (StringUtils.isEmpty(className)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(Constant.COMMA);
            }
            sb.append(className);
        }
        configuration.setClassName(sb.toString());
    }

    private List<String> setAllRunClassNamesByPackage(CangjieTestRunConfiguration configuration) {
        List<String> registerClassNames = new ArrayList<>();
        String packagePath = configuration.getWholePackageName();
        Path path = Path.of(packagePath);
        if (!path.toFile().exists()) {
            return registerClassNames;
        }
        if (!path.toFile().isDirectory()) {
            path = Path.of(path.toFile().getParent()).toAbsolutePath().normalize();
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path.toString());
        if (virtualFile == null) {
            return registerClassNames;
        }
        boolean isOhosTest = configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH;
        Map<String, List<String>> packageNameMap = new HashMap<>();
        configuration.setPackageNameMap(packageNameMap);
        Queue<VirtualFile> queue = new LinkedList<>();
        queue.add(virtualFile);
        while (!queue.isEmpty()) {
            VirtualFile currentDir = queue.poll();
            if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
                String curPackageName = LocalTestUtil.getLocalPackageName(configuration.getModule(), currentDir);
                List<String> curPkgRegisterClassNames = LocalTestUtil.getAllTestClassInOnePkg(
                        configuration.getModule(), curPackageName, launchAppInfo.isDebug());
                packageNameMap.put(curPackageName, curPkgRegisterClassNames);
            } else {
                List<String> curPkgRegisterClassNames =
                        TestUtil.getRegisterClassNamesInPackage(project, currentDir.getCanonicalPath());
                registerClassNames.addAll(curPkgRegisterClassNames);
                String curPackageName = TestUtil.getPackageName(configuration.getModule(), currentDir, isOhosTest);
                packageNameMap.put(curPackageName, curPkgRegisterClassNames);
            }

            VirtualFile[] subDirs = currentDir.getChildren();
            if (subDirs == null) {
                continue;
            }
            for (VirtualFile subDir : subDirs) {
                // check is contain cj file
                if (!TestUtil.hasCjFiles(subDir)) {
                    continue;
                }
                queue.add(subDir);
            }
        }
        return registerClassNames;
    }

    @Override
    public boolean canRun(Project project, LaunchAppInfo appInfo) {
        if (appInfo == null) {
            return false;
        }

        DeviceItem deviceItem = appInfo.getDeviceItem();
        if (appInfo instanceof OpenHarmonyLaunchAppInfo) {
            ohosLaunchAppInfo = (OpenHarmonyLaunchAppInfo) appInfo;
        }
        RunConfiguration config = appInfo.getConfiguration();
        if (!(config instanceof CangjieTestRunConfiguration configuration)) {
            return false;
        }
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            return appInfo.isTest();
        }
        return deviceItem.isOhosDevice() && appInfo.isTest();
    }

    @Override
    public boolean isTest(Project project) {
        LaunchAppInfo appInfo = LaunchAppInfo.getLaunchAppInfo(project);
        if (appInfo == null) {
            return false;
        }
        return (appInfo.getConfiguration() instanceof CangjieTestRunConfiguration) && appInfo.isTest();
    }
}
