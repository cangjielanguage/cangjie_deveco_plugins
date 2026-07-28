/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.dao;

import com.huawei.deveco.insight.ohos.ability.database.TraceDatabase;
import com.huawei.deveco.insight.ohos.ability.database.manager.DatabaseManager;
import com.huawei.cjprofiler.ability.database.manager.CjMapperManager;
import com.huawei.deveco.insight.ohos.common.ProfilerError;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.common.enums.DatabaseType;

import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

/**
 * AbstractDataStore
 *
 * @since 2025/04/22
 */
public abstract class AbstractDao {
    /**
     * relate to mapper
     *
     * @return <T> mapper class
     */
    abstract Class<?> relatedMapper();

    /**
     * get absolute offset by session id
     *
     * @param sessionId frontend session id
     * @return trace absolute offset
     */
    protected Long getAbsoluteOffsetBySessionId(String sessionId) {
        return TraceDatabase.getInstance().getAbsoluteOffsetBySessionId(sessionId);
    }

    /**
     * Get sqlSession by session id
     *
     * @param sessionId frontend session id
     * @return sqlSession
     */
    protected SqlSession obtainSqlSession(String sessionId) {
        DatabaseType databaseType = CjMapperManager.getInstance().getDatabaseTypeByMapper(relatedMapper());
        if (databaseType == null) {
            throw new ProfilerException(ProfilerError.SQL_ERROR,
                String.format("Failed to query database, %s has not been registered", relatedMapper()));
        }
        Optional<SqlSession> sqlSessionOptional = DatabaseManager.getInstance()
            .getSqlSession(sessionId, databaseType);
        if (sqlSessionOptional.isEmpty()) {
            throw new ProfilerException(ProfilerError.SQL_ERROR, "Failed to query database, SqlSession is null");
        }
        return sqlSessionOptional.get();
    }

    /**
     * cangjie mappers register
     *
     * @param sessionId frontend session id
     * @return true for success, false for failure
     */
    public boolean registerMapper(String sessionId) {
        DatabaseType databaseType = CjMapperManager.getInstance().getDatabaseTypeByMapper(relatedMapper());
        if (databaseType == null) {
            throw new ProfilerException(ProfilerError.SQL_ERROR,
                String.format("Failed to query database, %s has not been registered", relatedMapper()));
        }
        return DatabaseManager.getInstance().addMapper(sessionId, databaseType, relatedMapper());
    }
}
