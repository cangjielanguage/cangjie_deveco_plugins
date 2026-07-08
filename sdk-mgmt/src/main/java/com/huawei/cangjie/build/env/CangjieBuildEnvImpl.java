/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.build.env;

import static com.huawei.cangjie.sdkmanager.constants.SdkConstants.CANGJIE_COMPATIBLE_SDK_KEY;
import static com.huawei.cangjie.sdkmanager.constants.SdkConstants.CANGJIE_HARMONY_SDK_KEY;
import static com.huawei.cangjie.sdkmanager.constants.SdkConstants.CANGJIE_OPEN_HARMONY_SDK_KEY;
import static com.huawei.cangjie.sdkmanager.constants.SdkConstants.CANGJIE_PLUGIN_ID;

import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.util.CangjieSdkUtil;
import com.huawei.hvigor.api.HvigorExtendEnvProvider;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Cangjie build env.
 *
 * @since 2025 -08-22
 */
public class CangjieBuildEnvImpl implements HvigorExtendEnvProvider {
    @Override
    public Map<String, String> getExtendEnvs() {
        Map<String, String> envsMap = new HashMap<>();
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(CANGJIE_PLUGIN_ID));
        if (plugin != null) {
            Path pluginPath = plugin.getPluginPath();
            String hvigorCangjiePluginPath = FileUtil.toSystemIndependentName(
                pluginPath.resolve("lib").resolve("hvigor").resolve("cangjie-build-support").toString());
            envsMap.put("DEVECO_HVIGOR_CANGJIE_PLUGIN", hvigorCangjiePluginPath);
        }
        envsMap.put("DEVECO_CANGJIE_CONFIG_PATH", PathManager.getConfigPath());
        envsMap.put("DEVECO_CANGJIE_PLUGIN_ENABLED", "true");
        Path cangejieSdkPath = new CangjieIdeaSdkInfoHandler().getCangejieSdkPath();
        if (StringUtils.isNoneBlank(cangejieSdkPath.toString()) && Files.exists(cangejieSdkPath)) {
            envsMap.put("DEVECO_CANGJIE_SDK_PATH", cangejieSdkPath.toString());
        }
        String compatibleSdkPath = CangjieSdkUtil.getIdeSdkConfigPath(CANGJIE_COMPATIBLE_SDK_KEY);
        if (StringUtils.isNotBlank(compatibleSdkPath)) {
            envsMap.put("CANGJIE_COMPATIBLE_SDK_PATH", compatibleSdkPath);
        }
        String harmonySdkPath = CangjieSdkUtil.getIdeSdkConfigPath(CANGJIE_HARMONY_SDK_KEY);
        if (StringUtils.isNotEmpty(harmonySdkPath)) {
            envsMap.put("CANGJIE_HARMONY_SDK_PATH", harmonySdkPath);
        }
        String openHarmonySdkPath = CangjieSdkUtil.getIdeSdkConfigPath(CANGJIE_OPEN_HARMONY_SDK_KEY);
        if (StringUtils.isNotEmpty(openHarmonySdkPath)) {
            envsMap.put("CANGJIE_OPEN_HARMONY_SDK_PATH", openHarmonySdkPath);
        }
        return envsMap;
    }
}
