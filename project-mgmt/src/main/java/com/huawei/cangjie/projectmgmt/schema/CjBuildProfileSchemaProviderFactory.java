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
import com.huawei.deveco.res.ohos.json.profile.schema.BuildProfileSchemaProviderFactory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
    /** Keep the native factory because unregistering an extension removes it globally. */
    private static volatile BuildProfileSchemaProviderFactory nativeFactory;

    /**
     * Keep native factory.
     *
     * @param factory the factory
     */
    public static void keepNativeFactory(BuildProfileSchemaProviderFactory factory) {
        nativeFactory = factory;
    }
    @Override
    @NotNull
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        ArrayList<JsonSchemaFileProvider> providers = new ArrayList<>();
        if (!CangjieEnvUtils.isProjectChinaCountryCode()) {
            providers.addAll(getNativeProviders(project));
        } else {
            providers.add(
                new CjModuleBuildProfileProvider(project, BuildProfileConstant.OHOS_MODULE_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(new CjProjectBuildProfileProvider(project,
                BuildProfileConstant.OHOS_PROJECT_BUILD_PROFILE_SCHEMA_JSON));
            providers.add(new CjHarModuleBuildProfileProvider(project,
                BuildProfileConstant.OHOS_HAR_MODULE_BUILD_PROFILE_SCHEMA_JSON));
            providers.addAll(getUnreplacedNativeProviders(project));
        }
        return providers;
    }


    /**
     * Return the complete native provider list.  The native factory is the source of truth
     * because the native schema set may contain more than the three well-known providers.
     *
     * @param project project owning the providers
     * @return all native providers
     */
    private List<JsonSchemaFileProvider> getNativeProviders(Project project) {
        BuildProfileSchemaProviderFactory factory = getNativeFactory();
        if (factory != null) {
            return new ArrayList<>(factory.getProviders(project));
        }
        return List.of(
            new ModuleBuildProfileProvider(project, BuildProfileConstant.OHOS_MODULE_BUILD_PROFILE_SCHEMA_JSON),
            new ProjectBuildProfileProvider(project, BuildProfileConstant.OHOS_PROJECT_BUILD_PROFILE_SCHEMA_JSON),
            new HarModuleBuildProfileProvider(project,
                BuildProfileConstant.OHOS_HAR_MODULE_BUILD_PROFILE_SCHEMA_JSON));
    }

    private List<JsonSchemaFileProvider> getUnreplacedNativeProviders(Project project) {
        List<JsonSchemaFileProvider> nativeProviders = new ArrayList<>();
        BuildProfileSchemaProviderFactory factory = getNativeFactory();
        if (factory != null) {
            addUnreplacedProviders(nativeProviders, factory.getProviders(project));
        }
        return nativeProviders;
    }

    private BuildProfileSchemaProviderFactory getNativeFactory() {
        BuildProfileSchemaProviderFactory factory = nativeFactory;
        if (factory == null) {
            for (JsonSchemaProviderFactory extension : JsonSchemaProviderFactory.EP_NAME.getExtensions()) {
                if (extension instanceof BuildProfileSchemaProviderFactory) {
                    factory = (BuildProfileSchemaProviderFactory) extension;
                    nativeFactory = factory;
                    break;
                }
            }
        }
        return factory;
    }

    private void addUnreplacedProviders(List<JsonSchemaFileProvider> target,
        List<JsonSchemaFileProvider> candidates) {
        for (JsonSchemaFileProvider provider : candidates) {
            VirtualFile schemaFile = provider.getSchemaFile();
            if (schemaFile == null || !isCangjieSchema(schemaFile.getName())) {
                target.add(provider);
            }
        }
    }

    private boolean isCangjieSchema(String schemaName) {
        return BuildProfileConstant.OHOS_MODULE_BUILD_PROFILE_SCHEMA_JSON.equals(schemaName)
            || BuildProfileConstant.OHOS_PROJECT_BUILD_PROFILE_SCHEMA_JSON.equals(schemaName)
            || BuildProfileConstant.OHOS_HAR_MODULE_BUILD_PROFILE_SCHEMA_JSON.equals(schemaName);
    }
}
