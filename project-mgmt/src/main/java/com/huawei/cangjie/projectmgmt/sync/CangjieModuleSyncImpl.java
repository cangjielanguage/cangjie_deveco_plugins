/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync;

import com.huawei.cangjie.projectmgmt.utils.Constants;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.sync.syncinterface.SyncModule;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;

import com.intellij.json.psi.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

/**
 * CangjieModuleSyncImpl
 *
 * @since 2024/04/16
 */
public class CangjieModuleSyncImpl implements SyncModule {
    @Override
    public void syncModule(@NotNull ModuleModel moduleModel, @NotNull SyncRequest syncRequest) {
        if (!(moduleModel instanceof OhosModuleModel)
                || !FileUtils.isCangjieModule((OhosModuleModel) moduleModel)) {
            return;
        }
        initModuleOption((OhosModuleModel) moduleModel);
    }

    private void initModuleOption(OhosModuleModel moduleModel) {
        Path etsPath = Path.of(moduleModel.getModulePath(), "src", "main", "ets");
        if (etsPath.toFile().exists()) {
            moduleModel.setExtraBuildOption("CangjieProjectType", "ArkTsCangjie");
        } else {
            moduleModel.setLanguage("Cangjie");
            moduleModel.setExtraBuildOption("CangjieProjectType", "Cangjie");
            ProjectModel projectModel = moduleModel.getProjectModel();
            if (projectModel == null) {
                return;
            }
            Optional<JsonObject> buildOptions = FileUtils.getBuildOption(projectModel.getProject(), moduleModel);
            if (buildOptions.isEmpty()) {
                return;
            }
            JsonObject cangjieOptions = PsiJsonFileUtil.getPsiJsonObject(buildOptions.get(), Constants.CANGJIE_OPTIONS);
            moduleModel.setExtraBuildOption(Constants.CANGJIE_OPTIONS, PsiJsonFileUtil.getJsonText(cangjieOptions));
        }
    }
}
