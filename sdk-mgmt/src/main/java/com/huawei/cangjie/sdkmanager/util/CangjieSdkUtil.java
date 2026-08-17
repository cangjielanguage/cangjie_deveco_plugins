/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import static com.huawei.cangjie.sdkmanager.constants.SdkConstants.CANGJIE_COMPATIBLE_SDK_KEY;

import com.huawei.cangjie.sdkmanager.idea.api.CangjieComponent;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.SystemInfo;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Cangjie sdk util.
 *
 * @since 2024-07-06
 */
public class CangjieSdkUtil {
    /**
     * Gets cangjie sdk path.
     *
     * @param isHarmony is harmony
     * @param sdkVersion the sdk version
     * @return the cangjie sdk path
     */
    public static String getCangjieSdkPath(boolean isHarmony, int sdkVersion) {
        return getSdkPathCommon(CangjieComponentPath.CANGJIE, isHarmony, sdkVersion);
    }

    /**
     * Gets cangjie sdk path.
     *
     * @param isHarmony is harmony
     * @param sdkVersion the sdk version
     * @return the cangjie sdk path
     */
    public static String getCangjieSdkPath(boolean isHarmony, String sdkVersion) {
        return getSdkPathCommon(CangjieComponentPath.CANGJIE, isHarmony, sdkVersion);
    }

    /**
     * Gets sdk path common.
     *
     * @param component the component
     * @param isHarmony is harmony
     * @param sdkVersion the sdk version
     * @return the sdk path common
     */
    public static String getSdkPathCommon(CangjieComponentPath component, boolean isHarmony, int sdkVersion) {
        Map<String, CangjieComponent> localSdks = new CangjieIdeaSdkInfoHandler().getLocalSdks(isHarmony, sdkVersion);
        return getComponentPath(component, localSdks);
    }

    /**
     * Gets sdk path common.
     *
     * @param component the component
     * @param isHarmony is harmony
     * @param sdkVersion the sdk version
     * @return the sdk path common
     */
    public static String getSdkPathCommon(CangjieComponentPath component, boolean isHarmony, String sdkVersion) {
        Map<String, CangjieComponent> localSdks = new CangjieIdeaSdkInfoHandler().getLocalSdks(isHarmony, sdkVersion);
        return getComponentPath(component, localSdks);
    }

    /**
     * Obtain installed sdk path string.
     *
     * @param component the component
     * @param isHarmony the is harmony
     * @param sdkVersion the sdk version
     * @return the string
     */
    public static String obtainInstalledSdkPath(CangjieComponentPath component, boolean isHarmony, int sdkVersion) {
        Map<String, CangjieComponent> installedSdkMap = new HashMap<>();
        new CangjieIdeaSdkInfoHandler().getHarmonyComponentMap(isHarmony, sdkVersion, installedSdkMap, false);
        return getComponentPath(component, installedSdkMap);
    }

    /**
     * Gets sdk expect path.
     *
     * @return the sdk expect path
     */
    public static Path getSdkExpectPath() {
        return new CangjieIdeaSdkInfoHandler().getCangejieSdkVersionPath();
    }

    /**
     * Gets sdk default path.
     *
     * @return the sdk default path
     */
    public static Path getSdkDefaultPath() {
        String envPath = System.getenv("DEVECO_CANGJIE_PATH");
        if (StringUtils.isEmpty(envPath)) {
            envPath = SystemInfo.isWindows ? System.getenv("USERPROFILE") : System.getenv("HOME");
        }
        return Paths.get(envPath).resolve(".cangjie-sdk");
    }

    /**
     * validate sdk
     *
     * @param sdkLocation String
     * @return boolean
     */
    public static boolean isValidSdk(String sdkLocation) {
        if (StringUtils.isEmpty(sdkLocation)) {
            return false;
        }
        Path sdkLocationPath = Paths.get(sdkLocation);
        Path uniJsonPath = sdkLocationPath.resolve("uni-package.json");
        Path ohUniJsonPath = sdkLocationPath.resolve("oh-uni-package.json");
        return Files.exists(uniJsonPath) || Files.exists(ohUniJsonPath);
    }

    /**
     * Gets ide sdk config path.
     *
     * @param configKey the config key
     * @return the ide sdk config path
     */
    public static String getIdeSdkConfigPath(String configKey) {
        String sdkLocation = System.getProperty(configKey);
        return StringUtils.isNotBlank(sdkLocation) ? sdkLocation.trim() : StringUtils.EMPTY;
    }

    /**
     * Is config compatible sdk boolean.
     *
     * @return the boolean
     */
    public static boolean isConfigCompatibleSdk() {
        return StringUtils.isNotBlank(System.getProperty(CANGJIE_COMPATIBLE_SDK_KEY));
    }

    /**
     * Gets dev eco short version.
     *
     * @return the dev eco short version
     */
    public static String getDevEcoShortVersion() {
        return ApplicationInfo.getInstance().getShortVersion();
    }

    @NotNull
    private static String getComponentPath(CangjieComponentPath component,
        Map<String, CangjieComponent> installedSdkMap) {
        CangjieComponent cangjieComponent = installedSdkMap.get(component.value());
        if (cangjieComponent == null) {
            return StringUtils.EMPTY;
        }
        Path componentLocation = cangjieComponent.getLocation();
        if (componentLocation == null) {
            return StringUtils.EMPTY;
        }
        return componentLocation.toString();
    }
}
