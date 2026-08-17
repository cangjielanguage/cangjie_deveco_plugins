/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.localtest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Cangjie dap launch parameters
 *
 * @since 2025-8-31
 */
@Getter
@Setter
@Builder
public class CangjieLaunchRequestArgs {
    /**
     * run configuration name
     */
    private String name;

    /**
     * debugger type
     */
    private String type;

    /**
     * launch or attach
     */
    private String request;

    /**
     * debuggee
     */
    private String program;

    /**
     * debuggee arguments
     */
    private List<String> args;

    /**
     * stopAtEntry
     */
    private boolean stopAtEntry;

    /**
     * project directory
     */
    private String cwd;

    /**
     * externalConsole
     */
    private boolean externalConsole;

    /**
     * lldb server connect address
     */
    private String remoteAddress;

    /**
     * remote platform type
     */
    private String remotePlatform;

    /**
     * startup commands
     */
    private List<String> startupCommands;

    /**
     * environments
     */
    private Map<String, String> env;

    /**
     * setup commands
     */
    private List<String> setupCommands;

    /**
     * script commands
     */
    private List<String> scriptCommands;

    /**
     * debuggee started
     */
    private boolean debuggeeStarted;
}
