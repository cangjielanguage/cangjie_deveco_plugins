/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.render;

import static com.huawei.cangjie.projectmgmt.render.RenderUtil.setViewName;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_PACKAGE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.DEVECO_CANGJIE_HOME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.ENABLE_CANGJIE_ACTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.HW_SDK_DIR;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.NO;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PROJECT;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.KEY_ACTION;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;

import com.huawei.cangjie.projectmgmt.config.ConfigPathHandler;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.cangjie.projectmgmt.utils.CangjieTemplateUtils;
import com.huawei.deveco.projectmgmt.ohos.template.Template;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.render.init.InitProjectTemplate;
import com.huawei.deveco.sdkmanager.hos.idea.util.SdkUtil;

import com.intellij.openapi.util.io.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * CangjieInitProjectTemplate
 *
 * @since 2024/03/13
 */
public class CangjieInitProjectTemplate extends InitProjectTemplate {
    @Override
    public void initRenderTemplate(@NotNull List<Template> list, @NotNull RenderHashMap renderHashMap) {
        TraceUtils.traceByTemplateName(PROJECT, renderHashMap.getString(KEY_TEMPLATE_NAME));
        renderHashMap.put(HW_SDK_DIR, FileUtil.toSystemIndependentName(SdkUtil.getSdkLocation()));
        renderHashMap.put(DEVECO_CANGJIE_HOME, "${DEVECO_CANGJIE_HOME}");
        renderHashMap.putIfAbsent(ENABLE_CANGJIE_ACTION, NO);
        renderHashMap.putIfAbsent(CANGJIE_PACKAGE_NAME, "ohos_app_cangjie_" + renderHashMap.getString(MODULE_NAME));
        ConfigPathHandler.replaceCjPackageName(renderHashMap);
        super.initRenderTemplate(list, renderHashMap);
        setViewName(renderHashMap);
        CangjieTemplateUtils.setTemplateParams(renderHashMap);
    }

    @Override
    public boolean shouldRender(RenderHashMap renderParameterMap) {
        return getInitTemplateKey().equals(renderParameterMap.getString(KEY_ACTION)) && renderParameterMap.getString(
            KEY_TEMPLATE_NAME).toLowerCase().contains("cangjie");
    }
}
