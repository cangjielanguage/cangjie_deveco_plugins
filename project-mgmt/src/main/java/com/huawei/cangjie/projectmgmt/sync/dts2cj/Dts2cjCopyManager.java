/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.dts2cj;

import com.intellij.openapi.project.Project;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Dts2cj copy manager.
 *
 * @since 2025 -01-18
 */
public class Dts2cjCopyManager {
    /**
     * <project location, <src path, dest path>
     */
    private final Map<String, Map<Path, Path>> dts2cjRecordMap = new HashMap<>();

    private static class Holder {
        /**
         * The constant INSTANCE.
         */
        public static final Dts2cjCopyManager INSTANCE = new Dts2cjCopyManager();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Dts2cjCopyManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Gets and remove sync copy.
     *
     * @param project the project
     * @return the sync copy
     */
    public Map<Path, Path> popSyncCopy(Project project) {
        String projectLocationHash = project != null ? project.getLocationHash() : "";
        Map<Path, Path> copyMap = dts2cjRecordMap.remove(projectLocationHash);
        return copyMap == null ? Collections.emptyMap() : copyMap;
    }

    /**
     * Gets sync copy.
     *
     * @param project the project
     * @return the sync copy
     */
    public Map<Path, Path> getSyncCopy(Project project) {
        String projectLocationHash = project != null ? project.getLocationHash() : "";
        return dts2cjRecordMap.getOrDefault(projectLocationHash, Collections.emptyMap());
    }

    /**
     * add dts2cj results copy
     *
     * @param copyPathMap the copy path
     * @param project the project
     */
    public synchronized void addSyncCopy(Map<Path, Path> copyPathMap, Project project) {
        String projectLocationHash = project != null ? project.getLocationHash() : "";
        dts2cjRecordMap.put(projectLocationHash, copyPathMap);
    }

    /**
     * Remove sync copy.
     *
     * @param project the project
     */
    public synchronized void removeSyncCopy(Project project) {
        String projectLocationHash = project != null ? project.getLocationHash() : "";
        dts2cjRecordMap.remove(projectLocationHash);
    }
}
