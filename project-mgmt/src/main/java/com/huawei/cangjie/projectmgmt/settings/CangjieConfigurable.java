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

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.TitledSeparator;

import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * cangjie 语言配置
 *
 * @since 2025-11-17
 */
public class CangjieConfigurable implements CangjiePluginSettingProvider {
    // 正则表达式：由小写字母开头，由字母、数字、下划线组成，且总长度不超过 30 个字符
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^(?!.{31})[a-z][a-z0-9_]{0,29}$",
            Pattern.MULTILINE);

    private JPanel panel;

    private JCheckBox enableCheckbox;

    private JTextField packageNameField;

    @Override
    public void createComponent() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 1. 第一行：标题文本 + 分割线
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        TitledSeparator title = new TitledSeparator("Cangjie Package Name Settings");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(title);

        panel.add(titlePanel);

        // 2. 第二行：Enable 复选框
        enableCheckbox = new JCheckBox(message("cangjie.customize.package.name.prefix"));
        enableCheckbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        enableCheckbox.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(enableCheckbox);

        // 3. 第三行：输入框
        packageNameField = new JTextField();
        packageNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, packageNameField.getPreferredSize().height));
        packageNameField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(packageNameField);

        // 4. 初始状态和监听逻辑
        packageNameField.setEnabled(enableCheckbox.isSelected());
        enableCheckbox.addItemListener(e -> packageNameField.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
    }

    @Nullable
    @Override
    public JComponent getConfigSettings() {
        return panel;
    }

    @Override
    public boolean isModified() {
        CangjieSettings settings = CangjieSettings.getInstance();
        return enableCheckbox.isSelected() != settings.isEnabled()
                || !packageNameField.getText().equals(settings.getPackageName());
    }

    @Override
    public void apply() {
        String text = packageNameField.getText();
        boolean isSelected = enableCheckbox.isSelected();
        // 只有开启时才进行格式校验
        if (isSelected) {
            Matcher matcher = PACKAGE_PATTERN.matcher(text);
            if (!matcher.matches()) {
                Messages.showErrorDialog(panel, message("cangjie.package.name.invalid"), "Error");
                return;
            }
        }
        CangjieSettings settings = CangjieSettings.getInstance();
        if (settings.getState() != null) {
            settings.getState().setEnabled(enableCheckbox.isSelected());
            settings.getState().setPackageName(text);
        }
    }

    @Override
    public void reset() {
        CangjieSettings settings = CangjieSettings.getInstance();
        enableCheckbox.setSelected(settings.isEnabled());
        packageNameField.setText(settings.getPackageName());
        packageNameField.setEnabled(settings.isEnabled());
    }
}