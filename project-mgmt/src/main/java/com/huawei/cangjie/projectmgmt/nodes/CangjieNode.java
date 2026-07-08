/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.nodes;

import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE;

import com.huawei.deveco.projectmgmt.ohos.view.nodes.SourceRootNode;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * CangjieNode
 *
 * @since 2024/08/13
 */
public class CangjieNode extends SourceRootNode {
    private static final List<String> relativeDirectoryRepresentedByCurrentNode = Arrays.asList(
            "src", "src/main"
    );

    public CangjieNode(Project project, VirtualFile dir, ViewSettings viewSettings, VirtualFile rootFile) {
        super(project, dir, viewSettings, rootFile, CANGJIE);
    }

    @Override
    protected List<String> relativeDirectoryRepresentedByCurrentNode() {
        return relativeDirectoryRepresentedByCurrentNode;
    }

    @Override
    protected VirtualFile getOhosTestFileDirectory() {
        return null;
    }

    @Override
    @Nullable
    protected VirtualFile getTestFileDirectory() {
        return null;
    }

    @Override
    @Nullable
    protected VirtualFile getMockFileDirectory() {
        return null;
    }
}
