/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service.file;

import com.huawei.cjprofiler.service.CjMemoryService;
import com.huawei.cjprofiler.service.CjRecordManager;
import com.huawei.deveco.insight.ohos.ability.arkcpu.parser.JSTraceParser;
import com.huawei.deveco.insight.ohos.ability.arkmemory.entity.RawHeapSnapshot;
import com.huawei.deveco.insight.ohos.ability.database.manager.DatabaseManager;
import com.huawei.deveco.insight.ohos.common.enums.DatabaseType;
import com.huawei.deveco.insight.ohos.common.enums.UnitKey;
import com.huawei.deveco.insight.ohos.model.dto.DataStructure;
import com.huawei.deveco.insight.ohos.model.dto.request.ImportDataDto;
import com.huawei.deveco.insight.ohos.service.file.JsTraceFileService;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;

/**
 * CjImportService
 *
 * @since 2025-07-01
 */
public class CjImportService {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjImportService.class);

    /**
     * importCjHeapTimeLine 拓展点调用
     *
     * @param importDataDto ImportDataParams
     * @param unitKey UnitKey
     * @return result boolean
     */
    public static boolean importCjHeapTimeLine(ImportDataDto importDataDto, UnitKey unitKey) {
        String sessionId = importDataDto.getSessionId();
        CjMemoryService service = CjRecordManager.getInstance().creatCjMemoryService(sessionId);
        for (DataStructure dataStructure : importDataDto.getDataStructure()) {
            if (dataStructure == null || dataStructure.getFile() == null) {
                LOGGER.warn("Don't need to import {}, file information is empty", unitKey.getType());
                return true;
            }
            String filePath = dataStructure.getFile().getPath();
            if (!filePath.endsWith(".cjheaptimeline")) {
                continue;
            }
            RawHeapSnapshot rawHeapSnapshot = service.handlerTraceFile(filePath);
            if (!saveTraceFile(filePath, rawHeapSnapshot, sessionId, unitKey)) {
                return false;
            }
            // Revert the recording start time during import.
            int tid = JSTraceParser.getTidFromProfilePath(filePath);
            tid = tid <= 0 ? importDataDto.getPid() : tid;
            service.setStartRecordTimeMap(tid, rawHeapSnapshot.getStartTime());
            service.parseArkHeapTimeline(sessionId, tid, rawHeapSnapshot, false);
        }
        CjRecordManager.getInstance().getCjMemoryService(sessionId).calculateTotalSize();
        return true;
    }

    /**
     * importCjHeapSnapshot 拓展点调用
     *
     * @param importDataDto ImportDataParams
     * @param unitKey UnitKey
     * @return result boolean
     */
    public static boolean importCjHeapSnapshot(ImportDataDto importDataDto, UnitKey unitKey) {
        String sessionId = importDataDto.getSessionId();
        CjMemoryService service = CjRecordManager.getInstance().creatCjMemoryService(sessionId);
        for (DataStructure dataStructure : importDataDto.getDataStructure()) {
            if (dataStructure == null) {
                LOGGER.warn("Don't need to import {}, dataStructure is empty", unitKey.getType());
                return true;
            }
            if (dataStructure.getFile() != null) {
                String filePath = dataStructure.getFile().getPath();
                RawHeapSnapshot rawHeapSnapshot = service.handlerTraceFile(filePath);
                if (!saveTraceFile(filePath, rawHeapSnapshot, sessionId, unitKey)) {
                    return false;
                }
                service.saveHeapSnapshotToRootMap(rawHeapSnapshot);
            } else if (dataStructure.getDatabase() != null) {
                if (!dealDbData(dataStructure, sessionId, DatabaseType.MEMORY)) {
                    return false;
                }
            } else {
                LOGGER.warn("The imported information is incorrect.");
            }
        }
        return true;
    }

    private static boolean saveTraceFile(String filePath, RawHeapSnapshot rawHeapSnapshot, String sessionId,
        UnitKey unitKey) {
        if (rawHeapSnapshot == null) {
            LOGGER.warn("Failed to handler trace file.");
            return false;
        }
        JsTraceFileService.getInstance()
            .saveTraceFile(sessionId, unitKey.getType(), filePath, rawHeapSnapshot.getStartTime());
        return true;
    }

    private static boolean dealDbData(DataStructure dataStructure, String sessionId, DatabaseType databaseType) {
        if (dataStructure == null || dataStructure.getDatabase() == null) {
            LOGGER.warn("Don't need to import {}, database information is empty", databaseType);
            return true;
        }
        String dbPath = dataStructure.getDatabase().getPath();
        DatabaseManager.getInstance().connectDatabase(sessionId, dbPath, databaseType);
        return true;
    }
}
