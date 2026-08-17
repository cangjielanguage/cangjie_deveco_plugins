/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.intellij.openapi.util.SystemInfo;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import java.io.IOException;

/**
 * Pty process utils
 *
 * @since 2025-09-11
 */
public class PtyUtils {
    private static final String CMD_PATH = "C:\\Windows\\System32\\cmd.exe";

    private static final String BASH_PATH = "/bin/bash";

    /**
     * create hidden pty process
     *
     * @return process id
     * @throws IOException IOException
     */
    public static PtyProcess createHiddenPtyProcess() throws IOException {
        String[] command = getDefaultShellCommand();
        String workingDir = System.getProperty("user.home");

        PtyProcessBuilder builder = new PtyProcessBuilder(command)
                .setEnvironment(System.getenv())
                .setDirectory(workingDir)
                .setConsole(false);

        return builder.start();
    }

    /**
     * get default shell command
     *
     * @return default shell command
     */
    private static String[] getDefaultShellCommand() {
        if (SystemInfo.isWindows) {
            return new String[]{CMD_PATH};
        } else if (SystemInfo.isMac) {
            return new String[]{BASH_PATH};
        } else {
            throw new UnsupportedOperationException("Unsupported systems");
        }
    }
}

