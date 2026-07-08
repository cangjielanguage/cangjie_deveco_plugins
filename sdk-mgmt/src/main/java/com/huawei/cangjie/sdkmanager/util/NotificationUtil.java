/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

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
     * The constant CANGJIE_SDK_NOTIFICATION_GROUP.
     */
    public static final String CANGJIE_SDK_NOTIFICATION_GROUP = "Cangjie.Sdk.Manager.Notification";

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
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_SDK_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(title, message, level);
        msg.notify(project);
    }

    /**
     * notify info
     *
     * @param message message
     * @param project project
     * @param level message level
     */
    public static void notifyInfo(String message, Project project, NotificationType level) {
        NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_SDK_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(message, level);
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
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_SDK_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(title, message, level).addAction(action);
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
        NotificationGroup balloon = notificationGroupManager.getNotificationGroup(CANGJIE_SDK_NOTIFICATION_GROUP);
        Notification msg = balloon.createNotification(message, level).addAction(action);
        msg.notify(project);
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
