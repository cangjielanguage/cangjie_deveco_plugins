/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import java.util.List;

/**
 * Cangjie dap launch parameters
 *
 * @since 2022-12-15
 */
public class CangjieAttachRequestArgs {
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
     * debugee
     */
    private String program;

    /**
     * debugee arguments
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

    private boolean remote;

    private String processId;

    /**
     * after connect commands
     */
    private List<String> afterConnectCommands;

    /**
     * post attach commands
     */
    private List<String> postAttachCommands;

    private String attachStep;

    private boolean showStaticGlobalVars;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public boolean isStopAtEntry() {
        return stopAtEntry;
    }

    public void setStopAtEntry(boolean stopAtEntry) {
        this.stopAtEntry = stopAtEntry;
    }

    public String getCwd() {
        return cwd;
    }

    public void setCwd(String cwd) {
        this.cwd = cwd;
    }

    public boolean isExternalConsole() {
        return externalConsole;
    }

    public void setExternalConsole(boolean externalConsole) {
        this.externalConsole = externalConsole;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemotePlatform() {
        return remotePlatform;
    }

    public void setRemotePlatform(String remotePlatform) {
        this.remotePlatform = remotePlatform;
    }

    public List<String> getStartupCommands() {
        return startupCommands;
    }

    public void setStartupCommands(List<String> startupCommands) {
        this.startupCommands = startupCommands;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public List<String> getAfterConnectCommands() {
        return afterConnectCommands;
    }

    public void setAfterConnectCommands(List<String> afterConnectCommands) {
        this.afterConnectCommands = afterConnectCommands;
    }

    public List<String> getPostAttachCommands() {
        return postAttachCommands;
    }

    public void setPostAttachCommands(List<String> postAttachCommands) {
        this.postAttachCommands = postAttachCommands;
    }

    public String getAttachStep() {
        return attachStep;
    }

    public void setAttachStep(String attachStep) {
        this.attachStep = attachStep;
    }

    public boolean isShowStaticGlobalVars() {
        return showStaticGlobalVars;
    }

    public void setShowStaticGlobalVars(boolean showStaticGlobalVars) {
        this.showStaticGlobalVars = showStaticGlobalVars;
    }
}
