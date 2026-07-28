/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.vo;

import java.util.List;

/**
 * The type Toml update type data.
 *
 * @since 2025 -02-08
 */
public class TomlUpdateTypeData {
    private List<String> tomlKey;

    private OperationEnum operation;

    private Object removeValue;

    private Object updateValue;

    /**
     * Instantiates a new Toml update type data.
     *
     * @param tomlKey the toml key
     * @param operation the operation
     * @param removeValue the remove value
     * @param updateValue the add value
     */
    public TomlUpdateTypeData(List<String> tomlKey, OperationEnum operation, Object removeValue, Object updateValue) {
        this.tomlKey = tomlKey;
        this.operation = operation;
        this.removeValue = removeValue;
        this.updateValue = updateValue;
    }

    /**
     * Gets toml key.
     *
     * @return the toml key
     */
    public List<String> getTomlKey() {
        return tomlKey;
    }

    /**
     * Sets toml key.
     *
     * @param tomlKey the toml key
     */
    public void setTomlKey(List<String> tomlKey) {
        this.tomlKey = tomlKey;
    }

    /**
     * Gets operation.
     *
     * @return the operation
     */
    public OperationEnum getOperation() {
        return operation;
    }

    /**
     * Sets operation.
     *
     * @param operation the operation
     */
    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    /**
     * Gets remove value.
     *
     * @return the remove value
     */
    public Object getRemoveValue() {
        return removeValue;
    }

    /**
     * Sets remove value.
     *
     * @param removeValue the remove value
     */
    public void setRemoveValue(Object removeValue) {
        this.removeValue = removeValue;
    }

    /**
     * Gets add value.
     *
     * @return the add value
     */
    public Object getUpdateValue() {
        return updateValue;
    }

    /**
     * Sets add value.
     *
     * @param updateValue the add value
     */
    public void setUpdateValue(Object updateValue) {
        this.updateValue = updateValue;
    }

    /**
     * The enum Operation enum.
     */
    public static enum OperationEnum {
        /**
         * Add operation enum.
         */
        ADD,

        /**
         * Replace operation enum.
         */
        REPLACE,

        /**
         * FORCED_REPLACE
         * If the key does not exist, the key is added and the value is updated.
         */
        FORCED_REPLACE,

        /**
         * Override operation enum.
         * If the key does not exist, the update is ignored.
         */
        OVERRIDE;
    }
}
