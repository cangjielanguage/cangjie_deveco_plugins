/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.type;

import com.huawei.bitfun.intellij.breakpoint.properties.SourceXBreakpointProperties;
import com.huawei.bitfun.intellij.breakpoint.type.SourceBreakpointTypeBase;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.deveco.lsp.LspUtils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Cangjie source breakpoint type
 *
 * @since 2022-10-19
 */
public class CangjieSourceBreakpointType extends SourceBreakpointTypeBase implements CangjieBreakpointBaseType {
    public CangjieSourceBreakpointType() {
        super("cangjie-line", "Cangjie Line Breakpoint");
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        return LspUtils.isVirtualFileSupportedByLsp(file);
    }

    @Override
    @Nullable
    public XDebuggerEditorsProvider getEditorsProvider(
            @NotNull XLineBreakpoint<SourceXBreakpointProperties> breakpoint, @NotNull Project project) {
        return LspUtils.getXDebuggerEditorsProvider();
    }

    /**
     * get instance of CangjieSourceBreakpointType
     *
     * @return CangjieSourceBreakpointType
     */
    public static CangjieSourceBreakpointType getInstance() {
        return XBreakpointUtil.breakpointTypes()
            .map(type -> {
                if (type instanceof CangjieSourceBreakpointType) {
                    return (CangjieSourceBreakpointType) type;
                }
                return CodeCheckByPassUtils.getNull();
            })
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }
}
