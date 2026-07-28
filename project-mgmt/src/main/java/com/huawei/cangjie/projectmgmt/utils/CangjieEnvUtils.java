/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import static com.huawei.cangjie.projectmgmt.utils.Constants.NAME;
import static com.huawei.deveco.projectmodel.ohos.util.CommonConstants.OH_PACKAGE_JSON5_FILE;
import static com.huawei.deveco.projectmodel.ohos.util.ConfigPath.PACKAGE_JSON;
import static com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil.findPsiFileJsonObject;
import static com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil.getPsiJsonString;

import com.huawei.cangjie.projectmgmt.enums.AbiEnum;
import com.huawei.cangjie.projectmgmt.env.CangjieEnvProvider;
import com.huawei.deveco.common.country.constants.CountryRegionConstants;
import com.huawei.deveco.common.country.setting.CountryRegionSetting;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;
import com.huawei.deveco.projectmodel.ohos.util.PackageType;
import com.huawei.deveco.projectmodel.ohos.util.ProjectUtil;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;
import com.huawei.deveco.sdkmanager.core.constants.ComponentPath;
import com.huawei.deveco.sdkmanager.core.domain.Component;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;
import com.huawei.deveco.sdkmanager.hos.common.api.HosPrjSdkType;
import com.huawei.deveco.sdkmanager.hos.common.api.UniSdkInfoHandler;
import com.huawei.deveco.sdkmanager.hos.idea.api.IdeHosPrjSdkHandlerV2;

import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cangjie envs
 *
 * @since 2024-05-03
 */
public class CangjieEnvUtils {
    private static final Logger LOG = Logger.getInstance(CangjieEnvUtils.class);

    private static final Map<Project, Map<String, String>> PROJECT_ENVS_MAP = new ConcurrentHashMap<>();

    private static final String API = "api";

    private static final String LIB = "lib";

    private static final String LINUX_AARCH_CJNATIVE = "linux_ohos_aarch64_cjnative";

    private static final String WIN_X86_64_MINGW = "x86_64-w64-mingw32";

    private static final String OHOS = "ohos";

    private static final String MACRO = "macro";


    private static final String KIT = "kit";

    /**
     * config project Depends Env
     *
     * @param projectModel projectModel
     */
    public static void configDependsEnv(ProjectModel projectModel) {
        if (projectModel == null) {
            return;
        }
        Project project = projectModel.getProject();
        if (project == null || StringUtil.isEmpty(project.getBasePath())) {
            return;
        }
        Map<String, String> dependsEnvMap = new ConcurrentHashMap<>();
        PROJECT_ENVS_MAP.put(project, dependsEnvMap);
        dependsEnvMap.put("DEVECO_OH_NATIVE_HOME", getNativeDir(projectModel));
        setDefaultEnv(dependsEnvMap, SdkUtils.getSdkPath(projectModel));
        addEnvByExtensions(dependsEnvMap, projectModel);
        addCompileConditionEnv(dependsEnvMap, projectModel);
        configOhpmDependsEnv(project, dependsEnvMap);
        configLocalModulesEnv(projectModel, project, dependsEnvMap);
        configMacEnv(dependsEnvMap);
    }

    /**
     * get project envs
     *
     * @param project project
     * @return project envs
     */
    public static Map<String, String> getProjectEnvs(Project project) {
        return PROJECT_ENVS_MAP.computeIfAbsent(project, k -> new ConcurrentHashMap<>());
    }

    private static String dependToPlaceHolder(String envName) {
        if (StringUtils.isEmpty(envName)) {
            return "";
        }
        return formatEnvName(String.join("_", envName.split("/")));
    }

    private static String formatEnvName(String envName) {
        if (StringUtils.isEmpty(envName)) {
            return "";
        }
        String env = envName;
        if (env.startsWith("@")) {
            env = env.substring(1);
        }
        return env.replaceAll("[-+.]", "_");
    }

    private static void setDefaultEnv(Map<String, String> envMap, String cangjieSdkPath) {
        envMap.put("DEVECO_CANGJIE_HOME", cangjieSdkPath);
        envMap.put("ABI", "arm64-v8a");
        if (SystemInfo.isMac) {
            envMap.put("AARCH64_LIBS",
                cangjieSdkPath + File.separator + API + File.separator
                        + LIB + File.separator + LINUX_AARCH_CJNATIVE + File.separator + OHOS);
            envMap.put("AARCH64_MACRO_LIBS",
                cangjieSdkPath + File.separator + API + File.separator + MACRO + File.separator + OHOS);
            envMap.put("AARCH64_KIT_LIBS",
                cangjieSdkPath + File.separator + API + File.separator
                        + LIB + File.separator + LINUX_AARCH_CJNATIVE + File.separator + KIT);
        } else {
            envMap.put("AARCH64_LIBS",
                cangjieSdkPath + File.separator + API + File.separator
                        + LIB + File.separator + LINUX_AARCH_CJNATIVE + File.separator + OHOS);
            envMap.put("AARCH64_KIT_LIBS",
                cangjieSdkPath + File.separator + API + File.separator
                        + LIB + File.separator + LINUX_AARCH_CJNATIVE + File.separator + KIT);
            envMap.put("AARCH64_MACRO_LIBS",
                cangjieSdkPath + File.separator + API + File.separator + MACRO + File.separator + OHOS);
            String x86Libs =
                cangjieSdkPath + File.separator + API + File.separator + LIB + File.separator + WIN_X86_64_MINGW
                    + File.separator + OHOS;
            if (Paths.get(x86Libs).toFile().exists()) {
                envMap.put("X86_64_LIBS", x86Libs);
            } else {
                envMap.put("X86_64_LIBS", StringUtils.EMPTY);
            }
            envMap.put("X86_64_MACRO_LIBS",
                    cangjieSdkPath + File.separator + API + File.separator + MACRO + File.separator + OHOS);
        }
    }

    /**
     * Gets native dir.
     *
     * @param projectModel the project model
     * @return the native dir
     */
    public static String getNativeDir(ProjectModel projectModel) {
        String apiVersion = projectModel.getFullCompileSdkVersion().getValue();
        IdeHosPrjSdkHandlerV2 ideHosPrjSdkHandlerV2 = new IdeHosPrjSdkHandlerV2();
        UniSdkInfoHandler sdkHandler = ideHosPrjSdkHandlerV2.getSdkHandler(HosPrjSdkType.OPENHARMONY);
        var localComponents = sdkHandler.getLocalSdks(apiVersion);
        Component nativeComponent = localComponents.get(ComponentPath.NATIVE.value());
        ProgressIndicator globalProgressIndicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        if (globalProgressIndicator != null) {
            globalProgressIndicator.setIndeterminate(true);
        }
        return nativeComponent == null ? Strings.EMPTY : String.valueOf(nativeComponent.getLocation());
    }

    /**
     * Is project china country code boolean.
     *
     * @return the boolean
     */
    public static boolean isProjectChinaCountryCode() {
        String userCountryRegion = CountryRegionSetting.getInstance().getUserCountryRegion();
        return CountryRegionConstants.COUNTRY_CODE_CHINA.equals(userCountryRegion);
    }

    private static boolean hasCjBins(Path dependPath) {
        for (AbiEnum abiEnum : AbiEnum.values()) {
            if (Files.exists(dependPath.resolve("libs").resolve(abiEnum.getAbi()).resolve("cjbins"))) {
                return true;
            }
        }
        return false;
    }

    private static void configOhpmDependsEnv(Project project, Map<String, String> dependsEnvMap) {
        String basePath = project.getBasePath();
        if (StringUtils.isEmpty(basePath)) {
            return;
        }
        Path ohpmPath = Path.of(basePath, "oh_modules", ".ohpm");
        File ohpmDir = ohpmPath.toFile();
        if (!ohpmDir.exists()) {
            return;
        }
        File[] childFiles = ohpmDir.listFiles();
        if (childFiles == null) {
            return;
        }
        for (File childFile : childFiles) {
            String childFileName = childFile.getName();
            if (!childFile.isDirectory() || "oh_modules".equals(childFileName)) {
                continue;
            }
            int splitIndex = childFileName.startsWith("@") ? childFileName.indexOf('@', 1) : childFileName.indexOf('@');
            if (splitIndex == -1) {
                continue;
            }
            String dependName = childFileName.substring(0, splitIndex);
            String[] dependNameArr = dependName.split("\\+");
            dependName = formatEnvName(dependName);

            Path dependPath =
                Path.of(ohpmPath.toString(), childFileName, "oh_modules", String.join(File.separator, dependNameArr));
            File dependFile = Path.of(dependPath.toString(), "src", "main", "cangjie").toFile();
            if (!dependFile.exists() && !hasCjBins(dependPath)) {
                continue;
            }
            dependsEnvMap.put(dependName, dependPath.toString());
        }
    }

    private static void configLocalModulesEnv(ProjectModel projectModel, Project project,
        Map<String, String> dependsEnvMap) {
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return;
        }
        for (ModuleModel moduleModel : moduleModelList) {
            if (ModuleType.ENTRY.toString().equalsIgnoreCase(moduleModel.getModuleType())
                || ModuleType.FEATURE.toString().equalsIgnoreCase(moduleModel.getModuleType())) {
                continue;
            }
            Path packageJsonPath = Paths.get(moduleModel.getModulePath(), PACKAGE_JSON);
            if (PackageType.OHPM.equals(ProjectUtil.getPackageType(moduleModel.getProjectModel()))) {
                packageJsonPath = Paths.get(moduleModel.getModulePath(), OH_PACKAGE_JSON5_FILE);
            }

            JsonObject packageJson5Object = findPsiFileJsonObject(project, packageJsonPath);
            if (packageJson5Object == null) {
                continue;
            }
            String packageName = getPsiJsonString(packageJson5Object, NAME);
            if (StringUtils.isEmpty(packageName)) {
                continue;
            }
            String envKeyName = dependToPlaceHolder(packageName);
            if (dependsEnvMap.containsKey(envKeyName)) {
                continue;
            }
            dependsEnvMap.put(envKeyName, moduleModel.getModulePath());
        }
    }

    private static void addEnvByExtensions(Map<String, String> envMap, ProjectModel projectModel) {
        for (CangjieEnvProvider envProvider : CangjieEnvProvider.ENV_PROVIDER_EXTENSION_LIST.getExtensionList()) {
            Map<String, String> envs = envProvider.collectNeedEnv(projectModel);
            for (Map.Entry<String, String> envItem : envs.entrySet()) {
                String envKey = envItem.getKey();
                if (!envMap.containsKey(envKey)) {
                    envMap.put(envKey, "");
                }
                StringBuilder originalEnv = new StringBuilder(envMap.get(envKey));
                if (!originalEnv.isEmpty()) {
                    originalEnv.append(getEnvSeparator());
                }
                originalEnv.append(envItem.getValue());
                envMap.put(envKey, originalEnv.toString());
            }
        }
    }

    private static String getEnvSeparator() {
        if (SystemInfo.isWindows) {
            return ";";
        }
        return ":";
    }

    private static String getCompileCondition(ProjectModel projectModel) {
        String cfg;
        String apiVersion = projectModel.getFullCompatibleSdkVersion().getValue();
        cfg = "APILevel_level=" + apiVersion;
        Project project = projectModel.getProject();
        if (project == null || StringUtil.isEmpty(project.getBasePath())) {
            LOG.info("Failed to set COMPILE_CONDITION env, due to project is null");
            return cfg;
        }
        Path apiJsonPath =
                Path.of(project.getBasePath(), ".idea", ".deveco", "cangjie", "syscap_api_config.json");
        return String.format("%s,APILevel_syscap=%s", cfg, apiJsonPath);
    }

    private static void addCompileConditionEnv(Map<String, String> dependsEnvMap, ProjectModel projectModel) {
        String productVal = "default";
        HvigorProductV2 product = ProductManager.getInstance().getCurrentProduct(projectModel);
        if (product != null) {
            productVal = product.getName();
        }
        String apiLevelCondition = getCompileCondition(projectModel);
        dependsEnvMap.put("COMPILE_CONDITION", apiLevelCondition + ",product=" + productVal + ",target=default");
        for (ModuleModel model : projectModel.getModuleModelList()) {
            if (!(model instanceof OhosModuleModel) || !FileUtils.isCangjieModule(model)) {
                continue;
            }
            if (ModuleType.HAR.toString().equalsIgnoreCase(model.getModuleType())) {
                continue;
            }
            String targetVal = "default";
            OhosTarget currentTarget = TargetManager.getInstance().getCurrentTarget(model);
            if (currentTarget != null) {
                targetVal = currentTarget.getName();
            }
            dependsEnvMap.put("COMPILE_CONDITION_" + StringUtils.upperCase(model.getModuleName()),
                    apiLevelCondition + ",product=" + productVal + ",target=" + targetVal);
        }
    }

    private static void configMacEnv(Map<String, String> envMap) {
        if (!SystemInfo.isMac) {
            return;
        }
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("xcrun", "--sdk", "macosx", "--show-sdk-path");
            process = processBuilder.start();
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            int exitCode = process.waitFor();
            if (exitCode != 0 || StringUtils.isEmpty(output)) {
                LOG.warn("Failed to get mac SDKROOT env:" + error);
                return;
            }
            envMap.put("SDKROOT", output.split("\n")[0]);
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to get mac SDKROOT env.");
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
