/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;

/**
 * OpenHarmonyTestBrowseListener
 *
 * @since 2025/02/20
 */
public class OpenHarmonyTestBrowseListener extends ComponentWithBrowseButton.BrowseFolderActionListener {
    private static String defaultPath;

    private final String title;
    private final String description;
    private final ComponentWithBrowseButton textField;
    private final Project project;
    private final FileChooserDescriptor fileChooserDescriptor;

    /**
     * OpenHarmonyTestBrowseListener
     *
     * @param title title
     * @param description description
     * @param textField textField
     * @param project project
     * @param fileChooserDescriptor fileChooserDescriptor
     * @param defaultPath defaultPath
     */
    public OpenHarmonyTestBrowseListener(@Nullable @NlsContexts.DialogTitle String title,
                                         @Nullable @NlsContexts.Label String description,
                                         @Nullable ComponentWithBrowseButton textField,
                                         @Nullable Project project,
                                         FileChooserDescriptor fileChooserDescriptor,
                                         String defaultPath) {
        super(title, description, textField, project, fileChooserDescriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        this.title = title;
        this.description = description;
        this.textField = textField;
        this.project = project;
        this.fileChooserDescriptor = fileChooserDescriptor;
        OpenHarmonyTestBrowseListener.defaultPath = defaultPath;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (title != null || description != null) {
            if (title != null) {
                fileChooserDescriptor.setTitle(title);
            }
            if (description != null) {
                fileChooserDescriptor.setDescription(description);
            }
        }
        VirtualFile initialFile = LocalFileSystem.getInstance().findFileByPath(defaultPath);
        FileChooser.chooseFile(fileChooserDescriptor, project, textField.getChildComponent(), initialFile,
            this::onFileChosen);
    }
}
