/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action;

import static com.huawei.cangjie.projectmgmt.utils.Constants.OH_PACKAGE_JSON5_FILE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_STAGE_MODE;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;

import com.huawei.cangjie.projectmgmt.utils.DependencyHandlerUtil;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.build.ohos.service.HvigorParamsBuilder;
import com.huawei.deveco.build.ohos.service.HvigorService;
import com.huawei.deveco.build.ohos.util.ModuleUtils;
import com.huawei.deveco.build.ohos.util.NpmInstallUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.OhosSyncInvoker;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The type Generate cangjie idl action.
 *
 * @since 2024 -07-10
 */
public class GenerateCangjieIdlAction extends AnAction {
    private static final Logger LOGGER = Logger.getInstance(GenerateCangjieIdlAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || !virtualFile.getName().endsWith(".cj")) {
            return;
        }
        List<ModuleModel> selectedModulesList =
            ModuleUtils.getSelectedModulesList(project, new VirtualFile[]{virtualFile});
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (!NpmInstallUtil.doOhpmInstallAll(project)) {
                return;
            }
            List<ModuleModel> executeModules = getSelectedCangjieModules(selectedModulesList);
            try {
                LOGGER.info("start refresh virtual file");
                this.refreshProjectVirtualFiles(project);
                HvigorParamsBuilder makeModuleBuilder = ModuleUtils.getOhosTaskCommond(project, executeModules);
                Set<String> moduleParams = new HashSet<>();
                Set<String> configurationNames = new HashSet<>();
                executeModules.stream().forEach(module -> {
                    moduleParams.add(module.getModuleName() + "@default");
                    configurationNames.add(module.getModuleName() + ":GenerateCangjieInteropApi");
                });
                makeModuleBuilder.withParam("module", String.join(",", moduleParams))
                    .withTaskName("GenerateCangjieInteropApi");
                HvigorService.getHvigorRunManager(project, makeModuleBuilder.build(),
                    String.join(",", configurationNames)).executeSyncWithConsole();
                this.refreshProjectVirtualFiles(project);
                LOGGER.info("finish refresh virtual file");
                addOhPackageJsonDepend(project, executeModules);
            } catch (InvalidVirtualFileAccessException e) {
                LOGGER.warn(e.getMessage());
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || !virtualFile.getName().endsWith(".cj")) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }

        // check module api type and project api version
        // check is contain cangjie
        List<ModuleModel> selectedModulesList =
            ModuleUtils.getSelectedModulesList(project, new VirtualFile[]{virtualFile});
        if (CollectionUtils.isEmpty(getSelectedCangjieModules(selectedModulesList))) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        anActionEvent.getPresentation().setEnabled(isSyncFinished(project));
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private List<ModuleModel> getSelectedCangjieModules(List<ModuleModel> selectedModules) {
        if (CollectionUtils.isEmpty(selectedModules)) {
            return Collections.emptyList();
        }
        return selectedModules.stream().filter(
                moduleModel -> DTO_STAGE_MODE.equals(moduleModel.getApiType()) && FileUtils.isSupportApiVersion(
                    moduleModel.getProjectModel().getFullCompatibleSdkVersion().getMajor())
                        && FileUtils.isCangjieModule(moduleModel))
            .collect(Collectors.toList());
    }

    private void refreshProjectVirtualFiles(Project project) {
        if (project == null) {
            return;
        }
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return;
        }
        VirtualFile projectBaseDir = LocalFileSystem.getInstance().findFileByPath(projectBasePath);
        if (projectBaseDir == null) {
            return;
        }
        LocalFileSystem.getInstance().refreshFiles(Collections.singletonList(projectBaseDir), true, true, null);
    }

    private static void addOhPackageJsonDepend(Project project, List<ModuleModel> executeModules) {
        AtomicBoolean isAddDepend = new AtomicBoolean(false);
        executeModules.forEach(module -> {
            Path ohPackageJsonPath = Paths.get(module.getModulePath(), OH_PACKAGE_JSON5_FILE);
            if (!Files.exists(ohPackageJsonPath)) {
                return;
            }
            if (!(module instanceof OhosModuleModel)) {
                return;
            }
            String srcRootPath = FileUtils.getCangjieSrcRootPath((OhosModuleModel) module, false);
            Path interopApiPath = Paths.get(srcRootPath, "ark_interop_api");
            Path idlDependFile = Paths.get(srcRootPath, "IDL_Dependencies_List~");
            Path apiDts = interopApiPath.resolve("ark_interop_api.d.ts");
            Path apiJson5 = interopApiPath.resolve(OH_PACKAGE_JSON5_FILE);
            boolean isInteropApiExist = Files.exists(interopApiPath) && Files.exists(apiDts) && Files.exists(apiJson5);
            boolean isIdlDependFileExist = Files.exists(idlDependFile);
            boolean isNeedAddDepend =
                FileUtils.isContainEtsModule((OhosModuleModel) module) && (isInteropApiExist || isIdlDependFileExist);
            if (isNeedAddDepend) {
                Path relativizePath = Paths.get(module.getModulePath()).relativize(interopApiPath);
                Map<String, String> apiDepends = new HashMap<>();
                if (isInteropApiExist) {
                    apiDepends.put("libark_interop_api.so", "file:" + relativizePath.toString().replace("\\", "/"));
                }
                if (isIdlDependFileExist) {
                    readIdlDependMap(idlDependFile, apiDepends, module.getModulePath(), srcRootPath);
                }
                if (DependencyHandlerUtil.updateModuleDependencies(project, module, apiDepends)) {
                    isAddDepend.set(true);
                }
            }
        });
        if (isAddDepend.get()) {
            OhosSyncInvoker.getInstance().doSync(project, SyncRequest.ACTIVE_TRIGGERING);
        }
    }

    private static void readIdlDependMap(Path idlDependFile, Map<String, String> dependenciesMap, String modulePath,
        String srcRootPath) {
        try {
            List<String> lines = Files.readAllLines(idlDependFile);
            Path moduleRootPath = Paths.get(modulePath);
            for (String line : lines) {
                String[] parts = line.trim().split(",");
                if (parts.length < 2) {
                    continue;
                }
                String soName = parts[0].trim();
                String soDirPath = parts[1].trim();
                if (StringUtils.isEmpty(soName) || StringUtils.isEmpty(soDirPath)) {
                    continue;
                }
                Path soNorDirPath = Paths.get(srcRootPath, soDirPath).normalize();
                if (Files.notExists(soNorDirPath) || Files.notExists(soNorDirPath.resolve("Index.d.ts"))
                    || Files.notExists(soNorDirPath.resolve(OH_PACKAGE_JSON5_FILE))) {
                    continue;
                }
                Path relativizePath = moduleRootPath.relativize(soNorDirPath);
                dependenciesMap.put(soName, "file:" + relativizePath.toString().replace("\\", "/"));
            }
        } catch (IOException e) {
            LOGGER.warn("Read IDL_Dependencies_List~ file failed.");
        }
    }
}
