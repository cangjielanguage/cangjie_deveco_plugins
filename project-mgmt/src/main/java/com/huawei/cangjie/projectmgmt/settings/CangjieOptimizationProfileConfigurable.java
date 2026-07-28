/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.settings;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.cangjie.mgmt.settings.CangjiePluginSettingProvider;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.TitledSeparator;

import org.jetbrains.annotations.Nullable;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * cangjie pgo配置
 *
 * @since 2026 -04-24
 */
public class CangjieOptimizationProfileConfigurable implements CangjiePluginSettingProvider {
    private JPanel panel;

    private JCheckBox debugCheckbox;

    private JCheckBox releaseCheckbox;

    @Override
    public void createComponent() {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        TitledSeparator title = new TitledSeparator("Use Optimization Profile");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(title);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(titlePanel);
        // 第二行：Debug 复选框
        debugCheckbox = new JCheckBox(message("cangjie.pgo.use.optimization.profile.debug"));
        debugCheckbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        debugCheckbox.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(debugCheckbox);
        // 第二行：Release 复选框
        releaseCheckbox = new JCheckBox(message("cangjie.pgo.use.optimization.profile.release"));
        releaseCheckbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        releaseCheckbox.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(releaseCheckbox);
    }

    @Nullable
    @Override
    public JComponent getConfigSettings() {
        return panel;
    }

    @Override
    public boolean isModified() {
        CangjieOptimizationProfileSettings settings = CangjieOptimizationProfileSettings.getInstance();
        return debugCheckbox.isSelected() != settings.isDebugUseProfile()
            || releaseCheckbox.isSelected() != settings.isReleaseUseProfile();
    }

    @Override
    public void apply() {
        CangjieOptimizationProfileSettings settings = CangjieOptimizationProfileSettings.getInstance();
        if (settings.getState() != null) {
            settings.getState().setDebugUseProfile(debugCheckbox.isSelected());
            settings.getState().setReleaseUseProfile(releaseCheckbox.isSelected());
            ApplicationManager.getApplication().saveSettings();
        }
    }

    @Override
    public void reset() {
        CangjieOptimizationProfileSettings settings = CangjieOptimizationProfileSettings.getInstance();
        debugCheckbox.setSelected(settings.isDebugUseProfile());
        releaseCheckbox.setSelected(settings.isReleaseUseProfile());
    }
}