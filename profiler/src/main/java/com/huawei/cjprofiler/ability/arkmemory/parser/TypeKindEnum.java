/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.parser;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * cangjie snapshot
 *
 * @since 2025-03-01
 */
public enum TypeKindEnum {
    // reference type
    TYPE_KIND_CLASS(-128, "class"),
    TYPE_KIND_INTERFACE(-127, "interface"),
    TYPE_KIND_RAWARRAY(-126, "rawarray"),
    TYPE_KIND_FUNC(-125, "func"),
    TYPE_KIND_TEMP_ENUM(-124, "temp_enum"),
    TYPE_KIND_WEAKREF_CLASS(-123, "weakref_class"),
    TYPE_KIND_GENERIC_TI(-1, "generic_ti"),
    TYPE_KIND_GENERIC_CUSTOM(-2, "generic_custom"),
    // value type
    TYPE_KIND_NOTHING(0, "nothing"),
    TYPE_KIND_UNIT(1, "unit"),
    TYPE_KIND_BOOL(2, "bool"),
    TYPE_KIND_RUNE(3, "rune"),
    TYPE_KIND_UINT8(4, "uint8"),
    TYPE_KIND_UINT16(5, "uint16"),
    TYPE_KIND_UINT32(6, "uint32"),
    TYPE_KIND_UINT64(7, "uint64"),
    TYPE_KIND_UINT_NATIVE(8, "uint_native"),
    TYPE_KIND_INT8(9, "int8"),
    TYPE_KIND_INT16(10, "int16"),
    TYPE_KIND_INT32(11, "int32"),
    TYPE_KIND_INT64(12, "int64"),
    TYPE_KIND_INT_NATIVE(13, "int_native"),
    TYPE_KIND_FLOAT16(14, "float16"),
    TYPE_KIND_FLOAT32(15, "float32"),
    TYPE_KIND_FLOAT64(16, "float64"),
    TYPE_KIND_CSTRING(17, "cstring"),
    TYPE_KIND_CPOINTER(18, "cpointer"),
    TYPE_KIND_CFUNC(19, "cfunc"),
    TYPE_KIND_VARRAY(20, "varray"),
    TYPE_KIND_TUPLE(21, "tuple"),
    TYPE_KIND_STRUCT(22, "struct"),
    TYPE_KIND_ENUM(23, "enum"),
    TYPE_KIND_MAX(24, "max");

    @Getter
    private final int value;

    @Getter
    private final String typeName;

    private static final Map<Integer, TypeKindEnum> VALUE_MAP = new HashMap<>();

    static {
        for (TypeKindEnum enumItem : TypeKindEnum.values()) {
            VALUE_MAP.put(enumItem.getValue(), enumItem);
        }
    }

    TypeKindEnum(int value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    /**
     * 根据给定的值获取对应的TypeKindEnum枚举项。
     *
     * @param value 需要查找的枚举值
     * @return 对应的TypeKindEnum枚举项，如果找不到则返回null
     */
    public static TypeKindEnum getByValue(Integer value) {
        return VALUE_MAP.getOrDefault(value, TYPE_KIND_NOTHING);
    }

    /**
     * 根据给定的值获取对应的typeName。
     *
     * @param value 需要查找的枚举值
     * @return 对应的typeName，如果找不到则返回null
     */
    public static String getTypeNameByValue(Integer value) {
        TypeKindEnum enumItem = getByValue(value);
        return enumItem.getTypeName();
    }
}
