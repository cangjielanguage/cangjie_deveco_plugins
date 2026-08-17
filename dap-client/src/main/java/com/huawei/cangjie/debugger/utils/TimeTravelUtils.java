/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.huawei.bitfun.intellij.utils.PersistenceUtils;

import com.intellij.openapi.project.Project;

/**
 * time travel utils
 *
 * @since 2023-05-23
 */
public class TimeTravelUtils {
    /**
     * IS_ALL_THREADS_DEFAULT
     */
    public static final boolean IS_ALL_THREADS_DEFAULT = false;

    /**
     * FRAMES_SIZE_DEFAULT
     */
    public static final int FRAMES_SIZE_DEFAULT = 1;

    /**
     * SCOPES_DEFAULT
     */
    public static final int SCOPES_DEFAULT = 1;

    /**
     * VARIABLE_PAGE_SIZE_DEFAULT
     */
    public static final int VARIABLE_PAGE_SIZE_DEFAULT = 30;

    /**
     * VARIABLES_DEPTH_DEFAULT
     */
    public static final int VARIABLES_DEPTH_DEFAULT = 2;

    /**
     * VARIABLE_CHILDREN_COUNT_DEFAULT
     */
    public static final int VARIABLE_CHILDREN_COUNT_DEFAULT = 100;
    private static final String TIME_TRAVEL = "cangjie_time_travel";

    private static final String ALL_THREADS = "cangjie_ttd_all_threads";

    private static final String FRAMES_SIZE = "cangjie_ttd_frames_size";

    private static final String SCOPES = "cangjie_ttd_scopes";

    private static final String VARIABLES_DEPTH = "cangjie_ttd_variables_depth";

    private static final String VARIABLE_CHILDREN_COUNT = "cangjie_ttd_variable_children_count";

    /**
     * get time travel
     *
     * @param project project
     * @return boolean
     */
    public static boolean getTimeTravel(Project project) {
        return PersistenceUtils.getBool(project, TIME_TRAVEL, false);
    }

    /**
     * set time travel
     *
     * @param project project
     * @param isTimeTravel isTimeTravel
     */
    public static void setTimeTravel(Project project, boolean isTimeTravel) {
        PersistenceUtils.setBool(project, TIME_TRAVEL, isTimeTravel);
    }

    /**
     * get is all thread
     *
     * @param project project
     * @return boolean
     */
    public static boolean getIsAllThread(Project project) {
        return PersistenceUtils.getBool(project, ALL_THREADS, IS_ALL_THREADS_DEFAULT);
    }

    /**
     * set is all threads
     *
     * @param project project
     * @param isAllThreads isAllThreads
     */
    public static void setIsAllThreads(Project project, boolean isAllThreads) {
        PersistenceUtils.setBool(project, ALL_THREADS, isAllThreads);
    }

    /**
     * get frames size
     *
     * @param project project
     * @return int
     */
    public static int getFramesSize(Project project) {
        return PersistenceUtils.getInt(project, FRAMES_SIZE, FRAMES_SIZE_DEFAULT);
    }

    /**
     * set frames size
     *
     * @param project project
     * @param framesSize framesSize
     */
    public static void setFramesSize(Project project, String framesSize) {
        PersistenceUtils.setValue(project, FRAMES_SIZE, framesSize);
    }

    /**
     * get scopes
     *
     * @param project project
     * @return int
     */
    public static int getScopes(Project project) {
        return PersistenceUtils.getInt(project, SCOPES, SCOPES_DEFAULT);
    }

    /**
     * set scopes
     *
     * @param project project
     * @param scopes scopes
     */
    public static void setScopes(Project project, int scopes) {
        PersistenceUtils.setInt(project, SCOPES, scopes);
    }

    /**
     * get variables page size
     *
     * @return int
     */
    public static int getVariablesPageSize() {
        return VARIABLE_PAGE_SIZE_DEFAULT;
    }

    /**
     * get variables depth
     *
     * @param project project
     * @return int
     */
    public static int getVariablesDepth(Project project) {
        return PersistenceUtils.getInt(project, VARIABLES_DEPTH, VARIABLES_DEPTH_DEFAULT);
    }

    /**
     * set variables depth
     *
     * @param project project
     * @param variablesDepth variablesDepth
     */
    public static void setVariablesDepth(Project project, String variablesDepth) {
        PersistenceUtils.setValue(project, VARIABLES_DEPTH, variablesDepth);
    }

    /**
     * get variable children count
     *
     * @param project project
     * @return int
     */
    public static int getVariableChildrenCount(Project project) {
        return PersistenceUtils.getInt(project, VARIABLE_CHILDREN_COUNT, VARIABLE_CHILDREN_COUNT_DEFAULT);
    }

    /**
     * set variable children count
     *
     * @param project project
     * @param variableChildrenCount variableChildrenCount
     */
    public static void setVariableChildrenCount(Project project, String variableChildrenCount) {
        PersistenceUtils.setValue(project, VARIABLE_CHILDREN_COUNT, variableChildrenCount);
    }
}
