/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.OH_PACKAGE_JSON5_FILE;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.HVIGOR;

import com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider;
import com.huawei.cangjie.projectmgmt.utils.DependencyHandlerUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.util.ProjectUtil;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;
import com.huawei.hvigor.utils.HvigorPathUtil;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Upgrade the plugin project to an integrated project.
 *
 * @since 2025 -07-24
 */
public class CangjieIntegrationUpdateImpl implements CangjieUpdateProvider {
    private static final Logger LOGGER = Logger.getInstance(CangjieIntegrationUpdateImpl.class);

    private static final Pattern OLD_IMPORT_REGEX = Pattern.compile(
        "import\\s*\\{\\s*(appTasks|ha[pr]Tasks)\\s*}\\s*from\\s*['\"]@ohos/cangjie-build-support['\"]\\s*;?",
        Pattern.MULTILINE);

    private static final String NEW_PATH = "@ohos/hvigor-ohos-plugin";

    private static final String DEPENDENCIES_KEY = "dependencies";

    private static final String CANGJIE_BUILD_KEY = "@ohos/cangjie-build-support";

    private static final String LOCAL_DEPENDENCIES_PREFIX = "file:";

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        Project project = projectModel.getProject();
        Path projectHvigorConfigPath = HvigorPathUtil.getProjectHvigorConfigPath(project);
        Optional<JsonObject> packageJsonOption = getPackageJson(project, projectHvigorConfigPath);
        if (packageJsonOption.isEmpty()) {
            return false;
        }
        JsonObject dependenciesObject = PsiJsonFileUtil.getPsiJsonObject(packageJsonOption.get(), DEPENDENCIES_KEY);
        if (dependenciesObject == null) {
            return false;
        }
        boolean isContainsCangjie =
            ReadAction.compute(() -> dependenciesObject.findProperty(CANGJIE_BUILD_KEY) != null);
        if (isContainsCangjie) {
            return true;
        }
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return false;
        }
        for (ModuleModel moduleModel : moduleModelList) {
            Path moduleHvigorFilePath = HvigorPathUtil.getModuleHvigorFilePath(moduleModel.getModulePath());
            if (!moduleHvigorFilePath.toFile().exists()) {
                continue;
            }
            VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(moduleHvigorFilePath.toString());
            if (vFile == null || vFile.isDirectory()) {
                continue;
            }
            try {
                String original = new String(vFile.contentsToByteArray(), StandardCharsets.UTF_8);
                if (OLD_IMPORT_REGEX.matcher(original).find()) {
                    return true;
                }
            } catch (IOException e) {
                LOGGER.warn("Read %s failed".formatted(moduleHvigorFilePath));
            }
        }
        return false;
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.remove.build.update.text"), Collections.EMPTY_SET);
        return updateMsgs;
    }

    @Override
    public boolean doUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        Project project = projectModel.getProject();
        // update project/hvigor
        boolean isUpdateConfigSuccess = updateHvigorConfig(project);
        // update project/hvigorfile.ts
        boolean isUpdateProjectHvigorSuccess = updateProjectHvigorFile(project);
        // update module/hvigorfile.ts
        boolean isUpdateModuleHvigorSuccess = updateModuleHvigorFile(projectModel);
        return isUpdateConfigSuccess && isUpdateProjectHvigorSuccess && isUpdateModuleHvigorSuccess;
    }

    private boolean updateHvigorConfig(Project project) {
        // delete dependencies @ohos/cangjie-build-support
        Path projectHvigorConfigPath = HvigorPathUtil.getProjectHvigorConfigPath(project);
        Optional<JsonObject> packageJsonOpt = getPackageJson(project, projectHvigorConfigPath);
        if (packageJsonOpt.isEmpty()) {
            return false;
        }
        JsonObject packageJsonRootObj = packageJsonOpt.get();
        JsonObject dependenciesObject = PsiJsonFileUtil.getPsiJsonObject(packageJsonRootObj, DEPENDENCIES_KEY);
        if (dependenciesObject == null) {
            return true;
        }
        JsonProperty buildObj = ReadAction.compute(() -> dependenciesObject.findProperty(CANGJIE_BUILD_KEY));
        if (buildObj == null) {
            return true;
        }
        String buildSupportPath =
            StringUtil.trim(PsiJsonFileUtil.getPsiJsonString(dependenciesObject, CANGJIE_BUILD_KEY));
        WriteCommandAction.runWriteCommandAction(project, () -> {
            deletePropWithComma(buildObj);
            CodeStyleManager.getInstance(project).reformat(packageJsonRootObj);
        });
        String jsonText = ReadAction.compute(packageJsonRootObj::getText);
        DependencyHandlerUtil.writeTextToFile(projectHvigorConfigPath.toFile(), jsonText, project);
        // delete cangjie-build-support.tgz
        if (StringUtils.isBlank(buildSupportPath)) {
            return true;
        }
        String rawPath = StringUtils.removeStart(buildSupportPath.trim(), LOCAL_DEPENDENCIES_PREFIX);
        Path tgzPath = Paths.get(rawPath);
        if (!tgzPath.isAbsolute()) {
            tgzPath = Paths.get(ProjectUtil.getProjectPath(project), HVIGOR).resolve(tgzPath).normalize();
        }
        try {
            return Files.deleteIfExists(tgzPath);
        } catch (IOException e) {
            LOGGER.warn(String.format(Locale.ENGLISH, "Delete cangjie-build-support.tgz file failed: %s", tgzPath));
            return false;
        }
    }

    private Optional<JsonObject> getPackageJson(Project project, Path hvigorConfigPath) {
        if (!hvigorConfigPath.toFile().exists()) {
            return Optional.empty();
        }
        JsonObject packageJsonObj =
            PsiJsonFileUtil.copyPsiJsonObject(PsiJsonFileUtil.findPsiFileJsonObject(project, hvigorConfigPath));
        if (packageJsonObj == null) {
            LOGGER.warn(String.format(Locale.ENGLISH, "find file %s error.", OH_PACKAGE_JSON5_FILE));
            return Optional.empty();
        }
        return Optional.of(packageJsonObj);
    }

    private boolean updateProjectHvigorFile(Project project) {
        Path projectHvigorFilePath = HvigorPathUtil.getProjectHvigorFilePath(project);
        if (!projectHvigorFilePath.toFile().exists()) {
            return true;
        }
        return replaceImport(project, projectHvigorFilePath.toString());
    }

    private boolean updateModuleHvigorFile(ProjectModel projectModel) {
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return true;
        }
        boolean isSuccess = true;
        for (ModuleModel moduleModel : moduleModelList) {
            Path moduleHvigorFilePath = HvigorPathUtil.getModuleHvigorFilePath(moduleModel.getModulePath());
            if (!moduleHvigorFilePath.toFile().exists()) {
                continue;
            }
            isSuccess = isSuccess && replaceImport(projectModel.getProject(), moduleHvigorFilePath.toString());
        }
        return isSuccess;
    }

    /**
     * Replace cangjie-build-support with hvigor-ohos-plugin.
     *
     * @param project IntelliJ Project
     * @param hvigorFilePath hvigorfile.ts 绝对路径
     * @return boolean
     */
    private boolean replaceImport(Project project, String hvigorFilePath) {
        VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(hvigorFilePath);
        if (vFile == null || vFile.isDirectory()) {
            return false;
        }
        try {
            String original = new String(vFile.contentsToByteArray(), StandardCharsets.UTF_8);
            String replaced = buildReplacedContent(original);
            if (original.equals(replaced)) {
                return true;
            }
            WriteCommandAction.writeCommandAction(project)
                .run(() -> vFile.setBinaryContent(replaced.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            LOGGER.warn("Update of %s failed".formatted(hvigorFilePath));
            return false;
        }
        return true;
    }

    private String buildReplacedContent(@NotNull String text) {
        Matcher m = OLD_IMPORT_REGEX.matcher(text);
        if (!m.find()) {
            return text;
        }
        // hapTasks / harTasks / appTasks
        String taskName = m.group(1);
        String newImport = "import { " + taskName + " } from '" + NEW_PATH + "';";
        return m.replaceFirst(Matcher.quoteReplacement(newImport));
    }

    private static void deletePropWithComma(@NotNull JsonProperty prop) {
        PsiElement toDeleteStart = prop;
        PsiElement toDeleteEnd = prop;
        // Check if there is a comma at the end.
        PsiElement next = PsiTreeUtil.skipWhitespacesForward(prop);
        if (next != null && ",".equals(next.getText())) {
            toDeleteEnd = next;
        } else {
            PsiElement prev = PsiTreeUtil.skipWhitespacesBackward(prop);
            if (prev != null && ",".equals(prev.getText())) {
                toDeleteStart = prev;
            }
        }
        prop.getParent().deleteChildRange(toDeleteStart, toDeleteEnd);
    }
}
