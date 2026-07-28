/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.nodes;

import com.huawei.deveco.projectmgmt.ohos.view.provider.ProjectNodeProvider;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProjectModelV2;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;

/**
 * CangjieNodeProvider
 *
 * @since 2024/08/12
 */
public class CangjieNodeProvider implements ProjectNodeProvider {
    @Override
    public boolean canProcessProject(ProjectModel projectModel) {
        return projectModel instanceof HvigorProjectModelV2;
    }

    @Override
    public AbstractTreeNode<?> getProjectNode(ProjectModel projectModel, ViewSettings viewSettings) {
        return new CangjieProjectNode(projectModel, viewSettings);
    }
}
