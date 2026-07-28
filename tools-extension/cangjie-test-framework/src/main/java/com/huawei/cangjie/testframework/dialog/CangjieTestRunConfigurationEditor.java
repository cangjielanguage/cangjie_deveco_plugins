/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.dialog;

import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.deveco.debugger.ohos.debug.OpenHarmonyAutoDebugger;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.ohos.debugcommon.debugger.DebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.OpenHarmonyDebuggerContext;
import com.huawei.deveco.ohos.debugcommon.module.ModuleModelComboBox;
import com.huawei.deveco.ohos.testframework.run.editor.ConfigurationSpecificEditor;
import com.huawei.deveco.ohos.testframework.run.editor.OpenHarmonyTestDebuggerPanel;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JComponent;

/**
 * CangjieTestRunConfigurationEditor
 *
 * @since 2025/02/20
 */
public class CangjieTestRunConfigurationEditor<T extends CangjieTestRunConfiguration> extends SettingsEditor<T>
        implements PanelWithAnchor, ActionListener {
    private final CangjieTestModuleSelector myModuleSelector;

    private JPanel myPanel;
    private JBTabbedPane mJBTabbedPane;
    private JBLabel myModuleJBLabel;
    private JPanel myConfigurationSpecificPanel;
    private ModuleModelComboBox myModulesComboBox;

    private JComponent anchor;
    private ConfigurationSpecificEditor<T> myConfigurationSpecificEditor;

    private ModuleModel originalModuleModel;
    private final List<String> originalNames = new ArrayList<>();

    private final OpenHarmonyTestDebuggerPanel openHarmonyTestDebuggerPanel;
    private final CjNativeAppTestDebuggerEditor nativeAppDebuggerEditor;
    private final OpenHarmonyDebuggerContext debuggerContext;

    public CangjieTestRunConfigurationEditor(Project project, T configuration) {
        Disposer.register(project, this);
        myModuleSelector = new CangjieTestModuleSelector(project, myModulesComboBox);
        resetModuleSelector(configuration);
        setModulesBoxListener(configuration);

        this.debuggerContext = configuration.getHarmonyDebuggerContext();
        this.openHarmonyTestDebuggerPanel = new OpenHarmonyTestDebuggerPanel(configuration, debuggerContext, project);
        mJBTabbedPane.add("Debugger", openHarmonyTestDebuggerPanel.getComponent());
        openHarmonyTestDebuggerPanel.getDebuggerType().addActionListener(actionEvent -> {
            Object selectedItem = openHarmonyTestDebuggerPanel.getDebuggerType().getSelectedItem();
            if (selectedItem instanceof String debuggerType) {
                switchDebugOption(debuggerType, configuration);
            }
        });
        nativeAppDebuggerEditor = new CjNativeAppTestDebuggerEditor(project);
    }

    /**
     * setConfigurationSpecificEditor
     *
     * @param specificEditor ConfigurationSpecificEditor
     */
    public void setConfigurationSpecificEditor(ConfigurationSpecificEditor<T> specificEditor) {
        myConfigurationSpecificEditor = specificEditor;
        myConfigurationSpecificPanel.add(specificEditor.getComponent());
        setAnchor(myConfigurationSpecificEditor.getAnchor());
    }

    /**
     * getModuleSelector
     *
     * @return ConfigurationModuleSelector
     */
    public CangjieTestModuleSelector getModuleSelector() {
        return myModuleSelector;
    }

    @Override
    protected void resetEditorFrom(@NotNull T configuration) {
        originalModuleModel = configuration.getModule();
        // 根据configuration.getModule()初始化模块选择器
        resetModuleSelector(configuration);
        if (myConfigurationSpecificEditor == null) {
            return;
        }
        myConfigurationSpecificEditor.resetFrom(configuration);
        if (myConfigurationSpecificEditor instanceof CangjieTestRunParameters testRunParameters) {
            saveLabeledComponentText(testRunParameters);
        }
        if (openHarmonyTestDebuggerPanel != null) {
            openHarmonyTestDebuggerPanel.resetFrom(configuration.getHarmonyDebuggerContext());
        }
    }

    private void switchDebugOption(String openHarmonyDebuggerType, OpenHarmonyRunConfiguration configuration) {
        if (openHarmonyTestDebuggerPanel.getOptionComponent() != null) {
            openHarmonyTestDebuggerPanel.getOptionPanel().remove(openHarmonyTestDebuggerPanel.getOptionComponent());
            openHarmonyTestDebuggerPanel.setOptionComponent(null);
        }
        openHarmonyTestDebuggerPanel.setOptionComponent(nativeAppDebuggerEditor.createEditor());
        if (openHarmonyDebuggerType == null) {
            return;
        }
        DebuggerState debuggerState = debuggerContext.getDebuggerState(openHarmonyDebuggerType);
        if (!(debuggerState instanceof NativeDebuggerState nativeDebuggerState)) {
            return;
        }
        nativeAppDebuggerEditor.resetEditorFrom(nativeDebuggerState, configuration);
        if (!(OpenHarmonyAutoDebugger.DISPLAY_NAME.equals(openHarmonyDebuggerType))) {
            openHarmonyTestDebuggerPanel.getOptionPanel().add(nativeAppDebuggerEditor.createEditor());
        }
    }

    private void saveLabeledComponentText(CangjieTestRunParameters testRunParameters) {
        List<LabeledComponent<TextFieldWithBrowseButton>> labeledComponentList =
                getLabeledComponentList(testRunParameters);
        for (LabeledComponent<TextFieldWithBrowseButton> labeledComponent : labeledComponentList) {
            originalNames.add(labeledComponent.getComponent().getText());
        }
    }

    private List<LabeledComponent<TextFieldWithBrowseButton>> getLabeledComponentList(
            CangjieTestRunParameters testRunParameters) {
        List<LabeledComponent<TextFieldWithBrowseButton>> labeledComponentList = new ArrayList<>();
        labeledComponentList.add(testRunParameters.getPackageComponent());
        labeledComponentList.add(testRunParameters.getFileComponent());
        return labeledComponentList;
    }

    @Override
    protected void applyEditorTo(@NotNull T configuration) throws ConfigurationException {
        if (myModuleSelector.getModule().isEmpty()) {
            throw new RuntimeConfigurationError(OpenHarmonyHintBundle.message("module.is.null.message"));
        }
        myModuleSelector.applyTo(configuration);
        if (configuration.getModule() != null) {
            TestUtil.saveModuleInfo(configuration);
        }
        if (myConfigurationSpecificEditor != null) {
            myConfigurationSpecificEditor.applyTo(configuration);
        }

        if (openHarmonyTestDebuggerPanel != null) {
            openHarmonyTestDebuggerPanel.applyTo(configuration);
            String debuggerType = configuration.getHarmonyDebuggerContext().getDebuggerType();
            DebuggerState debuggerState = configuration.getHarmonyDebuggerContext().getDebuggerState(debuggerType);
            if (debuggerState instanceof NativeDebuggerState nativeDebuggerState) {
                nativeAppDebuggerEditor.applyEditorTo(configuration, nativeDebuggerState);
            }
        }
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent jComponent) {
        this.anchor = anchor;
        myModuleJBLabel.setAnchor(anchor);
        mJBTabbedPane.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }

    private void resetModuleSelector(T configuration) {
        if (configuration.getModule() != null) {
            myModuleSelector.reset(configuration);
        } else {
            // 查出所有的module并设置到下拉列表中
            myModuleSelector.reset();
            // 正常情况将默认module设置为第一个module，异常情况默认设置为空
            if (myModulesComboBox.getItemCount() > 0) {
                ModuleModel module = myModulesComboBox.getItemAt(0);
                myModulesComboBox.setSelectedModule(module);
                // 信息初始化
                setSelectedModule(module);
            }
        }
    }

    private void setSelectedModule(ModuleModel module) {
        if (module != null && myConfigurationSpecificEditor != null) {
            if (myConfigurationSpecificEditor instanceof CangjieTestRunParameters testRunParameters) {
                testRunParameters.setFileRootsAndFilter(module);
                testRunParameters.setPackageRoots(module);
            }
        }
    }

    /**
     * module下拉框监听
     *
     * @param config config
     */
    private void setModulesBoxListener(T config) {
        if (config == null) {
            return;
        }

        myModulesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ModuleModel module = myModulesComboBox.getSelectedModule();
                setSelectedModule(module);
            }
        });
    }
}
