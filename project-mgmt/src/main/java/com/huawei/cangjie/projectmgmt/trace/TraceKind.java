/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.trace;

/**
 * TraceKind
 *
 * @since 2024/12/06
 */
public enum TraceKind {
    CJ_CREATE_HYBRID_PROJECT("cjCreatProject", "hybrid cangjie project"),
    CJ_CREATE_PURE_PROJECT("cjCreatProject", "pure cangjie project"),
    CJ_OPEN_PROJECT("cjOpenProject", "open cangjie project"),
    CJ_CREATE_HYBRID_HAP("cjCreateHap", "hybrid cangjie hap"),
    CJ_CREATE_PURE_HAP("cjCreateHap", "pure cangjie hap"),
    CJ_CREATE_HYBRID_HAR("cjCreateHar", "hybrid cangjie har"),
    CJ_CREATE_PURE_HAR("cjCreateHar", "pure cangjie har"),
    CJ_ADD_FILE("cjAddFile", "cangjie normal file"),
    CJ_ADD_HYBRID_PAGE_FILE("cjAddFile", "cangjie hybrid page file"),
    CJ_ENABLE("cjEnable", "enable cangjie"),
    CJ_COMPILE_APP("cjCompileApp", "normal compile app"),
    CJ_COMPILE_HAP("cjCompileHap", "normal compile hap"),
    CJ_COMPILE_HAR("cjCompileHar", "normal compile har");

    private final String action;
    private final String cause;

    TraceKind(String action, String cause) {
        this.action = action;
        this.cause = cause;
    }

    /**
     * get action
     *
     * @return action name
     */
    public String getAction() {
        return action;
    }

    /**
     * get kind
     *
     * @return kind name
     */
    public String getCause() {
        return cause;
    }
}
