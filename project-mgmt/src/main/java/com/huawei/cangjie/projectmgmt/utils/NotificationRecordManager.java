/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Notification manager.
 *
 * @since 2024 -11-24
 */
public class NotificationRecordManager {
    /**
     * <groupId, <project location, notification>
     */
    private final Map<String, Map<String, Notification>> notificationGroupMap = new HashMap<>();

    private static class Holder {
        /**
         * The constant INSTANCE.
         */
        public static final NotificationRecordManager INSTANCE = new NotificationRecordManager();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NotificationRecordManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Notify project.
     *
     * @param notification the notification
     * @param project the project
     */
    public synchronized void notifyProject(Notification notification, Project project) {
        final String groupId = notification.getGroupId();
        Map<String, Notification> notificationMap =
            notificationGroupMap.computeIfAbsent(groupId, (id) -> new HashMap<>());
        String projectLocationHash = "";
        if (project != null) {
            projectLocationHash = project.getLocationHash();
        }

        Notification oldNotification = notificationMap.remove(projectLocationHash);
        if (oldNotification != null) {
            oldNotification.expire();
        }

        final String finalProjectLocationHash = projectLocationHash;
        notificationMap.put(projectLocationHash, notification);
        notification.whenExpired(() -> removeNotification(groupId, finalProjectLocationHash));
        notification.notify(project);
    }

    private void removeNotification(String groupId, String projectLocationHash) {
        Map<String, Notification> notificationMap = notificationGroupMap.get(groupId);
        if (notificationMap != null) {
            notificationMap.remove(projectLocationHash);
        }
    }

    /**
     * Expire notification by group.
     *
     * @param groupId the group id
     */
    public synchronized void expireNotificationByGroup(String groupId) {
        Map<String, Notification> removedMap = notificationGroupMap.remove(groupId);
        if (removedMap == null) {
            return;
        }
        List<String> projectHashes = new ArrayList<>(removedMap.keySet());
        for (String projectHash : projectHashes) {
            Notification toRemove = removedMap.remove(projectHash);
            if (toRemove != null) {
                toRemove.expire();
            }
        }
    }
}
