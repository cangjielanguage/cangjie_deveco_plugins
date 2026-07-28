/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.refactor.extract;

import com.huawei.idea.edit.CangjieEditorEventManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.ui.UsageViewDescriptorAdapter;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;

import org.eclipse.lsp4j.Command;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows IntelliJ's Refactoring Preview before executing the LSP
 * extract-interface command.
 * The actual edits are still produced by the C++ language server. This
 * processor only provides
 * the preview/confirmation step expected by the IDE refactoring flow.
 *
 * @since 2026-05-28
 */
public class CangjieExtractInterfacePreviewProcessor extends BaseRefactoringProcessor {
    private static final String REFACTORING_ID = "cangjie.extract.interface";

    private final Command command;
    private final CangjieEditorEventManager manager;
    private final JsonObject extraOptions;
    private final PsiElement sourceElement;
    private final String sourceTypeName;
    private final String interfaceName;

    /**
     * Constructor for CangjieExtractInterfacePreviewProcessor.
     *
     * @param project The current active project.
     * @param sourceElement The source class/struct element.
     * @param request The preview request content.
     */
    public CangjieExtractInterfacePreviewProcessor(@NotNull Project project, @NotNull PsiElement sourceElement,
            @NotNull PreviewRequest request) {
        super(project);
        this.manager = request.manager;
        this.command = request.command;
        this.extraOptions = request.extraOptions;
        this.sourceElement = sourceElement;
        this.sourceTypeName = request.sourceTypeName;
        this.interfaceName = request.interfaceName;
        setPreviewUsages(true);
    }

    /**
     * Preview request encapsulating the context of the extract interface action.
     */
    public static class PreviewRequest {
        private final CangjieEditorEventManager manager;
        private final Command command;
        private final JsonObject extraOptions;
        private final String sourceTypeName;
        private final String interfaceName;

        /**
         * Constructor for PreviewRequest.
         *
         * @param manager The editor event manager.
         * @param command The LSP command.
         * @param extraOptions Additional refactoring options.
         * @param sourceTypeName The original concrete type name.
         * @param interfaceName The targeted interface name.
         */
        public PreviewRequest(@NotNull CangjieEditorEventManager manager, @NotNull Command command,
                              @NotNull JsonObject extraOptions, @NotNull String sourceTypeName,
                              @NotNull String interfaceName) {
            this.manager = manager;
            this.command = command;
            this.extraOptions = extraOptions;
            this.sourceTypeName = sourceTypeName;
            this.interfaceName = interfaceName;
        }
    }

    @Override
    protected UsageViewDescriptor createUsageViewDescriptor(@NotNull UsageInfo[] usages) {
        return new UsageViewDescriptorAdapter() {
            @Override
            public PsiElement[] getElements() {
                return PsiElement.EMPTY_ARRAY;
            }

            @Override
            public String getProcessedElementsHeader() {
                return "";
            }

            @Override
            public String getCodeReferencesText(int usagesCount, int filesCount) {
                return usagesCount + " type reference" + (usagesCount == 1 ? "" : "s") + " to replace with "
                        + interfaceName + " in " + filesCount + " file" + (filesCount == 1 ? "" : "s");
            }
        };
    }

    @Override
    protected UsageInfo @NotNull [] findUsages() {
        if (sourceTypeName.isEmpty()) {
            return UsageInfo.EMPTY_ARRAY;
        }

        List<UsageInfo> usages = new ArrayList<>();
        VirtualFile baseDir = myProject.getBaseDir();
        if (baseDir == null) {
            return UsageInfo.EMPTY_ARRAY;
        }

        PsiManager psiManager = PsiManager.getInstance(myProject);
        Pattern typeUsagePattern = Pattern
                .compile("(?<![A-Za-z0-9_])" + Pattern.quote(sourceTypeName) + "(?![A-Za-z0-9_])");
        VfsUtilCore.visitChildrenRecursively(baseDir, new VirtualFileVisitor<>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (file.isDirectory()) {
                    return !isIgnoredDirectory(file);
                }
                if (!isCangjieFile(file)) {
                    return true;
                }
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile == null) {
                    return true;
                }
                collectTypeUsages(new PreviewUsageContext(psiFile, psiFile.getText(), parseSelectedMembers()),
                        typeUsagePattern, usages);
                return true;
            }
        });
        return usages.toArray(UsageInfo.EMPTY_ARRAY);
    }

    @Override
    protected void performRefactoring(UsageInfo @NotNull [] usages) {
        JsonObject payload = getCommandPayload(command);
        extraOptions.addProperty("hasSelectedTypeReferences", true);
        extraOptions.add("selectedTypeReferences", buildSelectedTypeReferences(usages));
        payload.add("extraOptions", extraOptions);

        Map<?, ?> finalMap = new Gson().fromJson(payload, Map.class);
        command.setArguments(new ArrayList<>(List.of(finalMap)));

        ApplicationManager.getApplication()
                .invokeLater(() -> manager.executeCommands4Refactor(Collections.singletonList(command)));
    }

    @Override
    protected void refreshElements(PsiElement @NotNull [] elements) {
    }

    @Override
    protected String getCommandName() {
        return "Extract Interface";
    }

    @Override
    protected String getRefactoringId() {
        return REFACTORING_ID;
    }

    /**
     * Collects and triggers the internal usage collection process for preview
     * purposes.
     *
     * @return An array of collected UsageInfo elements.
     */
    public UsageInfo @NotNull [] collectPreviewUsages() {
        return findUsages();
    }

    private static class PreviewUsageContext {
        private final PsiFile psiFile;
        private final String text;
        private final String[] selectedMembers;

        private PreviewUsageContext(PsiFile psiFile, String text, String[] selectedMembers) {
            this.psiFile = psiFile;
            this.text = text;
            this.selectedMembers = selectedMembers;
        }
    }

    private JsonArray buildSelectedTypeReferences(UsageInfo[] usages) {
        JsonArray refs = new JsonArray();
        for (UsageInfo usage : usages) {
            PsiElement element = usage.getElement();
            if (element == null) {
                continue;
            }
            PsiFile psiFile = element.getContainingFile();
            if (psiFile == null) {
                continue;
            }
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile == null) {
                continue;
            }
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document == null) {
                continue;
            }

            TextRange elementRange = element.getTextRange();
            if (elementRange == null) {
                continue;
            }
            TextRange rangeInElement = usage.getRangeInElement();
            int startOffset = rangeInElement != null
                    ? elementRange.getStartOffset() + rangeInElement.getStartOffset()
                    : elementRange.getStartOffset();
            int endOffset = rangeInElement != null
                    ? elementRange.getStartOffset() + rangeInElement.getEndOffset()
                    : elementRange.getEndOffset();
            if (startOffset < 0 || endOffset < startOffset || endOffset > document.getTextLength()) {
                continue;
            }

            JsonObject ref = new JsonObject();
            ref.addProperty("uri", Path.of(virtualFile.getPath()).toUri().toString());
            JsonObject start = new JsonObject();
            start.addProperty("line", document.getLineNumber(startOffset));
            start.addProperty("character",
                    startOffset - document.getLineStartOffset(document.getLineNumber(startOffset)));
            JsonObject end = new JsonObject();
            end.addProperty("line", document.getLineNumber(endOffset));
            end.addProperty("character", endOffset - document.getLineStartOffset(document.getLineNumber(endOffset)));
            ref.add("start", start);
            ref.add("end", end);
            refs.add(ref);
        }
        return refs;
    }

    private void collectTypeUsages(PreviewUsageContext context, Pattern pattern, List<UsageInfo> usages) {
        Matcher matcher = pattern.matcher(context.text);
        while (matcher.find()) {
            PsiElement element = context.psiFile.findElementAt(matcher.start());
            if (isInComment(element)) {
                continue;
            }
            if (!isLikelyTypeUsage(context.text, matcher.start(), matcher.end())) {
                continue;
            }
            if (isUsedParameterTypeReference(context.text, matcher.start(), matcher.end())) {
                continue;
            }
            if (shouldKeepConcreteTypeReference(context, matcher.start(), matcher.end())) {
                continue;
            }
            usages.add(element == null
                    ? new UsageInfo(context.psiFile, matcher.start(), matcher.end())
                    : new UsageInfo(element, matcher.start() - element.getTextRange().getStartOffset(),
                            matcher.end() - element.getTextRange().getStartOffset(), false));
        }
    }

    private boolean isLikelyTypeUsage(String text, int start, int end) {
        int left = previousNonWhitespace(text, start - 1);
        if (left >= 0 && (text.charAt(left) == ':' || text.charAt(left) == '<' || text.charAt(left) == '&'
                || text.charAt(left) == ',' || text.charAt(left) == '(')) {
            return true;
        }

        int leftWordStart = previousIdentifierStart(text, start - 1);
        if (leftWordStart >= 0) {
            String leftWord = text.substring(leftWordStart, left + 1);
            if ("where".equals(leftWord) || "as".equals(leftWord)) {
                return true;
            }
        }

        if (looksLikeInheritClauseContext(text, start)) {
            return true;
        }
        int right = nextNonWhitespace(text, end);
        return right >= 0 && (text.charAt(right) == '>' || text.charAt(right) == '&' || text.charAt(right) == ','
                || text.charAt(right) == ')' || text.charAt(right) == '=');
    }

    private boolean isUsedParameterTypeReference(String text, int start, int end) {
        int colon = previousNonWhitespace(text, start - 1);
        if (colon < 0 || text.charAt(colon) != ':') {
            return false;
        }

        int paramNameEnd = previousNonWhitespace(text, colon - 1);
        if (paramNameEnd < 0 || !Character.isJavaIdentifierPart(text.charAt(paramNameEnd))) {
            return false;
        }
        int paramNameStart = paramNameEnd;
        while (paramNameStart > 0 && Character.isJavaIdentifierPart(text.charAt(paramNameStart - 1))) {
            paramNameStart--;
        }
        String paramName = text.substring(paramNameStart, paramNameEnd + 1);
        if (paramName.isEmpty()) {
            return false;
        }

        int funcKeyword = findPreviousKeyword(text, "func", paramNameStart);
        if (funcKeyword < 0 || !isInsideParameterList(text, funcKeyword, colon)) {
            return false;
        }

        int bodyStart = findFunctionBodyStart(text, end);
        if (bodyStart < 0) {
            return false;
        }

        String bodyText = extractFunctionBodyText(text, bodyStart);
        if (bodyText.isEmpty()) {
            return false;
        }
        return containsIdentifierUsage(bodyText, paramName);
    }

    private int previousNonWhitespace(String text, int index) {
        int localIndex = index;
        while (localIndex >= 0 && Character.isWhitespace(text.charAt(localIndex))) {
            localIndex--;
        }
        return localIndex;
    }

    private int nextNonWhitespace(String text, int index) {
        int localIndex = index;
        while (localIndex < text.length() && Character.isWhitespace(text.charAt(localIndex))) {
            localIndex++;
        }
        return localIndex < text.length() ? localIndex : -1;
    }

    private int previousIdentifierStart(String text, int index) {
        int end = index;
        while (end >= 0 && Character.isWhitespace(text.charAt(end))) {
            end--;
        }
        if (end < 0 || !Character.isJavaIdentifierPart(text.charAt(end))) {
            return -1;
        }
        int start = end;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        return start;
    }

    private int findPreviousKeyword(String text, String keyword, int before) {
        int limit = Math.min(before - keyword.length(), text.length() - keyword.length());
        for (int i = limit; i >= 0; i--) {
            if (!text.regionMatches(i, keyword, 0, keyword.length())) {
                continue;
            }
            boolean leftBoundary = i == 0 || !Character.isJavaIdentifierPart(text.charAt(i - 1));
            int rightIndex = i + keyword.length();
            boolean rightBoundary = rightIndex >= text.length()
                    || !Character.isJavaIdentifierPart(text.charAt(rightIndex));
            if (leftBoundary && rightBoundary) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInsideParameterList(String text, int funcKeyword, int offset) {
        int parenDepth = 0;
        boolean seenOpenParen = false;
        for (int i = funcKeyword + 4; i < offset && i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '(') {
                parenDepth++;
                seenOpenParen = true;
            } else if (ch == ')' && parenDepth > 0) {
                parenDepth--;
            } else if ((ch == '{' || ch == '=') && parenDepth == 0) {
                return false;
            } else {
                continue;
            }
        }
        return seenOpenParen && parenDepth > 0;
    }

    private int findFunctionBodyStart(String text, int from) {
        int parenDepth = 0;
        for (int i = from; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '(') {
                parenDepth++;
            } else if (ch == ')' && parenDepth > 0) {
                parenDepth--;
            } else if (parenDepth == 0 && ch == '{') {
                return i;
            } else if (parenDepth == 0 && ch == '=' && i + 1 < text.length() && text.charAt(i + 1) == '>') {
                return i;
            } else if (parenDepth == 0 && ch == ';') {
                return -1;
            } else {
                continue;
            }
        }
        return -1;
    }

    private String extractFunctionBodyText(String text, int bodyStart) {
        if (bodyStart < 0 || bodyStart >= text.length()) {
            return "";
        }
        if (text.charAt(bodyStart) == '{') {
            int braceDepth = 1;
            for (int i = bodyStart + 1; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '{') {
                    braceDepth++;
                } else if (ch == '}') {
                    braceDepth--;
                    if (braceDepth == 0) {
                        return text.substring(bodyStart + 1, i);
                    }
                } else {
                    continue;
                }
            }
            return "";
        }
        if (text.charAt(bodyStart) == '=' && bodyStart + 1 < text.length() && text.charAt(bodyStart + 1) == '>') {
            int lineEnd = text.indexOf('\n', bodyStart + 2);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            return text.substring(bodyStart + 2, lineEnd);
        }
        return "";
    }

    private boolean containsIdentifierUsage(String text, String identifier) {
        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(identifier) + "(?![A-Za-z0-9_])");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int right = nextNonWhitespace(text, matcher.end());
            if (right >= 0 && text.charAt(right) == ':') {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean shouldKeepConcreteTypeReference(PreviewUsageContext context, int start, int end) {
        int colon = previousNonWhitespace(context.text, start - 1);
        if (colon < 0 || context.text.charAt(colon) != ':') {
            return false;
        }

        int nameEnd = previousNonWhitespace(context.text, colon - 1);
        if (nameEnd < 0 || !Character.isJavaIdentifierPart(context.text.charAt(nameEnd))) {
            return false;
        }
        int nameStart = nameEnd;
        while (nameStart > 0 && Character.isJavaIdentifierPart(context.text.charAt(nameStart - 1))) {
            nameStart--;
        }
        String variableName = context.text.substring(nameStart, nameEnd + 1);
        if (variableName.isEmpty()) {
            return false;
        }

        if (context.selectedMembers.length == 0) {
            return false;
        }

        int searchFrom = end;
        Pattern accessPattern = Pattern
                .compile("(?<![A-Za-z0-9_])" + Pattern.quote(variableName) + "\\s*\\.\\s*([A-Za-z_][A-Za-z0-9_]*)");
        Matcher matcher = accessPattern.matcher(context.text);
        matcher.region(searchFrom, context.text.length());
        while (matcher.find()) {
            String memberName = matcher.group(1);
            if (!isSelectedMemberName(context.selectedMembers, memberName)) {
                return true;
            }
        }
        return false;
    }

    private String[] parseSelectedMembers() {
        if (!extraOptions.has("selectedMembers") || !extraOptions.get("selectedMembers").isJsonArray()) {
            return new String[0];
        }
        JsonArray members = extraOptions.getAsJsonArray("selectedMembers");
        String[] result = new String[members.size()];
        for (int i = 0; i < members.size(); i++) {
            result[i] = members.get(i).getAsString();
        }
        return result;
    }

    private boolean isSelectedMemberName(String[] selectedMembers, String memberName) {
        for (String selectedMember : selectedMembers) {
            int paren = selectedMember.indexOf('(');
            if (paren < 0) {
                continue;
            }
            String name = selectedMember.substring(0, paren).trim();
            if (memberName.equals(name)) {
                return true;
            }
            int lastSpace = name.lastIndexOf(' ');
            if (lastSpace >= 0 && memberName.equals(name.substring(lastSpace + 1))) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeInheritClauseContext(String text, int start) {
        int scan = start - 1;
        while (scan >= 0 && Character.isWhitespace(text.charAt(scan))) {
            scan--;
        }
        if (scan < 1 || text.charAt(scan) != ':' || text.charAt(scan - 1) != '<') {
            return false;
        }
        int wordEnd = scan - 2;
        while (wordEnd >= 0 && Character.isWhitespace(text.charAt(wordEnd))) {
            wordEnd--;
        }
        int wordStart = wordEnd;
        while (wordStart >= 0 && Character.isJavaIdentifierPart(text.charAt(wordStart))) {
            wordStart--;
        }
        if (wordEnd < 0) {
            return false;
        }
        String keyword = text.substring(wordStart + 1, wordEnd + 1);
        return "class".equals(keyword) || "struct".equals(keyword) || "interface".equals(keyword)
                || "enum".equals(keyword) || "extend".equals(keyword);
    }

    private boolean isInComment(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof PsiComment) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private boolean isCangjieFile(VirtualFile file) {
        String extension = file.getExtension();
        if ("cj".equals(extension)) {
            return true;
        }
        FileType fileType = file.getFileType();
        return "Cangjie".equalsIgnoreCase(fileType.getName());
    }

    private boolean isIgnoredDirectory(VirtualFile file) {
        String name = file.getName();
        return ".git".equals(name) || ".idea".equals(name) || "build".equals(name) || "cmake-build-debug".equals(name)
                || "cmake-build-release".equals(name) || "output".equals(name) || "target".equals(name);
    }

    private JsonObject getCommandPayload(Command command) {
        if (command.getArguments() == null || command.getArguments().isEmpty()
                || command.getArguments().getFirst() == null) {
            return new JsonObject();
        }

        Object arg0 = command.getArguments().getFirst();

        if (arg0 instanceof JsonObject) {
            return (JsonObject) arg0;
        }

        if (arg0 instanceof Map) {
            JsonElement jsonElement = new Gson().toJsonTree(arg0);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            }
            return new JsonObject();
        }

        String rawArgsStr = arg0.toString().trim();
        return com.google.gson.JsonParser.parseString(rawArgsStr).getAsJsonObject();
    }
}