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
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.NO;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.KEY_ACTION;

import com.huawei.cangjie.projectmgmt.config.ConfigPathHandler;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.cangjie.projectmgmt.utils.CangjieTemplateUtils;
import com.huawei.deveco.projectmgmt.ohos.template.Template;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.render.init.InitLibraryTemplate;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * CangjieInitLibraryTemplate
 *
 * @since 2024/03/13
 */
public class CangjieInitLibraryTemplate extends InitLibraryTemplate {
    @Override
    public void initRenderTemplate(@NotNull List<Template> templates, @NotNull RenderHashMap renderParameterMap) {
        if (!renderParameterMap.containsKey(ENABLE_CANGJIE_ACTION)) {
            TraceUtils.traceByTemplateName(MODULE, renderParameterMap.getString(KEY_TEMPLATE_NAME));
        }
        super.initRenderTemplate(templates, renderParameterMap);
        renderParameterMap.put(DEVECO_CANGJIE_HOME, "${DEVECO_CANGJIE_HOME}");
        renderParameterMap.put("shouldCreateOhosTest", true);
        renderParameterMap.putIfAbsent(ENABLE_CANGJIE_ACTION, NO);
        renderParameterMap.putIfAbsent(CANGJIE_PACKAGE_NAME,
            "ohos_app_cangjie_" + renderParameterMap.getString(MODULE_NAME));
        ConfigPathHandler.replaceCjPackageName(renderParameterMap);
        setViewName(renderParameterMap);
        CangjieTemplateUtils.setTemplateParams(renderParameterMap);
    }

    @Override
    public boolean shouldRender(RenderHashMap renderParameterMap) {
        return getInitTemplateKey().equals(renderParameterMap.getString(KEY_ACTION))
                && renderParameterMap.getString(KEY_TEMPLATE_NAME).toLowerCase().contains("cangjie");
    }
}
