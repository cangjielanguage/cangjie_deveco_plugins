/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service;

import com.huawei.deveco.common.country.setting.CountryRegionSetting;
import com.huawei.cjprofiler.common.constant.CangJieConstant;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

/**
 * GlobalInfoService
 *
 * @since 2025-04-07
 */
public class GlobalInfoService {
    private static volatile GlobalInfoService globalInfoService = null;

    /**
     * getInstance
     *
     * @return GlobalInfoService
     */
    public static GlobalInfoService getInstance() {
        if (globalInfoService == null) {
            synchronized (GlobalInfoService.class) {
                if (globalInfoService == null) {
                    globalInfoService = new GlobalInfoService();
                }
            }
        }
        return globalInfoService;
    }

    /**
     * getCountryCode
     *
     * @return String
     */
    public String getCountryCode() {
        return CountryRegionSetting.getInstance().getUserCountryRegion();
    }

    /**
     * hasCJPluginApplied
     *
     * @return boolean
     */
    public boolean hasCJPluginApplied() {
        return PluginManager.getInstance().findEnabledPlugin(PluginId.getId(CangJieConstant.CJ_PLUGIN_ID)) != null;
    }
}