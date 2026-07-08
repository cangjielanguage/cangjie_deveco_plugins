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
import static com.huawei.cangjie.projectmgmt.utils.Constants.EVENT_ENABLE_CANGJIE_HAR;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_PROVIDER_NAME;

import com.huawei.cangjie.projectmgmt.config.ConfigPathHandler;
import com.huawei.cangjie.projectmgmt.utils.CangjieTemplateUtils;
import com.huawei.deveco.projectmgmt.ohos.template.Template;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.render.init.InitTemplate;
import com.huawei.deveco.projectmgmt.ohos.template.utils.Category;
import com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateUtil;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * CangjieEnableHarTemplate
 *
 * @since 2025/03/19
 */
public class CangjieEnableHarTemplate implements InitTemplate {
    @Override
    @NotNull
    public String getInitTemplateKey() {
        return EVENT_ENABLE_CANGJIE_HAR;
    }

    @Override
    public void initRenderTemplate(@NotNull List<Template> templates, @NotNull RenderHashMap renderHashMap) {
        Template template = TemplateUtil.createTemplate(renderHashMap.getString(KEY_TEMPLATE_PROVIDER_NAME),
                Category.OTHER.value(), renderHashMap.getString(KEY_TEMPLATE_NAME));
        templates.add(template);
        renderHashMap.putIfAbsent(CANGJIE_PACKAGE_NAME,
                "ohos_app_cangjie_" + renderHashMap.getString(MODULE_NAME));
        ConfigPathHandler.replaceCjPackageName(renderHashMap);
        setViewName(renderHashMap);
        CangjieTemplateUtils.setTemplateParams(renderHashMap);
    }

    @Override
    public SyncRequest getSyncRequest() {
        return SyncRequest.ADD_ABILITY;
    }
}
