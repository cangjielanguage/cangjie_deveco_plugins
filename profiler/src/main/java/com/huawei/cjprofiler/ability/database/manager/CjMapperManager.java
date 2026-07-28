/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.database.manager;

import com.huawei.cjprofiler.dao.mapper.CjThreadMapper;
import com.huawei.cjprofiler.dao.mapper.CjVmProfilerMapper;
import com.huawei.deveco.insight.ohos.common.enums.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL Mapper Manager
 *
 * @since 2023-03-13
 */
public class CjMapperManager {
    /**
     * Singleton
     */
    private static volatile CjMapperManager cjMapperManager = null;

    private static final Map<Class<?>, DatabaseType> MAPPER_MAPPING_DATABASE_MAP = new ConcurrentHashMap<>() {{
        put(CjThreadMapper.class, DatabaseType.TRACE);
        put(CjVmProfilerMapper.class, DatabaseType.TIME_SNAPSHOT);
    }};

    private CjMapperManager() {}

    /**
     * get MapperManager instance
     *
     * @return MapperManager instance
     */
    public static CjMapperManager getInstance() {
        if (cjMapperManager == null) {
            synchronized (CjMapperManager.class) {
                if (cjMapperManager == null) {
                    cjMapperManager = new CjMapperManager();
                }
            }
        }
        return cjMapperManager;
    }

    /**
     * 根据mapper获取对应操作数据库类型
     *
     * @param mapperClass Class<?>
     * @return DatabaseType
     */
    public DatabaseType getDatabaseTypeByMapper(Class<?> mapperClass) {
        return MAPPER_MAPPING_DATABASE_MAP.get(mapperClass);
    }
}