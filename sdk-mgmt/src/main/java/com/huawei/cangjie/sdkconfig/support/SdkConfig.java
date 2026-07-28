/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkconfig.support;

import com.huawei.cangjie.sdkconfig.annotations.SdkConfigKey;
import com.huawei.cangjie.sdkconfig.configkeys.SdkKeys;

/**
 * Sdk Config
 *
 * @since 2025-07-03
 */
public interface SdkConfig {
    /**
     * get sdk root path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.SDK_ROOT_PATH)
    String getSdkRootPath();

    /**
     * get sdk config file
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.SDK_CONFIG_FILE)
    String getSdkConfigFile();

    /**
     * get sdk type
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.SDK_TYPE)
    String getSdkType();

    /**
     * get build tools root path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_ROOT_PATH)
    String getBuildToolsRootPath();

    /**
     * get build path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_PATH)
    String getBuildPath();

    /**
     * get build cj decl path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_CJ_DECL_PATH)
    String getBuildCjDeclPath();

    /**
     * get build linux arm kit path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_LINUX_ARM_KIT_PATH)
    String getBuildLinuxArmKitPath();

    /**
     * get build ohos macro path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_OHOS_MACRO_PATH)
    String getBuildOhosMacroPath();

    /**
     * get build linux arm ohos path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_LINUX_ARM_OHOS_PATH)
    String getBuildLinuxArmOhosPath();

    /**
     * get build linux x86 kit path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_LINUX_X86_KIT_PATH)
    String getBuildLinuxX86KitPath();

    /**
     * get build linux x86 ohos path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_LINUX_X86_OHOS_PATH)
    String getBuildLinuxX86OhosPath();

    /**
     * get compatibility linux ohos arm api path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.COMPATIBILITY_LINUX_OHOS_ARM_API_MOCK_PATH)
    String getCompatibilityLinuxOhosArmApiMockPath();

    /**
     * get compatibility linux ohos x86 api path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.COMPATIBILITY_LINUX_OHOS_X86_API_MOCK_PATH)
    String getCompatibilityLinuxOhosX86ApiMockPath();

    /**
     * get compatibility  linux ohos arm wrapper mock path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.COMPATIBILITY_LINUX_OHOS_ARM_WRAPPER_MOCK_PATH)
    String getCompatibilityLinuxOhosArmWrapperMockPath();

    /**
     * get compatibility linux ohos x86 wrapper mock path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.COMPATIBILITY_LINUX_OHOS_X86_WRAPPER_MOCK_PATH)
    String getCompatibilityLinuxOhosX86WrapperMockPath();

    /**
     * get build tools path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_PATH)
    String getBuildToolsPath();

    /**
     * get build tools cjc path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_CJC_PATH)
    String getBuildToolsCjcPath();

    /**
     * get build tools std path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_STD_PATH)
    String getBuildToolsStdPath();

    /**
     * get build tools win runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_WIN_RUNTIME_PATH)
    String getBuildToolsWinRuntimePath();

    /**
     * get build tools mac arm runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_MAC_ARM_RUNTIME_PATH)
    String getBuildToolsMacArmRuntimePath();

    /**
     * get build tools mac x86 runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_MAC_X86_RUNTIME_PATH)
    String getBuildToolsMacX86RuntimePath();

    /**
     * get build tools linux ohos arm runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_LINUX_OHOS_ARM_RUNTIME_PATH)
    String getBuildToolsLinuxOhosArmRuntimePath();

    /**
     * get build tools linux ohos x86 runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_LINUX_OHOS_X86_RUNTIME_PATH)
    String getBuildToolsLinuxOhosX86RuntimePath();

    /**
     * get build tools linux x86 runtime path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_LINUX_X86_RUNTIME_PATH)
    String getBuildToolsLinuxX86RuntimePath();

    /**
     * get build tools asan path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_ASAN_PATH)
    String getBuildToolsAsanPath();

    /**
     * get build tools third party lib path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_THIRD_PARTY_LIB_PATH)
    String getBuildToolsThirdPartyLibPath();

    /**
     * get build tools bin path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_TOOLS_BIN_PATH)
    String getBuildToolsBinPath();

    /**
     * get build tools dts parser path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_TOOLS_DTS_PARSER_PATH)
    String getBuildToolsDtsParserPath();

    /**
     * get build tools lib path
     *
     * @return path
     */
    @SdkConfigKey(SdkKeys.BUILD_TOOLS_TOOLS_LIB_PATH)
    String getBuildToolsLibPath();
}
