/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console.editor;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lldb Language CodeStyle Provider
 *
 * @since 2022-10-19
 */
public class CangjieLldbLanguageCodeStyleProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings baseSettings,
        @NotNull CodeStyleSettings modelSettings) {
        return new LldbCodeStyleAbstractConfigurable(baseSettings, modelSettings);
    }

    @Nullable
    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return '(' + CangjieLldbLanguage.NAME + ") -exec-continue";
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return CangjieLldbLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new LldbCodeStyleSettings(settings);
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new IndentOptionsEditor();
    }

    /**
     * default lldb code style panel
     */
    private static class LldbCodeStylePanel extends TabbedLanguageCodeStylePanel {
        public LldbCodeStylePanel(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
            super(CangjieLldbLanguage.INSTANCE, settings, originalSettings);
        }

        @Override
        protected void initTabs(CodeStyleSettings settings) {
            this.addIndentOptionsTab(settings);
            // to_do The following code doesn't seem to be used
            for (CodeStyleSettingsProvider provider : CodeStyleSettingsProvider
                    .EXTENSION_POINT_NAME.getExtensionList()) {
                if (provider.getLanguage() == XMLLanguage.INSTANCE && !provider.hasSettingsPage()) {
                    this.createTab(provider);
                }
            }
        }
    }

    /**
     * default Lldb code style setting class
     */
    private static class LldbCodeStyleSettings extends CustomCodeStyleSettings {
        protected LldbCodeStyleSettings(CodeStyleSettings container) {
            super(CangjieLldbLanguage.INSTANCE.getID(), container);
        }
    }

    /**
     * Lldb CodeStyleAbstractConfigurable
     */
    private static class LldbCodeStyleAbstractConfigurable extends CodeStyleAbstractConfigurable {
        public LldbCodeStyleAbstractConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
            super(settings, cloneSettings, CangjieLldbLanguage.NAME);
        }

        @Override
        protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
            return new LldbCodeStylePanel(getCurrentSettings(), settings);
        }
    }
}
