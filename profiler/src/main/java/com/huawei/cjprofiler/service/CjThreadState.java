/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CjThreadState
 *
 * @since 2025/04/22
 */
@Getter
public enum CjThreadState {
    READY("Ready", "New READY"),
    PENDING("Pending", "Park PENDING"),
    RUNNING("Running", "Execute RUNNING"),
    IDLE("Idle", "Exit IDLE"),
    TOTAL("Total", "");

    private final String key;

    private final String value;

    CjThreadState(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private static final Map<String, String> valueToKeyMap = new HashMap<>();

    static {
        for (CjThreadState state : values()) {
            valueToKeyMap.put(state.getValue(), state.getKey());
        }
    }

    /**
     * getKeyByValue
     *
     * @param value value
     * @return key
     */
    public static String getKeyByValue(String value) {
        return valueToKeyMap.getOrDefault(value, StringUtils.EMPTY);
    }

    /**
     * getValueByKey
     *
     * @param key key
     * @return value
     */
    public static String getValueByKey(String key) {
        for (CjThreadState e : CjThreadState.values()) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return TOTAL.value;
    }

    /**
     * getStateList
     *
     * @return all state as a list
     */
    public static List<CjThreadState> getStateList() {
        return List.of(READY, PENDING, RUNNING, IDLE);
    }
}
