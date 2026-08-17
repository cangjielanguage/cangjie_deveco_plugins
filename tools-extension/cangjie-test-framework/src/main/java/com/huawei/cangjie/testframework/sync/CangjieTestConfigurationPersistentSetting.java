/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.sync;

import com.huawei.cangjie.testframework.configuration.CangjieLocalTestRunConfigurationType;
import com.huawei.cangjie.testframework.configuration.CangjieOhosTestRunConfigurationType;
import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * CangjieTestConfigurationPersistentSetting
 *
 * @since 2025/02/20
 */
@State(name = "CangjieTestConfigurationPersistentSettings",
        storages = @Storage("CangjieTestConfigurationModuleSettings.xml"))
public class CangjieTestConfigurationPersistentSetting
        implements PersistentStateComponent<CangjieTestConfigurationPersistentSetting> {
    private static final Logger LOG = Logger.getInstance(CangjieTestConfigurationPersistentSetting.class);

    /**
     * 存储configuration与模块的配置关系
     */
    @XMap(propertyElementName = "settings", keyAttributeName = "configurationName", valueAttributeName = "moduleName")
    private Map<String, String> moduleModelConfigurationState = new HashMap<>();

    @Override
    @Nullable
    public CangjieTestConfigurationPersistentSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CangjieTestConfigurationPersistentSetting setting) {
        XmlSerializerUtil.copyBean(setting, this);
    }

    public Map<String, String> getModuleModelConfigurationState() {
        return moduleModelConfigurationState;
    }

    public void setModuleModelConfigurationState(Map<String, String> moduleModelConfigurationState) {
        this.moduleModelConfigurationState = moduleModelConfigurationState;
    }

    /**
     * get cache setting
     *
     * @param project project
     * @return setting
     */
    public static CangjieTestConfigurationPersistentSetting getSetting(Project project) {
        Map<String, String> result = new HashMap<>();
        List<RunConfiguration> ohosTestConfigurations = RunManager.getInstance(project)
                .getConfigurationsList(CangjieOhosTestRunConfigurationType.getInstance());
        List<RunConfiguration> localTestConfigurations = RunManager.getInstance(project)
                .getConfigurationsList(CangjieLocalTestRunConfigurationType.getInstance());
        List<RunConfiguration> allRunConfigurations = Stream.concat(
                ohosTestConfigurations.stream(), localTestConfigurations.stream()).toList();
        for (RunConfiguration configuration : allRunConfigurations) {
            String name = configuration.getName();
            if (!(configuration instanceof CangjieTestRunConfiguration cangjieTestRunConfiguration)) {
                setModuleFromSettings(project, result, name);
                continue;
            }
            String moduleName = cangjieTestRunConfiguration.getModuleName();
            if (StringUtil.isEmpty(moduleName)) {
                setModuleFromSettings(project, result, name);
                continue;
            }
            result.put(name, moduleName);
        }
        CangjieTestConfigurationPersistentSetting setting = new CangjieTestConfigurationPersistentSetting();
        setting.setModuleModelConfigurationState(result);
        return setting;
    }

    /**
     * 从CangjieTestConfigurationPersistentSetting获取配置关系
     *
     * @param project Project
     * @param result  result
     * @param name    config name
     */
    private static void setModuleFromSettings(Project project, Map<String, String> result, String name) {
        try {
            CangjieTestConfigurationPersistentSetting sets = project.getService(
                    CangjieTestConfigurationPersistentSetting.class);
            String moduleName = sets.getModuleModelConfigurationState().get(name);
            if (!StringUtil.isEmpty(moduleName)) {
                result.put(name, moduleName);
            }
        } catch (AssertionError error) {
            LOG.warn("Failed to set settings for module " + name);
        }
    }
}
