/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.dialog.optimization;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.cangjie.projectmgmt.listener.CangjieExecutionManagerListener;
import com.huawei.cangjie.projectmgmt.settings.ProjectOptimizationSettingsService;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.build.ohos.actions.ActionUtil;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;

import com.intellij.ide.DataManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Paths;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

/**
 * The type Optimization settings dialog.
 *
 * @since 2026-05-04
 */
public class OptimizationSettingsDialog extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(OptimizationSettingsDialog.class);

    private static final String TARGET_RUN_ACTION_ID = "Run";

    private final Project project;

    private final OpenHarmonyRunConfiguration openHarmonyRunConfiguration;

    private ButtonGroup actionGroup;

    private JRadioButton runActionRadioButton;

    private JRadioButton useExistedProfdataRadioButton;

    private TextFieldWithBrowseButton profdataFilePathInput;

    /**
     * Instantiates a new Optimization settings dialog.
     *
     * @param project the project
     * @param openHarmonyRunConfiguration the open harmony run configuration
     */
    public OptimizationSettingsDialog(@Nullable Project project,
        OpenHarmonyRunConfiguration openHarmonyRunConfiguration) {
        super(project);
        this.project = project;
        this.openHarmonyRunConfiguration = openHarmonyRunConfiguration;
        setTitle(message("cangjie.pgo.generate.optimization.profile.title"));
        init();
        ProjectOptimizationSettingsService.State state =
            ProjectOptimizationSettingsService.getInstance(project).getState();
        if (state != null) {
            if (StringUtils.isBlank(state.getProfdataFilePath())) {
                profdataFilePathInput.setText(CangjieExecutionManagerListener.getProfilePath(project.getBasePath(),
                    openHarmonyRunConfiguration.getModuleName()).toString());
            } else {
                profdataFilePathInput.setText(state.getProfdataFilePath());
            }
            runActionRadioButton.setSelected(state.isUseRunActionSelected());
            useExistedProfdataRadioButton.setSelected(state.isUseExistedProfdataSelected());
        } else {
            profdataFilePathInput.setText(CangjieExecutionManagerListener.getProfilePath(project.getBasePath(),
                openHarmonyRunConfiguration.getModuleName()).toString());
            runActionRadioButton.setSelected(true);
            useExistedProfdataRadioButton.setSelected(false);
        }
        profdataFilePathInput.setEnabled(useExistedProfdataRadioButton.isSelected());
        setOKButtonText(runActionRadioButton.isSelected()
            ? message("cangjie.pgo.generate.optimization.profile.run.button.label")
            : message("cangjie.pgo.generate.optimization.profile.ok.button.label"));
        setCancelButtonText(message("cangjie.pgo.generate.optimization.profile.cancel.button.label"));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(JBUI.size(550, -1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String moduleName = this.openHarmonyRunConfiguration.getModuleName();
        JTextArea descriptionArea = new javax.swing.JTextArea(
            message("cangjie.pgo.generate.optimization.profile.desc", moduleName, moduleName));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(new JLabel().getFont());

        // ==========================================
        // 1. 初始化 Button 组和多行单选按钮
        // ==========================================
        actionGroup = new ButtonGroup();
        runActionRadioButton = new JRadioButton(message("cangjie.pgo.use.run.action.label"));
        useExistedProfdataRadioButton = new JRadioButton(message("cangjie.pgo.use.profdata.file.label"));

        actionGroup.add(runActionRadioButton);
        actionGroup.add(useExistedProfdataRadioButton);
        runActionRadioButton.setSelected(true); // 默认选中 A

        // ==========================================
        // 2. 初始化路径输入框并绑定事件
        // ==========================================
        FileType profdataFile = FileTypeManager.getInstance().getFileTypeByExtension("profdata");
        profdataFilePathInput = new TextFieldWithBrowseButton();
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle(message("cangjie.pgo.generate.optimization.profile.select.prodata.title"))
            .withDescription(message("cangjie.pgo.generate.optimization.profile.select.prodata.description"))
            .withExtensionFilter(profdataFile);

        profdataFilePathInput.addBrowseFolderListener(project, descriptor);

        // 监听单选按钮的切换事件，动态控制路径输入框是否可用，以及 OK 按钮的文字
        runActionRadioButton.addActionListener(e -> {
            profdataFilePathInput.setEnabled(false);
            // 选中时改为 Run
            setOKButtonText(message("cangjie.pgo.generate.optimization.profile.run.button.label"));
        });
        useExistedProfdataRadioButton.addActionListener(e -> {
            profdataFilePathInput.setEnabled(true);
            // 选中时改为 OK
            setOKButtonText(message("cangjie.pgo.generate.optimization.profile.ok.button.label"));
        });

        // ==========================================
        // 3. 布局组装
        // ==========================================

        // --- 第一行：说明文字 ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // 占两列
        gbc.weightx = 1.0;
        // 增加底部边距，让说明文字和下面的单选按钮稍微隔开
        gbc.insets = new Insets(5, 5, 10, 5);
        dialogPanel.add(descriptionArea, gbc);

        // --- 第二行：选项 1 (Use Run action) ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // 占两列
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        dialogPanel.add(runActionRadioButton, gbc);

        // --- 第三行：选项 2 (RadioButton) + 路径输入框 ---
        // 恢复单列占用
        gbc.gridwidth = 1;

        // 放置 RadioButton (第一列)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        dialogPanel.add(useExistedProfdataRadioButton, gbc);

        // 放置 路径输入框 (第二列)
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        dialogPanel.add(profdataFilePathInput, gbc);

        return dialogPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        // 只有当选中了需要填写路径的单选按钮时，才进行路径校验
        if (useExistedProfdataRadioButton.isSelected()) {
            String path = profdataFilePathInput.getText();
            if (StringUtils.isBlank(path) || !path.trim().endsWith(".profdata")) {
                return new ValidationInfo(message("cangjie.pgo.use.profile.select.incorrect"),
                    profdataFilePathInput.getTextField());
            }
            if (!Paths.get(path).toFile().exists()) {
                return new ValidationInfo(message("cangjie.pgo.use.profile.not.exist"),
                    profdataFilePathInput.getTextField());
            }
        }
        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        if (runActionRadioButton.isSelected()) {
            ActionManager actionManager = ActionManager.getInstance();
            AnAction runAction = actionManager.getAction(TARGET_RUN_ACTION_ID);
            if (runAction != null) {
                DataContext dataContext = DataManager.getInstance().getDataContext(getRootPane());
                AnActionEvent event =
                    AnActionEvent.createEvent(runAction, dataContext, null, ActionPlaces.UNKNOWN, ActionUiKind.NONE,
                        null);

                runAction.update(event);

                // 如果 Run 按钮不可用
                if (!event.getPresentation().isEnabled()) {
                    // 弹出右下角警告通知
                    NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                        message("cangjie.pgo.run.action.unavailable"), project, NotificationType.ERROR);
                    return;
                }
            } else {
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                    message("cangjie.pgo.run.action.not.found"), project, NotificationType.ERROR);
                return;
            }
            String buildMode = ActionUtil.getCurrentBuildModeName(project);
            if ("release".equals(buildMode)) {
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                    message("cangjie.pgo.root.release.not.support"), project, NotificationType.ERROR);
                return;
            }
        }

        // 保存到该工程的专有配置
        ProjectOptimizationSettingsService.State state =
            ProjectOptimizationSettingsService.getInstance(project).getState();
        if (state != null) {
            state.setUseRunActionSelected(runActionRadioButton.isSelected());
            state.setUseExistedProfdataSelected(useExistedProfdataRadioButton.isSelected());
            if (useExistedProfdataRadioButton.isSelected()) {
                state.setProfdataFilePath(profdataFilePathInput.getText());
            }
            project.save();
        }
        super.doOKAction();
    }

    /**
     * Is run action selected boolean.
     *
     * @return the boolean
     */
    public boolean isRunActionSelected() {
        return runActionRadioButton.isSelected();
    }

    /**
     * Gets selected path.
     *
     * @return the selected path
     */
    public String getSelectedPath() {
        return profdataFilePathInput.getText();
    }

    /**
     * Is use existed profdata selected boolean.
     *
     * @return the boolean
     */
    public boolean isUseExistedProfdataSelected() {
        return useExistedProfdataRadioButton.isSelected();
    }
}