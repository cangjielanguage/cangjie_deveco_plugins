/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.ui;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.MODULE_NAME_MAX_LENGTH;

import com.huawei.deveco.projectmgmt.ohos.resources.ProjectMgmtBundle;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;

import com.google.common.base.CharMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.TitledSeparator;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * creat dialog to creat new Cangjie static library name
 *
 * @since 2025 -01-19
 */
public class CangjieModuleDialog extends DialogWrapper {
    /**
     * module key
     */
    public static final String DEFAULT_MODULE_NAME = "cangjieLibraryModuleName";

    /**
     * scope key
     */
    public static final String DEFAULT_SCOPE_NAME = "cangjieLibraryScopeName";

    private JPanel myPanel;

    private TitledSeparator scopeSeparator;

    private JRadioButton currentFileRadioButton;

    private JRadioButton currentDirectoryRadioButton;

    private TitledSeparator moduleSeparator;

    private JTextField packageNameField;

    private String myErrorMessage = null;

    private final Project project;

    private final List<ModuleModel> modulesList;

    /**
     * Instantiates a new Cangjie module dialog.
     *
     * @param project the project
     * @param title the title
     * @param modulesList the modules list
     * @param defaultModuleName the default module name
     */
    public CangjieModuleDialog(Project project, String title, List<ModuleModel> modulesList, String defaultModuleName) {
        super(project, false);
        scopeSeparator.setText(message("dts2cj.dialog.scope.title"));
        moduleSeparator.setText(message("dts2cj.dialog.module.title"));
        this.project = project;
        this.modulesList = modulesList;
        init();
        setTitle(title);
        setValidationDelay(10);
        setOKActionEnabled(false);
        packageNameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent event) {
                myErrorMessage = null;
            }
        });
        packageNameField.setText(defaultModuleName);
        currentFileRadioButton.setSelected(true);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (myErrorMessage != null) {
            setOKActionEnabled(false);
            return new ValidationInfo(myErrorMessage, null);
        }

        String text = packageNameField.getText();
        String message = checkCharacter(text);
        if (StringUtils.isNotEmpty(message)) {
            setOKActionEnabled(false);
            return new ValidationInfo(message, packageNameField);
        }
        for (ModuleModel moduleModel : modulesList) {
            if (moduleModel.getModuleName().equals(text)) {
                setOKActionEnabled(true);
                return new ValidationInfo("Module exists", packageNameField).withOKEnabled().asWarning();
            }
        }
        setOKActionEnabled(true);
        return null;
    }

    @Override
    protected void doOKAction() {
        Map<String, Object> parameters = getParameters();
        if (!(parameters.getOrDefault(DEFAULT_MODULE_NAME, null) instanceof String text)) {
            super.doOKAction();
            return;
        }
        if (StringUtils.isNotEmpty(checkCharacter(text))) {
            // 弹出确认框
            initValidation();
            setOKActionEnabled(false);
        } else {
            boolean hasSameName = false;
            for (ModuleModel moduleModel : modulesList) {
                if (moduleModel.getModuleName().equals(text)) {
                    hasSameName = true;
                }
            }
            if (hasSameName) {
                int result = Messages.showYesNoDialog(this.project, message("dts2cj.override.confirm.msg"),
                    message("dts2cj.override.confirm.title"), Messages.getQuestionIcon());
                if (result != Messages.YES) {
                    return;
                }
            }
            super.doOKAction();
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return packageNameField;
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
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(DEFAULT_MODULE_NAME, packageNameField.getText());
        params.put(DEFAULT_SCOPE_NAME, currentFileRadioButton.isSelected()
                ? ScopeEnum.FILE.name() : ScopeEnum.DIRECTORY.name());
        return params;
    }

    @Override
    protected boolean postponeValidation() {
        return false;
    }

    private String checkCharacter(String inputString) {
        String error = ProjectMgmtBundle.message("rename.module.input.str.error.text");
        if (inputString == null || inputString.isEmpty() || inputString.contains(":") || !CharUtils.isAsciiAlpha(
            inputString.charAt(0))) {
            return error;
        }
        for (char aChar : inputString.toCharArray()) {
            if (!CharUtils.isAsciiAlphanumeric(aChar) && aChar != '_') {
                return error;
            }
        }
        if (!CharMatcher.ascii().matchesAllOf(inputString)) {
            return error;
        }
        CharMatcher charMatcher = CharMatcher.anyOf("[/\\\\?%*:|\"<>!; ]");
        if (charMatcher.matchesAnyOf(inputString)) {
            return error;
        }
        return inputString.length() <= MODULE_NAME_MAX_LENGTH ? Strings.EMPTY : error;
    }

    /**
     * The enum Scope enum.
     */
    public static enum ScopeEnum {
        /**
         * File scope enum.
         */
        FILE,
        /**
         * Directory scope enum.
         */
        DIRECTORY;
    }
}
