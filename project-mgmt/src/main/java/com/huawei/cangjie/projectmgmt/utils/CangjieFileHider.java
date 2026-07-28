/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;

import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * File Hider
 *
 * @since 2024/04/12
 */
public class CangjieFileHider implements TreeStructureProvider {
    private static final String MODULE_NAME_REG = "${moduleName}";

    private final List<String> fileList = List.of("cj_res", ".cache");

    private final List<String> moduleFileList = List.of("ability_mainability_entry.cj",
            "module_${moduleName}_entry.cj");

    @Override
    @NotNull
    public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent,
                                                @NotNull Collection<AbstractTreeNode<?>> children,
                                                ViewSettings settings) {
        if (!(parent instanceof PsiFileNode)
                && !(parent instanceof PsiDirectoryNode)) {
            return children;
        }

        // Remove the configuration file from display.
        ArrayList<AbstractTreeNode<?>> tweakedChildren = new ArrayList<>(children);
        tweakedChildren.removeIf(fileNode -> {
            if (fileNode instanceof PsiDirectoryNode) {
                VirtualFile virtualParentFile = ((BasePsiNode<?>) parent).getVirtualFile();
                if (virtualParentFile == null) {
                    return false;
                }
                if (!"cangjie".equals(virtualParentFile.getName())) {
                    return false;
                }
                VirtualFile virtualFile = ((PsiDirectoryNode) fileNode).getVirtualFile();
                if (virtualFile == null) {
                    return false;
                }
                return fileList.contains(virtualFile.getName());
            } else if (fileNode instanceof PsiFileNode) {
                VirtualFile virtualFile = ((PsiFileNode) fileNode).getVirtualFile();
                if (virtualFile == null) {
                    return false;
                }
                if (virtualFile.getName().endsWith(".cj.macrocall")) {
                    return true;
                }
                Project project = fileNode.getProject();
                OhosModuleModel module = getSelectFileOhosModuleModel(project, virtualFile);
                if (module == null) {
                    return false;
                }
                return isNeedHide(module, virtualFile);
            } else {
                return false;
            }
        });
        return tweakedChildren;
    }

    private boolean isNeedHide(OhosModuleModel module, VirtualFile virtualFile) {
        for (String hiddenFileName : moduleFileList) {
            if (hiddenFileName.replace(MODULE_NAME_REG, module.getModuleName()).equals(virtualFile.getName())) {
                return true;
            }
        }
        return false;
    }
}
