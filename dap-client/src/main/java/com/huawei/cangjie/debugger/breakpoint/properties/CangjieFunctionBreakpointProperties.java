/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.properties;

import com.intellij.util.xmlb.annotations.Attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * cangjie method breakpoint properties.
 *
 * @since 2022-11-26
 */
public class CangjieFunctionBreakpointProperties
    extends CangjieBreakpointFiltersProperties<CangjieFunctionBreakpointProperties> {
    @Attribute("functionName")
    private String functionName;

    /**
     * Instantiates a new Cangjie function breakpoint properties.
     */
    public CangjieFunctionBreakpointProperties() {
    }

    /**
     * Instantiates a new Cangjie function breakpoint properties.
     *
     * @param functionName the function name
     */
    public CangjieFunctionBreakpointProperties(String functionName) {
        this.functionName = functionName;
    }

    @Nullable
    @Override
    public CangjieFunctionBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CangjieFunctionBreakpointProperties state) {
        super.loadState(state);
        setFunctionName(state.getFunctionName());
    }

    /**
     * Gets function name.
     *
     * @return the function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Set function name.
     *
     * @param functionName the function name
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CangjieFunctionBreakpointProperties)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CangjieFunctionBreakpointProperties that = (CangjieFunctionBreakpointProperties) obj;
        return Objects.equals(functionName, that.functionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), functionName);
    }
}
