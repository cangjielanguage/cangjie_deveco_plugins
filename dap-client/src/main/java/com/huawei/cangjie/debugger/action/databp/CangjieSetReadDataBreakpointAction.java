/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.action.databp;

import com.huawei.bitfun.intellij.action.databp.SetReadDataBreakpointAction;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;

/**
 * set read data breakpoint action
 *
 * @since 2023-1-20
 */
public class CangjieSetReadDataBreakpointAction extends SetReadDataBreakpointAction {
    @Override
    protected String getLanguageId() {
        return CangjieXDebugProcess.LANGUAGE_ID;
    }
}
