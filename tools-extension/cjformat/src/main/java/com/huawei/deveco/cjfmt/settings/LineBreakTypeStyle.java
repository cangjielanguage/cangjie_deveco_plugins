/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.settings;

import org.jetbrains.annotations.NotNull;

/**
 * LineBreakTypeStyle
 *
 * @since 2024 -11-09
 */
public enum LineBreakTypeStyle {
    /**
     * Lf line break type style.
     */
    LF("LF", "LF"),
    /**
     * Crlf line break type style.
     */
    CRLF("CRLF", "CRLF");

    private final String breakType;

    private final String description;

    LineBreakTypeStyle(@NotNull String breakType, @NotNull String description) {
        this.breakType = breakType;
        this.description = description;
    }

    /**
     * Gets break type.
     *
     * @return the break type
     */
    public String getBreakType() {
        return breakType;
    }

    /**
     * Gets my description.
     *
     * @return the my description
     */
    public String getDescription() {
        return description;
    }
}