/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common;

/**
 * 仓颉调优异常类
 *
 * @since 2025-3
 */
public class CjProfilerException extends RuntimeException {
    /**
     * 仓颉调优异常类构造器，将信息传给父类构造器
     *
     * @param message 异常信息
     */
    public CjProfilerException(String message) {
        super(message);
    }
}
