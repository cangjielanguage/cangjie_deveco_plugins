/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action.page;

import static com.huawei.cangjie.projectmgmt.action.EnableCangjieAbilityAction.isSupportDeviceType;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;

import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * PageUtil
 *
 * @since 2024/08/15
 */
public class PageUtil {
    /**
     * check if should show new page button
     *
     * @param event event
     * @return if should show new page button
     */
    public static boolean shouldShowNewPageButton(AnActionEvent event) {
        if (event == null) {
            return false;
        }
        Project project = event.getProject();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        OhosModuleModel moduleModel = getSelectFileOhosModuleModel(project, virtualFile);
        // check is cangjie support module
        if (!FileUtils.isSupportModule(project, moduleModel)) {
            return false;
        }
        // check is support dvice type
        boolean isSupportDeviceType = isSupportDeviceType(moduleModel);
        if (!isSupportDeviceType) {
            return false;
        }
        // check is contain ets module
        if (!FileUtils.isContainEtsModule(moduleModel)) {
            return false;
        }
        if (FileUtils.isInEtsCjLoader(moduleModel, virtualFile)) {
            return false;
        }
        // check is in cangjie dir
        return FileUtils.isInCangjieCodeDir(moduleModel, virtualFile, false);
    }
}
