/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Cangjie source breakpoint properties.
 *
 * @since 2023 -02-06
 */
public class CangjieSourceBreakpointProperties
    extends CangjieBreakpointFiltersProperties<CangjieSourceBreakpointProperties> {
    @Nullable
    @Override
    public CangjieSourceBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CangjieSourceBreakpointProperties state) {
        super.loadState(state);
    }
}
