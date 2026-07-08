/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.schema;

import com.huawei.cangjie.projectmgmt.utils.CangjieEnvUtils;
import com.huawei.deveco.res.ohos.json.profile.BuildProfileConstant;
import com.huawei.deveco.res.ohos.json.profile.schema.HarModuleBuildProfileProvider;
import com.huawei.deveco.res.ohos.json.profile.schema.ModuleBuildProfileProvider;
import com.huawei.deveco.res.ohos.json.profile.schema.ProjectBuildProfileProvider;

import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * BuildProfileSchemaProviderFactory
 *
 * @since 2024-03-21
 */
public class CjBuildProfileSchemaProviderFactory implements JsonSchemaProviderFactory {
    @Override
    @NotNull
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        ArrayList<JsonSchemaFileProvider> providers = new ArrayList<>();
        if (!CangjieEnvUtils.isProjectChinaCountryCode()) {
            providers.add(
                new ModuleBuildProfileProvider(project, BuildProfileConstant.OHOS_MODULE_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(
                new ProjectBuildProfileProvider(project, BuildProfileConstant.OHOS_PROJECT_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(new HarModuleBuildProfileProvider(project,
                BuildProfileConstant.OHOS_HAR_MODULE_BUILD_PROFILE_SCHEMA_JSON));
        } else {
            providers.add(
                new CjModuleBuildProfileProvider(project, BuildProfileConstant.OHOS_MODULE_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(new CjProjectBuildProfileProvider(project,
                BuildProfileConstant.OHOS_PROJECT_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(new CjHarModuleBuildProfileProvider(project,
                BuildProfileConstant.OHOS_HAR_MODULE_BUILD_PROFILE_SCHEMA_JSON));
        }
        return providers;
    }
}