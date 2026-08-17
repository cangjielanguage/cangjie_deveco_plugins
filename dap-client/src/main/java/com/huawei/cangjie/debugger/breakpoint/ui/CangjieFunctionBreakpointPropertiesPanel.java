/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.ui;

import com.huawei.cangjie.debugger.breakpoint.properties.CangjieFunctionBreakpointProperties;

import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;

import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Cangjie Method Breakpoint Properties Panel
 *
 * @since 2022-11-26
 */
public class CangjieFunctionBreakpointPropertiesPanel
    extends XBreakpointCustomPropertiesPanel<XBreakpoint<CangjieFunctionBreakpointProperties>> {
    private final CangjieFunctionBreakpointPropertiesComponent panel;

    public CangjieFunctionBreakpointPropertiesPanel(CangjieFunctionBreakpointProperties properties) {
        this.panel = new CangjieFunctionBreakpointPropertiesComponent(properties);
    }

    @Override
    public JComponent getComponent() {
        JComponent jComponent = panel.getCangjieMethodBreakpointPropertiesPanel();
        if (jComponent == null) {
            throw new IllegalStateException(
                "panel.getCangjieMethodBreakpointPropertiesPanel is null" + System.currentTimeMillis());
        }
        return jComponent;
    }

    @Override
    public void saveTo(@NotNull XBreakpoint<CangjieFunctionBreakpointProperties> breakpoint) {
        String functionName = panel.getFunctionName();
        JPanel cangjieMethodBreakpointPropertiesPanel = panel.getCangjieMethodBreakpointPropertiesPanel();
        if (functionName.isEmpty()) {
            if (cangjieMethodBreakpointPropertiesPanel != null) {
                Messages.showErrorDialog(cangjieMethodBreakpointPropertiesPanel, "Function name can't be empty!");
            }
            return;
        }
        CangjieFunctionBreakpointProperties properties = breakpoint.getProperties();
        if (!functionName.equals(properties.getFunctionName()) && breakpoint instanceof XBreakpointBase) {
            properties.setFunctionName(functionName);
            ((XBreakpointBase<?, ?, ?>) breakpoint).fireBreakpointChanged();
        }
    }

    @Override
    public void loadFrom(@NotNull XBreakpoint<CangjieFunctionBreakpointProperties> breakpoint) {
        CangjieFunctionBreakpointProperties properties = breakpoint.getProperties();
        if (properties != null) {
            panel.setFunctionName(properties.getFunctionName());
        }
    }

    @Override
    public String toString() {
        return "CangjieFunctionBreakpointPropertiesPanel{}";
    }
}
