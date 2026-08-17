/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.sync;

import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.testframework.configuration.CangjieLocalTestRunConfigurationType;
import com.huawei.cangjie.testframework.configuration.CangjieOhosTestRunConfigurationType;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.deveco.ohos.testframework.utils.TestProjectUtils;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.sync.syncinterface.SyncProject;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * CangjieConfigurationSyncProject
 *
 * @since 2025/02/20
 */
public class CangjieConfigurationSyncProject implements SyncProject {
    @Override
    public void syncProject(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        if (!FileUtils.isCangjieProject(projectModel)) {
            return;
        }
        if (syncRequest != SyncRequest.OPEN_PROJECT && syncRequest != SyncRequest.ACTIVE_TRIGGERING) {
            return;
        }
        List<RunConfiguration> ohosTestRunConfigurations = RunManager.getInstance(projectModel.getProject())
                .getConfigurationsList(CangjieOhosTestRunConfigurationType.getInstance());
        List<RunConfiguration> localTestRunConfigurations = RunManager.getInstance(projectModel.getProject())
                .getConfigurationsList(CangjieLocalTestRunConfigurationType.getInstance());
        List<RunConfiguration> allRunConfigurations = Stream.concat(
                ohosTestRunConfigurations.stream(), localTestRunConfigurations.stream()).toList();
        for (RunConfiguration runConfiguration : allRunConfigurations) {
            if (!(runConfiguration instanceof CangjieTestRunConfiguration cangjieTestRunConfiguration)) {
                continue;
            }
            persistenceSettings(cangjieTestRunConfiguration, projectModel);
        }
    }

    private void persistenceSettings(CangjieTestRunConfiguration configuration, ProjectModel projectModel) {
        String name = configuration.getName();
        String moduleName = CangjieTestConfigurationPersistentSetting.getSetting(configuration.getProject())
                .getModuleModelConfigurationState()
                .get(name);
        if (moduleName == null) {
            return;
        }
        List<OhosModuleModel> acceptedModules = TestProjectUtils.getAcceptedModules(projectModel);
        for (OhosModuleModel moduleModel : acceptedModules) {
            if (moduleModel.getModuleName().equals(moduleName)) {
                configuration.setModule(moduleModel);
            }
        }
    }
}
