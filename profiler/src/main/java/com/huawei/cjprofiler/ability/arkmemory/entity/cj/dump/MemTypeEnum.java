/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump;

import lombok.Getter;

/**
 * 类型定义
 *
 * @since 2025-02-10
 */
@Getter
public enum MemTypeEnum {
    /**
     * class/struct
     */
    CLASS_OR_STRUCT(2, 0),

    /**
     * 	Bool/Int8/UInt8
     */
    BOOL(4, 1),

    /**
     * Int16/UInt16
     */
    INT16(9, 2),

    /**
     * Int32/UInt32/Float32
     */
    INT32(10, 4),

    /**
     * Int64/UInt64/Float64
     */
    FLOAT64(11, 8);

    private final int type;

    private final int size;

    MemTypeEnum(int type, int size) {
        this.type = type;
        this.size = size;
    }

    /**
     * get PerfEventType enum by type
     *
     * @param type int type
     * @return PerfEventType enum
     */
    public static int getSize(int type) {
        for (MemTypeEnum curType : values()) {
            if (curType.getType() == type) {
                return curType.getSize();
            }
        }
        return 0;
    }
}
