/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.huawei.deveco.common.url.service.HelpUrlPathServiceImpl;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * OhCangjieBalloonNotificationsUtils
 *
 * @since 2024-12-02
 */
public class OhCangjieBalloonNotificationsUtils {
    /**
     * BALLOON_NOTIFICATION_TITLE = "Cangjie Debug";
     */
    public static final String BALLOON_NOTIFICATION_TITLE = "Cangjie Debug";

    /**
     * show error notification with link
     *
     * @param project project
     * @param content content
     * @param link link
     * @param url url
     */
    public static void showErrorNotificationWithLink(Project project, String content, String link, String url) {
        showNotificationWithLink(project, content, link, url, NotificationType.ERROR);
    }

    /**
     * show warn notification with link
     *
     * @param project project
     * @param content content
     * @param link link
     * @param url url
     */
    public static void showWarnNotificationWithLink(Project project, String content, String link, String url) {
        showNotificationWithLink(project, content, link, url, NotificationType.WARNING);
    }

    private static void showNotificationWithLink(Project project, String content, String link,
                                                 String url, NotificationType type) {
        Notification notification = new Notification(BALLOON_NOTIFICATION_TITLE,
                BALLOON_NOTIFICATION_TITLE, content, type);
        notification.addAction(new NotificationAction(link) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                String developUrl = HelpUrlPathServiceImpl.getInstance().getDevelopUrl(url);
                BrowserUtil.browse(developUrl);
            }
        });
        Notifications.Bus.notify(notification, project);
    }
}
