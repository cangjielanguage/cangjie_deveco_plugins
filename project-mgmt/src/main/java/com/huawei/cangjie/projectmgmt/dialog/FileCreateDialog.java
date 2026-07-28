/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.dialog;

import com.huawei.cangjie.projectmgmt.action.BaseFileCreateAction;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * FileCreateDialog
 *
 * @since 2024/12/21
 */
public class FileCreateDialog extends DialogWrapper implements DialogWithParameters {
    /**
     * FILENAME
     */
    public static final String FILENAME = "FILENAME";

    /**
     * DIR
     */
    public static final String DIR = "DIR";

    /**
     * the properties of dialog boxes
     */
    protected final BaseFileCreateAction myAction;

    private JLabel nameField;

    private JTextField inputField;

    private JPanel myPanel;

    private String myErrorMessage = null;

    private List<String> fileNames = new ArrayList<>();

    private VirtualFile dir;

    public FileCreateDialog(Project project, String title,
                            String name,
                            BaseFileCreateAction myAction,
                            List<String> fileNames,
                            VirtualFile dir) {
        this(project, title, name, myAction);
        this.fileNames.addAll(fileNames);
        this.dir = dir;
    }

    /**
     * the dialog to creat new hybrid page component file
     *
     * @param project project
     * @param title  title
     * @param name name
     * @param myAction myAction
     */
    public FileCreateDialog(Project project, String title, String name, BaseFileCreateAction myAction) {
        super(project, false);

        this.myAction = myAction;
        this.nameField.setText(name);
        init();
        setTitle(title);
        setValidationDelay(10);
        setOKActionEnabled(false);

        inputField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent event) {
                myErrorMessage = null;
            }
        });
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String text = inputField.getText();
        if (text.isEmpty()) {
            setOKActionEnabled(false);
            return null;
        }
        myErrorMessage = myAction.validateInput(getParameters());
        if (!StringUtil.isEmpty(myErrorMessage)) {
            setOKActionEnabled(false);
            return new ValidationInfo(myErrorMessage, inputField);
        }
        setOKActionEnabled(true);
        return null;
    }

    @Override
    protected void doOKAction() {
        myErrorMessage = myAction.validateInput(getParameters());
        if (StringUtil.isEmpty(myErrorMessage)) {
            super.doOKAction();
        } else {
            initValidation();
            setOKActionEnabled(false);
        }
    }

    /**
     * set default input
     *
     * @param defaultInput defaultInput
     */
    public void setDefaultInput(String defaultInput) {
        inputField.setText(defaultInput);
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return inputField;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    /**
     * Obtain parameters in the dialog box.
     *
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(FILENAME, inputField.getText());
        params.put(DIR, dir);
        return params;
    }

    @Override
    protected boolean postponeValidation() {
        return false;
    }
}
