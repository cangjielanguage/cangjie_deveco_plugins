/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.nodes;

import com.huawei.deveco.projectmgmt.ohos.view.nodes.HvigorProjectNode;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * CangjieProjectNode
 *
 * @since 2024/08/12
 */
public class CangjieProjectNode extends HvigorProjectNode {
    public CangjieProjectNode(ProjectModel projectModel, ViewSettings viewSettings) {
        super(projectModel, viewSettings);
    }

    @Override
    @NotNull
    public List<AbstractTreeNode<?>> createModuleNodes(@NotNull ProjectModel projectModel) {
        List<AbstractTreeNode<?>> nodes = new ArrayList<>();
        for (ModuleModel moduleModel : projectModel.getModuleModelList()) {
            nodes.add(new CangjieModuleNode(getProject(), moduleModel, getSettings()));
        }
        return nodes;
    }
}
