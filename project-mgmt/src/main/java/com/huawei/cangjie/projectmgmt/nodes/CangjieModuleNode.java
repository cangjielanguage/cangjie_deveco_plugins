/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.nodes;

import com.huawei.deveco.projectmgmt.ohos.view.nodes.HvigorModuleNode;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CangjieModuleNode
 *
 * @since 2024/08/13
 */
public class CangjieModuleNode extends HvigorModuleNode {
    private static final String CANGJIE_RELATIVE_PATH = "src/main/cangjie";

    public CangjieModuleNode(Project project, @NotNull ModuleModel moduleModel, ViewSettings viewSettings) {
        super(project, moduleModel, viewSettings);
    }

    @Override
    @NotNull
    public List<AbstractTreeNode<?>> createExtraNodes(ModuleModel moduleModel) {
        List<AbstractTreeNode<?>> nodes = new ArrayList<>(super.createExtraNodes(moduleModel));
        Optional<AbstractTreeNode<?>> cangjieNode = createCangjieNode();
        if (!cangjieNode.isEmpty()) {
            nodes.add(cangjieNode.get());
        }
        return nodes;
    }

    private Optional<AbstractTreeNode<?>> createCangjieNode() {
        VirtualFile cangjieDirectory = rootFile.findFileByRelativePath(CANGJIE_RELATIVE_PATH);
        if (cangjieDirectory == null || !cangjieDirectory.isDirectory()) {
            return Optional.empty();
        }
        return Optional.of(new CangjieNode(myProject, cangjieDirectory, getSettings(), rootFile));
    }
}
