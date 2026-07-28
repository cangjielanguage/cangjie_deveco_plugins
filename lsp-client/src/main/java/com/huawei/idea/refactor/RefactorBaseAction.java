/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.refactor;

import com.huawei.idea.edit.CangjieEditorEventManager;
import com.huawei.idea.lsp.utils.CangJieLanguage;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.actions.BasePlatformRefactoringAction;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;
import org.wso2.lsp4intellij.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RefactorBaseAction
 *
 * @since 2025-06-09
 */
public abstract class RefactorBaseAction extends BasePlatformRefactoringAction {
    /**
     * Command
     */
    @NotNull
    protected Command command;

    /**
     * title
     */
    protected String title;

    /**
     * validCommands
     */
    protected ArrayList<Command> validCommands;

    @Override
    @Nullable
    protected abstract RefactoringActionHandler getRefactoringHandler(@NotNull RefactoringSupportProvider provider);

    @Override
    protected boolean isAvailableInEditorOnly() {
        return true;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(false);

        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return;
        }
        if (!isAvailableForFile(file) || file instanceof PsiCompiledElement && disableOnCompiledElement()) {
            return;
        }

        DataContext dataContext = e.getDataContext();
        PsiElement element = findRefactoringTargetInEditor(dataContext, this::isAvailableForLanguage);
        if (element == null) {
            return;
        }

        if (enable(e) && isAvailableOnElementInEditorAndFile(element, editor, file, dataContext, e.getPlace())) {
            presentation.setEnabledAndVisible(true);
        }
    }

    /**
     * refactor action default enable method
     *
     * @param event action event
     * @return enable
     */
    protected boolean enable(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return false;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.getSelectionEnd();
        Command fromTweaks = getCommandFromTweaks(editor, selectionStart, selectionEnd);
        if (fromTweaks == null) {
            return false;
        }
        command = fromTweaks;
        return command.getCommand() != null;
    }

    /**
     * enableForSingleElement
     *
     * return true if only selects one element and has command to run.
     *
     * @param event event
     * @return is enable
     */
    protected boolean enableForSingleElement(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return false;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.hasSelection() ? selectionModel.getSelectionEnd() - 1 : selectionStart;

        String uri = FileUtils.documentToUriInLocation(editor.getDocument());
        PsiFile psiFile = FileUtils.psiFileFromUri(editor.getProject(), uri);
        if (psiFile == null || !psiFile.isValid()) {
            return false;
        }
        PsiElement eleStart = psiFile.findElementAt(selectionStart);
        PsiElement eleEnd = psiFile.findElementAt(selectionEnd);
        if (eleStart != eleEnd) {
            return false;
        }

        Command fromTweaks = getCommandFromTweaks(editor, selectionStart, selectionEnd);
        if (fromTweaks == null) {
            return false;
        }
        command = fromTweaks;
        return command.getCommand() != null;
    }

    /**
     * getCommandFromTweaks
     * check if CodeAction Response contains tweak id
     *
     * @param editor editor
     * @param startOffset startOffset
     * @param endOffset endOffset
     * @return Command
     */
    @Nullable
    protected Command getCommandFromTweaks(Editor editor, int startOffset, int endOffset) {
        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        if (!(manager instanceof CangjieEditorEventManager extendManager)) {
            return new Command();
        }
        List<Either<Command, CodeAction>> res = extendManager.codeAction4Refactor(startOffset, endOffset);
        for (Either<Command, CodeAction> either : res) {
            Command cmd = either.isLeft() ? either.getLeft() : either.getRight().getCommand();
            if (cmd.getTitle().startsWith(title)) {
                return cmd;
            }
        }
        return new Command();
    }

    protected ArrayList<Command> getValidCommands(Editor editor, int startOffset, int endOffset) {
        ArrayList<Command> commands = new ArrayList<>();
        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        if (!(manager instanceof CangjieEditorEventManager extendManager)) {
            return commands;
        }
        List<Either<Command, CodeAction>> res = extendManager.codeAction4Refactor(startOffset, endOffset);
        for (Either<Command, CodeAction> either : res) {
            Command cmd = either.isLeft() ? either.getLeft() : either.getRight().getCommand();
            commands.add(cmd);
        }
        return commands;
    }

    @Override
    protected boolean isAvailableForFile(PsiFile file) {
        return file.getLanguage().is(CangJieLanguage.INSTANCE);
    }
}
