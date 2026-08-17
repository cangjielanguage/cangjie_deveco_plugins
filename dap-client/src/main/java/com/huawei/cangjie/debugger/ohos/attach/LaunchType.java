/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.attach;

/**
 * client start type enum
 *
 * @since 2023-3-2
 */
public enum LaunchType {
    /**
     * start debug,Distinguishing the attach mode
     */
    DEBUGGER,
    /**
     * attach debug to process after run process
     */
    ATTACH_DEBUGGER_TO_PROCESS
}
