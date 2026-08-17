/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.huawei.deveco.hdclib.ohos.client.Client;

import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.TimeoutException;

/**
 * Hdc utils
 *
 * @since 2024-6-5
 */
public class HdcUtils {
    private static final int TIMEOUT = 30000;

    private static final Logger LOGGER = Logger.getInstance(HdcUtils.class);

    /**
     * send hdc shell command
     *
     * @param command command
     * @param client  client
     */
    public static void sendSyncShellCommand(String command, Client client) {
        try {
            String result = client.sendSyncShellCommand(command, TIMEOUT);
            LogUtils.printCangjieLogInfo(LOGGER,
                    String.format("sendSyncShellCommand,command:%s; result:%s", command, result));
        } catch (TimeoutException e) {
            String errorMessage = String.format("sendSyncShellCommand error,command:%s; errorMessage:%s",
                    command, e.getMessage());
            LogUtils.printCangjieLogWarn(LOGGER, errorMessage);
        }
    }

    /**
     * send hdc command
     *
     * @param command client
     * @param client  client
     */
    public static void sendSyncCommand(String command, Client client) {
        try {
            String result = client.sendSyncCommand(command, TIMEOUT);
            LogUtils.printCangjieLogInfo(LOGGER,
                    String.format("sendSyncCommand,command:%s; result:%s", command, result));
        } catch (TimeoutException e) {
            String errorMessage = String.format("sendSyncCommand error,command:%s; errorMessage:%s",
                    command, e.getMessage());
            LogUtils.printCangjieLogWarn(LOGGER, errorMessage);
        }
    }
}
