/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.dialog;

import com.huawei.cangjie.testframework.OpenHarmonyTestBrowseListener;
import com.huawei.cangjie.testframework.configuration.CangjieLocalTestRunConfigurationType;
import com.huawei.cangjie.testframework.configuration.CangjieTestConfigurationProducer;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.testframework.utils.LocalTestUtil;
import com.huawei.cangjie.testframework.utils.TestFrameworkBundle;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.ohos.testframework.run.editor.ConfigurationSpecificEditor;
import com.huawei.deveco.ohos.testframework.run.editor.dialog.TestArgsComponent;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyLogBundle;
import com.huawei.deveco.ohos.testframework.utils.PsiUtils;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * CangjieTestRunParameters
 *
 * @since 2025/02/20
 */
public class CangjieTestRunParameters implements ConfigurationSpecificEditor<CangjieTestRunConfiguration> {
    private static final Logger LOG = Logger.getInstance(CangjieTestRunParameters.class);

    private final JRadioButton[] myTestingType2RadioButton = new JRadioButton[4];
    private final Project myProject;
    private final CangjieTestModuleSelector myModuleSelector;
    private final TextFieldWithBrowseButton packageField;
    private final TextFieldWithBrowseButton fileField;
    private final ModuleModel moduleModel;
    private final FileChooserDescriptor fileDescriptor;
    private final FileChooserDescriptor packageDescriptor;

    private String cjCodeRootPath = "";
    private String cjTestRootPath = "";

    private JPanel myPanel;
    private JRadioButton packageButton;
    private JRadioButton fileRadioButton;
    private JRadioButton classRadioButton;
    private JRadioButton methodRadioButton;
    private LabeledComponent<TextFieldWithBrowseButton> packageComponent;
    private LabeledComponent<TextFieldWithBrowseButton> fileComponent;
    private JTextField timeout;
    private TestArgsComponent testArgsComponent;
    private LabeledComponent<JComboBox<String>> classSelected;
    private LabeledComponent<JComboBox<String>> methodSelected;
    private JPanel parametersPanel;

    private JComponent anchor;

    private ComponentWithBrowseButton.BrowseFolderActionListener packageSelectListener;
    private ComponentWithBrowseButton.BrowseFolderActionListener fileSelectListener;

    public CangjieTestRunParameters(Project project, CangjieTestRunConfiguration configuration,
                                    CangjieTestRunConfigurationEditor<CangjieTestRunConfiguration> editor) {
        myProject = project;
        myModuleSelector = editor.getModuleSelector();
        moduleModel = configuration.getModule() != null
                ? configuration.getModule() : myModuleSelector.getModule().get();

        cjCodeRootPath = Path.of(moduleModel.getModulePath(), Constant.SRC, Constant.MAIN, Constant.CANGJIE)
                .toAbsolutePath().normalize().toString();
        if (configuration.getType().getId().equals(CangjieLocalTestRunConfigurationType.ID)) {
            cjTestRootPath = Path.of(moduleModel.getModulePath(), Constant.SRC, Constant.TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
        } else {
            cjTestRootPath = Path.of(moduleModel.getModulePath(), Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize().toString();
        }

        // 设置测试目录文件选择器相关属性
        packageComponent.setComponent(packageField = new TextFieldWithBrowseButton());
        packageField.setEditable(false);
        packageDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        setPackageRoots(moduleModel);
        // set package select listener
        updatePackageSelectListener();

        // 设置测试文件文件选择器相关属性
        fileComponent.setComponent(fileField = new TextFieldWithBrowseButton());
        fileField.setEditable(false);
        fileDescriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
        setFileRootsAndFilter(moduleModel);
        // set file select listener
        updateFileSelectListener();

        classSelected.setComponent(new JComboBox<>());
        methodSelected.setComponent(new JComboBox<>());
        registerClassSelectedListener();
        registerMethodSelectedListener();

        // test type radio button
        registerRadioButtonListener(CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE, packageButton);
        registerRadioButtonListener(CangjieTestRunConfiguration.TestingType.TEST_FILE, fileRadioButton);
        registerRadioButtonListener(CangjieTestRunConfiguration.TestingType.TEST_CLASS, classRadioButton);
        registerRadioButtonListener(CangjieTestRunConfiguration.TestingType.TEST_METHOD, methodRadioButton);

        // 2025/02/18, unsupported component
        parametersPanel.setVisible(false);
        timeout.setVisible(false);
        testArgsComponent.setVisible(false);
    }

    private void registerRadioButtonListener(CangjieTestRunConfiguration.TestingType type, JRadioButton button) {
        myTestingType2RadioButton[type.getCode()] = button;
        button.addActionListener(listener -> updateButtonsAndLabelComponents(type));
    }

    private void registerClassSelectedListener() {
        classSelected.getComponent().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                String filePath = fileComponent.getComponent().getText();
                if (StringUtils.isEmpty(filePath)) {
                    // reset class selected
                    classSelected.getComponent().removeAllItems();
                    classSelected.getComponent().addItem("");
                    classSelected.getComponent().setSelectedItem("");
                    return;
                }
                Set<String> classNames = TestUtil.getClassNamesByFile(myProject, filePath, StringUtils.EMPTY);
                if (classNames.isEmpty()) {
                    return;
                }
                if (!Path.of(moduleModel.getModulePath(), Constant.SRC, Constant.TEST, Constant.CANGJIE)
                        .toAbsolutePath().normalize().toString().equals(cjTestRootPath)) {
                    // get all register class names
                    List<String> registerClassNames = TestUtil.getRegisterClassNamesInPackage(myProject, filePath);
                    // remove class name when it is not registered
                    Iterator<String> iterator = classNames.iterator();
                    while (iterator.hasNext()) {
                        String className = iterator.next();
                        if (!registerClassNames.contains(className)) {
                            iterator.remove();
                        }
                    }
                }
                // reset class selected
                classSelected.getComponent().removeAllItems();
                classSelected.getComponent().addItem("");
                classSelected.getComponent().setSelectedItem("");
                for (String className : classNames) {
                    classSelected.getComponent().addItem(className);
                }
                // reset method selected
                methodSelected.getComponent().removeAllItems();
                methodSelected.getComponent().addItem("");
                methodSelected.getComponent().setSelectedItem("");
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            }
        });
    }

    private void registerMethodSelectedListener() {
        methodSelected.getComponent().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                String filePath = fileComponent.getComponent().getText();
                Object classNameSelected = classSelected.getComponent().getSelectedItem();
                if (StringUtils.isEmpty(filePath) || classNameSelected == null) {
                    methodSelected.getComponent().removeAllItems();
                    methodSelected.getComponent().addItem("");
                    methodSelected.getComponent().setSelectedItem("");
                    return;
                }
                String className = classNameSelected.toString();
                if (StringUtils.isEmpty(className)) {
                    methodSelected.getComponent().removeAllItems();
                    methodSelected.getComponent().addItem("");
                    methodSelected.getComponent().setSelectedItem("");
                    return;
                }
                Set<String> methodNames = TestUtil.getClassNamesByFile(myProject, filePath, className);
                if (methodNames.isEmpty()) {
                    return;
                }
                // reset method selected
                methodSelected.getComponent().removeAllItems();
                methodSelected.getComponent().addItem("");
                methodSelected.getComponent().setSelectedItem("");
                for (String methodName : methodNames) {
                    methodSelected.getComponent().addItem(methodName);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            }
        });
    }

    /**
     * 更新 package select listener（初始化或更新module选项时使用）
     *
     */
    private void updatePackageSelectListener() {
        packageField.removeActionListener(packageSelectListener);
        String title = "Select Cangjie Package";
        String defaultPath = cjTestRootPath == null ? cjCodeRootPath : cjTestRootPath;
        packageSelectListener = new OpenHarmonyTestBrowseListener(title, null,
                packageField, myProject, packageDescriptor, defaultPath);
        packageField.addActionListener(packageSelectListener);
    }

    /**
     * 更新 file select listener（初始化或更新module选项时使用）
     *
     */
    private void updateFileSelectListener() {
        fileField.removeActionListener(fileSelectListener);
        String title = "Select Cangjie Test File";
        String defaultPath = cjTestRootPath == null ? cjCodeRootPath : cjTestRootPath;
        fileSelectListener = new OpenHarmonyTestBrowseListener(title, null, fileField, myProject,
                fileDescriptor, defaultPath);
        fileField.addActionListener(fileSelectListener);
    }

    /**
     * 为package弹出框列表设置根目录，过滤
     *
     * @param moduleModel moduleModel
     */
    public final void setPackageRoots(ModuleModel moduleModel) {
        if (!FileUtils.isCangjieModule(moduleModel)) {
            return;
        }
        VirtualFile cjTestRootFile = LocalFileSystem.getInstance().findFileByPath(cjTestRootPath);
        if (cjTestRootFile != null) {
            packageDescriptor.setRoots(cjTestRootFile);
        }
    }

    /**
     * 为file弹出框设置过滤条件
     *
     * @param moduleModel moduleModel
     */
    public final void setFileRootsAndFilter(ModuleModel moduleModel) {
        if (!FileUtils.isCangjieModule(moduleModel)) {
            return;
        }
        fileDescriptor.withFileFilter(new VirtualFileCondition());
        VirtualFile cjTestRootFile = LocalFileSystem.getInstance().findFileByPath(cjTestRootPath);
        if (cjTestRootFile != null) {
            fileDescriptor.setRoots(cjTestRootFile);
        }
    }

    @Override
    public void applyTo(CangjieTestRunConfiguration configuration) throws ConfigurationException {
        configuration.setTestType(CangjieTestConfigurationProducer.TESTING_CANGJIE);
        packageField.setText(packageField.getText().replace(Constant.BLACKS_SLASH, Constant.SLASH));
        fileField.setText(fileField.getText().replace(Constant.BLACKS_SLASH, Constant.SLASH));
        try {
            setConfigurationData(configuration);
        } catch (RuntimeConfigurationError error) {
            LOG.warn(error.getMessage());
            throw error;
        }
    }

    private void setConfigurationData(CangjieTestRunConfiguration configuration) throws ConfigurationException {
        configuration.setTestingType(getTestingType());
        validateTime();
        configuration.setTimeout(timeout.getText());
        configuration.setTestArgs(testArgsComponent.getComponent().getText());
        setConfigurationDataBySelected(configuration);
    }

    private void initConfiguration(CangjieTestRunConfiguration configuration) {
        configuration.setWholePackageName(StringUtils.EMPTY);
        configuration.setPackageName(StringUtils.EMPTY);
        configuration.setFilePath(StringUtils.EMPTY);
        configuration.setClassName(StringUtils.EMPTY);
        configuration.setMethodName(StringUtils.EMPTY);
        configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH);
    }

    private void setConfigurationDataBySelected(
            CangjieTestRunConfiguration configuration) throws ConfigurationException {
        if (configuration.getTestingType() == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE) {
            validatePackage(configuration);
            configuration.setWholePackageName(packageComponent.getComponent().getText());
            String wholePackage = configuration.getWholePackageName();
            setPackageName(wholePackage, configuration);
            return;
        }
        // set test file data
        validateFile(configuration);
        String filePath = fileComponent.getComponent().getText();
        configuration.setFilePath(filePath);
        setPackageName(filePath, configuration);
        // set test class data
        configuration.setClassName(Objects.requireNonNull(classSelected.getComponent().getSelectedItem()).toString());
        // set test func data
        configuration.setMethodName(Objects.requireNonNull(methodSelected.getComponent().getSelectedItem()).toString());
    }

    private void setPackageName(String filePath, CangjieTestRunConfiguration configuration) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (virtualFile == null) {
            return;
        }
        String packageName = "";
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.MAIN_PATH) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.MAIN_PATH);
            packageName = TestUtil.getPackageName(
                    configuration.getModule(), virtualFile, false);
        }
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH);
            packageName = TestUtil.getPackageName(
                    configuration.getModule(), virtualFile, true);
        }
        if (configuration.getTestPathType() == CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH);
            packageName = LocalTestUtil.getLocalPackageName(
                    configuration.getModule(), virtualFile);
        }
        if (!StringUtils.isEmpty(packageName)) {
            configuration.setPackageName(packageName);
            configuration.getPackageNameMap().put(packageName, new ArrayList<>());
        }
    }

    private void validatePackage(CangjieTestRunConfiguration configuration) throws ConfigurationException {
        String packagePath = packageComponent.getComponent().getText();
        if (StringUtils.isEmpty(packagePath)) {
            throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("package.not.specified"));
        }
        if (!PsiUtils.replaceBackSlashOfSlash(packagePath).contains(configuration.getModule().getModulePath())) {
            throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("package.not.in.current.module"));
        }
        String cjMainCodePath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.MAIN, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        String cjOhosTestPath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        String cjLocalTestPath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.TEST, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        if (!TestUtil.isSameOrSubPath(cjMainCodePath, packagePath)
                && !TestUtil.isSameOrSubPath(cjOhosTestPath, packagePath)
                && !TestUtil.isSameOrSubPath(cjLocalTestPath, packagePath)) {
            throw new RuntimeConfigurationError(TestFrameworkBundle.message("package.not.exist.cj.path"));
        }
        if (packagePath.contains("src/main/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.MAIN_PATH);
        } else if (packagePath.contains("src/test/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH);
        } else {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH);
        }
    }

    private void validateFile(CangjieTestRunConfiguration configuration) throws RuntimeConfigurationError {
        String filePath = fileComponent.getComponent().getText();
        if (StringUtils.isEmpty(filePath)) {
            throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("file.not.specified"));
        }
        String modulePath = configuration.getModule().getModulePath();
        if (!PsiUtils.replaceBackSlashOfSlash(filePath).contains(modulePath)) {
            throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("file.not.in.current.module"));
        }
        String cjMainCodePath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.MAIN, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        String cjOhosTestPath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        String cjLocalTestPath = Path.of(
                configuration.getModule().getModulePath(), Constant.SRC, Constant.TEST, Constant.CANGJIE
        ).toAbsolutePath().normalize().toString();
        if (!TestUtil.isSameOrSubPath(cjMainCodePath, filePath)
                && !TestUtil.isSameOrSubPath(cjOhosTestPath, filePath)
                && !TestUtil.isSameOrSubPath(cjLocalTestPath, filePath)) {
            throw new RuntimeConfigurationError(TestFrameworkBundle.message("file.not.exist.cj.path"));
        }
        if (filePath.contains("src/main/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.MAIN_PATH);
        } else if (filePath.contains("src/test/cangjie")) {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.LOCAL_TEST_PATH);
        } else {
            configuration.setTestPathType(CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH);
        }
    }

    private CangjieTestRunConfiguration.TestingType getTestingType() {
        for (int i = 0; i < myTestingType2RadioButton.length; i++) {
            JRadioButton button = myTestingType2RadioButton[i];
            if (button != null && button.isSelected()) {
                return CangjieTestRunConfiguration.TestingType.fromCode(i);
            }
        }
        return CangjieTestRunConfiguration.TestingType.UNDEFINED;
    }

    private void validateTime() throws ConfigurationException {
        try {
            if (timeout.getText().length() > 6 || !timeout.getText().matches(Constant.TIMEOUT_REGEX) || !(
                    Integer.parseInt(timeout.getText()) <= 60 * 60 * 24)) {
                throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("timeout.must.valid.integer"));
            }
        } catch (NumberFormatException e) {
            LOG.warn(OpenHarmonyLogBundle.message("validate.time.error"));
        }
    }

    @Override
    public void resetFrom(CangjieTestRunConfiguration configuration) {
        updateButtonsAndLabelComponents(configuration.getTestingType());
        packageComponent.getComponent().setText(configuration.getWholePackageName());
        fileComponent.getComponent().setText(configuration.getFilePath());
        classSelected.getComponent().addItem(configuration.getClassName());
        classSelected.getComponent().setSelectedItem(configuration.getClassName());
        methodSelected.getComponent().addItem(configuration.getMethodName());
        methodSelected.getComponent().setSelectedItem(configuration.getMethodName());
        timeout.setText(configuration.getTimeout());
        testArgsComponent.getComponent().setText(configuration.getTestArgs());
    }

    private void updateButtonsAndLabelComponents(CangjieTestRunConfiguration.TestingType type) {
        packageButton.setSelected(type == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE);
        fileRadioButton.setSelected(type == CangjieTestRunConfiguration.TestingType.TEST_FILE);
        classRadioButton.setSelected(type == CangjieTestRunConfiguration.TestingType.TEST_CLASS);
        methodRadioButton.setSelected(type == CangjieTestRunConfiguration.TestingType.TEST_METHOD);
        updateLabelComponents(type);
    }

    private void updateLabelComponents(CangjieTestRunConfiguration.TestingType type) {
        packageComponent.setVisible(type == CangjieTestRunConfiguration.TestingType.TEST_ALL_IN_PACKAGE);
        fileComponent.setVisible(type == CangjieTestRunConfiguration.TestingType.TEST_FILE
                || type == CangjieTestRunConfiguration.TestingType.TEST_CLASS
                || type == CangjieTestRunConfiguration.TestingType.TEST_METHOD);
        classSelected.setVisible(type == CangjieTestRunConfiguration.TestingType.TEST_CLASS
                || type == CangjieTestRunConfiguration.TestingType.TEST_METHOD);
        methodSelected.setVisible(type == CangjieTestRunConfiguration.TestingType.TEST_METHOD);
    }

    @Override
    public Component getComponent() {
        return myPanel;
    }

    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent jComponent) {

    }

    /**
     * 获取package组件
     *
     * @return LabeledComponent<TextFieldWithBrowseButton>
     */
    public LabeledComponent<TextFieldWithBrowseButton> getPackageComponent() {
        return packageComponent;
    }

    /**
     * 获取class组件
     *
     * @return LabeledComponent<TextFieldWithBrowseButton>
     */
    public LabeledComponent<TextFieldWithBrowseButton> getFileComponent() {
        return fileComponent;
    }

    /**
     * 获取class组件
     *
     * @return LabeledComponent<TextFieldWithBrowseButton>
     */
    public LabeledComponent<JComboBox<String>> getClassComponent() {
        return classSelected;
    }

    /**
     * 获取method组件
     *
     * @return LabeledComponent<TextFieldWithBrowseButton>
     */
    public LabeledComponent<JComboBox<String>> getMethodComponent() {
        return methodSelected;
    }

    private static class VirtualFileCondition implements Condition<VirtualFile> {
        @Override
        public boolean value(VirtualFile virtualFile) {
            if (virtualFile.getName().equals(Constant.CJ_TEST_LIST_FILE_NAME)) {
                return false;
            }
            return virtualFile.getName().endsWith(Constant.CJ_TEST_FILE_SUFFIX);
        }
    }
}
