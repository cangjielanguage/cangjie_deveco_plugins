/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.cangjie.projectmgmt.dialog.FileCreateDialog;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * BaseFileCreateAction
 *
 * @since 2024/12/21
 */
public abstract class BaseFileCreateAction extends DumbAwareAction {
    private static final Pattern CANGJIE_NAME_PATTERN = Pattern.compile(
            "^[_\\w]+(?:_|\\w)*$",
            Pattern.UNICODE_CHARACTER_CLASS);

    private final String actionType;

    public BaseFileCreateAction(String text, String actionType) {
        super(text);
        this.actionType = actionType;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * check the validity of the file name.
     *
     * @param parameters parameters of dialog
     * @return String
     */
    @Nullable
    public String validateInput(Map<String, Object> parameters) {
        if (!(parameters.getOrDefault(FileCreateDialog.DIR, null) instanceof VirtualFile dir)) {
            return null;
        }
        if (!dir.isDirectory()) {
            return null;
        }
        if (!(parameters.getOrDefault(FileCreateDialog.FILENAME, null) instanceof String inputFileName)) {
            return null;
        }
        String fullFileName = inputFileName.endsWith("cj") ? inputFileName : inputFileName + ".cj";
        VirtualFile child = dir.findChild(fullFileName);
        if (child != null && child.exists()) {
            return message("create.file.exists.info", fullFileName);
        }
        if (Character.isDigit(fullFileName.charAt(0))) {
            return message("create.file.start.with.digit.info");
        }
        if (!CANGJIE_NAME_PATTERN.matcher(inputFileName).matches()) {
            return message("create.file.name.invalid", actionType);
        }
        return null;
    }
}
