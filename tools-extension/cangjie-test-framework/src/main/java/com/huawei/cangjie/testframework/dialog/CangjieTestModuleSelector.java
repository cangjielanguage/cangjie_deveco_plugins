/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.dialog;

import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.DebugMessageBundle;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.ohos.debugcommon.module.ModuleModelComboBox;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.ListItem;
import com.intellij.ui.SortedComboBoxModel;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;

/**
 * CangjieTestModuleSelector
 *
 * @since 2025/02/20
 */
public class CangjieTestModuleSelector {
    @NotNull
    private final Project project;
    private final JComboBox<? extends ModuleModel> modulesList;

    /**
     * ModuleSelector
     *
     * @param project project
     * @param modulesComboBox modulesComboBox
     */
    public CangjieTestModuleSelector(@NotNull Project project, ModuleModelComboBox modulesComboBox) {
        this(project, modulesComboBox, DebugMessageBundle.message("list.item.no.module"));
    }

    /**
     * ModuleSelector
     *
     * @param project project
     * @param modulesComboBox modulesComboBox
     * @param noModule noModule
     */
    public CangjieTestModuleSelector(@NotNull Project project, ModuleModelComboBox modulesComboBox,
                                         @ListItem String noModule) {
        this.project = project;
        this.modulesList = modulesComboBox;
    }

    /**
     * applyTo
     *
     * @param configuration OpenHarmonyRunConfiguration
     */
    public void applyTo(final OpenHarmonyRunConfiguration configuration) {
        Object selectedItem = this.modulesList.getSelectedItem();
        if (selectedItem instanceof OhosModuleModel) {
            configuration.setModule((OhosModuleModel) selectedItem);
        }
    }

    /**
     * reset
     *
     * @param configuration OpenHarmonyRunConfiguration
     */
    public void reset(final OpenHarmonyRunConfiguration configuration) {
        reset();
        this.modulesList.setSelectedItem(configuration.getModule());
    }

    /**
     * reset
     */
    public void reset() {
        ProjectModel projectModel = ProjectModelManager.getInstance().getTargetProjectModel(project);
        List<OhosModuleModel> moduleList = TestUtil.getAcceptedModules(projectModel);
        setModules(moduleList);
    }

    /**
     * getProject
     *
     * @return Project
     */
    @NotNull
    public Project getProject() {
        return this.project;
    }

    private void setModules(final Collection<? extends ModuleModel> modules) {
        if (this.modulesList instanceof ModuleModelComboBox) {
            ((ModuleModelComboBox) this.modulesList).setModules(modules);
        } else {
            SortedComboBoxModel<ModuleModel> model = (SortedComboBoxModel<ModuleModel>) this.modulesList.getModel();
            model.setAll(modules);
            model.add(null);
        }
    }

    /**
     * getModule
     *
     * @return ModuleModel
     */
    public Optional<ModuleModel> getModule() {
        Object selectedItem = this.modulesList.getSelectedItem();
        if (selectedItem instanceof ModuleModel) {
            return Optional.of((ModuleModel) selectedItem);
        }
        return Optional.empty();
    }
}
