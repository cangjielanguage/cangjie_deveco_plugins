/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.ui;

import com.huawei.cangjie.debugger.breakpoint.properties.CangjieBreakpointFiltersProperties;

import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;

import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The type Cangjie breakpoint filter panel.
 *
 * @param <B> the type parameter
 * @since 2023 -02-06
 */
public class CangjieBreakpointFiltersPanel<B extends XBreakpoint<? extends CangjieBreakpointFiltersProperties<?>>>
    extends XBreakpointCustomPropertiesPanel<B> {
    private JPanel filtersPanel;

    private JCheckBox hitCountCheckBox;

    private JTextField hitCountTextField;

    /**
     * Instantiates a new Cangjie breakpoint filter panel.
     */
    public CangjieBreakpointFiltersPanel() {
        hitCountCheckBox.addActionListener(e -> updateCheckboxes());
    }

    @Override
    public JComponent getComponent() {
        return filtersPanel;
    }

    @Override
    public boolean isVisibleOnPopup(@NotNull B breakpoint) {
        return false;
    }

    @Override
    public void saveTo(@NotNull B breakpoint) {
        CangjieBreakpointFiltersProperties<?> properties = breakpoint.getProperties();
        if (properties == null) {
            return;
        }
        String hitCount = hitCountTextField.getText().trim();
        boolean isHitCountChanged = properties.setHitCount(hitCount);
        boolean isCheckBoxChanged =
            properties.setHitCountEnabled(!hitCount.isEmpty() && hitCountCheckBox.isSelected());
        if ((isHitCountChanged || isCheckBoxChanged) && breakpoint instanceof XBreakpointBase) {
            ((XBreakpointBase) breakpoint).fireBreakpointChanged();
        }
    }

    @Override
    public void loadFrom(@NotNull B breakpoint) {
        CangjieBreakpointFiltersProperties<?> properties = breakpoint.getProperties();
        if (properties != null) {
            hitCountTextField.setText(properties.getHitCount());
            hitCountCheckBox.setSelected(properties.isHitCountEnabled());
        }
        updateCheckboxes();
    }

    /**
     * Update checkboxes.
     */
    private void updateCheckboxes() {
        hitCountTextField.setEditable(hitCountCheckBox.isSelected());
        hitCountTextField.setEnabled(hitCountCheckBox.isSelected());
    }

    @Override
    public String toString() {
        return "CangjieBreakpointFiltersPanel{}";
    }
}
