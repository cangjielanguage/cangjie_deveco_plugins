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
import com.huawei.bitfun.intellij.utils.DapClientTimeouts;
import com.huawei.bitfun.intellij.utils.EdtRequestCallbacks;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandArgs;
import com.huawei.cangjie.debugger.protocol.extend.ExecuteDebuggerCommandResponse;

import com.intellij.util.Consumer;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * ExecuteLldbCommandHandler
 *
 * @since 2022-10-19
 */
public class ExecuteCommandHandler implements Consumer<String> {
    private final DapXDebugProcess<DapFromServerService, CangjieDapToServerService> cangjieXDebugProcess;

    private final ConsolePrinter lldbConsolePrinter;

    public ExecuteCommandHandler(DapXDebugProcess<DapFromServerService, CangjieDapToServerService> cangjieXDebugProcess,
        ConsolePrinter lldbConsolePrinter) {
        this.cangjieXDebugProcess = cangjieXDebugProcess;
        this.lldbConsolePrinter = lldbConsolePrinter;
    }

    @Override
    public void consume(String input) {
        String checkCommand = commandCheck(input);
        if (checkCommand != null) {
            executeDebuggerCommand(checkCommand);
        }
    }

    /**
     *  check input command
     *
     * @param inputCommand input
     * @return Verification successful return input.trim() otherwise return null
     */
    @Nullable
    private String commandCheck(String inputCommand) {
        if (inputCommand == null || inputCommand.isEmpty()) {
            return CodeCheckByPassUtils.getNull();
        }
        String inputTrim = inputCommand.trim();
        if (isInputInValid(inputTrim)) {
            lldbConsolePrinter.printErrorInfo(String.format(Locale.ENGLISH,
                    "Error : %s is not a valid command", inputTrim));
            return CodeCheckByPassUtils.getNull();
        }
        return inputTrim;
    }

    /**
     * basic check input command is 'a' to 'z' and not '-'
     *
     * @param userInput input
     * @return boolean If the command is invalid, it returns true; otherwise, it returns false
     */
    private boolean isInputInValid(String userInput) {
        return userInput.isEmpty() || ((userInput.charAt(0) < 'a' || userInput.charAt(0) > 'z')
                && userInput.charAt(0) != '-');
    }

    /**
     * executeDebuggerCommand
     *
     * @param userInput userInput
     */
    private void executeDebuggerCommand(String userInput) {
        ExecuteDebuggerCommandArgs executeDebuggerCommandArgs = new ExecuteDebuggerCommandArgs(userInput);
        cangjieXDebugProcess.getDapConnection().getToRemoteService().getExecuteDebuggerCommandRequester()
            .requestAsync(executeDebuggerCommandArgs, DapClientTimeouts.COMMON_LONG_TIMEOUT,
                new EdtRequestCallbacks<>() {
                    @Override
                    public void whenSuccessEdt(ExecuteDebuggerCommandResponse executeDebuggerCommandResponse) {
                        String responseOutput = executeDebuggerCommandResponse.getOutput();
                        lldbConsolePrinter.printDebugInfo(deleteLineFeed(responseOutput));
                        // clearLineExtensionInfo
                        CommandCompletion.INSTANCE.clearLineExtensionInfos();
                    }

                    @Override
                    public void whenErrorEdt(Exception ex) {
                        lldbConsolePrinter.printErrorInfo(
                                String.format(Locale.ENGLISH, "Error : %s command Execution failed", userInput));
                    }
                });
    }

    /**
     * Delete \r\n , \n
     *
     * @param output output
     * @return java.lang.String
     */
    private String deleteLineFeed(String output) {
        return output.replaceAll("(\\\\r\\\\n|\\\\n)", System.lineSeparator());
    }

    @Override
    public String toString() {
        return "ExecuteCommandHandler{" + "cangjieXDebugProcess=" + cangjieXDebugProcess
                + ", lldbConsolePrinter=" + lldbConsolePrinter + '}';
    }
}
