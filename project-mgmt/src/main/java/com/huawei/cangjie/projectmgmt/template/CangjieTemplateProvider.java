/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.template;

import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_PLUGIN_ID;

import com.huawei.cangjie.projectmgmt.utils.CangjieEnvUtils;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.projectmgmt.ohos.template.parser.TemplateParser;
import com.huawei.deveco.projectmgmt.ohos.template.provider.TemplateProvider;

import com.intellij.openapi.application.PathManager;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * CangjieTemplateProvider
 *
 * @author c30037635
 * @since 2023-12-15
 */
public class CangjieTemplateProvider implements TemplateProvider {
    private static final String OHOS_TEMPLATE_PROVIDER_ID = "ohos";

    private static final String META_FILE_NAMES = "templateCompatible.json";
    private static final String META_NO_COMPATIBLE_FILE_NAMES = "templateNoCompatible.json";

    @Override
    public String getProviderId() {
        return OHOS_TEMPLATE_PROVIDER_ID;
    }

    @Override
    public String getTemplatePath() {
        if (!CangjieEnvUtils.isProjectChinaCountryCode()) {
            return StringUtils.EMPTY;
        }
        return "lib/templates";
    }

    @Override
    public String getPluginId() {
        return CANGJIE_PLUGIN_ID;
    }

    @Override
    public String getAddOnName() {
        return "";
    }

    @Override
    public String getType() {
        return "2";
    }

    @Override
    public TemplateParser getTemplateParser(String s) {
        return null;
    }

    @Override
    public List<Path> getMarketTemplateRootPaths() {
        return List.of(Paths.get(PathManager.getSystemPath(), "templates").normalize());
    }

    @Override
    public String getMetaFileName() {
        if (!SdkUtils.isConfigCompatibleSdk()) {
            return META_NO_COMPATIBLE_FILE_NAMES;
        }
        return META_FILE_NAMES;
    }
}
