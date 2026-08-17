/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.po.cjprof;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.LocalObject;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * StackFrame
 *
 * @since 2026-04-14
 */
@Builder
@Data
public class StackFrame {
    /**
     * id
     */
    private int id;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 行号
     */
    private int lineNumber;

    /**
     * 局部对象列表
     */
    private List<LocalObject> localObjectList;
}
