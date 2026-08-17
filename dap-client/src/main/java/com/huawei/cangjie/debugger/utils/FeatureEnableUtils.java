/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

/**
 * enable/disable features in code
 * should set to false on master branch
 * should set to true on oh branch
 *
 * @since 2023-1-16
 */
public class FeatureEnableUtils {
    /**
     * is data breakpoint enabled
     */
    public static final boolean IS_DATA_BP_ENABLED = true;

    /**
     * is support auto save
     */
    public static final boolean IS_SUPPORT_AUTO_SAVE = true;

    /**
     * is timeline view enabled
     */
    public static final boolean IS_TIMELINE_VIEW_ENABLED = true;

    /**
     * is set execution point enabled
     */
    public static final boolean IS_SET_EXECUTION_POINT_ENABLED = false;
}
