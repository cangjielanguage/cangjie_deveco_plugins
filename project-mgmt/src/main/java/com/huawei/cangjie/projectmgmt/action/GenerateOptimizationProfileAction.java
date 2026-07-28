/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;
import static com.intellij.openapi.wm.ToolWindowId.RUN;

import com.huawei.cangjie.projectmgmt.dialog.optimization.OptimizationSettingsDialog;
import com.huawei.cangjie.projectmgmt.settings.CangjieOptimizationProfileSettings;
import com.huawei.cangjie.projectmgmt.settings.ProjectOptimizationSettingsService;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import org.jetbrains.annotations.NotNull;

/**
 * The type Generate optimization profile action.
 *
 * @since 2026-05-04
 */
public class GenerateOptimizationProfileAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateOptimizationProfileAction.class);

    /**
     * Instantiates a new Generate optimization profile action.
     */
    public GenerateOptimizationProfileAction() {
        super("Generate Optimization Profile", "Generate optimization profile", AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取目标 Run Action
        AnAction targetRunAction = ActionManager.getInstance().getAction(RUN);
        if (targetRunAction == null) {
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                message("cangjie.pgo.run.action.not.found"), project, NotificationType.ERROR);
            return;
        }
        // 如果没有选中任何配置，或者项目刚打开没有默认配置，可能返回 null
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings selectedConfiguration = runManager.getSelectedConfiguration();
        if (selectedConfiguration == null
            || !(selectedConfiguration.getConfiguration() instanceof OpenHarmonyRunConfiguration
            openHarmonyRunConfiguration)) {
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                message("cangjie.pgo.run.action.unavailable"), project, NotificationType.ERROR);
            return;
        }
        OptimizationSettingsDialog dialog = new OptimizationSettingsDialog(project, openHarmonyRunConfiguration);
        if (!dialog.showAndGet()) {
            return;
        }
        // 提示是否开启release配置use optimization profile
        CangjieOptimizationProfileSettings settings = CangjieOptimizationProfileSettings.getInstance();
        CangjieOptimizationProfileSettings.State settingsState = settings.getState();
        if (settingsState != null) {
            boolean releaseUseProfile = settingsState.isReleaseUseProfile();
            if (!releaseUseProfile) {
                int enableUseProfile =
                    Messages.showYesNoDialog(project, message("cangjie.pgo.enable.use.optimization.profile.context"),
                        message("cangjie.pgo.enable.use.optimization.profile.title"),
                        message("cangjie.pgo.enable.use.optimization.profile.ok.text"),
                        message("cangjie.pgo.enable.use.optimization.profile.cancel.text"), Messages.getQuestionIcon());
                if (enableUseProfile == Messages.YES) {
                    settingsState.setReleaseUseProfile(true);
                    ApplicationManager.getApplication().saveSettings();
                }
            }
        }
        if (dialog.isUseExistedProfdataSelected()) {
            return;
        }
        ProjectOptimizationSettingsService.State state =
            ProjectOptimizationSettingsService.getInstance(project).getState();
        if (state != null) {
            state.setGenerateOptimizationProfile(true);
            project.save();
        } else {
            LOG.warn("Failed to configure compilation optimization.");
        }
        // 执行原始的 Run Action
        // 使用当前的 AnActionEvent 来执行，这样原始 Action 就能获取到上下文
        targetRunAction.actionPerformed(e);
        LOG.warn("CangjiePlugin: Standard Run action triggered.");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(isSyncFinished(e.getProject()));
    }
}