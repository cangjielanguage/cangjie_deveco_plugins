/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.extend.toml;

import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Toml
 *
 * @since 2024/03/11
 */
public class Toml {
    private Map<String, Object> values;

    private final Toml defaults;

    private final CJPMTomlDecoder decoder = new CJPMTomlDecoder();

    private String rawContent;

    public Toml() {
        this(null);
    }

    public Toml(Toml defaults) {
        this(defaults, new HashMap<>());
    }

    private Toml(Toml defaults, Map<String, Object> values) {
        this.values = values;
        this.defaults = defaults;
    }

    /**
     * read toml file
     *
     * @param file target file
     * @return toml result
     */
    public Optional<Toml> read(File file) {
        if (file == null || !file.exists()) {
            return Optional.empty();
        }
        String tomlContent = FileUtils.readToString(file);
        this.rawContent = tomlContent;
        Optional<HashMap<String, Object>> result = decoder.decode(tomlContent);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        this.values = result.get();
        return Optional.of(this);
    }

    /**
     * Gets string
     *
     * @param key key
     * @return get string
     */
    public String getString(String key) {
        Object value = get(key);
        if (value instanceof String strValue) {
            return strValue;
        }
        return StringUtil.EMPTY;
    }

    /**
     * Gets long.
     *
     * @param key key
     * @return the long
     */
    public Long getLong(String key) {
        Object value = get(key);
        if (value instanceof Long longValue) {
            return longValue;
        }
        return Long.getLong("0");
    }

    /**
     * Gets list.
     *
     * @param <T> type of list items
     * @param key key
     * @return List<T>
     */
    public <T> List<T> getList(String key) {
        return (List<T>) get(key);
    }

    /**
     * Gets boolean.
     *
     * @param key key
     * @return the boolean
     */
    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        return Boolean.FALSE;
    }

    /**
     * Gets table.
     *
     * @param key A table name, not including square brackets.
     * @return Toml
     */
    @SuppressWarnings("unchecked")
    public Optional<Toml> getTable(String key) {
        Map<String, Object> map = null;
        Object value = get(key);
        if (value instanceof Map mapValue) {
            map = mapValue;
        }
        return map == null ? Optional.empty() : Optional.of(new Toml(null, map));
    }

    /**
     * Gets tables.
     *
     * @param key Name of array of tables, not including square brackets.
     * @return List<Toml>
     */
    @SuppressWarnings("unchecked")
    public List<Toml> getTables(String key) {
        Object value = get(key);
        List<Map<String, Object>> tableArray = (List<Map<String, Object>>) value;

        if (tableArray == null) {
            return Collections.emptyList();
        }

        ArrayList<Toml> tables = new ArrayList<>();
        tableArray.forEach(table -> tables.add(new Toml(null, table)));
        return tables;
    }

    /**
     * Check is containing target key.
     *
     * @param key key
     * @return target key is present
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Is empty
     *
     * @return is empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * convert values to map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        HashMap<String, Object> valuesCopy = new HashMap<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            valuesCopy.put(entry.getKey(), deepCopy(entry.getValue()));
        }

        if (defaults != null) {
            for (Map.Entry<String, Object> entry : defaults.values.entrySet()) {
                if (!valuesCopy.containsKey(entry.getKey())) {
                    valuesCopy.put(entry.getKey(), deepCopy(entry.getValue()));
                }
            }
        }

        return valuesCopy;
    }

    @SuppressWarnings("unchecked")
    private Object deepCopy(Object value) {
        if (value == null) {
            return value;
        }

        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        if (value instanceof Map) {
            Map<String, Object> original = (Map<String, Object>) value;
            Map<String, Object> copy = new HashMap<>();
            for (Map.Entry<String, Object> e : original.entrySet()) {
                copy.put(e.getKey(), deepCopy(e.getValue()));
            }
            return copy;
        }

        if (value instanceof List) {
            List<Object> original = (List<Object>) value;
            List<Object> copy = new ArrayList<>();
            for (Object item : original) {
                copy.add(deepCopy(item));
            }
            return copy;
        }

        return value;
    }

    /**
     * get decoder
     *
     * @return decoder
     */
    protected CJPMTomlDecoder getDecoder() {
        return decoder;
    }

    /**
     * get raw toml content
     *
     * @return raw content
     */
    public String getRawContent() {
        return rawContent;
    }

    private Object get(String key) {
        return values.get(key);
    }
}
