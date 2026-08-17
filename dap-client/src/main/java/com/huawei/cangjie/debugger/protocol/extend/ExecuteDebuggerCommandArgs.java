/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.protocol.extend;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import java.util.Objects;

/**
 * execute gdb/lldb command args
 *
 * @since 2022-10-19
 */
public class ExecuteDebuggerCommandArgs {
    /**
     * The debug command input by users
     */
    @NonNull
    private String debugCommand;

    public ExecuteDebuggerCommandArgs(String command) {
        this.debugCommand = command;
    }

    public String getDebugCommand() {
        return debugCommand;
    }

    public void setDebugCommand(String debugCommand) {
        this.debugCommand = debugCommand;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder stringBuilder = new ToStringBuilder(this);
        stringBuilder.add("debugCommand", this.debugCommand);
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExecuteDebuggerCommandArgs)) {
            return false;
        }
        ExecuteDebuggerCommandArgs that = (ExecuteDebuggerCommandArgs) obj;
        return Objects.equals(debugCommand, that.debugCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debugCommand);
    }
}
