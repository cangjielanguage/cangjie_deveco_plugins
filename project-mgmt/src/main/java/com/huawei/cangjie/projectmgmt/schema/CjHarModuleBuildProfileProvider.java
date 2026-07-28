/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.schema;

import com.huawei.deveco.res.ohos.json.profile.schema.HarModuleBuildProfileProvider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nullable;

/**
 * cangjie har module build profile.
 *
 * @since 2024-03-21
 */
public class CjHarModuleBuildProfileProvider extends HarModuleBuildProfileProvider {
    public CjHarModuleBuildProfileProvider(Project project, String schemaName) {
        super(project, schemaName);
    }

    /**
     * get cangjie schema path
     *
     * @return the path of schema file.
     */
    @Override
    @Nullable
    public VirtualFile getSchemaFile() {
        return CjBaseBuildProfileProvider.getCangjieSchemaFile(mySchemaFileName, super::getSchemaFile);
    }
}
