/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.type;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.breakpoint.properties.CangjieFunctionBreakpointProperties;
import com.huawei.cangjie.debugger.breakpoint.ui.AddCangjieFunctionBreakpointDialog;
import com.huawei.cangjie.debugger.breakpoint.ui.CangjieBreakpointFiltersPanel;
import com.huawei.cangjie.debugger.breakpoint.ui.CangjieFunctionBreakpointPropertiesPanel;
import com.huawei.cangjie.debugger.deveco.lsp.LspUtils;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * cangjie function breakpoint type
 *
 * @since 2022-11-26
 */
public class CangjieFunctionBreakpointType
    extends XBreakpointType<XBreakpoint<CangjieFunctionBreakpointProperties>, CangjieFunctionBreakpointProperties>
    implements CangjieBreakpointBaseType {
    private static final String EMPTY = "<Empty>";

    private static final String TITLE = "Cangjie Function Breakpoints";

    private CangjieFunctionBreakpointProperties properties;

    public CangjieFunctionBreakpointType() {
        super("cangjie-function-method", TITLE);
    }

    @Override
    public boolean isAddBreakpointButtonVisible() {
        return true;
    }

    /**
     * get instance of CangjieFunctionBreakpointType
     *
     * @return CangjieFunctionBreakpointType
     */
    public static CangjieFunctionBreakpointType getInstance() {
        return XBreakpointUtil.breakpointTypes()
            .map(type -> {
                if (type instanceof CangjieFunctionBreakpointType) {
                    return (CangjieFunctionBreakpointType) type;
                }
                return CodeCheckByPassUtils.getNull();
            })
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }

    /**
     * overwrite suspendThreadSupport to hide box
     *
     * @return false
     */
    @Override
    public boolean isSuspendThreadSupported() {
        return false;
    }

    @Override
    public String getDisplayText(XBreakpoint<CangjieFunctionBreakpointProperties> breakpoint) {
        properties = breakpoint.getProperties();
        if (properties == null) {
            return EMPTY;
        }
        String functionName = properties.getFunctionName();
        return functionName.isEmpty() ? EMPTY : functionName;
    }

    @Nullable
    @Override
    public XBreakpoint<CangjieFunctionBreakpointProperties> addBreakpoint(Project project, JComponent parentComponent) {
        AddCangjieFunctionBreakpointDialog dialog = new AddCangjieFunctionBreakpointDialog(project);
        if (!dialog.showAndGet()) {
            return null;
        }
        return WriteAction.compute(() -> {
            properties = new CangjieFunctionBreakpointProperties(dialog.getFunctionName());
            return XDebuggerManager.getInstance(project).getBreakpointManager().addBreakpoint(this, properties);
        });
    }

    @Override
    @Nullable
    public CangjieFunctionBreakpointProperties createProperties() {
        return new CangjieFunctionBreakpointProperties();
    }

    @Nullable
    @Override
    public XDebuggerEditorsProvider getEditorsProvider(
            @NotNull XBreakpoint<CangjieFunctionBreakpointProperties> breakpoint, @NotNull Project project) {
        return LspUtils.getXDebuggerEditorsProvider();
    }

    @Override
    @Nullable
    public XBreakpointCustomPropertiesPanel<XBreakpoint<CangjieFunctionBreakpointProperties>>
        createCustomTopPropertiesPanel(
        @NotNull Project project) {
        return new CangjieFunctionBreakpointPropertiesPanel(properties);
    }

    @Override
    public Icon getEnabledIcon() {
        return AllIcons.Debugger.Db_method_breakpoint;
    }

    @Override
    public Icon getDisabledIcon() {
        return AllIcons.Debugger.Db_disabled_method_breakpoint;
    }

    @Override
    public Icon getSuspendNoneIcon() {
        return AllIcons.Debugger.Db_no_suspend_method_breakpoint;
    }

    @NotNull
    @Override
    public final XBreakpointCustomPropertiesPanel<XBreakpoint<CangjieFunctionBreakpointProperties>>
        createCustomRightPropertiesPanel(
        @NotNull Project project) {
        return new CangjieBreakpointFiltersPanel<>();
    }
}
