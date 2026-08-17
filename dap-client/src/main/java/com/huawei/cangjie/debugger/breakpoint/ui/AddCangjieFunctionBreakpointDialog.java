/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * add cangjie function breakpoint dialog
 *
 * @since 2022-11-26
 */
public class AddCangjieFunctionBreakpointDialog extends DialogWrapper {
    private JPanel rootPanel;

    private JTextField functionNameField;

    /**
     * Instantiates a new Add cangjie function breakpoint dialog.
     *
     * @param project the project
     */
    public AddCangjieFunctionBreakpointDialog(@Nullable Project project) {
        super(project, true);
        setTitle("Cangjie Function Breakpoint");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected void doOKAction() {
        if (getFunctionName().isEmpty()) {
            Messages.showErrorDialog(rootPanel, "Function Name can't be empty!");
            return;
        }
        super.doOKAction();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return functionNameField;
    }

    /**
     * get Function Name
     *
     * @return function path
     */
    public String getFunctionName() {
        return functionNameField.getText().trim();
    }

    @Override
    public String toString() {
        return "AddCangjieFunctionBreakpointDialog{}";
    }
}
