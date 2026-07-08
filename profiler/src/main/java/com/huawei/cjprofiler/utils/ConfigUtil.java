/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.cangjie.sdkconfig.support.SdkConfig;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;
import com.huawei.cangjie.sdkmanager.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * ConfigUtil
 *
 * @since 2026-04-20
 */
@Slf4j
public class ConfigUtil {
    /**
     * get CANGJIE_HOME path
     *
     * @return CANGJIE_HOME path
     */
    public static String getCangjieHomePath() {
        try {
            SdkConfig config = getSdkConfig();
            File dynamicLibFile = Path.of(config.getBuildToolsRootPath()).toFile();
            if (dynamicLibFile.exists()) {
                return dynamicLibFile.getCanonicalPath();
            }
        } catch (IOException e) {
            LOGGER.warn("Cangjie home directory is not exist");
        }
        return StringUtil.EMPTY;
    }

    /**
     * get sdk config
     *
     * @return sdk config
     */
    private static SdkConfig getSdkConfig() {
        int apiVersion = 20;
        boolean isHarmony = true;
        return new CangjieIdeaSdkInfoHandler().getLocalSdks(isHarmony, apiVersion)
                .get(CangjieComponentPath.CANGJIE.value()).getSdkConfig();
    }
}
