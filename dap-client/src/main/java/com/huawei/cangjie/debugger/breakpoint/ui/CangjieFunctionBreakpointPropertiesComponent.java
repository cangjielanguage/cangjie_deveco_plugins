/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.ui;

import com.huawei.cangjie.debugger.breakpoint.properties.CangjieFunctionBreakpointProperties;

import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Cangjie Method Breakpoint Properties Component
 *
 * @since 2022-11-26
 */
public class CangjieFunctionBreakpointPropertiesComponent {
    private JPanel rootPanel;

    private JTextField functionNameField;

    /**
     * Instantiates a new Cangjie function breakpoint properties component.
     *
     * @param properties the properties
     */
    public CangjieFunctionBreakpointPropertiesComponent(
        CangjieFunctionBreakpointProperties properties) {
        functionNameField.setText(properties.getFunctionName());
    }

    /**
     * get properties panel
     *
     * @return panel cangjie method breakpoint properties panel
     */
    @Nullable
    public JPanel getCangjieMethodBreakpointPropertiesPanel() {
        return rootPanel;
    }

    /**
     * get FunctionName
     *
     * @return FunctionName function name
     */
    public String getFunctionName() {
        return functionNameField.getText().trim();
    }

    /**
     * set function name
     *
     * @param functionName name
     */
    public void setFunctionName(String functionName) {
        functionNameField.setText(functionName);
    }

    @Override
    public String toString() {
        return "CangjieFunctionBreakpointPropertiesComponent{}";
    }
}
