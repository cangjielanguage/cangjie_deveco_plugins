/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.cjprofiler.service.CjRecordManager;
import com.huawei.cjprofiler.service.CjRecordService;
import com.huawei.cjprofiler.service.file.CjImportService;
import com.huawei.deveco.insight.ohos.common.enums.UnitKey;
import com.huawei.deveco.insight.ohos.model.dto.request.ImportDataDto;
import com.huawei.deveco.insight.ohos.model.dto.request.ark.ArkConnectRequest;
import com.huawei.deveco.insight.ohos.utils.CangjieExtPoint;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * UseCangjieExtImpl
 *
 * @since 2025-11
 */
@Slf4j
public class UseCangjieExtImpl implements CangjieExtPoint {
    /**
     * createCjConnect 拓展点调用
     *
     * @param request request
     */
    public void createCjConnect(ArkConnectRequest request) {
        LOGGER.info("createCjConnect start");
        CjRecordManager.getInstance().creatCjRecordService(request.getPid());
        Optional<CjRecordService> service = CjRecordManager.getInstance().getCjRecordService(request.getPid());
        service.ifPresent(CjRecordService::initFlag);
    }

    /**
     * releaseAllMemoryResources 拓展点调用
     */
    public void releaseAllMemoryResources() {
        LOGGER.info("releaseAllMemoryResources start");
        CjRecordManager.getInstance().releaseAllMemoryResources();
    }

    /**
     * importCjHeapSnapshot 拓展点调用
     *
     * @param importDataDto ImportDataParams
     * @param unitKey UnitKey
     * @return result boolean
     */
    public boolean importCjHeapSnapshot(ImportDataDto importDataDto, UnitKey unitKey) {
        LOGGER.info("importCjHeapSnapshot start");
        return CjImportService.importCjHeapSnapshot(importDataDto, unitKey);
    }

    /**
     * importCjHeapTimeLine 拓展点调用
     *
     * @param importDataDto ImportDataParams
     * @param unitKey UnitKey
     * @return result boolean
     */
    public boolean importCjHeapTimeLine(ImportDataDto importDataDto, UnitKey unitKey) {
        LOGGER.info("importCjHeapTimeLine start");
        return CjImportService.importCjHeapTimeLine(importDataDto, unitKey);
    }
}
