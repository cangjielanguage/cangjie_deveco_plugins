/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.parseprof;

import lombok.Getter;

import java.util.Optional;

/**
 * 该子记录存放一个对象数组
 *
 * @since 2024-10-18
 */
public enum SubRecordType {
    ROOT_UNKNOWN(255, 9),
    ROOT_GLOBAL(1, 9),
    ROOT_LOCAL(2, 17),
    CLASS_DUMP(32, 13),
    INSTANCE_DUMP(33, 21),
    OBJECT_ARRAY_DUMP(34, 21),
    PRIMITIVE_ARRAY_DUMP(35, 14),
    STRUCT_ARRAY_DUMP(36, 25),
    PINNED_INSTANCE(37, 21),
    LARGE_INSTANCE(38, 21),
    LARGE_OBJECT_ARRAY(39, 21),
    LARGE_PRIMITIVE_ARRAY(40, 14),
    LARGE_STRUCT_ARRAY(41, 25),
    UNMOVABLE_INSTANCE(42, 21),
    UNMOVABLE_OBJECT_ARRAY(43, 21),
    UNMOVABLE_PRIMITIVE_ARRAY(44, 14),
    UNMOVABLE_STRUCT_ARRAY_DUMP(45, 25);

    @Getter
    private final int type;

    @Getter
    private final int size;

    SubRecordType(int type, int size) {
        this.type = type;
        this.size = size;
    }

    /**
     * get PerfEventType enum by type
     *
     * @param type int type
     * @return PerfEventType enum
     */
    public static Optional<SubRecordType> fromType(int type) {
        for (SubRecordType subRecordType : values()) {
            if (subRecordType.getType() == type) {
                return Optional.of(subRecordType);
            }
        }
        return Optional.empty();
    }
}
