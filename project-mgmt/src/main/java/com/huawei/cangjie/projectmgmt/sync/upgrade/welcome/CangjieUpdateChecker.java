/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.welcome;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider.UPDATE_PROVIDER_EXTENSION_LIST;

import com.huawei.cangjie.projectmgmt.sync.upgrade.dialog.CangjieUpdateDialog;
import com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.sync.syncinterface.PostSync;

import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Cangjie upgrade checker.
 *
 * @since 2024 -11-06
 */
public class CangjieUpdateChecker implements PostSync {
    @Override
    public void checkStatus(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        doCheckUpdate(projectModel, syncRequest);
    }

    /**
     * Do check upgrade.
     *
     * @param projectModel the project model
     * @param syncRequest  the sync request
     */
    protected void doCheckUpdate(ProjectModel projectModel, SyncRequest syncRequest) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<CangjieUpdateProvider> updateProviderList = computeUpdateList(projectModel, syncRequest);
            if (CollectionUtils.isNotEmpty(updateProviderList)) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    showNotification(projectModel, syncRequest, updateProviderList);
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

    /**
     * getTitle
     *
     * @return title
     */
    protected String getTitle() {
        return message("upgrade.notification.title");
    }

    /**
     * Show notification.
     *
     * @param projectModel       the project model
     * @param syncRequest        the sync request
     * @param updateProviderList the upgrade provider list
     */
    protected void showNotification(ProjectModel projectModel, SyncRequest syncRequest,
                                    List<CangjieUpdateProvider> updateProviderList) {
        if (CollectionUtils.isEmpty(updateProviderList)) {
            return;
        }
        Project project = projectModel.getProject();
        NotificationAction updateAction = NotificationAction.create(
                message("upgrade.notification.available.link"), (actionEvent, notification) -> {
                    showUpdateInfoDialog(projectModel, syncRequest);
                });
        NotificationUtil.notifyExpireInfo(getTitle(), message("upgrade.notification.available"), project,
                NotificationType.INFORMATION, updateAction);
    }

    /**
     * Show update info dialog.
     *
     * @param projectModel the project model
     * @param syncRequest  the sync request
     */
    protected void showUpdateInfoDialog(ProjectModel projectModel, SyncRequest syncRequest) {
        CangjieUpdateDialog.showDialog(projectModel, syncRequest, getTitle());
    }
}
