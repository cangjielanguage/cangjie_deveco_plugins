/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import static com.huawei.cangjie.projectmgmt.utils.Constants.KEY_DEPENDENCIES;
import static com.huawei.cangjie.projectmgmt.utils.Constants.OH_PACKAGE_JSON5_FILE;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.getModulePath;
import static com.huawei.deveco.projectmodel.ohos.util.ProjectUtil.getProjectPath;
import static com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil.findJsonPsiFile;

import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.util.PsiJsonFileUtil;

import com.intellij.application.options.CodeStyle;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonPsiUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type Dependency handler util.
 *
 * @since 2025-1-9
 */
public class DependencyHandlerUtil {
    private static final Logger LOG = Logger.getInstance(DependencyHandlerUtil.class);

    private static final Pattern NEWLINE_PATTERN =
        Pattern.compile(SystemInfo.isWindows ? "\r\n" : (SystemInfo.isMac ? "\r" : "\n"));

    /**
     * Update project ohpm dependencies.
     *
     * @param project the project
     * @param updateMap the update map
     * @return the boolean
     */
    public static boolean updateProjectDependencies(@NotNull Project project, @NotNull Map<String, String> updateMap) {
        if (updateMap.isEmpty()) {
            return false;
        }
        Path ohPackageJson5Path = Paths.get(getProjectPath(project), OH_PACKAGE_JSON5_FILE);
        return updateDependencies(project, ohPackageJson5Path, updateMap, KEY_DEPENDENCIES);
    }

    /**
     * Update module ohpm dependencies.
     *
     * @param project the project
     * @param module the module
     * @param updateMap the update map
     * @return the boolean
     */
    public static boolean updateModuleDependencies(@NotNull Project project, @NotNull ModuleModel module,
        @NotNull Map<String, String> updateMap) {
        if (updateMap.isEmpty()) {
            return false;
        }
        Path ohPackageJson5Path = Paths.get(getModulePath(module), OH_PACKAGE_JSON5_FILE);
        return updateDependencies(project, ohPackageJson5Path, updateMap, KEY_DEPENDENCIES);
    }

    private static boolean updateDependencies(@NotNull Project project, @NotNull Path jsonPath,
        @NotNull Map<String, String> updateMap, @NotNull String dependencyKey) {
        JsonObject packageJsonObj =
            PsiJsonFileUtil.copyPsiJsonObject(PsiJsonFileUtil.findPsiFileJsonObject(project, jsonPath));
        if (packageJsonObj == null) {
            LOG.warn(String.format(Locale.ENGLISH, "find file %s error.", OH_PACKAGE_JSON5_FILE));
            return false;
        }
        JsonElementGenerator jsonElementGenerator = new JsonElementGenerator(project);
        int indentSize = CodeStyle.getIndentSize(findJsonPsiFile(project, jsonPath));
        if (PsiJsonFileUtil.getPsiJsonObject(packageJsonObj, dependencyKey) == null) {
            String dependIndent = " ".repeat(Math.max(0, indentSize));
            PsiElement dependIntentLine = jsonElementGenerator.createDummyFile("\n" + dependIndent).findElementAt(0);
            JsonProperty property =
                jsonElementGenerator.createProperty(dependencyKey, "{\n%s}".formatted(dependIndent));
            PsiElement jsonElement = JsonPsiUtil.addProperty(packageJsonObj, property, false);
            if (dependIntentLine != null) {
                packageJsonObj.addBefore(dependIntentLine, jsonElement);
            }
        }
        JsonObject dependenciesObj = PsiJsonFileUtil.getPsiJsonObject(packageJsonObj, dependencyKey);
        Map<String, JsonProperty> dependenciesMap =
            dependenciesObj.getPropertyList().stream().collect(Collectors.toMap(JsonProperty::getName, ele -> ele));
        AtomicBoolean isModify = new AtomicBoolean(false);
        for (Map.Entry<String, String> entry : updateMap.entrySet()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String version = "\"" + entry.getValue() + "\"";
                JsonProperty jsonProperty = dependenciesMap.get(entry.getKey());
                if (jsonProperty != null && jsonProperty.getValue() != null && !version.equals(
                    jsonProperty.getValue().getText())) {
                    jsonProperty.getValue().replace(jsonElementGenerator.createValue(version));
                    isModify.set(true);
                    return;
                }
                if (jsonProperty != null && jsonProperty.getValue() != null) {
                    return;
                }
                PsiElement newLine =
                        jsonElementGenerator.createDummyFile("\n" + " ".repeat(Math.max(0, indentSize * 2)))
                                .findElementAt(0);
                JsonProperty property = jsonElementGenerator.createProperty(entry.getKey(), version);
                PsiElement addProperty = JsonPsiUtil.addProperty(dependenciesObj, property, false);
                if (newLine != null) {
                    dependenciesObj.addBefore(newLine, addProperty);
                }
                isModify.set(true);
            });
        }
        String jsonText = ReadAction.compute(() -> packageJsonObj.getText());
        writeTextToFile(jsonPath.toFile(), jsonText, project);
        return isModify.get();
    }

    /**
     * Write text to file.
     *
     * @param targetFile the target file
     * @param context the context
     * @param project the project
     */
    public static void writeTextToFile(@NotNull File targetFile, @NotNull String context, @NotNull Project project) {
        WriteCommandAction.runWriteCommandAction(null, (Computable<Boolean>) () -> {
            try {
                LOG.info(String.format(Locale.ENGLISH, "write file start %1$s", targetFile.getName()));
                LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
                VirtualFile virtualFile = localFileSystem.refreshAndFindFileByIoFile(targetFile);
                if (virtualFile != null) {
                    virtualFile.refresh(false, false);
                }
                if (virtualFile == null || !virtualFile.exists()) {
                    VirtualFile parentDir = getParentDir(targetFile);
                    virtualFile = parentDir.createChildData(localFileSystem, targetFile.getName());
                    LOG.info(String.format(Locale.ENGLISH, "create file %1$s", targetFile.getName()));
                }
                FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                Document document = fileDocumentManager.getDocument(virtualFile);
                if (document == null) {
                    throw new IOException("document is null");
                }
                String resultContext = NEWLINE_PATTERN.matcher(context).replaceAll(System.lineSeparator());
                document.setText(resultContext);
                fileDocumentManager.saveDocument(document);
                virtualFile.refresh(false, false);
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                if (project.isInitialized()) {
                    psiDocumentManager.commitDocument(document);
                }
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                LOG.info(String.format(Locale.ENGLISH, "write file success %1$s", targetFile.getName()));
                return true;
            } catch (IOException exception) {
                LOG.error("write file error " + targetFile.getName(), exception.getMessage());
                return false;
            }
        });
    }

    private static VirtualFile getParentDir(@NotNull File targetFile) throws IOException {
        File parentFile = targetFile.getParentFile();
        if (parentFile == null) {
            throw new IOException("target parent file is null");
        }
        VirtualFile parentDir = VfsUtil.createDirectoryIfMissing(parentFile.getCanonicalPath());
        if (parentDir == null || !parentDir.isDirectory()) {
            throw new IOException("target parent dir is null");
        }
        return parentDir;
    }
}