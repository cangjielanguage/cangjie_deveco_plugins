/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.cjprof.jni.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 堆内存快照信息类
 *
 * @since 2026-04-14
 */
@SuperBuilder
@Data
@AllArgsConstructor
public class HeapSnapshot {
    private long id;

    private long fileSize;

    private String filePath;
}
