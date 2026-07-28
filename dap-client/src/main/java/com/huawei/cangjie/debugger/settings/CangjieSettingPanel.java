/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * ***
 *
 * @since 2024-9-10
 */
public class CangjieSettingPanel implements SearchableConfigurable {
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final List<DebugConfigPanel> items;

    /**
     * cangjie debug setting panel
     *
     * @param project project
     */
    public CangjieSettingPanel(Project project) {
        this.items = List.of(new TimeTravelPanel(project));
        Box vBox = Box.createVerticalBox();
        items.forEach(item -> {
            JPanel itemPanel = item.getPanel();
            if (itemPanel != null) {
                vBox.add(itemPanel);
                vBox.add(Box.createVerticalStrut(25));
            }
        });
        panel.add(vBox);
    }

    @Override
    @NlsContexts.ConfigurableName
    public String getDisplayName() {
        return "Cangjie Debugger";
    }

    @Override
    @NotNull
    @NonNls
    public String getId() {
        return "com.huawei.settings.CangjieSettingPanel";
    }

    @Override
    @Nullable
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return items.stream().anyMatch(DebugConfigPanel::isModified);
    }

    @Override
    public void apply() {
        items.forEach(DebugConfigPanel::apply);
    }

    @Override
    public void reset() {
        items.forEach(DebugConfigPanel::reset);
    }
}
