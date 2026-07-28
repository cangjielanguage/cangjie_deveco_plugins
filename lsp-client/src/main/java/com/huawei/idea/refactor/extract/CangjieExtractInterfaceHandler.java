/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.refactor.extract;

import com.huawei.idea.edit.CangjieEditorEventManager;
import com.huawei.idea.refactor.RefactorBaseHandler;
import com.huawei.idea.refactor.extract.dialog.CangjieExtractInterfaceDialog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.usageView.UsageInfo;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

/**
 * ExtractInterfaceHandler handles the action triggering mechanisms and pipeline lifecycle
 * for extracting members into a standalone interface definition.
 *
 * @since 2025-12-23
 */
public class CangjieExtractInterfaceHandler extends RefactorBaseHandler {
    private static final Logger LOG = Logger.getInstance(CangjieExtractInterfaceHandler.class);

    private static final int ERROR_INVALID_SELECTION = 0;
    private static final int ERROR_SYNTAX = 1;
    private static final int ERROR_INVALID_TARGET = 2;
    private static final int ERROR_EMPTY_MEMBERS = 3;

    private static final String MSG_INVALID_SELECTION = "The selected range cannot be refactored.";
    private static final String MSG_SYNTAX = "The selected range contains syntax errors.";
    private static final String MSG_INVALID_TARGET =
            "Extract interface is only supported for classes, structs, interfaces, enums, and extends.";
    private static final String MSG_EMPTY_MEMBERS =
            "Cannot extract an interface because the selected type has no extractable members.";

    private static final Set<String> SUPPORTED_TYPE_BODY_NAMES = new HashSet<>(Arrays.asList(
            "CjClassBody", "CjStructBody", "CjInterfaceBody", "CjEnumBody", "CjExtendBody"
    ));
    private static final Set<String> SUPPORTED_TYPE_DEFINITION_NAMES = new HashSet<>(Arrays.asList(
            "CjClassDefinition", "CjStructDefinition", "CjInterfaceDefinition", "CjEnumDefinition",
            "CjExtendDefinition", "CjExtend"
    ));

    /**
     * Error mapping constants to localized notification strings.
     */
    private static final Map<Integer, String> ERROR_CODE_MAP = Map.of(
            ERROR_INVALID_SELECTION, MSG_INVALID_SELECTION,
            ERROR_SYNTAX, MSG_SYNTAX,
            ERROR_INVALID_TARGET, MSG_INVALID_TARGET,
            ERROR_EMPTY_MEMBERS, MSG_EMPTY_MEMBERS
    );

    /**
     * Standard default constructor configuring core tweaking constraints.
     */
    public CangjieExtractInterfaceHandler() {
        setTweak("Extract to interface");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] elements,
                       @Nullable com.intellij.openapi.actionSystem.DataContext dataContext) {
        if (elements == null || elements.length == 0) {
            return;
        }
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        invoke(project, editor, elements[0].getContainingFile(), dataContext);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file,
                       @Nullable com.intellij.openapi.actionSystem.DataContext dataContext) {
        CangjieCodeBlock codeBlock = new CangjieCodeBlock(project, editor);
        int start = editor.getSelectionModel().getSelectionStart();
        int end = editor.getSelectionModel().getSelectionEnd();

        if (start == end) {
            Integer[] fullTypeRange = resolveEnclosingTypeRange(editor, file, codeBlock);
            if (fullTypeRange.length == 0) {
                return;
            }
            start = fullTypeRange[0];
            end = fullTypeRange[1];
        }

        try {
            executeCodeAction(editor, codeBlock, start, end);
        } catch (ExecutionException e) {
            LOG.debug(e);
        }
    }

    @Override
    protected void executeCodeAction(Editor editor, CangjieCodeBlock codeBlock,
                                     int start, int end) throws ExecutionException {
        if (!(EditorEventManagerBase.forEditor(editor) instanceof CangjieEditorEventManager manager)) {
            return;
        }

        List<Either<Command, CodeAction>> actions = manager.codeAction4Refactor(start, end);
        for (Either<Command, CodeAction> either : actions) {
            Command command = either.isLeft() ? either.getLeft() : either.getRight().getCommand();
            if (command != null && command.getTitle().startsWith(tweak)) {
                if (!checkCommand(codeBlock, command)) {
                    return;
                }
                handleExtractInterfaceCommand(editor, codeBlock, manager, command);
                return;
            }
        }
    }

    private void handleExtractInterfaceCommand(Editor editor, CangjieCodeBlock codeBlock,
                                               CangjieEditorEventManager manager, Command command) {
        Project project = codeBlock.getProject();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        if (command.getArguments() == null || command.getArguments().isEmpty()
                || command.getArguments().getFirst() == null) {
            reportError(codeBlock, "Invalid refactoring arguments received from server.");
            return;
        }

        String rawArgsStr = command.getArguments().getFirst().toString().trim();
        if (!rawArgsStr.startsWith("{")) {
            reportError(codeBlock, "Refactoring payload format is invalid.");
            return;
        }

        JsonObject payload = JsonParser.parseString(rawArgsStr).getAsJsonObject();
        String suggestName = payload.has("suggestName") ? payload.get("suggestName").getAsString() : "";
        List<CangjieMemberInfo> memberInfos = buildMemberInfos(payload, codeBlock, psiFile);

        PsiElement sourceType = psiFile.findElementAt(editor.getCaretModel().getOffset());
        CangjieExtractInterfaceDialog dialog = new CangjieExtractInterfaceDialog(
                project, editor, sourceType, memberInfos, suggestName);
        dialog.show();

        if (!dialog.isOK()) {
            return;
        }

        JsonObject extraOptions = new JsonObject();
        boolean renameOriginalClass = dialog.isRenameOriginalClassAndUseInterfaceWherePossible();
        if (renameOriginalClass) {
            extraOptions.addProperty("renameOriginalClassAndUseInterfaceWherePossible", true);
            extraOptions.addProperty("implementationClassName", dialog.getImplementationClassName());
        } else {
            extraOptions.addProperty("interfaceName", dialog.getInterfaceName());
        }
        String dotPackageName = dialog.getTargetPackageName();
        String absolutePath = calculateAbsolutePath(project, editor, dotPackageName);
        extraOptions.addProperty("targetPath", absolutePath);

        JsonArray members = new JsonArray();
        dialog.getSelectedMemberInfos().forEach(member -> members.add(member.getSignature()));
        extraOptions.add("selectedMembers", members);

        payload.add("extraOptions", extraOptions);
        Map<?, ?> finalMap = new Gson().fromJson(payload, Map.class);
        command.setArguments(new ArrayList<>(List.of(finalMap)));

        if (renameOriginalClass) {
            executeRefactorCommandDirectly(manager, command);
            return;
        }

        Optional<PsiElement> previewSourceElement = findEnclosingTypeDefinition(sourceType);
        if (previewSourceElement.isEmpty()) {
            previewSourceElement = Optional.of(psiFile);
        }

        String sourceTypeName = suggestName == null ? "" : suggestName.trim();
        if (sourceTypeName.isEmpty()) {
            executeRefactorCommandDirectly(manager, command);
            return;
        }

        CangjieExtractInterfacePreviewProcessor previewProcessor = new CangjieExtractInterfacePreviewProcessor(
                project,
                previewSourceElement.get(),
                new CangjieExtractInterfacePreviewProcessor.PreviewRequest(
                        manager,
                        command,
                        extraOptions,
                        sourceTypeName,
                        dialog.getInterfaceName()
                )
        );
        UsageInfo[] usages = previewProcessor.collectPreviewUsages();
        if (usages.length == 0) {
            executeRefactorCommandDirectly(manager, command);
            return;
        }
        previewProcessor.run();
    }

    private void executeRefactorCommandDirectly(CangjieEditorEventManager manager, Command command) {
        manager.executeCommands4Refactor(Collections.singletonList(command));
    }

    private String calculateAbsolutePath(Project project, Editor editor, String dotPackageName) {
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (currentFile == null) {
            return "";
        }

        if (dotPackageName == null || dotPackageName.isEmpty()) {
            VirtualFile parent = currentFile.getParent();
            return parent != null ? parent.getPath() : "";
        }

        Module module = ModuleUtil.findModuleForFile(currentFile, project);
        VirtualFile moduleRoot = (module != null)
                ? com.intellij.openapi.project.ProjectUtil.guessModuleDir(module)
                : project.getBaseDir();
        VirtualFile rootDir = (moduleRoot != null)
                ? moduleRoot.findFileByRelativePath("src/main/cangjie")
                : null;

        if (rootDir == null) {
            rootDir = (moduleRoot != null) ? moduleRoot : project.getBaseDir();
        }

        String relativePath = dotPackageName.replace('.', '/');
        String rootPath = rootDir.getPath();
        return rootPath.endsWith("/") ? rootPath + relativePath : rootPath + "/" + relativePath;
    }

    private List<CangjieMemberInfo> buildMemberInfos(JsonObject payload, CangjieCodeBlock codeBlock, PsiFile psiFile) {
        List<CangjieMemberInfo> memberInfos = new ArrayList<>();
        if (!payload.has("members") || !payload.get("members").isJsonArray()) {
            return memberInfos;
        }

        JsonArray members = payload.getAsJsonArray("members");
        for (JsonElement member : members) {
            JsonObject mObj = member.isJsonObject() ? member.getAsJsonObject() : new JsonObject();
            String sig = mObj.has("signature") ? mObj.get("signature").getAsString() : member.getAsString();
            boolean isStatic = mObj.has("isStatic") && mObj.get("isStatic").getAsBoolean();
            String visibility = mObj.has("visibility") ? mObj.get("visibility").getAsString() : "";

            if (sig == null || sig.isEmpty()) {
                continue;
            }
            PsiElement stub = createLightMember(codeBlock.getProject(), psiFile, sig);
            CangjieMemberInfo info = new CangjieMemberInfo(stub, sig, isStatic, visibility);
            info.setDisplayName(sig);
            memberInfos.add(info);
        }
        return memberInfos;
    }

    private Integer[] resolveEnclosingTypeRange(Editor editor, PsiFile file, CangjieCodeBlock codeBlock) {
        PsiElement elementAtCaret = file.findElementAt(editor.getCaretModel().getOffset());
        if (elementAtCaret == null) {
            return new Integer[0];
        }
        Optional<PsiElement> enclosingTypeDefinition = findEnclosingTypeDefinition(elementAtCaret);
        PsiElement typeDefinition = enclosingTypeDefinition.orElse(null);
        if (typeDefinition == null) {
            reportError(codeBlock, "Cursor is not inside a class, struct, interface, enum or extend.");
            return new Integer[0];
        }
        return new Integer[]{typeDefinition.getTextOffset(),
                typeDefinition.getTextOffset() + typeDefinition.getTextLength()};
    }

    private Optional<PsiElement> findEnclosingTypeDefinition(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (isSupportedTypeDefinition(current)) {
                return Optional.of(current);
            }
            if (isSupportedTypeBody(current)) {
                PsiElement parent = current.getParent();
                if (isSupportedTypeDefinition(parent)) {
                    return Optional.of(parent);
                }
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    private boolean isSupportedTypeBody(PsiElement element) {
        return element != null && SUPPORTED_TYPE_BODY_NAMES.contains(element.getClass().getSimpleName());
    }

    private boolean isSupportedTypeDefinition(PsiElement element) {
        return element != null && SUPPORTED_TYPE_DEFINITION_NAMES.contains(element.getClass().getSimpleName());
    }

    /**
     * Checks the payload mapping information returned from the service side.
     *
     * @param codeBlock Code container context.
     * @param command   Execution platform action descriptor.
     * @return True if verified without configuration mismatch; false otherwise.
     */
    protected boolean checkCommand(CangjieCodeBlock codeBlock, Command command) {
        JsonObject payload = getCommandPayload(command);
        if (payload == null || !payload.has("ErrorCode")) {
            return true;
        }
        int errorCode = payload.get("ErrorCode").getAsInt();
        reportError(codeBlock, ERROR_CODE_MAP.getOrDefault(errorCode, MSG_EMPTY_MEMBERS));
        return false;
    }

    private JsonObject getCommandPayload(Command command) {
        if (command.getArguments() == null || command.getArguments().isEmpty()
                || command.getArguments().getFirst() == null) {
            return new JsonObject();
        }

        String rawArgsStr = command.getArguments().getFirst().toString().trim();
        if (!rawArgsStr.startsWith("{")) {
            return new JsonObject();
        }

        return JsonParser.parseString(rawArgsStr).getAsJsonObject();
    }

    @Override
    protected void executeCommands(CangjieEditorEventManager manager, Command command) {
    }

    private static PsiElement createLightMember(Project project, PsiFile file, String signature) {
        Language lang = file != null ? file.getLanguage() : Language.ANY;
        return new LightElement(PsiManager.getInstance(project), lang) {
            @Override
            public String toString() {
                return "CangjieLightMember:" + signature;
            }

            @Override
            public PsiElement getParent() {
                return file;
            }

            @Override
            public boolean isValid() {
                return true;
            }
        };
    }
}