/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync;

import static com.huawei.cangjie.projectmgmt.extend.toml.CJPMTomlEncoder.INTENT_SPACE;
import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.NEW_LINE;
import static com.huawei.deveco.build.ohos.actions.ActionUtil.COMPILE_BUILD_LISTENER;

import com.huawei.cangjie.projectmgmt.extend.toml.CJPMTomlEncoder;
import com.huawei.cangjie.projectmgmt.extend.toml.Toml;
import com.huawei.cangjie.projectmgmt.schema.CjBuildProfileSchemaProviderFactory;
import com.huawei.cangjie.projectmgmt.trace.TraceKind;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.cangjie.projectmgmt.utils.CangjieEnvUtils;
import com.huawei.cangjie.projectmgmt.utils.CangjieModulePathType;
import com.huawei.cangjie.projectmgmt.utils.CangjieTemplateUtils;
import com.huawei.cangjie.projectmgmt.utils.Constants;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.build.ohos.actions.ActionUtil;
import com.huawei.deveco.build.ohos.api.HvigorConfigChecker;
import com.huawei.deveco.build.ohos.service.HvigorParamsBuilder;
import com.huawei.deveco.build.ohos.service.HvigorService;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.sync.syncinterface.PreSync;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;
import com.huawei.deveco.res.ohos.json.profile.schema.BuildProfileSchemaProviderFactory;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CangjieProjectSyncImpl
 *
 * @since 2024/03/14
 */
public class CangjieProjectSyncImpl implements PreSync {
    private static final Logger LOG = Logger.getInstance(CangjieProjectSyncImpl.class);

    private static final List<String> GIT_IGNORE_FILES =
        List.of("**/cj_res", "**/*.cj.macrocall", "**/ability_mainability_entry.cj", "**/module_**_entry.cj",
            "**/IDL_Dependencies_List~", "**/build-script-cache");

    private static final List<String> MODULE_GIT_IGNORE_FILES =
        List.of("**/IDL_Dependencies_List~", "**/build-script-cache");

    private static final String CJ_MATCHER = "**/*.cj";

    private Map<String, List<String>> tomlPathMap = new HashMap<>();

    @Override
    public void checkStatus(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        unregisterSchema(projectModel);
        List<ModuleModel> modelsList = projectModel.getModuleModelList();
        boolean hasCangjieModule = false;
        for (ModuleModel model : modelsList) {
            if ((model instanceof OhosModuleModel)
                    && FileUtils.isCangjieModule((OhosModuleModel) model)) {
                hasCangjieModule = true;
                break;
            }
        }
        if (!hasCangjieModule) {
            return;
        }
        createCache(projectModel);
        FileUtils.removeStdxAttribute(projectModel);
        doReformatToml(syncRequest, modelsList, projectModel.getProject());
        generateCangjieResourceFile(projectModel);
        CangjieEnvUtils.configDependsEnv(projectModel);
        addCangjieGitIgnore(projectModel);
        addModuleGitIgnore(projectModel);
        if (syncRequest == SyncRequest.OPEN_PROJECT) {
            TraceUtils.trace(TraceKind.CJ_OPEN_PROJECT);
        }
    }

    private void doReformatToml(@NonNull SyncRequest syncRequest, List<ModuleModel> modelsList, Project project) {
        if (CollectionUtils.isEmpty(modelsList)) {
            return;
        }
        List<CangjieModulePathType> pathTypes =
            List.of(CangjieModulePathType.MAIN, CangjieModulePathType.OHOS_TEST, CangjieModulePathType.LOCAL_TEST);
        List<String> tomlList = new ArrayList<>();
        List<String> oldTomlList =
            tomlPathMap.getOrDefault(project.getLocationHash(), Collections.emptyList());
        if (SyncRequest.CREATE_PROJECT.equals(syncRequest) || SyncRequest.ADD_MODULE.equals(syncRequest)
            || SyncRequest.ADD_ABILITY.equals(syncRequest)) {
            for (ModuleModel moduleModel : modelsList) {
                for (CangjieModulePathType pathType : pathTypes) {
                    Path tomlPath = FileUtils.getRealCjpmFilePath(moduleModel, pathType);
                    if (!tomlPath.toFile().exists()) {
                        continue;
                    }
                    tomlList.add(tomlPath.toString());
                    if (oldTomlList.contains(tomlPath.toString())) {
                        continue;
                    }
                    Optional<Toml> moduleTomlOpt;
                    try {
                        moduleTomlOpt = new Toml().read(tomlPath.toFile());
                    } catch (IllegalStateException e) {
                        continue;
                    }
                    if (moduleTomlOpt.isEmpty()) {
                        continue;
                    }
                    if (isAlreadyFormatted(tomlPath.toFile())) {
                        continue;
                    }
                    try {
                        CJPMTomlEncoder encoder = new CJPMTomlEncoder(moduleTomlOpt.get());
                        encoder.rewriteAll(tomlPath.toFile());
                    } catch (IOException e) {
                        LOG.warn("Error writing TOML file path: " + tomlPath + ", reason" + e.getMessage());
                    }
                }
            }
        } else {
            for (ModuleModel moduleModel : modelsList) {
                for (CangjieModulePathType pathType : pathTypes) {
                    Path tomlPath = FileUtils.getRealCjpmFilePath(moduleModel, pathType);
                    if (!tomlPath.toFile().exists()) {
                        continue;
                    }
                    tomlList.add(tomlPath.toString());
                }
            }
        }
        tomlPathMap.put(project.getLocationHash(), tomlList);
    }

    private void generateCangjieResourceFile(ProjectModel projectModel) {
        Project project = projectModel.getProject();
        if (project == null) {
            return;
        }
        HvigorParamsBuilder builder = HvigorParamsBuilder.newBuilder().withModuleMode()
                .withBuildMode(ActionUtil.getCurrentBuildModeName(project));
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        for (ModuleModel model : moduleModelList) {
            if (!(model instanceof OhosModuleModel)
                    || !FileUtils.isCangjieModule((OhosModuleModel) model)) {
                continue;
            }
            builder.withModuleNameAndTargets(model.getModuleName(), "default");
        }
        builder.withTaskName("SyncCangjieResource");

        boolean shouldColdStart = false;
        ProjectModel tempProjectModel = ProjectModelManager.getInstance().getTargetProjectModel(project);
        if (tempProjectModel != null) {
            shouldColdStart = HvigorConfigChecker.hasNeedDownloadPack(tempProjectModel);
        }
        ProcessListener listener = new ProcessListener() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                COMPILE_BUILD_LISTENER.buildEnd(project, event.getExitCode() == 0);
            }
        };
        COMPILE_BUILD_LISTENER.buildStart(project);
        HvigorService.getHvigorRunManager(project, builder.build(), "SyncCangjieResource")
                .shouldColdStart(shouldColdStart)
                .withProcessListeners(listener)
                .execute();
    }

    private void addCangjieGitIgnore(ProjectModel projectModel) {
        Path gitIgnorePath = Path.of(projectModel.getProjectPath(), ".gitignore");
        doAddGitIgnore(gitIgnorePath, GIT_IGNORE_FILES);

        // CJLint支持code-linter.json5配置
        // 判断code-linter.json5中是否已添加仓颉相关修改
        try {
            checkIfNeedNotifyAddCangjieConfig(projectModel);
        } catch (IOException exception) {
            LOG.warn(exception.getMessage());
        }
    }

    private void checkIfNeedNotifyAddCangjieConfig(ProjectModel projectModel) throws IOException {
        boolean hasCJConfigInCodeLinterJson = false;
        Path jsonPath = Path.of(projectModel.getProjectPath(), "code-linter.json5");
        File jsonFile = jsonPath.toFile().getCanonicalFile();
        if (!jsonFile.exists()) {
            return;
        }
        hasCJConfigInCodeLinterJson =
                CangjieTemplateUtils.checkCJConfigInCodeLinterJson(projectModel.getProjectPath());
        if (hasCJConfigInCodeLinterJson) {
            return;
        }
        // code-linter.json5中未添加仓颉相关修改，弹窗提示用户，确认是否需要修改
        String title = "Tip";
        Project project = projectModel.getProject();
        NotificationAction updateAction =
            NotificationAction.create(message("upgrade.code.linter.json.add.cangjie.yes"),
                    (actionEvent, notification) -> {
                ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        addCangjieConfig(jsonFile, jsonPath, project);
                    });
                }, message("upgrade.code.linter.json.checking"), true, project);
            });
        NotificationUtil.notifyAddCodeLinterConfigExpireInfo(
                title, message("upgrade.code.linter.json.add.cangjie"),
                project, NotificationType.INFORMATION, updateAction);
    }

    /**
     * add Cangjie config to files in code-linter.json5
     *
     * @param jsonFile code-linter.json5 file
     * @param jsonPath code-linter.json5 path
     * @param project project
     */
    private void addCangjieConfig(File jsonFile, Path jsonPath, Project project) {
        if (project == null) {
            return;
        }
        if (jsonFile.exists()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                JsonObject linterJsonObject = PsiJsonFileUtil.findPsiFileJsonObject(project, jsonPath);
                if (linterJsonObject == null) {
                    return;
                }
                JsonArray files = PsiJsonFileUtil.getPsiJsonArray(linterJsonObject, "files");
                if (files == null) {
                    return;
                }
                JsonElementGenerator generator = new JsonElementGenerator(project);
                JsonValue cjMatcher = generator.createStringLiteral(CJ_MATCHER);
                PsiJsonFileUtil.addJsonElementToJsonArray(files, cjMatcher, project);
            });
            com.huawei.deveco.projectmgmt.ohos.utils.FileUtils.reformatFile(jsonFile, project);
        } else {
            LOG.warn("Can't find json file: " + jsonPath);
        }
        NotificationUtil.clearAddCodeLinterConfigNotifications();
    }

    private void addModuleGitIgnore(ProjectModel projectModel) {
        List<ModuleModel> modelsList = projectModel.getModuleModelList();
        for (ModuleModel model : modelsList) {
            if (!(model instanceof OhosModuleModel)
                || !FileUtils.isCangjieModule((OhosModuleModel) model)) {
                continue;
            }
            Path gitIgnorePath = Path.of(model.getModulePath(), ".gitignore");
            doAddGitIgnore(gitIgnorePath, MODULE_GIT_IGNORE_FILES);
        }
    }

    private void doAddGitIgnore(Path gitIgnorePath, List<String> moduleGitIgnoreFiles) {
        if (!gitIgnorePath.toFile().exists()) {
            return;
        }
        StringBuilder content = new StringBuilder(FileUtils.readToString(gitIgnorePath.toFile()));
        for (String file : moduleGitIgnoreFiles) {
            if (!content.toString().contains(file)) {
                content.append(NEW_LINE).append(file);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(gitIgnorePath.toFile())) {
            byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unregisterSchema(ProjectModel projectModel) {
        ExtensionPoint<@NotNull JsonSchemaProviderFactory> point = JsonSchemaProviderFactory.EP_NAME.getPoint();
        // Get the registered implementation and remove it
        boolean isChina = CangjieEnvUtils.isProjectChinaCountryCode();
        for (JsonSchemaProviderFactory implementation : point.getExtensions()) {
            if (!(implementation instanceof BuildProfileSchemaProviderFactory
                || implementation instanceof CjBuildProfileSchemaProviderFactory)) {
                continue;
            }
            List<JsonSchemaFileProvider> providers = implementation.getProviders(projectModel.getProject());
            if (providers.size() != 3) {
                continue;
            }
            if (isChina && implementation instanceof BuildProfileSchemaProviderFactory) {
                point.unregisterExtension(BuildProfileSchemaProviderFactory.class);
                break;
            }
            if (!isChina && implementation instanceof CjBuildProfileSchemaProviderFactory) {
                point.unregisterExtension(CjBuildProfileSchemaProviderFactory.class);
                break;
            }
        }
    }

    private void createCache(ProjectModel projectModel) {
        if (!SystemInfo.isWindows) {
            return;
        }
        File ideaDir = new File(projectModel.getProjectPath(), Constants.DOT_IDEA_DIR);
        boolean isIdeaCreated = ideaDir.mkdir();
        if (!isIdeaCreated) {
            LOG.info("Failed to create the idea folder.");
        }
        File devecoDir = new File(ideaDir, Constants.DOT_DEVECO_DIR);
        boolean isCreated = devecoDir.mkdir();
        if (!isCreated) {
            LOG.info("Failed to create the deveco folder.");
        }
        File cangjieDir = new File(devecoDir, Constants.CANGJIE_CACHE_DIR);
        isCreated = cangjieDir.mkdir();
        if (!isCreated) {
            LOG.info("Failed to create the cangjie folder.");
        }
        File cacheDir = new File(cangjieDir, Constants.CACHE_DIR);
        isCreated = cacheDir.mkdir();
        if (!isCreated) {
            LOG.info("Failed to create the .cache folder.");
        }
        if (!cacheDir.exists()) {
            return;
        }
        try {
            Files.setAttribute(cacheDir.toPath(), "dos:hidden", true);
        } catch (IOException exception) {
            LOG.info("Failed to hide the .cache folder.");
        }
    }

    /**
     * Is package name formatted boolean.
     *
     * @param file the file
     * @return the boolean
     */
    private boolean isAlreadyFormatted(File file) {
        String content = FileUtils.readToString(file);
        if (content.isEmpty()) {
            return true;
        }
        String[] lines = content.split("\n|\r\n|\r");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("name")) {
                return line.startsWith(INTENT_SPACE);
            }
        }
        return true;
    }
}
