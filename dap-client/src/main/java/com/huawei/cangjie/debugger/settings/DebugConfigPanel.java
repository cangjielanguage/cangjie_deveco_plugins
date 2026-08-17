/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.settings;

import javax.swing.JPanel;

/**
 * DebugConfigPanel
 *
 * @since 2024-9-10
 */
public interface DebugConfigPanel {
    /**
     * getPanel
     *
     * @return javax.swing.JPanel
     */
    JPanel getPanel();

    /**
     * isModified
     *
     * @return boolean
     */
    boolean isModified();

    /**
     * apply
     */
    void apply();

    /**
     * reset
     */
    void reset();
}
