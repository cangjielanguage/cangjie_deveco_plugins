/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import static com.huawei.cangjie.projectmgmt.utils.Constants.API_VERSION_22;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_COMPATIBLE_BASE_API;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CODE_LINTER_JSON5;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CODE_LINTER_JSON_HAS_CJ;
import static com.huawei.cangjie.projectmgmt.utils.Constants.IS_COMPATIBLE;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPATIBLE_BASE_API;
import static com.huawei.deveco.projectmgmt.ohos.template.freemarker.FreeMarkerParam.COMPILE_BASE_API;

import com.huawei.cangjie.projectmgmt.sync.upgrade.impl.CangjieCfgUpdateImpl;
import com.huawei.cangjie.projectmgmt.template.CangjieTemplateParamProvider;
import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * cangjie template params
 *
 * @since 2024-10-22
 */
public class CangjieTemplateUtils {
    private static final Logger LOG = Logger.getInstance(CangjieCfgUpdateImpl.class);

    /**
     * Sets template params.
     *
     * @param renderHashMap the render hash map
     */
    public static void setTemplateParams(RenderHashMap renderHashMap) {
        renderHashMap.put(IS_COMPATIBLE, SdkUtils.isConfigCompatibleSdk());
        renderHashMap.put(CANGJIE_COMPATIBLE_BASE_API, extractMainApiVersion(renderHashMap.get(COMPATIBLE_BASE_API)));
        String cjcVersion = SdkUtils.getCjcVersion(renderHashMap.getInt(COMPILE_BASE_API, 0));
        if (StringUtils.isEmpty(cjcVersion)) {
            cjcVersion = Constants.CUR_CJC_VERSION;
        }
        renderHashMap.put(Constants.CJC_VERSION, cjcVersion);
        CangjieTemplateParamProvider.TEMPLATE_PARAM_PROVIDER_EXTENSION_LIST.getExtensionList()
            .stream().map(CangjieTemplateParamProvider::collectTemplateParams)
            .filter(Objects::nonNull)
            .forEach(templateParamsMap -> renderHashMap.putAll(templateParamsMap.entrySet().stream()
                .filter(element -> !renderHashMap.containsKey(element.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));

        // CJLint支持code-linter.json5配置
        // 判断code-linter.json5中是否已添加仓颉相关修改
        renderHashMap.put(
            CODE_LINTER_JSON_HAS_CJ,
            checkCJConfigInCodeLinterJson(renderHashMap.getString("projectPath"))
        );
    }

    /**
     * Check whether Cangjie configurations have been added to code-linter.json5
     *
     * @param projectPath project root path
     * @return code-linter.json5 has cangjie configuration
     */
    public static boolean checkCJConfigInCodeLinterJson(String projectPath) {
        boolean hasCJConfigInCodeLinterJson = false;
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectPath));
        if (virtualFile == null) {
            return hasCJConfigInCodeLinterJson;
        }
        Project project = ProjectUtil.guessProjectForFile(virtualFile);
        if (project == null) {
            return hasCJConfigInCodeLinterJson;
        }
        Path jsonPath = Path.of(projectPath, CODE_LINTER_JSON5);
        if (!jsonPath.toFile().exists()) {
            return hasCJConfigInCodeLinterJson;
        }
        JsonObject config = PsiJsonFileUtil.findPsiFileJsonObject(project, jsonPath);
        JsonArray filesJsonArray = PsiJsonFileUtil.getPsiJsonArray(config, "files");
        if (filesJsonArray == null) {
            return hasCJConfigInCodeLinterJson;
        }
        return PsiJsonFileUtil.getStrListFromJsonArray(filesJsonArray).stream()
                .anyMatch(item -> item.endsWith(".cj"));
    }

    private static int extractMainApiVersion(Object compatibleApiObj) {
        if (compatibleApiObj == null) {
            return API_VERSION_22;
        }
        if (compatibleApiObj instanceof Integer) {
            return (Integer) compatibleApiObj;
        } else if (compatibleApiObj instanceof String versionStr) {
            if (versionStr.isEmpty()) {
                return API_VERSION_22;
            }
            int firstDotIndex = versionStr.indexOf('.');
            String mainVersionPart;
            if (firstDotIndex != -1) {
                mainVersionPart = versionStr.substring(0, firstDotIndex);
            } else {
                mainVersionPart = versionStr;
            }

            try {
                return Integer.parseInt(mainVersionPart);
            } catch (NumberFormatException e) {
                LOG.warn("Error parsing main API version part '" + mainVersionPart + "' from original string: "
                    + versionStr);
                return API_VERSION_22;
            }
        } else if (compatibleApiObj instanceof Number) {
            return ((Number) compatibleApiObj).intValue();
        } else {
            LOG.warn("Unexpected type for compatibleApiObj: " + compatibleApiObj.getClass().getName()
                + ". Returning null for main API version.");
        }
        return API_VERSION_22;
    }
}
