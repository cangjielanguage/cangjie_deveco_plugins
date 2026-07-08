/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.settings;

import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.utils.ActionUtils;
import com.huawei.cangjie.debugger.utils.FeatureEnableUtils;
import com.huawei.cangjie.debugger.utils.TimeTravelUtils;

import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

/**
 * TimeTravelPanel
 *
 * @since 2024-9-10
 */
public class TimeTravelPanel implements DebugConfigPanel {
    private static final int FRAMES_SIZE_MAX = 100;

    private static final int FRAMES_SIZE_MIN = 1;

    private static final int VARIABLES_DEPTH_MAX = 10;

    private static final int VARIABLES_DEPTH_MIN = 1;

    private static final int VARIABLE_CHILDREN_COUNT_MAX = 1000;

    private static final int VARIABLE_CHILDREN_COUNT_MIN = 0;

    private JPanel panel;

    private JCheckBox enableTimeTravelCheckBox;

    private JPanel autoSavePanel;

    private JRadioButton allThreadsRadioButton;

    private JRadioButton activeThreadRadioButton;

    private JSpinner framesSizeSpinner;

    private JCheckBox localsCheckBox;

    private JCheckBox globalsCheckBox;

    private JCheckBox staticsCheckBox;

    private JCheckBox registersCheckBox;

    private JSpinner variablesDepthSpinner;

    private JSpinner variableChildrenCountSpinner;

    private JLabel frameSizeLabel;

    private JLabel scopeLabel;

    private JLabel variableDepthLabel;

    private JLabel variableChildrenCountLabel;

    private boolean isModified = false;

    private final JCheckBox[] scopeCheckboxes;

    private final List<JComponent> autoSavePanelComponentList;

    private final Project project;

    /**
     * init TimeTravelPanel: init time travel checkbox and add listener
     *
     * @param project project
     */
    public TimeTravelPanel(Project project) {
        this.project = project;

        boolean isEnabledTimeTravel = TimeTravelUtils.getTimeTravel(project);
        enableTimeTravelCheckBox.setSelected(isEnabledTimeTravel);
        enableTimeTravelCheckBox.addActionListener(e -> isModified = true);

        scopeCheckboxes = new JCheckBox[] {
            localsCheckBox, globalsCheckBox, staticsCheckBox, registersCheckBox
        };

        autoSavePanelComponentList = List.of(allThreadsRadioButton, activeThreadRadioButton,
            frameSizeLabel, framesSizeSpinner,
            scopeLabel, localsCheckBox, globalsCheckBox, staticsCheckBox, registersCheckBox,
            variableDepthLabel, variablesDepthSpinner,
            variableChildrenCountLabel, variableChildrenCountSpinner);

        initAutoSavePanel();
    }

    private void initAutoSavePanel() {
        if (!FeatureEnableUtils.IS_SUPPORT_AUTO_SAVE) {
            autoSavePanel.setVisible(false);
            return;
        }

        setAutoSavePanelEnabled();

        enableTimeTravelCheckBox.addActionListener(e -> setAutoSavePanelEnabled());

        // init config value
        initRadioButton(allThreadsRadioButton, activeThreadRadioButton, TimeTravelUtils.getIsAllThread(project));
        initSpinner(framesSizeSpinner, TimeTravelUtils.getFramesSize(project), FRAMES_SIZE_MIN, FRAMES_SIZE_MAX);
        initCheckBoxes(scopeCheckboxes, TimeTravelUtils.getScopes(project));
        initSpinner(variablesDepthSpinner, TimeTravelUtils.getVariablesDepth(project),
            VARIABLES_DEPTH_MIN, VARIABLES_DEPTH_MAX);
        initSpinner(variableChildrenCountSpinner, TimeTravelUtils.getVariableChildrenCount(project),
            VARIABLE_CHILDREN_COUNT_MIN, VARIABLE_CHILDREN_COUNT_MAX);

        allThreadsRadioButton.addActionListener(e -> isModified = true);
        activeThreadRadioButton.addActionListener(e -> isModified = true);
        framesSizeSpinner.addChangeListener(e -> isModified = true);
        for (JCheckBox scopeCheckbox : scopeCheckboxes) {
            scopeCheckbox.addActionListener(e -> isModified = true);
        }
        variablesDepthSpinner.addChangeListener(e -> isModified = true);
        variableChildrenCountSpinner.addChangeListener(e -> isModified = true);
    }

    private void setAutoSavePanelEnabled() {
        boolean isEnableTimeTravel = enableTimeTravelCheckBox.isSelected();
        autoSavePanelComponentList.forEach(component -> component.setEnabled(isEnableTimeTravel));
    }

    private void initRadioButton(JRadioButton positiveRadioButton, JRadioButton negativeRadioButton,
        boolean isSelected) {
        positiveRadioButton.setSelected(isSelected);
        negativeRadioButton.setSelected(!isSelected);
    }

    private void initSpinner(JSpinner spinner, int value, int minimum, int maximum) {
        spinner.setModel(new SpinnerNumberModel(value, minimum, maximum, 1));
        JComponent editor = spinner.getEditor();
        if (!(editor instanceof JSpinner.NumberEditor)) {
            return;
        }
        JFormattedTextField textField = ((JSpinner.NumberEditor) editor).getTextField();
        textField.setColumns(4);
        JFormattedTextField.AbstractFormatter formatter = textField.getFormatter();
        if (formatter instanceof DefaultFormatter defaultFormatter) {
            defaultFormatter.setCommitsOnValidEdit(true);
            defaultFormatter.setAllowsInvalid(false);
        }
    }

    private void initCheckBoxes(JCheckBox[] checkBoxes, int value) {
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setSelected((value & (1 << i)) != 0);
        }
    }

    private int getScopesValue() {
        int value = 0;
        for (int i = 0; i < scopeCheckboxes.length; i++) {
            if (scopeCheckboxes[i].isSelected()) {
                value |= 1 << i;
            }
        }
        return value;
    }

    /**
     * get debug config panel
     *
     * @return javax.swing.JPanel
     */
    @Override
    @Nullable
    public JPanel getPanel() {
        return panel;
    }

    /**
     * when this function return true, reset and apply buttons will be enabled
     *
     * @return boolean
     */
    @Override
    public boolean isModified() {
        return isModified;
    }

    @Override
    public void reset() {
        enableTimeTravelCheckBox.setSelected(TimeTravelUtils.getTimeTravel(project));

        initRadioButton(allThreadsRadioButton, activeThreadRadioButton, TimeTravelUtils.getIsAllThread(project));
        framesSizeSpinner.setValue(TimeTravelUtils.getFramesSize(project));
        initCheckBoxes(scopeCheckboxes, TimeTravelUtils.getScopes(project));
        variablesDepthSpinner.setValue(TimeTravelUtils.getVariablesDepth(project));
        variableChildrenCountSpinner.setValue(TimeTravelUtils.getVariableChildrenCount(project));

        isModified = false;
    }

    @Override
    public void apply() {
        TimeTravelUtils.setTimeTravel(project, enableTimeTravelCheckBox.isSelected());

        TimeTravelUtils.setIsAllThreads(project, allThreadsRadioButton.isSelected());
        TimeTravelUtils.setScopes(project, getScopesValue());
        TimeTravelUtils.setFramesSize(project, String.valueOf(framesSizeSpinner.getValue()));
        TimeTravelUtils.setVariablesDepth(project, String.valueOf(variablesDepthSpinner.getValue()));
        TimeTravelUtils.setVariableChildrenCount(project, String.valueOf(variableChildrenCountSpinner.getValue()));

        XDebugSession session = XDebuggerManager.getInstance(project).getCurrentSession();
        DapXDebugProcess<?, ?> debugProcess = ActionUtils.getDapXDebugProcess(session);
        if (debugProcess != null) {
            debugProcess.getTimeTravelProcess().modifyCacheDataConfig();
        }

        isModified = false;
    }

    @Override
    public String toString() {
        return "TimeTravelPanel{" + "isModified=" + isModified + '}';
    }
}
