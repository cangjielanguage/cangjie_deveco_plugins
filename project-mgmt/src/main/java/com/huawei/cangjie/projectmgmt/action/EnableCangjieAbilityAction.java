/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.action;

import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_HYBRID_ABILITY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_HYBRID_LIBRARY;
import static com.huawei.cangjie.projectmgmt.utils.Constants.DEFAULT_TYPE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.ENABLE_CANGJIE_ACTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.EVENT_ENABLE_CANGJIE_HAR;
import static com.huawei.cangjie.projectmgmt.utils.Constants.IS_COMPATIBLE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PHONE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.TABLET;
import static com.huawei.cangjie.projectmgmt.utils.Constants.YES;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.ABILITY_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.ATOMIC_SERVICE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.BUNDLE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPATIBLE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPILE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.MODULE_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.PROJECT_PATH;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap.KEY_TEMPLATE_PROVIDER_NAME;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.CATEGORY;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_API_TYPE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.DTO_UI_SYNTAX;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.KEY_PROJECT;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.OHOS_TEMPLATE_PROVIDER_ID;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.PERMISSIONS_EXISTS;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SRC_ENTRANCE;
import static com.huawei.deveco.projectmgmt.ohos.template.utils.TemplateConstant.SUPPORT_HOS;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonConstant.KEY_ACTION;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.isSyncFinished;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.isAddOnModule;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.isAtomicService;
import static com.intellij.util.PathUtil.toSystemIndependentName;

import com.huawei.cangjie.projectmgmt.trace.TraceKind;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.projectmgmt.ohos.template.TemplateRenderClient;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmgmt.ohos.template.utils.Category;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;
import com.huawei.deveco.sdkmanager.core.util.StringUtil;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enable Cangjie Ability Action
 *
 * @since 2024/04/08
 */
public class EnableCangjieAbilityAction extends DumbAwareAction {
    private static final Logger LOGGER = Logger.getInstance(EnableCangjieAbilityAction.class);

    private static final Map<String, String> MODULE_TYPE_STRING_MAP = Map.of(
            ModuleType.ENTRY.toString(), "Cangjie(Interop) (Only Support Phone, Tablet)",
            ModuleType.FEATURE.toString(), "Cangjie(Interop) (Only Support Phone, Tablet)",
            ModuleType.HAR.toString(), "Cangjie(Interop) (Only Support Default, Phone, Tablet)",
            ModuleType.SHARED.toString(), "Cangjie(Interop) (Only Support Phone, Tablet)"
    );

    private static final Map<String, List<String>> MODULE_SUPPORT_DEVICES = Map.of(
            ModuleType.ENTRY.toString(), List.of(PHONE, DEFAULT_TYPE, TABLET),
            ModuleType.FEATURE.toString(), List.of(PHONE, DEFAULT_TYPE, TABLET),
            ModuleType.HAR.toString(), List.of(DEFAULT_TYPE, PHONE, TABLET),
            ModuleType.SHARED.toString(), List.of(DEFAULT_TYPE, PHONE, TABLET)
    );

    private final Map<String, Object> extensionParam = new HashMap<>();

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());
        OhosModuleModel moduleModel = getSelectFileOhosModuleModel(project, virtualFile);
        // check is cangjie support module
        if (!FileUtils.isSupportModule(project, moduleModel)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        String titleInfo = MODULE_TYPE_STRING_MAP.get(moduleModel.getModuleType());
        // check module build type
        if (StringUtil.isEmpty(titleInfo)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        // check is contain cangjie ability
        if (FileUtils.isContainCangjieAbility(moduleModel)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        boolean isSupportDeviceType = isSupportDeviceType(moduleModel);
        if (isSupportDeviceType) {
            anActionEvent.getPresentation().setText("Cangjie(Interop)");
        } else {
            anActionEvent.getPresentation().setText(titleInfo);
        }
        anActionEvent.getPresentation().setEnabled(isSyncFinished(project) && isSupportDeviceType);
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());
        OhosModuleModel moduleModel = getSelectFileOhosModuleModel(anActionEvent.getProject(), virtualFile);
        if (moduleModel == null) {
            LOGGER.warn("create ability error, cause:get module model error");
            return;
        }

        extensionParam.put(BUNDLE_NAME, moduleModel.getProjectModel().getBundleName());
        extensionParam.put(PERMISSIONS_EXISTS, false);

        RenderHashMap renderHashMap = parseParams(moduleModel);
        TraceUtils.trace(TraceKind.CJ_ENABLE);
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                new TemplateRenderClient(renderHashMap).executeRender());
    }

    private RenderHashMap parseParams(OhosModuleModel moduleModel) {
        RenderHashMap renderHashMap = new RenderHashMap();
        renderHashMap.put(ENABLE_CANGJIE_ACTION, YES);

        renderHashMap.put(ABILITY_NAME, moduleModel.getModuleName());
        String action = "ohosCreateAbility";
        String category = Category.ABILITY.value();
        String templateName = CANGJIE_HYBRID_ABILITY;
        if (moduleModel.isHarLibrary()) {
            action = EVENT_ENABLE_CANGJIE_HAR;
            category = Category.LIBRARY.value();
            templateName = CANGJIE_HYBRID_LIBRARY;
            renderHashMap.put(DTO_UI_SYNTAX, "eTS");
        }
        renderHashMap.put(KEY_ACTION, action);
        renderHashMap.put(KEY_TEMPLATE_NAME, templateName);
        extensionParam.put(CATEGORY, category);
        renderHashMap.put(KEY_TEMPLATE_PROVIDER_NAME, OHOS_TEMPLATE_PROVIDER_ID);
        renderHashMap.put(MODULE_NAME, moduleModel.getModuleName());
        ProjectModel projectModel = moduleModel.getProjectModel();

        renderHashMap.put(KEY_PROJECT, projectModel.getProject());
        renderHashMap.put(PROJECT_PATH, toSystemIndependentName(projectModel.getProjectPath()));
        String modulePath = toSystemIndependentName(moduleModel.getModulePath());
        renderHashMap.put(MODULE_PATH, modulePath);
        renderHashMap.put(DTO_API_TYPE, moduleModel.getApiType());

        renderHashMap.put(SRC_ENTRANCE, moduleModel.getSrcEntry());
        renderHashMap.put(SUPPORT_HOS, isAddOnModule(moduleModel));
        renderHashMap.put(ATOMIC_SERVICE, isAtomicService(projectModel));
        renderHashMap.putAll(extensionParam);
        renderHashMap.put(COMPATIBLE_BASE_API, projectModel.getFullCompatibleSdkVersion().getMajor());
        renderHashMap.put(COMPILE_BASE_API, projectModel.getFullCompileSdkVersion().getMajor());
        renderHashMap.put(IS_COMPATIBLE, SdkUtils.isConfigCompatibleSdk());
        return renderHashMap;
    }

    /**
     * check is support device type
     *
     * @param moduleModel moduleModel
     * @return is support device type
     */
    public static boolean isSupportDeviceType(OhosModuleModel moduleModel) {
        if (moduleModel == null) {
            return false;
        }
        List<String> supportDevices = MODULE_SUPPORT_DEVICES.get(moduleModel.getModuleType());
        List<String> dviceTypeList = moduleModel.getDeviceTypeList();
        if (dviceTypeList == null || dviceTypeList.isEmpty()) {
            return false;
        }
        for (String deviceType : dviceTypeList) {
            if (!supportDevices.contains(deviceType)) {
                return false;
            }
        }
        return true;
    }
}
