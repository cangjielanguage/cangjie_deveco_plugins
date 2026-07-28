/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * log format util
 *
 * @since 2022 -12-03
 */
public class MessageFormatUtil {
    /**
     * Remove line separator.
     *
     * @param message the message
     * @return the string
     */
    public static String removeCrlf(String message) {
        if (StringUtils.isEmpty(message)) {
            return "";
        }
        return message.replaceAll("[\r\n]", "");
    }
}
