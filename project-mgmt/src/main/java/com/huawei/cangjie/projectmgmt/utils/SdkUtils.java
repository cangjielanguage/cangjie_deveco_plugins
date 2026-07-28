/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import static com.huawei.cangjie.projectmgmt.utils.Constants.API_VERSION_22;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_COMPATIBLE_SDK_KEY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.NEED_OH_CANGJIE_SDK_DIR;
import static com.huawei.deveco.projectmodel.ohos.model.constants.RuntimeOS.HARMONY_OS;

import com.huawei.cangjie.sdkmanager.idea.api.CangjieComponent;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.sdkmanager.core.domain.ApiVersion;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.Strings;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SdkUtils
 *
 * @since 2022-10-18
 */
public class SdkUtils {
    /**
     * VERSION_PATTERN
     */
    public static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+)");

    /**
     * WIN_BAT
     */
    public static final String WIN_BAT = "cmd.exe";

    /**
     * WIN_BAT_OPTION
     */
    public static final String WIN_BAT_OPTION = "/c";

    /**
     * MAC_BASH
     */
    public static final String MAC_BASH = "/bin/zsh";

    /**
     * MAC_BASH_OPTION
     */
    public static final String MAC_BASH_OPTION = "-c";

    private static final Logger LOG = Logger.getInstance(SdkUtils.class);
    private static final String RUNTIME_PATH = "build-tools\\runtime\\lib\\windows_x86_64_cjnative";
    private static final String CPM_PATH = "build-tools\\tools\\bin";
    private static final String CJC_PATH = "build-tools\\bin";
    private static final String TP_PATH = "build-tools\\third_party\\llvm\\lib";
    private static final List<String> envPath =
        new ArrayList<>(Arrays.asList(RUNTIME_PATH, CPM_PATH, CJC_PATH, TP_PATH));

    /**
     * get sdk env path
     *
     * @param projectModel cur projectModel
     * @return the sdk env path
     * @throws IOException IO Exception
     */
    public static String getSdkEnvPath(ProjectModel projectModel) throws IOException {
        String sdkPath = getSdkPath(projectModel);
        if (Strings.isEmpty(sdkPath)) {
            return StringUtil.EMPTY;
        }

        Process process = null;
        try {
            if (FileUtils.isMac()) {
                String envsetupScriptPath = Paths.get(sdkPath, "build-tools", "envsetup.sh").toString();
                String command = "source " + "\"" + envsetupScriptPath + "\"" + " && echo $DYLD_LIBRARY_PATH"
                    + " && echo $DYLD_FALLBACK_LIBRARY_PATH" + " && echo $PATH";
                process = new ProcessBuilder(MAC_BASH, MAC_BASH_OPTION, command).start();
            } else {
                String batPath = Paths.get(sdkPath, "build-tools", "envsetup.bat").toString();
                process = new ProcessBuilder(WIN_BAT, WIN_BAT_OPTION, "\"" + batPath + "\"" + "&&PATH").start();
            }
            try (InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder stringBuffer = new StringBuilder();
                char separatorChar = File.pathSeparatorChar;
                String separatorStr = File.pathSeparator;
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (!stringBuffer.isEmpty() && stringBuffer.charAt(stringBuffer.length() - 1) != separatorChar) {
                        stringBuffer.append(separatorStr);
                    }
                    stringBuffer.append(line);
                }
                return stringBuffer.toString().replace("PATH=", "").replace("DYLD_LIBRARY_PATH=", "")
                    .replace("DYLD_FALLBACK_LIBRARY_PATH=", "");
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * get cangjie sdk path
     *
     * @param projectModel the target projectModel
     * @return cangjie sdk path
     */
    public static String getSdkPath(ProjectModel projectModel) {
        String apiVersion = projectModel.getFullCompileSdkVersion().getValue();
        boolean isHarmony = projectModel.getActiveRuntimeOS() == HARMONY_OS;
        Map<String, CangjieComponent> localSdks = new CangjieIdeaSdkInfoHandler().getLocalSdks(isHarmony, apiVersion);
        CangjieComponent cangjieComponent = localSdks.get(CangjieComponentPath.CANGJIE.value());
        if (cangjieComponent == null) {
            return StringUtil.EMPTY;
        }
        Path cangjieSdkPath = cangjieComponent.getLocation();
        if (cangjieSdkPath == null) {
            return StringUtil.EMPTY;
        }
        return cangjieSdkPath.toString();
    }

    /**
     * check need Oh Cangjie Sdk
     *
     * @param renderParameterMap template map
     */
    public static void needOhCangjieSdk(RenderHashMap renderParameterMap) {
        renderParameterMap.put(NEED_OH_CANGJIE_SDK_DIR, "");
        String projectPath = renderParameterMap.getString("projectPath");
        if (!projectPath.isEmpty()) {
            Path sdkConfigFilePath = Path.of(projectPath, "local.properties");
            if (sdkConfigFilePath.toFile().exists()) {
                // not create project
                String content = FileUtils.readToString(sdkConfigFilePath.toFile());
                if (!content.contains("cangjie.dir")) {
                    renderParameterMap.put(NEED_OH_CANGJIE_SDK_DIR, "need");
                }
            } else {
                // create project
                renderParameterMap.put(NEED_OH_CANGJIE_SDK_DIR, "need");
            }
        }
    }

    /**
     * get cpm path
     *
     * @param cjSdkPath Cangjie sdk path
     * @return string cpm path
     */
    public static String getCpmEnvs(String cjSdkPath) {
        StringBuilder cpmEnvs = new StringBuilder();
        for (String env : envPath) {
            String totalPath = cjSdkPath + env + ";";
            cpmEnvs.append(totalPath);
        }
        return cpmEnvs.toString();
    }

    /**
     * Is config compatible sdk boolean.
     *
     * @return the boolean
     */
    public static boolean isConfigCompatibleSdk() {
        int latestApiVersion = getLatestApiVersion();
        return !(latestApiVersion >= API_VERSION_22 && !hasCompatibleSdkConfig());
    }

    /**
     * Gets latest api version.
     *
     * @return the latest api version
     */
    public static int getLatestApiVersion() {
        int latestApiVersion = API_VERSION_22;
        try {
            Field field = TemplateConstant.class.getDeclaredField("LATEST_API_VERSION");
            field.setAccessible(true);
            Object obj = field.get(null);
            if (obj instanceof ApiVersion latestApiVersionObject) {
                latestApiVersion = latestApiVersionObject.getMajor();
            } else if (obj instanceof Integer latestApiVersionObject) {
                latestApiVersion = latestApiVersionObject;
            } else {
                LOG.warn("Unsupported LATEST_API_VERSION type.");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.warn("Failed to obtain the latest API version.");
        }
        return latestApiVersion;
    }

    /**
     * Is config compatible sdk boolean.
     *
     * @return the boolean
     */
    public static boolean hasCompatibleSdkConfig() {
        return StringUtils.isNotBlank(System.getProperty(CANGJIE_COMPATIBLE_SDK_KEY));
    }

    /**
     * get cjc version
     *
     * @param compileVersion compileVersion
     * @return cjc version
     */
    public static String getCjcVersion(int compileVersion) {
        Map<String, CangjieComponent> localSdks = new CangjieIdeaSdkInfoHandler().getLocalSdks(true, compileVersion);
        CangjieComponent cangjieComponent = localSdks.get(CangjieComponentPath.CANGJIE.value());
        if (cangjieComponent == null) {
            LOG.warn("Failed to obtain the cjc version, sdk component not found.");
            return Constants.CUR_CJC_VERSION;
        }
        Path cangjieSdkPath = cangjieComponent.getLocation();
        if (cangjieSdkPath == null) {
            LOG.warn("Failed to obtain the cjc version, sdk path not found.");
            return Constants.CUR_CJC_VERSION;
        }
        String sdkPath = cangjieSdkPath.toString();
        if (Strings.isEmpty(sdkPath)) {
            LOG.warn("Failed to obtain the cjc version, sdk path not found.");
            return Constants.CUR_CJC_VERSION;
        }
        Process process = null;
        try {
            if (FileUtils.isMac()) {
                String batPath = Paths.get(sdkPath, "build-tools", "envsetup.sh").toString();
                process = new ProcessBuilder(MAC_BASH, MAC_BASH_OPTION,
                    "source " + "\"" + batPath + "\"" + "&&echo $DYLD_LIBRARY_PATH" + "&&cjc -v").start();
            } else {
                String batPath = Paths.get(sdkPath, "build-tools", "envsetup.bat").toString();
                process = new ProcessBuilder(WIN_BAT, WIN_BAT_OPTION, "\"" + batPath + "\"" + "&&cjc -v").start();
            }
        } catch (IOException exception) {
            LOG.warn("Failed to obtain the cjc version, execute cjc failed.");
            return Constants.CUR_CJC_VERSION;
        }
        try (InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            Matcher matcher = VERSION_PATTERN.matcher(stringBuffer.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
            return Constants.CUR_CJC_VERSION;
        } catch (IOException exception) {
            LOG.warn("Failed to obtain the cjc version, get cjc version failed.");
            return Constants.CUR_CJC_VERSION;
        } finally {
            process.destroy();
        }
    }
}
