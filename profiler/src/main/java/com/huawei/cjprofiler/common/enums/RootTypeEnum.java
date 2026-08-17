/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * RootTypeEnum
 *
 * @since 2026-04-14
 */
@Getter
public enum RootTypeEnum {
    /**
     * 全局根节点
     */
    GLOBAL((byte) 1, "global"),

    /**
     * 局部根节点
     */
    LOCAL((byte) 2, "local"),

    /**
     * 终结器根节点
     */
    UNKNOWN((byte) 3, "unknown"),

    /**
     * 非根节点
     */
    NOT_ROOT((byte) 0, "-");

    /**
     * RootTypeNum
     */
    public static final int ROOT_TYPE_NUM = 4;

    private final byte key;

    private final String value;

    // 1. 建立静态 Map 用于缓存映射关系
    private static final Map<String, Byte> VALUE_TO_KEY_MAP = new HashMap<>();

    // 2. 在静态块中初始化 Map
    static {
        for (RootTypeEnum type : RootTypeEnum.values()) {
            VALUE_TO_KEY_MAP.put(type.value, type.key);
        }
    }

    RootTypeEnum(byte key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * getKeyByValue
     *
     * @param value value
     * @return Byte
     */
    public static Byte getKeyByValue(String value) {
        return VALUE_TO_KEY_MAP.get(value);
    }

    /**
     * getValueByKey
     *
     * @param key key
     * @return value
     */
    public static String getValueByKey(byte key) {
        for (RootTypeEnum e : RootTypeEnum.values()) {
            if (e.key == key) {
                return e.value;
            }
        }
        return NOT_ROOT.value;
    }
}