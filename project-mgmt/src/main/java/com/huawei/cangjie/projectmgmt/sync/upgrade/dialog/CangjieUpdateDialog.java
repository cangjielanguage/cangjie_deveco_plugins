/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.dialog;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider.UPDATE_PROVIDER_EXTENSION_LIST;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;

import com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.OhosSyncInvoker;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.AbstractUpdateDialog;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.UIUtil;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The type Cangjie update dialog.
 *
 * @since 2024 -11-05
 */
public class CangjieUpdateDialog extends AbstractUpdateDialog {
    /**
     * The Project model.
     */
    protected ProjectModel projectModel;

    /**
     * The Sync request.
     */
    protected SyncRequest syncRequest;

    /**
     * The Upgrade provider list.
     */
    protected List<CangjieUpdateProvider> updateProviderList;

    private JPanel rootPanel;

    private JLabel descLabel;

    private JScrollPane updateScrollPanel;

    private JEditorPane updateInfoPanel;

    /**
     * Instantiates a new Cangjie upgrade dialog.
     *
     * @param projectModel       the project model
     * @param syncRequest        the sync request
     * @param title              the title
     * @param updateProviderList the update provider list (computed in background)
     */
    public CangjieUpdateDialog(ProjectModel projectModel, SyncRequest syncRequest, String title,
                               List<CangjieUpdateProvider> updateProviderList) {
        super(false);
        this.projectModel = projectModel;
        this.syncRequest = syncRequest;
        this.updateProviderList = updateProviderList;
        setTitle(title);
        int width = 550;
        int height = 130;
        rootPanel.setPreferredSize(new JBDimension(width, height));
    }

    /**
     * Creates and shows the update dialog after computing update list in background thread.
     * This avoids "Slow operations are prohibited on EDT" error when accessing PSI operations.
     *
     * @param projectModel the project model
     * @param syncRequest  the sync request
     * @param title        the title
     */
    public static void showDialog(ProjectModel projectModel, SyncRequest syncRequest, String title) {
        Project project = projectModel.getProject();
        // Compute update list in background thread to avoid slow operations on EDT
        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, message("upgrade.available.checking"), true) {
            private List<CangjieUpdateProvider> computedProviderList;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                computedProviderList = computeUpdateList(projectModel, syncRequest);
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    CangjieUpdateDialog dialog = new CangjieUpdateDialog(
                        projectModel, syncRequest, title, computedProviderList);
                    dialog.show();
                });
            }
        });
    }

    /**
     * Computes the update provider list. This method should be called from a background thread
     * because it may trigger slow PSI operations.
     *
     * @param projectModel the project model
     * @param syncRequest  the sync request
     * @return the list of providers that need update
     */
    private static List<CangjieUpdateProvider> computeUpdateList(ProjectModel projectModel, SyncRequest syncRequest) {
        List<CangjieUpdateProvider> result = new ArrayList<>();
        for (CangjieUpdateProvider cangjieUpdateProvider : UPDATE_PROVIDER_EXTENSION_LIST.getExtensionList()) {
            if (cangjieUpdateProvider.isNeedUpdate(projectModel, syncRequest)) {
                result.add(cangjieUpdateProvider);
            }
        }
        return result;
    }

    @Override
    public void show() {
        if (CollectionUtils.isNotEmpty(updateProviderList)) {
            initUpdateInfo();
        } else {
            initNoUpdateInfo();
        }
        super.init();
        super.show();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        if (CollectionUtils.isEmpty(updateProviderList)) {
            Action closeAction = new AbstractAction(message("upgrade.not.available.dialog.close.button.text")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    doCancelAction();
                }
            };
            closeAction.putValue(DEFAULT_ACTION, Boolean.TRUE);
            return new Action[]{closeAction};
        }
        Action updateAction = new UpdateAction();
        Action cancelAction = new AbstractAction(message("upgrade.available.dialog.cancel.button.text")) {
            @Override
            public void actionPerformed(ActionEvent event) {
                doCancelAction();
            }
        };
        updateAction.putValue(DEFAULT_ACTION, Boolean.TRUE);
        return new Action[]{updateAction, cancelAction};
    }

    private void initNoUpdateInfo() {
        rootPanel.setPreferredSize(new JBDimension(550, 70));
        descLabel.setText(message("upgrade.nothing.todo"));
        updateScrollPanel.setVisible(false);
        updateInfoPanel.setVisible(false);
    }

    private void initUpdateInfo() {
        updateInfoPanel.setText(getUpdateInfoText());
        updateInfoPanel.setBackground(UIUtil.getPanelBackground());
        updateInfoPanel.setCaretPosition(0);
        updateScrollPanel.setVisible(true);
        updateInfoPanel.setVisible(true);
        descLabel.setText(message("upgrade.dialog.available.text"));
    }

    private String getUpdateInfoText() {
        HtmlChunk.Element ul = HtmlChunk.ul();
        for (CangjieUpdateProvider updateProvider : updateProviderList) {
            Map<String, Set<String>> updateMessageMap = updateProvider.getUpdateMessage(projectModel, syncRequest);
            if (updateMessageMap.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Set<String>> entry : updateMessageMap.entrySet()) {
                String updateMessage = entry.getKey();
                Set<String> details = entry.getValue();
                HtmlChunk.Element li = HtmlChunk.li().addRaw(updateMessage).addRaw(HtmlChunk.nbsp(2).toString());
                if (details == null || details.isEmpty()) {
                    ul = ul.child(li);
                    continue;
                }
                HtmlChunk.Element childUl = HtmlChunk.ul().attr("style", "margin-top: 3px; margin-bottom: 3px;");
                for (String detailMsg : details) {
                    HtmlChunk.Element childLi = HtmlChunk.li().addRaw(detailMsg).addRaw(HtmlChunk.nbsp(4).toString());
                    childUl = childUl.child(childLi);
                }
                li = li.child(childUl);
                ul = ul.child(li);
            }
        }
        HtmlChunk.Element body = HtmlChunk.body();
        HtmlBuilder builder = new HtmlBuilder();
        builder.append(ul);
        body = body.addRaw(builder.toString());
        HtmlChunk.Element head = HtmlChunk.head()
                .addRaw(UIUtil.getCssFontDeclaration(UIUtil.getLabelFont(), UIUtil.getLabelForeground(), null, null));
        return HtmlChunk.html().child(head).child(body).toString();
    }

    private class UpdateAction extends AbstractAction {
        private static final long serialVersionUID = 3006576515350553913L;

        public UpdateAction() {
            super(message("upgrade.available.dialog.update.button.text"));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            Project project = projectModel.getProject();
            if (!isSyncFinished(project)) {
                NotificationUtil.notifyInfo(getTitle(), message("upgrade.is.not.available.during.sync"), project,
                        NotificationType.ERROR);
                close(0);
                return;
            }
            ProgressManager.getInstance()
                    .run(new Task.Backgroundable(project, message("upgrade.background.task.title")) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            doCangjieUpdateRun(project);
                        }
                    });
            close(0);
        }
    }

    private void doCangjieUpdateRun(Project project) {
        boolean isSuccess = true;
        for (CangjieUpdateProvider updateProvider : updateProviderList) {
            if (!updateProvider.doUpdate(projectModel, syncRequest)) {
                isSuccess = false;
            }
        }
        if (isSuccess) {
            NotificationUtil.clearNotifications();
        }
        // call sync
        OhosSyncInvoker.getInstance().doSync(project, SyncRequest.RELOAD_PROJECT_MODEL);
    }
}