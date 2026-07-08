/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.configuration;

import com.huawei.cangjie.testframework.utils.LocalTestUtil;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProjectModelV2;
import com.huawei.deveco.res.ohos.utils.ModuleUtils;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * CangjieTestConfigurationProducer
 *
 * @since 2025/02/20
 */
public class CangjieTestConfigurationProducer extends RunConfigurationProducer<CangjieTestRunConfiguration> {
    /**
     * run configuration cangjie
     */
    public static final int TESTING_CANGJIE = 3;

    protected CangjieTestConfigurationProducer() {
        super(true);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull CangjieTestRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        Location<PsiElement> location = context.getLocation();
        if (location == null) {
            return false;
        }
        PsiElement element = location.getPsiElement();

        ProjectModel projectModel = ProjectModelManager.getInstance().getTargetProjectModel(element.getProject());
        if (!(projectModel instanceof HvigorProjectModelV2<?>)) {
            return false;
        }
        return setConfiguration(configuration, element);
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull CangjieTestRunConfiguration configuration,
                                              @NotNull ConfigurationContext configurationContext) {
        Location<PsiElement> location = configurationContext.getLocation();
        if (location == null) {
            return false;
        }
        PsiElement element = location.getPsiElement();

        ModuleModel moduleModel = ModuleUtils.findModuleModelByPsiElement(element);
        if (moduleModel == null) {
            return false;
        }
        if (!checkExistConfiguration(configuration, moduleModel)) {
            return false;
        }
        return checkIsExistConfiguration(configuration, element);
    }

    private boolean setConfiguration(CangjieTestRunConfiguration configuration, PsiElement element) {
        if (!TestUtil.isSupportTestRun(element)) {
            return false;
        }
        configuration.setTestType(TESTING_CANGJIE);
        return setCangjieConfiguration(configuration, element);
    }

    private boolean setCangjieConfiguration(CangjieTestRunConfiguration configuration, PsiElement element) {
        CangjieTestRunConfiguration.TestingType testingType = TestUtil.getTestingType(element);
        if (testingType == CangjieTestRunConfiguration.TestingType.UNDEFINED) {
            return false;
        }
        PsiFile elementFile = element.getContainingFile();
        if (elementFile == null) {
            return false;
        }
        VirtualFile virtualFile = elementFile.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        String filePath = virtualFile.getCanonicalPath();

        if (StringUtils.isEmpty(filePath)) {
            return false;
        }
        ModuleModel moduleModel = ModuleUtils.findModuleModelByPsiElement(element);
        if (!(moduleModel instanceof OhosModuleModel ohosModuleModel)) {
            return false;
        }
        configuration.setModule(ohosModuleModel);
        String packageName = "";
        if (filePath.contains("src/main/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.MAIN_PATH);
            packageName = TestUtil.getPackageName(
                    ohosModuleModel, element.getContainingFile().getVirtualFile(), false);
        } else if (filePath.contains("src/ohosTest/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH);
            packageName = TestUtil.getPackageName(
                    ohosModuleModel, element.getContainingFile().getVirtualFile(), true);
        } else if (filePath.contains("src/test/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH);
            packageName = LocalTestUtil.getLocalPackageName(
                    ohosModuleModel, element.getContainingFile().getVirtualFile());
        } else {
            return false;
        }

        if (!StringUtils.isEmpty(packageName)) {
            configuration.setPackageName(packageName);
            configuration.getPackageNameMap().put(packageName, new ArrayList<>());
        }
        // package
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            if (!(element instanceof PsiDirectory psiDirectory)) {
                return false;
            }
            configuration.setTestingType(CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE);
            String packagePath = psiDirectory.getVirtualFile().getCanonicalPath();
            configuration.setWholePackageName(packagePath);
            configuration.setGeneratedName();
            TestUtil.saveModuleInfo(configuration);
            return true;
        }
        // file
        configuration.setFilePath(filePath);
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_FILE) {
            configuration.setTestingType(CangjieTestRunConfiguration.TestingType.TEST_FILE);
            return true;
        }
        String className = "";
        // class
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_CLASS) {
            configuration.setTestingType(CangjieTestRunConfiguration.TestingType.TEST_CLASS);
            className = TestUtil.getTestClassNameByTestClass(element);
            if (StringUtils.isEmpty(className)) {
                return false;
            }
            configuration.setClassName(className);
            configuration.setGeneratedName();
            TestUtil.saveModuleInfo(configuration);
            return true;
        }
        // method
        className = TestUtil.getTestClassNameByTestCase(element);
        if (StringUtils.isEmpty(className)) {
            return false;
        }
        configuration.setClassName(className);
        configuration.setTestingType(CangjieTestRunConfiguration.TestingType.TEST_METHOD);
        String methodName = TestUtil.getTestCaseName(element);
        if (StringUtils.isEmpty(methodName)) {
            return false;
        }
        configuration.setMethodName(methodName);
        configuration.setGeneratedName();
        TestUtil.saveModuleInfo(configuration);
        return true;
    }

    private boolean checkExistConfiguration(CangjieTestRunConfiguration configuration, ModuleModel moduleModel) {
        ModuleModel moduleInConfig = configuration.getModule();
        return Comparing.equal(moduleModel, moduleInConfig);
    }

    private boolean checkIsExistConfiguration(CangjieTestRunConfiguration configuration,
                                              PsiElement element) {
        PsiFile elementFile = element.getContainingFile();
        if (elementFile == null) {
            return false;
        }
        if (elementFile instanceof PsiDirectory) {
            return checkPackageConfiguration(configuration, element);
        } else {
            return checkFileConfiguration(configuration, element, elementFile);
        }
    }

    private boolean checkFileConfiguration(CangjieTestRunConfiguration configuration,
                                           PsiElement element, PsiFile elementFile) {
        CangjieTestRunConfiguration.TestingType testingType = TestUtil.getTestingType(element);
        if (testingType != configuration.getTestingType()) {
            return false;
        }
        String path = elementFile.getVirtualFile().getCanonicalPath();
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        Path targetPath = Path.of(path);
        Path oldPath = Path.of(configuration.getFilePath());
        try {
            if (!Files.isSameFile(targetPath, oldPath)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        // file type
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_FILE) {
            return true;
        }
        String className = "";
        // class type
        if (testingType == CangjieTestRunConfiguration.TestingType.TEST_CLASS) {
            className = TestUtil.getTestClassNameByTestClass(element);
            return !StringUtils.isEmpty(className) && className.equals(configuration.getClassName());
        }
        // method type
        className = TestUtil.getTestClassNameByTestCase(element);
        if (StringUtils.isEmpty(className) || !className.equals(configuration.getClassName())) {
            return false;
        }
        String methodName = TestUtil.getTestCaseName(element);
        return !StringUtils.isEmpty(methodName) && methodName.equals(configuration.getMethodName());
    }

    private boolean checkPackageConfiguration(CangjieTestRunConfiguration configuration,
                                              PsiElement element) {
        CangjieTestRunConfiguration.TestingType testingType = TestUtil.getTestingType(element);
        if (testingType != CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            return false;
        }
        if (!(element instanceof PsiDirectory directory)) {
            return false;
        }
        String path = directory.getVirtualFile().getCanonicalPath();
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        Path targetPath = Path.of(path);
        Path oldPath = Path.of(configuration.getFilePath());
        try {
            if (Files.isSameFile(targetPath, oldPath)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
}
