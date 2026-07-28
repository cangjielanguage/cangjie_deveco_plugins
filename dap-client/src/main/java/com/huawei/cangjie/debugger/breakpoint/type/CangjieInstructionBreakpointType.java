/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.type;

import com.huawei.bitfun.intellij.breakpoint.type.InstructionBreakpointTypeBase;
import com.huawei.bitfun.intellij.disassembly.DisassemblyVirtualFile;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * cangjie instruction breakpoint type
 *
 * @since 2024-4-18
 */
public class CangjieInstructionBreakpointType extends InstructionBreakpointTypeBase
    implements CangjieBreakpointBaseType {
    protected CangjieInstructionBreakpointType() {
        super("cangjie-instruction", "Cangjie Instruction Breakpoint");
    }

    /**
     * get instance
     *
     * @return CangjieInstructionBreakpointType
     */
    public static CangjieInstructionBreakpointType getInstance() {
        return XBreakpointUtil.breakpointTypes()
                .map(type -> {
                    if (type instanceof CangjieInstructionBreakpointType) {
                        return (CangjieInstructionBreakpointType) type;
                    }
                    return CodeCheckByPassUtils.getNull();
                })
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile virtualFile, int line, @NotNull Project project) {
        if (super.canPutAt(virtualFile, line, project)) {
            if (virtualFile instanceof DisassemblyVirtualFile) {
                DisassemblyVirtualFile file = (DisassemblyVirtualFile) virtualFile;
                if (file.getDapProcess() instanceof CangjieXDebugProcess) {
                    return true;
                }
            }
        }
        return false;
    }
}
