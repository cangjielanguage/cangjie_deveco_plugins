/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Notification Util
 *
 * @since 2024/03/13
 */
public class NotificationUtil {
    /**
     * The constant CANGJIE_MGMT_NOTIFICATION_GROUP.
     */
    public static final String CANGJIE_MGMT_NOTIFICATION_GROUP = "Cangjie.Project.Mgmt.Notification";

    /**
     * The constant CANGJIE_MGMT_NOTIFICATION_ADD_CODE_LINTER_CONFIG_GROUP.
     */
    public static final String CANGJIE_MGMT_NOTIFICATION_ADD_CODE_LINTER_CONFIG_GROUP =
            "Cangjie.Project.Mgmt.Notification.AddCodeLinkerConfig";

    /**
     * The constant CANGJIE_MGMT_NOTIFICATION_EXPIRES_GROUP.
     * Expires automatically after 10 seconds.
     */
    public static final String CANGJIE_MGMT_NOTIFICATION_EXPIRES_GROUP = "Cangjie.Project.Mgmt.Expires.Notification";

    /**
     * notify info
     *
     * @param message message
     * @param project project
     * @param level message level
     */
    public static void notifyInfo(String message, Project project, NotificationType level) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(message, level);
        msg.notify(project);
    }

    /**
     * notify info
     *
     * @param message message
     * @param project project
     * @param level message level
     * @param action button action
     */
    public static void notifyInfo(String message, Project project, NotificationType level, AnAction action) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(message, level).addAction(action);
        msg.notify(project);
    }

    /**
     * notify info
     *
     * @param title title
     * @param message message
     * @param project project
     * @param level message level
     */
    public static void notifyInfo(String title, String message, Project project, NotificationType level) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(title, message, level);
        msg.notify(project);
    }

    /**
     * notify info
     *
     * @param title title
     * @param message message
     * @param project project
     * @param level message level
     * @param action button action
     */
    public static void notifyInfo(String title, String message, Project project, NotificationType level,
        AnAction action) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(title, message, level).addAction(action);
        msg.notify(project);
    }

    /**
     * Cache notification, which can be manually cleared.
     * It can be used together with {@link #clearNotifications}.
     *
     * @param title title
     * @param message message
     * @param project project
     * @param level message level
     * @param action button action
     */
    public static void notifyExpireInfo(String title, String message, Project project, NotificationType level,
        AnAction action) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(title, message, level).addAction(action);
        NotificationRecordManager.getInstance().notifyProject(msg, project);
    }

    /**
     * Cache notification, which can be manually cleared.
     * It can be used together with {@link #clearAddCodeLinterConfigNotifications}.
     *
     * @param title title
     * @param message message
     * @param project project
     * @param level message level
     * @param action button action
     */
    public static void notifyAddCodeLinterConfigExpireInfo(
            String title, String message, Project project, NotificationType level, @NotNull AnAction action) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon =
                notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_ADD_CODE_LINTER_CONFIG_GROUP);
        Notification msg = balloon.createNotification(title, message, level).addAction(action);
        NotificationRecordManager.getInstance().notifyProject(msg, project);
    }

    /**
     * Notify auto expires info.
     * Expires automatically after 10 seconds.
     *
     * @param title title
     * @param message message
     * @param project project
     * @param level message level
     */
    public static void notifyAutoExpiresInfo(String title, String message, Project project, NotificationType level) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon =
            notificationGroupManager.getNotificationGroup(CANGJIE_MGMT_NOTIFICATION_EXPIRES_GROUP);
        Notification msg = balloon.createNotification(title, message, level);
        msg.notify(project);
    }

    /**
     * clear notifications
     * It can be used together with {@link #notifyExpireInfo}.
     */
    public static void clearNotifications() {
        NotificationRecordManager.getInstance().expireNotificationByGroup(CANGJIE_MGMT_NOTIFICATION_GROUP);
    }

    /**
     * clear add code linter config notifications
     * It can be used together with {@link #notifyAddCodeLinterConfigExpireInfo}.
     */
    public static void clearAddCodeLinterConfigNotifications() {
        NotificationRecordManager.getInstance()
                .expireNotificationByGroup(CANGJIE_MGMT_NOTIFICATION_ADD_CODE_LINTER_CONFIG_GROUP);
    }

    /**
     * get open file action
     *
     * @param path target path
     * @return open file action
     */
    public static AnAction getOpenFileAction(String path) {
        if (!new File(path).exists()) {
            return new AnAction() {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                }
            };
        }
        return new AnAction("Open") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
                if (file != null) {
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), file);
                    FileEditorManager.getInstance(e.getProject()).openEditor(descriptor, true);
                }
            }
        };
    }
}
