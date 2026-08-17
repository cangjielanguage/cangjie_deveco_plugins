/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.domain;

import java.util.Optional;

/**
 * Progress
 *
 * @since 2025-02-25
 */
public interface Progress {
    /**
     * setText
     *
     * @param text text
     */
    void setText(String text);

    /**
     * setSubText
     *
     * @param text text
     */
    void setSubText(String text);

    /**
     * setFraction
     *
     * @param fraction fraction
     */
    void setFraction(double fraction);

    /**
     * getFraction
     *
     * @return Fraction
     */
    double getFraction();

    /**
     * cancel progress
     */
    void cancel();

    /**
     * 检查progress是否被cancel
     *
     * @return true if cancelled, otherwise false
     */
    boolean isCancelled();

    /**
     * debug
     *
     * @param msg debug msg
     */
    void debug(String msg);

    /**
     * debug
     *
     * @param msg msg
     * @param throwable throwable
     */
    default void debug(String msg, Throwable throwable) {
        debug(msg);
        Optional.ofNullable(throwable).ifPresent(thr -> debug(thr.toString()));
    }

    /**
     * info
     *
     * @param msg msg
     */
    void info(String msg);

    /**
     * info
     *
     * @param msg msg
     * @param throwable throwable
     */
    default void info(String msg, Throwable throwable) {
        info(msg);
        Optional.ofNullable(throwable).ifPresent(thr -> info(thr.toString()));
    }

    /**
     * warn
     *
     * @param msg msg
     */
    void warn(String msg);

    /**
     * warn
     *
     * @param msg msg
     * @param throwable throwable
     */
    default void warn(String msg, Throwable throwable) {
        warn(msg);
        Optional.ofNullable(throwable).ifPresent(thr -> warn(thr.toString()));
    }

    /**
     * error
     *
     * @param msg error msg
     */
    void error(String msg);

    /**
     * error
     *
     * @param msg msg
     * @param throwable throwable
     */
    default void error(String msg, Throwable throwable) {
        error(msg);
        Optional.ofNullable(throwable).ifPresent(thr -> error(thr.toString()));
    }
}
