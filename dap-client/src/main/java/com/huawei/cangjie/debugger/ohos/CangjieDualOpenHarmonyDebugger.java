/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.huawei.deveco.debugger.ohos.dual.DualDebugger;
import com.huawei.deveco.ohos.debugcommon.debugger.DebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;

import org.jetbrains.annotations.NotNull;

/**
 * Cangjie dual debugger
 *
 * @since 2024-3-12
 */
public class CangjieDualOpenHarmonyDebugger extends DualDebugger {
    /**
     * debugger ID
     */
    public static final String ID = "CangjieJs";

    /**
     * debugger display name
     */
    public static final String DISPLAY_NAME = OhConstants.DUAL_JS_CANGJIE_DEBUG_TYPE;
    private static final int SORT_ID = 5;

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DebuggerState createState() {
        return new NativeDebuggerState();
    }

    @Override
    public int getSortId() {
        return SORT_ID;
    }
}