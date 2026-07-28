/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.render;

import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.VIEW_NAME;

import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * RenderUtil
 *
 * @since 2024/09/12
 */
public class RenderUtil {
    /**
     * set view name
     *
     * @param renderParameterMap renderParameterMap
     */
    public static void setViewName(@NotNull RenderHashMap renderParameterMap) {
        String moduleName = renderParameterMap.getString(MODULE_NAME);
        if (StringUtil.isEmpty(moduleName)) {
            renderParameterMap.putIfAbsent(VIEW_NAME, "MyView");
        } else {
            String viewName = moduleName.substring(0, 1).toUpperCase(Locale.ROOT)
                    + moduleName.substring(1) + "View";
            renderParameterMap.putIfAbsent(VIEW_NAME, viewName);
        }
    }
}
