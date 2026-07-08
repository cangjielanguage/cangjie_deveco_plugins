/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.dialog;

import com.huawei.cangjie.projectmgmt.action.BaseFileCreateAction;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;

import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * PageCreateDialog
 *
 * @since 2025/02/05
 */
public class ComponentCreateDialog extends DialogWrapper implements DialogWithParameters {
    /**
     * PAGE_NAME
     */
    public static final String PAGE_NAME = "PAGE_NAME";

    /**
     * FILES_NAME
     */
    public static final String FILES_NAME = "FILES_NAME";

    /**
     * ETS_WRAPPER_TYPE
     */
    public static final int ETS_WRAPPER_TYPE = 1;

    /**
     * CANGJIE_TYPE
     */
    public static final int CANGJIE_TYPE = 2;

    /**
     * ENABLE_ETS_WRAPPER
     */
    public static final String ENABLE_ETS_WRAPPER = "ENABLE_ETS_WRAPPER";

    private JPanel myPanel;
    private JLabel titleLabel;
    private LabeledComponent<JTextField> componentInput;
    private JLabel errorInfo;
    private LabeledComponent<JRadioButton> cangjieRadioButton;
    private LabeledComponent<JRadioButton> enable;
    private LabeledComponent<JRadioButton> unable;

    private final JTextField inputField;

    private final BaseFileCreateAction myAction;
    private final Set<String> filesName;

    public ComponentCreateDialog(Project project, String title,
                                 BaseFileCreateAction myAction,
                                 Set<String> filesName) {
        super(project, false);
        this.setSize(996, 663);
        getWindow().setMinimumSize(new Dimension(800, 533));
        this.titleLabel.setFont(new Font("Title", Font.BOLD, 18));
        this.componentInput.setComponent(inputField = new JTextField());
        this.cangjieRadioButton.setComponent(new JRadioButton("Cangjie"));
        this.cangjieRadioButton.getComponent().setSelected(true);
        this.cangjieRadioButton.getComponent().addItemListener(e -> {
            this.cangjieRadioButton.getComponent().setSelected(true);
        });
        this.enable.setComponent(new JRadioButton("With ArkTS Wrapper"));
        this.unable.setComponent(new JRadioButton("Without ArkTS Wrapper"));
        this.enable.getComponent().setSelected(true);
        registerRadioButtonListener(ETS_WRAPPER_TYPE, enable.getComponent());
        registerRadioButtonListener(CANGJIE_TYPE, unable.getComponent());
        this.errorInfo.setVisible(false);

        init();
        this.myAction = myAction;
        this.filesName = filesName;
        setTitle(title);
        setOKActionEnabled(false);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String text = inputField.getText();
        if (text.isEmpty()) {
            setOKActionEnabled(false);
            return null;
        }
        String errorMessage = myAction.validateInput(getParameters());
        if (!StringUtil.isEmpty(errorMessage)) {
            setOKActionEnabled(false);
            this.errorInfo.setVisible(true);
            this.errorInfo.setIcon(AllIcons.General.Error);
            this.errorInfo.setText(errorMessage);
            return new ValidationInfo("", inputField);
        }
        this.errorInfo.setVisible(false);
        setOKActionEnabled(true);
        return null;
    }

    @Override
    protected void doOKAction() {
        String errorMessage = myAction.validateInput(getParameters());
        if (StringUtil.isEmpty(errorMessage)) {
            super.doOKAction();
        } else {
            initValidation();
            setOKActionEnabled(false);
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return inputField;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(PAGE_NAME, inputField.getText());
        params.put(FILES_NAME, filesName);
        params.put(ENABLE_ETS_WRAPPER, this.enable.getComponent().isSelected());
        return params;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    @Override
    protected boolean postponeValidation() {
        return false;
    }

    private void registerRadioButtonListener(int type, JRadioButton button) {
        button.addActionListener(listener -> updateRadioButton(type));
    }

    private void updateRadioButton(int type) {
        enable.getComponent().setSelected(type == ETS_WRAPPER_TYPE);
        unable.getComponent().setSelected(type == CANGJIE_TYPE);
    }

    /**
     * set default input
     *
     * @param defaultInput defaultInput
     */
    public void setDefaultInput(String defaultInput) {
        inputField.setText(defaultInput);
    }
}
