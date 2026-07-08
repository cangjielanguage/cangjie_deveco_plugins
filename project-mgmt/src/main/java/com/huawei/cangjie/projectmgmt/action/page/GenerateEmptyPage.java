/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action.page;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_DYNAMIC_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_EMPTY_PAGE_COMPONENT;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_PACKAGE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.ETS;
import static com.huawei.cangjie.projectmgmt.utils.Constants.ETS_WRAPPER_ACTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.IS_COMPATIBLE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_TYPE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.NO;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PAGE_FILE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PAGE_MID_DIR_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PAGE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PKG_DIR;
import static com.huawei.cangjie.projectmgmt.utils.Constants.TARGET_DIR;
import static com.huawei.cangjie.projectmgmt.utils.Constants.YES;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.ABILITY_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.ATOMIC_SERVICE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPILE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.PROJECT_PATH;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.KEY_ACTION;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_PROVIDER_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.CATEGORY;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DEFAULT_PAGES_PROFILE_FILE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_API_TYPE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.KEY_PROJECT;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.MAIN_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.OHOS_TEMPLATE_PROVIDER_ID;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.PAGES_PROFILE_FILE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SRC_ENTRANCE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SRC_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SUPER_VISUAL_DIR;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SUPPORT_HOS;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;
import static com.huawei.deveco.projectmgmt.ohos.view.FileOrDirType.MAIN;
import static com.huawei.deveco.projectmgmt.ohos.view.FileOrDirType.SRC;
import static com.huawei.deveco.projectmodel.ohos.util.CommonConstants.STAGE_MODE;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.isAddOnModule;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.isAtomicService;
import static com.intellij.util.PathUtil.toSystemIndependentName;

import com.huawei.cangjie.projectmgmt.action.BaseFileCreateAction;
import com.huawei.cangjie.projectmgmt.config.ConfigPathHandler;
import com.huawei.cangjie.projectmgmt.dialog.DialogWithParameters;
import com.huawei.cangjie.projectmgmt.dialog.ComponentCreateDialog;
import com.huawei.cangjie.projectmgmt.trace.TraceKind;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.projectmgmt.ohos.template.TemplateRenderClient;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.utils.Category;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.util.UISyntax;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * GeneratePageComponent
 *
 * @since 2024/08/14
 */
public class GenerateEmptyPage extends BaseFileCreateAction {
    private static final Pattern PAGE_NAME_PATTERN = Pattern.compile(
            "([a-zA-Z])([A-Za-z0-9_])*$"
    );
    private static final int PAGE_NAME_MAX_LENGTH = 247;
    private static final String ETS_SUFFIX = ".ets";

    public GenerateEmptyPage() {
        super(message("wizard.new.cangjie.component"), "Page");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        boolean shouldShowButton = PageUtil.shouldShowNewPageButton(event);
        if (!shouldShowButton) {
            event.getPresentation().setVisible(false);
            return;
        }
        event.getPresentation().setEnabled(isSyncFinished(project));
    }

    @Override
    @Nullable
    public String validateInput(Map<String, Object> parameters) {
        if (!(parameters.getOrDefault(ComponentCreateDialog.PAGE_NAME, null) instanceof String pageName)) {
            return null;
        }
        if (!PAGE_NAME_PATTERN.matcher(pageName).matches() || pageName.length() > PAGE_NAME_MAX_LENGTH) {
            return message("wizard.new.component.name.invalid");
        }
        if (!(parameters.getOrDefault(ComponentCreateDialog.FILES_NAME, null) instanceof Set<?> filesName)) {
            return null;
        }
        if (filesName.contains(pageName.toLowerCase())) {
            return message("wizard.new.component.name.exists");
        }
        return null;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        if (virtualFile == null) {
            return;
        }
        if (!virtualFile.isDirectory()) {
            virtualFile = virtualFile.getParent();
            if (virtualFile == null) {
                return;
            }
        }

        OhosModuleModel moduleModel = getSelectFileOhosModuleModel(project, virtualFile);
        if (moduleModel == null || project == null) {
            return;
        }
        Set<String> filesName = getExistPagesName(moduleModel, virtualFile);
        DialogWithParameters dialog = new ComponentCreateDialog(project,
                message("wizard.new.cangjie.component.dialog.title"), this, filesName);
        // If user cancelled action.
        if (dialog instanceof DialogWrapper && !((DialogWrapper) dialog).showAndGet()) {
            return;
        }
        if (!(dialog.getParameters().getOrDefault(ComponentCreateDialog.PAGE_NAME, null) instanceof String pageName)) {
            return;
        }
        if (StringUtil.isEmpty(pageName)) {
            return;
        }
        boolean enableEtsWrapper = false;
        if (dialog.getParameters().get(ComponentCreateDialog.ENABLE_ETS_WRAPPER) instanceof Boolean flag) {
            enableEtsWrapper = flag;
        }
        RenderHashMap renderHashMap = parseParams(moduleModel, virtualFile, pageName, enableEtsWrapper);
        TraceUtils.trace(TraceKind.CJ_ADD_HYBRID_PAGE_FILE);
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                new TemplateRenderClient(renderHashMap).executeRender());
    }

    private RenderHashMap parseParams(OhosModuleModel moduleModel, VirtualFile virtualFile,
                                      String pageName, boolean enableEtsWrapper) {
        RenderHashMap renderHashMap = new RenderHashMap();
        if (virtualFile == null || StringUtil.isEmpty(virtualFile.getCanonicalPath())) {
            return renderHashMap;
        }
        renderHashMap.put(ABILITY_NAME, moduleModel.getModuleName());
        renderHashMap.put(KEY_ACTION, "CreatePage");
        renderHashMap.put(KEY_TEMPLATE_PROVIDER_NAME, OHOS_TEMPLATE_PROVIDER_ID);
        renderHashMap.put(MODULE_NAME, moduleModel.getModuleName());
        ProjectModel projectModel = moduleModel.getProjectModel();
        if (projectModel == null) {
            return renderHashMap;
        }
        renderHashMap.put(KEY_TEMPLATE_NAME, CANGJIE_EMPTY_PAGE_COMPONENT);
        renderHashMap.put(CATEGORY, Category.PAGE.value());
        renderHashMap.put(DTO_API_TYPE, moduleModel.getApiType());
        renderHashMap.put(SRC_ENTRANCE, moduleModel.getSrcEntry());
        renderHashMap.put(SUPPORT_HOS, isAddOnModule(moduleModel));
        renderHashMap.put(ATOMIC_SERVICE, isAtomicService(projectModel));
        renderHashMap.put(COMPILE_BASE_API, projectModel.getFullCompileSdkVersion().getMajor());
        renderHashMap.put(KEY_PROJECT, projectModel.getProject());
        renderHashMap.put(PROJECT_PATH, toSystemIndependentName(projectModel.getProjectPath()));
        renderHashMap.put(MODULE_PATH, toSystemIndependentName(moduleModel.getModulePath()));

        renderHashMap.put(PAGES_PROFILE_FILE_NAME, getPageProfileFileName(moduleModel));
        renderHashMap.put(TARGET_DIR, virtualFile.getCanonicalPath());
        renderHashMap.put(PAGE_NAME, pageName);
        renderHashMap.put(PAGE_FILE_NAME, FileUtils.convertFileNameToSnakeCase(pageName));
        String packageName = FileUtils.getPackageName(moduleModel, virtualFile, false);
        renderHashMap.put(CANGJIE_PACKAGE_NAME, packageName);
        ConfigPathHandler.replaceCjPackageName(renderHashMap);
        renderHashMap.put(CANGJIE_DYNAMIC_NAME, packageName);
        if (FileUtils.isDynamicCombined(moduleModel)) {
            renderHashMap.put(CANGJIE_DYNAMIC_NAME, FileUtils.getCangjieModuleName(moduleModel, false));
        }
        renderHashMap.put(MODULE_TYPE, moduleModel.getModuleType());
        renderHashMap.put(PAGE_MID_DIR_NAME, getPageMiddleDirectory(moduleModel, virtualFile.getCanonicalPath()));
        String cjModuleSrcDir = FileUtils.getCangjieModuleSrcDir(moduleModel, false);
        String cjpmDirPath = FileUtils.getRealCjpmTomlDir(moduleModel, false);
        String cjRootPath = Path.of(cjpmDirPath, cjModuleSrcDir).normalize().toString().replaceAll("\\\\", "/");
        String pkgDir = virtualFile.getCanonicalPath().substring(cjRootPath.length());
        renderHashMap.put(PKG_DIR, pkgDir);
        if (enableEtsWrapper) {
            renderHashMap.put(ETS_WRAPPER_ACTION, YES);
        } else {
            renderHashMap.put(ETS_WRAPPER_ACTION, NO);
        }
        renderHashMap.put(IS_COMPATIBLE, SdkUtils.isConfigCompatibleSdk());
        return renderHashMap;
    }

    private Set<String> getExistPagesName(OhosModuleModel moduleModel, VirtualFile selectedPath) {
        Set<String> filesName = getExistEtsPagesName(getPagesPath(moduleModel));
        Set<String> extNames = new HashSet<>();
        for (Object name : filesName) {
            if (!(name instanceof String fileName)) {
                continue;
            }
            extNames.add(fileName.toLowerCase());
            if (fileName.contains("_")) {
                extNames.add(FileUtils.replaceUnderlineFileName(fileName));
            }
        }
        extNames.addAll(FileUtils.getCjFilesName(selectedPath));
        filesName.addAll(extNames);
        return filesName;
    }

    /**
     * get page config
     *
     * @param moduleModel target moduleModel
     * @return String
     */
    private String getPageProfileFileName(ModuleModel moduleModel) {
        if (!(moduleModel instanceof OhosModuleModel)) {
            return DEFAULT_PAGES_PROFILE_FILE_NAME;
        }
        return getOhosPageProfileFileName((OhosModuleModel) moduleModel);
    }

    private String getOhosPageProfileFileName(OhosModuleModel moduleModel) {
        String apiType = moduleModel.getApiType();
        if (!STAGE_MODE.equals(apiType)) {
            return DEFAULT_PAGES_PROFILE_FILE_NAME;
        }
        Path pagesCfgPath = moduleModel.getPagesCfgPath();
        if (pagesCfgPath == null || !pagesCfgPath.toFile().isFile()) {
            return DEFAULT_PAGES_PROFILE_FILE_NAME;
        }
        return pagesCfgPath.toFile().getName();
    }

    private Set<String> getExistEtsPagesName(Path path) {
        Set<String> existNames = new HashSet<>();
        // ets page为ets文件，一个ability中不能重名，已有名称为当前ability的pages路径下所有ets文件名称
        if (Files.isDirectory(path)) {
            File[] files = path.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    existNames.addAll(getExistEtsPagesName(file.toPath()));
                }
            }
        }
        if (Files.isRegularFile(path) && path.toFile().getName().endsWith(ETS_SUFFIX)) {
            String fileName = path.toFile().getName();
            existNames.add(fileName.substring(0, fileName.length() - ETS_SUFFIX.length()));
        }
        return existNames;
    }

    private Path getPagesPath(ModuleModel moduleModel) {
        return Paths.get(
                convertSuperVisualToUiSyntaxPath(
                        moduleModel, Path.of(moduleModel.getModulePath(), SRC, MAIN, ETS).toString()
                )
        );
    }

    private String convertSuperVisualToUiSyntaxPath(ModuleModel moduleModel, String path) {
        // 如果选择的路径在supervisual目录下，返回uiSyntax目录下对应的路径
        String superVisualPath = FileUtil.toCanonicalPath(
                Paths.get(moduleModel.getModulePath(), SRC_PATH, MAIN_PATH, SUPER_VISUAL_DIR).normalize().toString()
        );
        String uiSyntaxPath = FileUtil.toCanonicalPath(
                Paths.get(
                        moduleModel.getModulePath(), SRC_PATH, MAIN_PATH, UISyntax.ETS.getSrcPath()
                ).normalize().toString()
        );
        return path.replaceFirst(superVisualPath, uiSyntaxPath);
    }

    private String getPageMiddleDirectory(ModuleModel moduleModel, String selectedPath) {
        String uiSyntaxSrcPath = UISyntax.getUISyntaxSrcPath(moduleModel.getLanguage());
        String midModulePath = selectedPath.substring(moduleModel.getModulePath().length());
        return new File(selectedPath).isFile()
                ? midModulePath.substring(midModulePath.indexOf(uiSyntaxSrcPath) + uiSyntaxSrcPath.length(),
                midModulePath.lastIndexOf("/"))
                : midModulePath.substring(midModulePath.indexOf(uiSyntaxSrcPath) + uiSyntaxSrcPath.length());
    }
}
