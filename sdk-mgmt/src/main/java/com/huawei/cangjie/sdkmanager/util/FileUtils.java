/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import com.huawei.cangjie.sdkmanager.constants.SdkConstants;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;

import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * FileUtils
 *
 * @since 2025-02-25
 */
public class FileUtils {
    /**
     * definition print log info instance
     */
    private static final Logger LOG = Logger.getInstance(FileUtils.class);

    /**
     * check is Cangjie Project
     *
     * @param projectModel projectModel
     * @return isCangjieProject
     */
    public static boolean isCangjieProject(ProjectModel projectModel) {
        if (projectModel == null) {
            return false;
        }
        List<ModuleModel> modelsList = projectModel.getModuleModelList();
        for (ModuleModel model : modelsList) {
            if ((model instanceof OhosModuleModel)
                    && FileUtils.isCangjieModule((OhosModuleModel) model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check is cangjie module
     *
     * @param module module
     * @return is cangjie module
     */
    public static boolean isCangjieModule(ModuleModel module) {
        ProjectModel projectModel = module.getProjectModel();
        if (projectModel == null) {
            return false;
        }
        Optional<JsonObject> buildOption = getBuildOption(projectModel.getProject(), module);
        return buildOption.filter(jsonObject ->
                PsiJsonFileUtil.getPsiJsonObject(jsonObject, SdkConstants.CANGJIE_OPTIONS) != null).isPresent();
    }

    /**
     * get build option
     *
     * @param project Project
     * @param module ModuleModel
     * @return Optional<JsonObject>
     */
    public static Optional<JsonObject> getBuildOption(Project project, ModuleModel module) {
        if (project == null || module == null) {
            return Optional.empty();
        }
        Path buildOptionPath = Paths.get(module.getModulePath(), SdkConstants.BUILD_PROFILE_JSON5);
        if (!buildOptionPath.toFile().exists()) {
            return Optional.empty();
        }
        JsonObject buildProfileJsonObject = PsiJsonFileUtil.findPsiFileJsonObject(project, buildOptionPath);
        if (buildProfileJsonObject == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PsiJsonFileUtil.getPsiJsonObject(buildProfileJsonObject, SdkConstants.BUILD_OPTION));
    }

    /**
     * read file content
     *
     * @param file file
     * @return file content
     */
    public static String readToString(File file) {
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        String result = "";
        try (InputStream in = Files.newInputStream(file.toPath())) {
            if (in.read(fileContent) > 0) {
                result = new String(fileContent, Charset.defaultCharset());
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        return result;
    }

    /**
     * check the os is mac
     *
     * @return is mac
     */
    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    /**
     * check is absolute path
     *
     * @param path target path
     * @return is absolute path
     */
    public static boolean isAbsolutePath(String path) {
        if (path.startsWith("/") || path.indexOf(":") > 0) {
            return true;
        }
        return false;
    }
}
