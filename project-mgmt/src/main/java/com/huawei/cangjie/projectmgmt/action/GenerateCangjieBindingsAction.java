/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_PACKAGE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_STATIC_LIBRARY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.DEFAULT_TYPE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPATIBLE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPILE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.DEVICE_TYPES;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.HAS_SKILL;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.IS_INSTALLATION_FREE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_TYPE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.PROJECT_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.VISIBLE;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_PROVIDER_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.CATEGORY;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_API_TYPE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_STAGE_MODE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_UI_SYNTAX;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.KEY_PROJECT;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.MODULE_TYPE_HAR;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.OHOS_TEMPLATE_PROVIDER_ID;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.KEY_ACTION;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.isAtomicService;
import static com.intellij.util.PathUtil.toSystemIndependentName;

import com.huawei.cangjie.projectmgmt.config.ConfigPathHandler;
import com.huawei.cangjie.projectmgmt.sync.dts2cj.Dts2cjCopyManager;
import com.huawei.cangjie.projectmgmt.sync.dts2cj.Dts2cjCopySync;
import com.huawei.cangjie.projectmgmt.ui.CangjieModuleDialog;
import com.huawei.cangjie.projectmgmt.utils.CangjieEnvUtils;
import com.huawei.cangjie.projectmgmt.utils.CangjieTemplateUtils;
import com.huawei.cangjie.projectmgmt.utils.Dts2cjExecUtil;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.build.ohos.util.ModuleUtils;
import com.huawei.deveco.common.ide.InnerToolsLocationUtil;
import com.huawei.deveco.projectmgmt.ohos.template.TemplateRenderClient;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.utils.Category;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.util.CommonConstants;
import com.huawei.deveco.sdkmanager.core.constants.ComponentPath;
import com.huawei.deveco.sdkmanager.core.domain.Component;
import com.huawei.deveco.sdkmanager.hos.common.api.HosPrjSdkType;
import com.huawei.deveco.sdkmanager.hos.common.api.UniSdkInfoHandler;
import com.huawei.deveco.sdkmanager.hos.idea.api.IdeHosPrjSdkHandlerV2;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.system.CpuArch;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The type Generate cangjie arkts wrapper code action.
 *
 * @since 2025 -01-19
 */
public class GenerateCangjieBindingsAction extends AnAction {
    private static final Logger LOGGER = Logger.getInstance(GenerateCangjieBindingsAction.class);

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || (!virtualFile.getName().endsWith(".d.ts") && !virtualFile.getName()
            .endsWith(".d.ets"))) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        String canonicalPath = virtualFile.getCanonicalPath();
        if (canonicalPath == null || !canonicalPath.contains("oh_modules")) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        ProjectModel projectModel = CommonProjectUtil.getProjectModel(project);
        String cangjieSdkDir = SdkUtils.getSdkPath(projectModel);
        if (StringUtils.isEmpty(cangjieSdkDir)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        Path sdkPath = Paths.get(cangjieSdkDir);
        Path disparserPath = getDisparsePath(sdkPath);
        Path dts2cjToolPath = getDts2cjToolPath(sdkPath);
        if (!Files.exists(disparserPath) || !Files.exists(dts2cjToolPath)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        anActionEvent.getPresentation().setVisible(true);
        anActionEvent.getPresentation().setEnabled(isSyncFinished(project));
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        String dirPath = virtualFile.getCanonicalPath();
        if (StringUtils.isEmpty(dirPath)) {
            return;
        }
        List<ModuleModel> modulesList = ModuleUtils.getModulesList(project);
        String defaultModuleName = virtualFile.getName() + "_cj";
        if (!virtualFile.isDirectory()) {
            defaultModuleName = virtualFile.getParent().getName() + "_cj";
        }
        CangjieModuleDialog dialog =
            new CangjieModuleDialog(project, message("dts2cj.enter.module.title"), modulesList, defaultModuleName);
        // If user cancelled action.
        if (!dialog.showAndGet() || !(dialog.getParameters()
            .getOrDefault(CangjieModuleDialog.DEFAULT_MODULE_NAME, null) instanceof String moduleName)) {
            return;
        }
        ProjectModel projectModel = CommonProjectUtil.getProjectModel(project);
        String cangjieSdkDir = SdkUtils.getSdkPath(projectModel);
        if (StringUtils.isEmpty(cangjieSdkDir)) {
            return;
        }
        Path sdkPath = Paths.get(cangjieSdkDir);
        Path disparserPath = getDisparsePath(sdkPath);
        Path dts2cjToolPath = getDts2cjToolPath(sdkPath);
        if (!Files.exists(disparserPath) || !Files.exists(dts2cjToolPath)) {
            return;
        }
        boolean isDirectory = CangjieModuleDialog.ScopeEnum.DIRECTORY.name().equals(
            dialog.getParameters().getOrDefault(CangjieModuleDialog.DEFAULT_SCOPE_NAME, null));
        if (isDirectory) {
            dirPath = virtualFile.getParent().getCanonicalPath();
        }
        String destPath =
            Paths.get(projectModel.getProjectPath(), ".idea", ".deveco", "cangjie", "dts2cj", moduleName).normalize()
                .toString().replaceAll("\\\\", "/");
        deleteTempFiles(destPath);
        List<String> a2cjCommands = generateDts2cjParameters(dirPath, moduleName, disparserPath, destPath, isDirectory);
        addOtherParameters(a2cjCommands, projectModel);
        GeneralCommandLine generalCommandLine =
            getGeneralCommandLine(dts2cjToolPath, projectModel, a2cjCommands, sdkPath);
        doExecuteCommand(modulesList, moduleName, projectModel, Paths.get(destPath), generalCommandLine);
    }

    private void addOtherParameters(List<String> a2cjCommands, ProjectModel projectModel) {
        IdeHosPrjSdkHandlerV2 ideHosPrjSdkHandlerV2 = new IdeHosPrjSdkHandlerV2();
        UniSdkInfoHandler sdkHandler = ideHosPrjSdkHandlerV2.getSdkHandler(HosPrjSdkType.OPENHARMONY);
        var localComponents =
                sdkHandler.getLocalSdks(projectModel.getFullCompileSdkVersion().getValue());
        Component etsComponent = localComponents.get(ComponentPath.ETS.value());
        if (etsComponent == null || etsComponent.getLocation() == null) {
            return;
        }
        Path typeScriptPath =
            etsComponent.getLocation().resolve("build-tools").resolve("ets-loader").resolve("node_modules")
                .resolve("typescript");
        a2cjCommands.add("-r");
        a2cjCommands.add(typeScriptPath.toString().replaceAll("\\\\", "/"));
    }

    private GeneralCommandLine getGeneralCommandLine(Path a2cjToolOption, ProjectModel projectModel,
        List<String> a2cjCommands, Path sdkPath) {
        GeneralCommandLine generalCommandLine =
            new GeneralCommandLine().withExePath(a2cjToolOption.toString())
                .withWorkDirectory(Paths.get(projectModel.getProjectPath()).toFile()).withParameters(a2cjCommands)
                .withEnvironment(CangjieEnvUtils.getProjectEnvs(projectModel.getProject()))
                .withRedirectErrorStream(true);
        String ideNodeLocation = InnerToolsLocationUtil.getIdeNodeLocation();
        Map<String, String> environment = generalCommandLine.getEnvironment();
        if (SystemInfo.isMac) {
            String systemPathEnv = System.getenv("PATH");
            environment.put("PATH", systemPathEnv + ":" + ideNodeLocation + ":" + ideNodeLocation + "/bin:");
            environment.put("DYLD_LIBRARY_PATH", System.getenv("DYLD_LIBRARY_PATH"));
            environment.put("DYLD_FALLBACK_LIBRARY_PATH", System.getenv("DYLD_FALLBACK_LIBRARY_PATH"));
        } else {
            String systemPathEnv = System.getenv("Path");
            environment.put("Path", systemPathEnv + ";" + ideNodeLocation);
        }
        configRuntimeEnv(sdkPath, environment);
        return generalCommandLine;
    }

    private void deleteTempFiles(String destPath) {
        if (StringUtils.isEmpty(destPath)) {
            return;
        }
        Path destDirPath = Paths.get(destPath);
        if (!Files.exists(destDirPath)) {
            return;
        }
        try {
            FileUtils.deleteDirectory(destDirPath.toFile());
        } catch (IOException e) {
            LOGGER.warn(String.format(Locale.ROOT, "delete dts2cj generate files failed: %s", e.getMessage()));
        }
    }

    private void doExecuteCommand(List<ModuleModel> modulesList, String moduleName, ProjectModel projectModel,
        Path destDirPath, GeneralCommandLine generalCommandLine) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Project project = projectModel.getProject();
            boolean isSuccess = new Dts2cjExecUtil(project).runWithConsole(generalCommandLine, moduleName);
            if (isSuccess) {
                if (!hasCjFile(destDirPath)) {
                    NotificationUtil.notifyInfo(message("dts2cj.cangjie.bindings.no.results"), project,
                        NotificationType.ERROR);
                    return;
                }
                Path libraryPath = getLibraryPath(projectModel, modulesList, moduleName);
                Map<Path, Path> copyMap = new HashMap<>();
                copyMap.put(destDirPath, libraryPath.resolve("src").resolve("main").resolve("cangjie"));
                Dts2cjCopyManager.getInstance().addSyncCopy(copyMap, project);
                if (libraryPath.toFile().exists()) {
                    new Dts2cjCopySync().doCopyDts2cjResults(projectModel, false);
                } else {
                    generateLibrary(libraryPath, moduleName, projectModel);
                    LOGGER.info(String.format(Locale.ROOT, "%s already exists, skipping generate cangjie library",
                        libraryPath));
                }
            } else {
                NotificationUtil.notifyInfo(message("dts2cj.generate.cangjie.bindings.failed"), project,
                    NotificationType.ERROR);
            }
        });
    }

    private Path getLibraryPath(ProjectModel projectModel, List<ModuleModel> modulesList, String moduleName) {
        Path libraryPath = Paths.get(projectModel.getProjectPath(), moduleName);
        if (CollectionUtils.isNotEmpty(modulesList)) {
            for (ModuleModel moduleModel : modulesList) {
                if (moduleModel.getModuleName().equals(moduleName)) {
                    libraryPath = Paths.get(moduleModel.getModulePath());
                    break;
                }
            }
        }
        return libraryPath;
    }

    private boolean hasCjFile(Path destDirPath) {
        if (!Files.exists(destDirPath) || !Files.isDirectory(destDirPath)) {
            return false;
        }
        File[] files = destDirPath.toFile().listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        return Arrays.stream(files).anyMatch(file -> file.getName().endsWith(".cj"));
    }

    @NotNull
    private List<String> generateDts2cjParameters(String dirPath, String moduleName, Path disparserPath,
        String destPath, boolean isDirectory) {
        List<String> a2cjCommands = new ArrayList<>();
        a2cjCommands.add("-j");
        a2cjCommands.add(disparserPath.resolve("analysis.js").toString().replaceAll("\\\\", "/"));
        if (isDirectory) {
            a2cjCommands.add("-d");
        } else {
            a2cjCommands.add("-i");
        }
        a2cjCommands.add(dirPath.replaceAll("\\\\", "/"));
        a2cjCommands.add("--lib");
        a2cjCommands.add("--module-name");
        a2cjCommands.add(moduleName);
        a2cjCommands.add("-o");
        a2cjCommands.add(destPath);
        return a2cjCommands;
    }

    private void generateLibrary(Path libraryPath, String moduleName, ProjectModel projectModel) {
        if (StringUtils.isEmpty(moduleName)) {
            LOGGER.warn("module is empty, skipping generate cangjie library");
            return;
        }
        RenderHashMap renderHashMap = new RenderHashMap();
        renderHashMap.put(KEY_ACTION, "CreateLibrary");
        renderHashMap.put(KEY_TEMPLATE_NAME, CANGJIE_STATIC_LIBRARY);
        renderHashMap.put(KEY_TEMPLATE_PROVIDER_NAME, OHOS_TEMPLATE_PROVIDER_ID);
        renderHashMap.put(KEY_PROJECT, projectModel.getProject());
        renderHashMap.put(PROJECT_PATH, toSystemIndependentName(projectModel.getProjectPath()));
        renderHashMap.put(CATEGORY, Category.LIBRARY.value());
        renderHashMap.put(MODULE_NAME, moduleName);
        renderHashMap.put(MODULE_PATH, toSystemIndependentName(libraryPath.toString()));
        renderHashMap.put(CANGJIE_PACKAGE_NAME, moduleName);
        ConfigPathHandler.replaceCjPackageName(renderHashMap);
        renderHashMap.put(DTO_API_TYPE, DTO_STAGE_MODE);
        renderHashMap.put(MODULE_TYPE, MODULE_TYPE_HAR);
        renderHashMap.put(DEVICE_TYPES, "\"" + DEFAULT_TYPE + "\"");
        renderHashMap.put(IS_INSTALLATION_FREE, isAtomicService(projectModel));
        renderHashMap.putIfAbsent(VISIBLE, true);
        renderHashMap.put(HAS_SKILL, false);
        int latestApiVersion = SdkUtils.getLatestApiVersion();
        renderHashMap.put(COMPATIBLE_BASE_API, latestApiVersion);
        renderHashMap.put(COMPILE_BASE_API, latestApiVersion);
        renderHashMap.put(DTO_UI_SYNTAX, "Cangjie");
        CangjieTemplateUtils.setTemplateParams(renderHashMap);
        ApplicationManager.getApplication()
            .executeOnPooledThread(() -> new TemplateRenderClient(renderHashMap).executeRender());
    }

    private Path getDisparsePath(Path sdkPath) {
        return sdkPath.resolve("build-tools").resolve("tools").resolve("config").resolve("dtsparser");
    }

    private Path getDts2cjToolPath(Path sdkPath) {
        String dts2cjToolName = SystemInfo.isWindows ? "hle.exe" : "hle";
        return sdkPath.resolve("build-tools").resolve("tools").resolve("bin").resolve(dts2cjToolName);
    }

    /**
     * cmd命令路径中带空格、()的需要前后加"
     *
     * @param path 路径
     * @return 返回兼容后的路径
     */
    public static String compatiblePathSpaces(@NotNull String path) {
        if (!SystemInfo.isWindows) {
            return path;
        }
        if (path.startsWith(CommonConstants.DOUBLE_QUOTATION) && path.endsWith(CommonConstants.DOUBLE_QUOTATION)) {
            return path;
        }
        return CommonConstants.DOUBLE_QUOTATION + path + CommonConstants.DOUBLE_QUOTATION;
    }

    /**
     * Gets runtime path.
     *
     * @param cjSdkPath the cj sdk path
     * @param env the env
     */
    private void configRuntimeEnv(Path cjSdkPath, Map<String, String> env) {
        Path toolBinPath = cjSdkPath.resolve("build-tools").resolve("tools").resolve("bin");
        Path cjcPath = cjSdkPath.resolve("build-tools").resolve("bin");
        Path toolLibPath = cjSdkPath.resolve("build-tools").resolve("tools").resolve("lib");
        Path runtimeLibPath = cjSdkPath
                .resolve("build-tools").resolve("runtime").resolve("lib").resolve(getRuntimeLib());
        if (SystemInfo.isMac) {
            String cjcEnvs = cjcPath + ":" + toolBinPath + ":";
            env.put("Path", cjcEnvs + env.getOrDefault("Path", Strings.EMPTY));
            String runtimePath = runtimeLibPath + ":" + toolLibPath + ":";
            env.put("DYLD_LIBRARY_PATH", runtimePath + env.getOrDefault("DYLD_LIBRARY_PATH", Strings.EMPTY));
            env.put("DYLD_FALLBACK_LIBRARY_PATH",
                runtimePath + env.getOrDefault("DYLD_FALLBACK_LIBRARY_PATH", Strings.EMPTY));
        } else {
            String cjcEnvs = cjcPath + ";" + toolBinPath + ";" + toolLibPath + ";" + runtimeLibPath + ";";
            env.put("PATH", cjcEnvs + env.getOrDefault("PATH", Strings.EMPTY));
        }
    }

    private String getRuntimeLib() {
        if (SystemInfo.isWindows) {
            return "windows_x86_64_cjnative";
        } else {
            if (CpuArch.isArm64()) {
                return "darwin_aarch64_cjnative";
            } else {
                return "darwin_x86_64_cjnative";
            }
        }
    }
}
