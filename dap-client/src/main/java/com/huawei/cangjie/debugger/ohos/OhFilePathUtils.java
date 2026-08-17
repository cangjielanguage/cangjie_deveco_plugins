/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieComponent;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;
import com.huawei.deveco.debugger.ohos.util.DebuggerUtil;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.sdkmanager.core.api.PathAndApiVersion;
import com.huawei.deveco.sdkmanager.core.api.SdkInfoHandler;
import com.huawei.deveco.sdkmanager.core.constants.ComponentPath;
import com.huawei.deveco.sdkmanager.core.domain.ApiVersion;
import com.huawei.deveco.sdkmanager.core.domain.Component;
import com.huawei.deveco.sdkmanager.hos.common.api.HosPrjSdkType;
import com.huawei.deveco.sdkmanager.hos.common.api.SimpleHosVersionMapper;
import com.huawei.deveco.sdkmanager.hos.common.api.UniSdkInfoHandler;
import com.huawei.deveco.sdkmanager.hos.idea.api.IdeHosPrjSdkHandlerV2;
import com.huawei.deveco.sdkmanager.ohos.common.api.OhPrjSdkHandler;
import com.huawei.deveco.sdkmanager.ohos.common.api.OhPrjSdkType;
import com.huawei.deveco.sdkmanager.ohos.idea.api.IdeaOhPrjSdkInfoHandler;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.system.CpuArch;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * file path utils
 *
 * @since 2022-12-1
 */
public class OhFilePathUtils {
    /**
     * The constant REMOTE_TEMP_DIR.
     */
    public static final String REMOTE_TEMP_DIR = "/data/local/tmp";

    private static final Logger LOGGER = Logger.getInstance(OhFilePathUtils.class);

    private static final String IDE_TOOLS_BASE_PATH = CodeCheckByPassUtils.getPath(PathManager.getHomePath(), "tools")
        .toString();

    private static final String IDE_LLVM_BASE_PATH = CodeCheckByPassUtils.getPath(IDE_TOOLS_BASE_PATH, "llvm")
        .toString();

    private static final String IDE_LLVM_SCRIPTS_PATH = CodeCheckByPassUtils.getPath(IDE_LLVM_BASE_PATH, "scripts")
        .toString();

    private static final String DAP_SERVER_NAME_BASE = "dap_server";

    /**
     * The constant HW_CLANG_BIN_DIR.
     */
    private static final String HW_CLANG_BIN_DIR = "/llvm/lib/clang/";

    /**
     * validate project has cangjie dir
     *
     * @param ohosModuleModel ohosModuleModel
     */
    public static void validateCangjieDir(OhosModuleModel ohosModuleModel) {
        String cangjieDir = getCangjieDir(ohosModuleModel);
        if (!(cangjieDir != null && CodeCheckByPassUtils.createFile(cangjieDir).exists())) {
            throw CodeCheckByPassUtils.createRuntimeException("Cangjie sdk doesn't exist.");
        }
    }

    /**
     * get cangjie dir in project.
     *
     * @param ohosModuleModel ohos module model
     * @return cangjie dir
     */
    @Nullable
    public static String getCangjieDir(OhosModuleModel ohosModuleModel) {
        String apiVersion = ohosModuleModel.getProjectModel().getFullCompileSdkVersion().getValue();
        boolean isHarmony = DebuggerUtil.isHarmonyRunTime(ohosModuleModel);
        Map<String, CangjieComponent> localSdks = new CangjieIdeaSdkInfoHandler().getLocalSdks(isHarmony, apiVersion);
        CangjieComponent cangjieComponent = localSdks.get(CangjieComponentPath.CANGJIE.value());
        // Mark the progress indeterminate
        ProgressIndicator globalProgressIndicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        if (globalProgressIndicator != null) {
            globalProgressIndicator.setIndeterminate(true);
        }
        if (cangjieComponent == null) {
            return CodeCheckByPassUtils.getNull();
        }
        return cangjieComponent.getLocation().toString();
    }

    /**
     * get native dir
     *
     * @param ohosModuleModel ohosModuleModel
     * @return native dir
     */
    public static String getNativeDir(OhosModuleModel ohosModuleModel) {
        boolean isHarmonyRunTime = DebuggerUtil.isHarmonyRunTime(ohosModuleModel);
        ApiVersion apiVersion = ohosModuleModel.getProjectModel().getFullCompileSdkVersion();
        String apiVersionValue = apiVersion.getValue();
        Component nativeComponent;
        if (isHarmonyRunTime) {
            // get HOS SDK
            IdeHosPrjSdkHandlerV2 ideHosPrjSdkHandlerV2 = new IdeHosPrjSdkHandlerV2();
            UniSdkInfoHandler sdkHandler = ideHosPrjSdkHandlerV2.getSdkHandler(HosPrjSdkType.OPENHARMONY);
            var localComponents = sdkHandler.getLocalSdks(apiVersionValue);
            nativeComponent = localComponents.get("native");
        } else {
            // get OH sdk
            OhPrjSdkHandler ideaOhPrjSdkHandler = new IdeaOhPrjSdkInfoHandler();
            SdkInfoHandler ohSdkInfoHandler = ideaOhPrjSdkHandler.getSdkHandler(OhPrjSdkType.OPENHARMONY);
            Map<PathAndApiVersion, Component> localComponents = ohSdkInfoHandler.getLocalSdks();
            nativeComponent = localComponents.get(new PathAndApiVersion(ComponentPath.NATIVE.value(), apiVersion));
        }
        // Mark the progress indeterminate
        ProgressIndicator globalProgressIndicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        if (globalProgressIndicator != null) {
            globalProgressIndicator.setIndeterminate(true);
        }
        if (nativeComponent == null) {
            return CodeCheckByPassUtils.getNull();
        }
        return nativeComponent.getLocation().toString();
    }

    /**
     * get server path
     *
     * @return path
     */
    public static String getServerInstallPath() {
        String ideDapServerBasePath = getDapServerBasePath();
        if (SystemInfo.isWindows) {
            return CodeCheckByPassUtils.getPath(ideDapServerBasePath, DAP_SERVER_NAME_BASE + ".exe").toString();
        } else if (SystemInfo.isMac) {
            String macServerName;
            // macOS only have these two architectures
            if (CpuArch.isArm64()) {
                macServerName = DAP_SERVER_NAME_BASE + "-macos_aarch64";
            } else {
                macServerName = DAP_SERVER_NAME_BASE + "-macos_x64";
            }
            return CodeCheckByPassUtils.getPath(ideDapServerBasePath, macServerName).toString();
        } else {
            throw new UnsupportedOperationException("Unsupported systems");
        }
    }

    /**
     * get dap server base path
     *
     * @return dap_server path
     */
    public static String getDapServerBasePath() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(OhConstants.CANGJIE_PLUGIN_ID));
        if (plugin == null) {
            return CodeCheckByPassUtils.getNull();
        }
        Path pluginPath = plugin.getPluginPath();
        if (pluginPath == null) {
            return CodeCheckByPassUtils.getNull();
        }
        return CodeCheckByPassUtils.getPath(pluginPath.toString(), "bin").toString();
    }

    /**
     * get start lldb script path
     *
     * @return File
     */
    public static File getLldbStartScript() {
        return CodeCheckByPassUtils.getPath(IDE_LLVM_SCRIPTS_PATH, "start_lldb_server.sh").toFile();
    }

    /**
     * get lldb server file
     *
     * @param ohosModuleModel ohosModuleModel
     * @param ohAbi abi
     * @return file
     */
    @Nullable
    public static File getLldbServer(OhosModuleModel ohosModuleModel, Abi ohAbi) {
        if (ohAbi == null) {
            return CodeCheckByPassUtils.getNull();
        }
        File lldbFile;
        if (DebuggerUtil.isHarmonyRunTime(ohosModuleModel)) {
            // get lldb-sever from hms if runtimeOS is harmonyOS
            lldbFile = getLldbServerFromHms(ohosModuleModel, ohAbi);
            if (lldbFile != null && lldbFile.exists()) {
                return lldbFile;
            }
        }
        String nativeDir = getNativeDir(ohosModuleModel);
        File clangFile = CodeCheckByPassUtils.getPath(nativeDir, HW_CLANG_BIN_DIR).toFile();
        Optional<String> versionDir = getVersionDir(clangFile);
        if (versionDir.isEmpty()) {
            LogUtils.printCangjieLogError(LOGGER, String.format("versionDir is not existed. clangFile dir is: {}",
                    clangFile.getParent()));
            return CodeCheckByPassUtils.getNull();
        }
        lldbFile = CodeCheckByPassUtils.getPath(nativeDir, HW_CLANG_BIN_DIR, versionDir.get(), File.separator,
                "bin", File.separator, ohAbi.getLldbType(), "lldb-server").toFile();
        // If a matching lldb-server is found, it will return
        if (lldbFile.exists()) {
            return lldbFile;
        } else {
            LogUtils.printCangjieLogError(LOGGER, String.format("lldbFile dir is not existed, lldb dir is: {}",
                    lldbFile.getPath()));
        }
        return CodeCheckByPassUtils.getNull();
    }


    @Nullable
    private static File getLldbServerFromHms(OhosModuleModel ohosModuleModel, Abi ohAbi) {
        IdeHosPrjSdkHandlerV2 ideHosPrjSdkHandlerV2 = new IdeHosPrjSdkHandlerV2();
        String apiVersionValue = ohosModuleModel.getProjectModel().getFullCompileSdkVersion().getValue();
        // get hms API version
        String hmsApiVersion = SimpleHosVersionMapper.INSTANCE.getHosVersion(apiVersionValue);
        UniSdkInfoHandler uniSdkInfoHandler = ideHosPrjSdkHandlerV2.getSdkHandler(HosPrjSdkType.HARMONYOS);
        Component nativeComponent = uniSdkInfoHandler.getLocalSdks(hmsApiVersion).get(ComponentPath.NATIVE.value());
        if (nativeComponent == null) {
            LogUtils.printCangjieLogWarn(LOGGER, "hms native.dir doesn't exist.");
            return CodeCheckByPassUtils.getNull();
        }
        File lldbServerFile = CodeCheckByPassUtils.getPath(nativeComponent.getLocation().toString(), "lldb",
                ohAbi.getLldbType(), "lldb-server").toFile();
        if (!lldbServerFile.exists()) {
            LogUtils.printCangjieLogWarn(LOGGER, String.format("hms lldbFile dir is not existed, lldb dir is: %s",
                    lldbServerFile.getPath()));
            return CodeCheckByPassUtils.getNull();
        }
        return lldbServerFile;
    }

    private static Optional<String> getVersionDir(File clangFile) {
        String versionDir = null;
        File[] versionFiles = clangFile.listFiles();
        if (versionFiles == null) {
            return Optional.empty();
        }
        String regex = "\\d+\\.\\d+\\.\\d+";
        for (File versionFile : versionFiles) {
            String fileName = versionFile.getName();
            if (fileName.matches(regex)) {
                versionDir = fileName;
                break;
            }
        }
        if (versionDir == null) {
            return Optional.empty();
        }
        return Optional.of(versionDir);
    }
}