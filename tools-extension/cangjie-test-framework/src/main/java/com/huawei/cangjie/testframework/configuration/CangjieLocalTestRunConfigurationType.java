/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.configuration;

import com.huawei.cangjie.testframework.testbuild.CangjieTestBuildTaskProvider;
import com.huawei.cangjie.testframework.utils.CangjieIcons;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyPanelBundle;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NotNullLazyValue;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import javax.swing.Icon;

/**
 * CangjieLocalTestRunConfigurationType
 *
 * @since 2025/08/28
 */
public class CangjieLocalTestRunConfigurationType extends SimpleConfigurationType {
    /**
     * necessary, distinguish between Cangjie and ArkTs (name is OpenHarmonyTest) test configuration
     */
    public static final String ID = "CangjieLocalTest";

    private static final String NAME_OR_DESCRIPTION = "Cangjie Local Test";

    public CangjieLocalTestRunConfigurationType() {
        super(ID, NAME_OR_DESCRIPTION, NAME_OR_DESCRIPTION,
                NotNullLazyValue.lazy(new Supplier<Icon>() {
                    @Override
                    public Icon get() {
                        return CangjieIcons.CANGJIE_TEST_ICON;
                    }
                }));
    }

    /**
     * getInstance
     *
     * @return CangjieLocalTestRunConfigurationType
     */
    public static CangjieLocalTestRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(CangjieLocalTestRunConfigurationType.class);
    }

    /**
     * 获取 ConfigurationFactory
     *
     * @return ConfigurationFactory
     */
    public ConfigurationFactory getFactory() {
        return getConfigurationFactories()[0];
    }

    /**
     * createTemplateConfiguration
     *
     * @param project project
     * @return RunConfiguration
     */
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CangjieTestRunConfiguration(project, this);
    }

    /**
     * getHelpTopic
     *
     * @return String
     */
    @Override
    public String getHelpTopic() {
        return OpenHarmonyPanelBundle.message("topic.id");
    }

    /**
     * configureBeforeRunTaskDefaults
     *
     * @param providerID providerID
     * @param task       task
     */
    @Override
    public void configureBeforeRunTaskDefaults(Key<? extends BeforeRunTask> providerID, BeforeRunTask task) {
        if (!CangjieTestBuildTaskProvider.OBJECT_KEY.equals(providerID)) {
            task.setEnabled(false);
        }
    }
}
