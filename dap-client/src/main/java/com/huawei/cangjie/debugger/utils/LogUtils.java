/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.intellij.openapi.diagnostic.Logger;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;

/**
 * Utils for printing log and console output
 *
 * @since 2023-4-14
 */
public class LogUtils {
    /**
     * Log header of C++ debugger log
     */
    public static final String CANGJIE_DEBUGGER_LOG_HEADER = "Cangjie Debug: ";

    /**
     * Print Log of information level
     *
     * @param logger  logger
     * @param message message
     */
    public static void printCangjieLogInfo(Logger logger, String message) {
        logger.info(CANGJIE_DEBUGGER_LOG_HEADER + message);
    }

    /**
     * Print Log of warning level
     *
     * @param logger  logger
     * @param message message
     */
    public static void printCangjieLogWarn(Logger logger, String message) {
        logger.warn(CANGJIE_DEBUGGER_LOG_HEADER + message);
    }

    /**
     * Print Log of warning level
     *
     * @param logger  logger
     * @param message message
     * @param t Throwable
     */
    public static void printCangjieLogWarn(Logger logger, String message, @Nullable Throwable t) {
        logger.warn(CANGJIE_DEBUGGER_LOG_HEADER + message, t);
    }

    /**
     * Print Log of error level
     *
     * @param logger  logger
     * @param message message
     */
    public static void printCangjieLogError(Logger logger, String message) {
        logger.error(CANGJIE_DEBUGGER_LOG_HEADER + message);
    }

    /**
     * Generate time stamp message of console
     *
     * @param message message
     * @return result
     */
    public static String generateTimeStampForConsole(String message) {
        return new SimpleDateFormat("MM/dd HH:mm:ss:").format(System.currentTimeMillis())
                + " " + message;
    }
}
