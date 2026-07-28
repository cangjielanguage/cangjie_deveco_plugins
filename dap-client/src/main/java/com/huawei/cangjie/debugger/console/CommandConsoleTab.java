/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.timetravel.TimeTravelProcess;
import com.huawei.bitfun.intellij.timetravel.TimeTravelView;
import com.huawei.bitfun.intellij.utils.DebugTabView;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;

import com.intellij.execution.console.LanguageConsoleBuilder;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.content.Content;

import org.jetbrains.annotations.NotNull;

/**
 * LldbCommandConsoleTab
 *
 * @since 2022-10-19
 */
public class CommandConsoleTab implements DebugTabView, TimeTravelView {
    /**
     * myProject
     */
    private final Project myProject;

    /**
     * display name for this console
     */
    private final String displayName;

    /**
     * debug process
     */
    private final DapXDebugProcess<DapFromServerService, CangjieDapToServerService> myProcess;

    /**
     * executeLldbCommandHandler
     */
    private final ExecuteCommandHandler executeCommandHandler;

    /**
     * language console view
     */
    private volatile LanguageConsoleView myLldbConsoleView;

    /**
     * lldbConsolePrinter
     */
    private final ConsolePrinter lldbConsolePrinter;

    /**
     * CommandConsoleTab struct
     *
     * @param project project
     * @param process process
     * @param displayName displayName
     */
    public CommandConsoleTab(@NotNull Project project, String displayName,
        DapXDebugProcess<DapFromServerService, CangjieDapToServerService> process) {
        this.myProject = project;
        this.displayName = displayName;
        this.myProcess = process;
        this.lldbConsolePrinter = new ConsolePrinter(project, displayName);
        this.myLldbConsoleView = lldbConsolePrinter.getMyLldbConsoleView();
        this.executeCommandHandler = new ExecuteCommandHandler(myProcess, lldbConsolePrinter);
    }

    /**
     * clear lldb command console
     */
    public void clearLldbCommandConsole() {
        myLldbConsoleView.clear();
    }

    /**
     * set prompt of lldb console tab
     *
     * @param text text
     */
    public void setPrompt(String text) {
        myLldbConsoleView.setPrompt(text);
    }

    /**
     * set prompt of lldb console tab to default
     */
    private void setDefaultPrompt() {
        setPrompt(lldbConsolePrinter.getDefaultPrompt());
    }

    /**
     * Print error log
     *
     * @param text text
     */
    private void printError(String text) {
        lldbConsolePrinter.printErrorInfo(text);
    }

    @Override
    public void registerContent(@NotNull RunnerLayoutUi runnerLayoutUi) {
        Content content = runnerLayoutUi.createContent("LLDB_API_DEBUGGER_CONSOLE", myLldbConsoleView.getComponent(),
            displayName, null, null);
        Disposer.register(runnerLayoutUi.getContentManager(), myLldbConsoleView);
        content.setCloseable(false);
        runnerLayoutUi.addContent(content, 0, PlaceInGrid.center, false);
        LanguageConsoleBuilder.registerExecuteAction(myLldbConsoleView, executeCommandHandler, "LLDB_API.console.debug",
            null, null);
    }

    @Override
    public void clearContent() {
        myLldbConsoleView.clear();
    }

    @Override
    public void refreshWhenTimeSwitchIsOpen() {
        clearContent();
        printError(TimeTravelProcess.NOT_SUPPORTED_WARNING);
        setPrompt("");
    }

    @Override
    public void refreshWhenTimeSwitchIsClose() {
        clearContent();
        setDefaultPrompt();
    }

    @Override
    public String toString() {
        return "CommandConsoleTab{" + "myProject=" + myProject + ", displayName='" + displayName + '\''
            + ", myProcess=" + myProcess + ", executeCommandHandler=" + executeCommandHandler
            + ", myLldbConsoleView=" + myLldbConsoleView + '}';
    }
}
