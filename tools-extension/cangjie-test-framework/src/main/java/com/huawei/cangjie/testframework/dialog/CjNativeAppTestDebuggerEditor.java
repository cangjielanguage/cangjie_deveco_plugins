/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.dialog;

import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.debugger.ohos.run.configuration.editor.NativeAppDebuggerEditor;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;

import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

/**
 * CjNativeAppTestDebuggerEditor
 *
 * @since 2025/02/20
 */
public class CjNativeAppTestDebuggerEditor extends NativeAppDebuggerEditor {
    public CjNativeAppTestDebuggerEditor(Project project) {
        super(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull NativeDebuggerState state, OpenHarmonyRunConfiguration configuration) {
        super.resetEditorFrom(state, configuration);
    }

    @Override
    protected void applyEditorTo(@NotNull OpenHarmonyRunConfiguration openHarmonyRunConfiguration,
                                 @NotNull NativeDebuggerState state) {
        super.applyEditorTo(openHarmonyRunConfiguration, state);
    }

    @Override
    protected JComponent createEditor() {
        return super.createEditor();
    }
}