/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.type;

import com.huawei.bitfun.intellij.breakpoint.type.DataBreakpointTypeBase;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;

import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;

import java.util.Objects;

/**
 * cangjie data breakpoint type
 *
 * @since 2023-1-19
 */
public class CangjieDataBreakpointType extends DataBreakpointTypeBase implements CangjieBreakpointBaseType {
    /**
     * constructor
     */
    protected CangjieDataBreakpointType() {
        super("cangjie-data", "Cangjie Data Breakpoint");
    }

    /**
     * get instance
     *
     * @return CangjieDataBreakpointType
     */
    public static CangjieDataBreakpointType getInstance() {
        return XBreakpointUtil.breakpointTypes()
            .map(type -> {
                if (type instanceof CangjieDataBreakpointType) {
                    return (CangjieDataBreakpointType) type;
                }
                return CodeCheckByPassUtils.getNull();
            })
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }
}
