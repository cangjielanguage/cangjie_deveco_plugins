/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.filter;

import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;

import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.google.common.collect.Lists;
import com.intellij.analysis.problemsView.ProblemsCollector;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Filter for highlight info
 *
 * @since 2024-05-13
 */
public class JsonHighlightInfoFilter implements HighlightInfoFilter {
    private static final List<String> IGNORE_FILES =
        Lists.newArrayList("module.json5", "oh-package.json5", "libark_interop_loader.d.ts", "build-profile.json5",
            "main_pages.json", "Index.d.ts", "ark_interop_api.d.ts");

    private static final String SRC_ENTRY_REGEX = "^[\"']?\\w+(\\.\\w+)+[\"']?$";

    private static final Pattern DECL_ERR_PATTERN =
        Pattern.compile("Declared function '.*' has no native implementation\\s*.\\s*<ArkTSCheck>");

    private static final String OH_PACKAGE_NAME_ERR = "String violates the pattern: '^(@(?![0-9\\-_])[a-z0-9\\-_]+"
        + "(?<![\\-_])/)?(?![0-9\\-_.])[a-z0-9\\-_.]+(?<![\\-_.])$'";

    private static final Pattern SO_DEPENDS_PATTERN = Pattern.compile(
        "Only the following .so dependencies are allowed: external .so files located in (.*) "
            + "files listed in CMakeLists.txt.");

    private static final Pattern SO_DEPENDS_PATTERN_ZH = Pattern.compile(
        "仅允许以下.so依赖项：(.*)路径下的外部.so文件和CMakeLists.txt中列出的内部.so文件。\\s*");

    private static final String[] DECLARE_PATH_ARR = {
        "src/main/cangjie/types", "src/main/cangjie/loader", "src/main/cangjie/ark_interop_api"
    };

    private static final Set<Path> DECLARE_PATH_SET = Arrays.stream(DECLARE_PATH_ARR)
        .map(Paths::get)
        .map(Path::normalize)
        .collect(Collectors.toSet());

    @Override
    public boolean accept(@NotNull HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if (file == null || highlightInfo.getDescription() == null) {
            return true;
        }
        VirtualFile virtualFile = file.getVirtualFile();
        if (isNotNeedFilter(virtualFile)) {
            return true;
        }
        String fileName = virtualFile.getName();
        OhosModuleModel module = getSelectFileOhosModuleModel(file.getProject(), virtualFile);
        if ("oh-package.json5".equals(fileName) && !handlePackageJson5Types(highlightInfo, file)) {
            return false;
        }
        if (!FileUtils.isCangjieModule(module)) {
            return true;
        }
        switch (fileName) {
            case "module.json5" -> {
                return handleModuleJson5(highlightInfo, file);
            }
            case "oh-package.json5" -> {
                return handleOhPackageJson5(highlightInfo, file, module);
            }
            case "libark_interop_loader.d.ts" -> {
                return handleLibInteropLoaderDTS(highlightInfo, file);
            }
            case "main_pages.json" -> {
                return handleMainPageJson(highlightInfo, module);
            }
            case "ark_interop_api.d.ts" -> {
                return handleIndexDts(highlightInfo, file);
            }
            default -> {
                return true;
            }
        }
    }

    private boolean isNotNeedFilter(VirtualFile virtualFile) {
        return virtualFile == null || StringUtil.isEmpty(virtualFile.getName()) || (
            !IGNORE_FILES.contains(virtualFile.getName()) && !virtualFile.getName().endsWith(".d.ts"));
    }

    private boolean handleIndexDts(HighlightInfo highlightInfo, PsiFile file) {
        if (DECL_ERR_PATTERN.matcher(highlightInfo.getDescription()).find() && isPathContainsCangjie(
            file.getVirtualFile().getCanonicalPath())) {
            timerFilterMsg(file);
            return false;
        }
        return true;
    }

    private boolean filterOhPackageNameErr(HighlightInfo highlightInfo, PsiFile file) {
        if (OH_PACKAGE_NAME_ERR.equals(highlightInfo.getDescription()) && isPathContainsCangjie(
            file.getVirtualFile().getCanonicalPath())) {
            timerFilterMsg(file);
            return false;
        }
        return true;
    }

    private static void timerFilterMsg(PsiFile file) {
        if (file == null) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int fileProblemCount =
                    ProblemsCollector.getInstance(file.getProject()).getFileProblemCount(file.getVirtualFile());
                if (fileProblemCount <= 1) {
                    WolfTheProblemSolver wolfTheProblemSolver = WolfTheProblemSolver.getInstance(file.getProject());
                    VirtualFile virtualFile = file.getVirtualFile();
                    wolfTheProblemSolver.clearProblemsFromExternalSource(virtualFile, "ArkUI Language Service Source");
                }
            }
        }, 50);
    }

    private boolean handleModuleJson5(HighlightInfo highlightInfo, PsiFile file) {
        if (!"Relative file path(like ./**) is required for srcEntry.".equals(highlightInfo.getDescription())
            && !"Set srcEntry to a relative file path (for example, ./**).".equals(highlightInfo.getDescription())) {
            return true;
        }
        PsiElement element = file.findElementAt(highlightInfo.getActualStartOffset());
        if (element == null) {
            return true;
        }
        String text = element.getText();
        return StringUtils.isEmpty(text) || !text.matches(SRC_ENTRY_REGEX);
    }

    private boolean handlePackageJson5Types(HighlightInfo highlightInfo, PsiFile file) {
        String description = highlightInfo.getDescription();
        if (!SO_DEPENDS_PATTERN.matcher(description).find() && !SO_DEPENDS_PATTERN_ZH.matcher(description).find()) {
            return true;
        }
        PsiElement element = file.findElementAt(highlightInfo.getActualStartOffset());
        if (element == null) {
            return true;
        }
        PsiElement nextSibling = element.getNextSibling();
        if (nextSibling == null) {
            nextSibling = PsiTreeUtil.nextVisibleLeaf(element);
        }
        Optional<PsiElement> soPathElementOptional = getNextValidElement(nextSibling);
        if (soPathElementOptional.isEmpty()) {
            return true;
        }
        String text = soPathElementOptional.get().getText();
        return !containsSubdirectory(text);
    }

    private boolean handleOhPackageJson5(HighlightInfo highlightInfo, PsiFile file, OhosModuleModel module) {
        String description = highlightInfo.getDescription();
        if ("Set either main or types, or both for this HSP/HAR module.".equals(description) && !Path.of(
            module.getModulePath(), "src", "main", "ets").toFile().exists()) {
            return false;
        }
        if (!filterOhPackageNameErr(highlightInfo, file)) {
            return false;
        }
        if (!"Ensure that the so name is include in CMakeLists.".equals(description) && !SO_DEPENDS_PATTERN.matcher(
            description).find() && !SO_DEPENDS_PATTERN_ZH.matcher(description).find()) {
            return true;
        }
        PsiElement element = file.findElementAt(highlightInfo.getActualStartOffset());
        if (element == null) {
            return true;
        }
        String text = element.getText();
        return StringUtils.isEmpty(text) || (!"\"libark_interop_loader.so\"".equals(text)
            && !"'libark_interop_loader.so'".equals(text) && !"\"libidl.so\"".equals(text) && !"'libidl.so'".equals(
            text) && !"\"libark_interop_api.so\"".equals(text) && !"'libark_interop_api.so'".equals(text)
            && !matchCjPackageName(text, module));
    }

    private boolean matchCjPackageName(String text, OhosModuleModel module) {
        String packageName = FileUtils.getCangjieModuleName(module, false);
        if (StringUtils.isEmpty(packageName)) {
            return false;
        }
        String pattern = "^[\"|']lib" + packageName.replace(".", "\\.") + "(\\.[^\\.]+)*\\.so[\"|']$";
        return text.matches(pattern);
    }

    private boolean handleLibInteropLoaderDTS(HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if (DECL_ERR_PATTERN.matcher(highlightInfo.getDescription()).find()) {
            timerFilterMsg(file);
            return false;
        }
        return true;
    }

    private boolean handleMainPageJson(HighlightInfo highlightInfo, OhosModuleModel module) {
        if (Path.of(module.getModulePath(), "src", "main", "ets").toFile().exists()) {
            return true;
        }
        Pattern errorPattern =
            Pattern.compile("Pages referenced in the config, '.*' was not found in the project or " + "the libraries");
        String description = highlightInfo.getDescription();
        if (("Array is shorter than 1").equals(description) || errorPattern.matcher(description).find()) {
            return false;
        }
        return true;
    }

    private boolean isPathContainsCangjie(String canonicalPath) {
        if (StringUtils.isEmpty(canonicalPath)) {
            return false;
        }
        Path filePath = Paths.get(canonicalPath);
        for (Path name : filePath) {
            if ("cangjie".equals(name.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSubdirectory(String filePathParam) {
        if (StringUtils.isEmpty(filePathParam)) {
            return true;
        }
        String filePath = filePathParam.replaceAll("^[\"']|[\"']$", "").trim();
        if (!filePath.startsWith("file:")) {
            return false;
        }
        Path path = Paths.get(filePath.substring(5).trim());
        Path currentPath = path.toAbsolutePath().normalize();
        while (currentPath != null) {
            for (Path subPath : DECLARE_PATH_SET) {
                if (currentPath.endsWith(subPath)) {
                    return true;
                }
            }
            currentPath = currentPath.getParent();
        }
        return false;
    }

    private Optional<PsiElement> getNextValidElement(PsiElement element) {
        if (element == null) {
            return Optional.empty();
        }
        PsiElement nextElement = element.getNextSibling();
        while (nextElement != null) {
            if (!(nextElement instanceof PsiWhiteSpace) && !":".equals(nextElement.getText())) {
                return Optional.of(nextElement);
            }
            nextElement = nextElement.getNextSibling();
        }
        return Optional.empty();
    }
}
