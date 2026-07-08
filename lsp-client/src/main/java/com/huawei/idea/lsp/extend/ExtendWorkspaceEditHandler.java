/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.lsp.extend;

import static com.huawei.idea.lsp.extend.ExtendLanguageClient.removeImportsFormatCode;
import static com.huawei.idea.lsp.extend.ExtendLanguageClient.implementMembersFormatCode;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;

import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;
import org.wso2.lsp4intellij.requests.WorkspaceEditHandler;
import org.wso2.lsp4intellij.utils.ApplicationUtils;
import org.wso2.lsp4intellij.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ExtendWorkspaceEditHandler extends the capability of base WorkspaceEditHandler
 * to support multi-file resource operations and customized text edits with undo support.
 *
 * @since 2026-05-28
 */
public class ExtendWorkspaceEditHandler extends WorkspaceEditHandler {
    private static final String ACTION_NAME = "LSP edits";
    private static final Logger LOG = Logger.getInstance(ExtendWorkspaceEditHandler.class);

    /**
     * Applies a WorkspaceEdit that performs a remove-imports action and schedules code reformatting.
     *
     * @param edit The WorkspaceEdit object provided by the LSP server.
     * @param name The descriptive name for the refactoring action.
     */
    public static void removeImportsApplyEdit(WorkspaceEdit edit, String name) {
        CompletableFuture<Boolean> response = applyEdit(edit, name, new ArrayList<>());
        response.thenApply(result -> {
            if (result) {
                ApplicationManager.getApplication().invokeAndWait(() -> removeImportsFormatCode(edit));
            }
            return result;
        });
    }

    /**
     *
     * implement members applyEdit and format
     *
     * @param edit           edit
     * @param name           name
     */
    public static void implementMembersApplyEdit(WorkspaceEdit edit, String name) {
        CompletableFuture<Boolean> response = applyEdit(edit, name, new ArrayList<>());
        response.thenApply(result -> {
            if (result) {
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    implementMembersFormatCode(edit);
                });
            }
            return result;
        });
    }

    /**
     * Standard entry point to apply a basic WorkspaceEdit wrapper using default action tags.
     *
     * @param edit Target configuration maps payload.
     * @return Future pipeline tracking execution status.
     */
    public static CompletableFuture<Boolean> applyEdit(WorkspaceEdit edit) {
        return applyEdit(edit, ACTION_NAME, new ArrayList<>());
    }

    /**
     * Applies the workspace edits securely with specific trackable undo steps and post-cleanup triggers.
     *
     * @param edit    The workspace change tree collection.
     * @param name    Command title for refactoring identification.
     * @param toClose Explicit document descriptors targeted to refresh/close.
     * @return Future callback monitoring final layout mapping status.
     */
    public static CompletableFuture<Boolean> applyEdit(WorkspaceEdit edit, String name, List<VirtualFile> toClose) {
        if (edit == null) {
            return CompletableFuture.completedFuture(false);
        }

        String newName = name != null ? name : ACTION_NAME;
        Project[] curProject = new Project[]{null};
        List<VirtualFile> openedEditors = new ArrayList<>();
        List<Runnable> toApply = new ArrayList<>();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (edit.getDocumentChanges() != null) {
            handleDocumentChangesManual(edit.getDocumentChanges(), curProject, toApply, openedEditors, newName);
        }

        if (edit.getChanges() != null && !edit.getChanges().isEmpty()) {
            if (!checkClosed(edit.getChanges())) {
                return CompletableFuture.completedFuture(false);
            }
            WorkspaceEditHandler.handleChanges(edit.getChanges(), curProject, toApply, openedEditors, newName);
        }

        ApplicationUtils.invokeLater(() -> {
            if (toApply.isEmpty()) {
                future.complete(true);
                return;
            }
            ApplicationUtils.writeAction(() -> {
                try {
                    CommandProcessor.getInstance().executeCommand(curProject[0], () -> {
                        for (Runnable runnable : toApply) {
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    }, newName, "LSPPlugin", UndoConfirmationPolicy.DEFAULT, false);

                    if (curProject[0] != null) {
                        FileEditorManager manager = FileEditorManager.getInstance(curProject[0]);
                        closeFilesSafely(manager, openedEditors);
                        closeFilesSafely(manager, toClose);
                    }
                    future.complete(true);
                } catch (ProcessCanceledException e) {
                    future.completeExceptionally(e);
                    LOG.warn("Command apply edit failed.");
                }
            });
        });
        return future;
    }

    private static void handleDocumentChangesManual(List<Either<TextDocumentEdit, ResourceOperation>> docChanges,
                                                    Project[] curProject, List<Runnable> toApply,
                                                    List<VirtualFile> openedEditors, String name) {
        for (Either<TextDocumentEdit, ResourceOperation> either : docChanges) {
            if (either.isRight()) {
                ResourceOperation op = either.isRight() ? either.getRight() : null;
                if (op instanceof CreateFile) {
                    String uri = ((CreateFile) op).getUri();
                    toApply.add(() -> performCreateFileVFS(curProject, uri));
                }
            }
            if (either.isLeft()) {
                TextDocumentEdit docEdit = either.getLeft();
                toApply.add(() -> performApplyTextEdit(curProject, docEdit, openedEditors, name));
            }
        }
    }

    private static void performCreateFileVFS(Project[] curProject, String fileUri) {
        try {
            URI uri = new URI(FileUtils.sanitizeURI(fileUri));
            File ioFile = new File(uri);
            File parentDirFile = ioFile.getParentFile();
            if (parentDirFile == null) {
                LOG.error("Cannot get parent directory for: " + fileUri);
                return;
            }
            // 递归创建父目录（如果不存在）
            if (!parentDirFile.exists()) {
                if (!parentDirFile.mkdirs()) {
                    LOG.error("Failed to create parent directory: " + parentDirFile.getCanonicalPath());
                    return;
                }
            }
            // 获取父目录的 VirtualFile
            VirtualFile parentDir = LocalFileSystem.getInstance()
                    .refreshAndFindFileByPath(parentDirFile.getCanonicalPath());
            if (parentDir == null) {
                LOG.error("Parent directory not found after creation: " + parentDirFile.getCanonicalPath());
                return;
            }
            // 创建新文件
            VirtualFile newFile = parentDir.createChildData(null, ioFile.getName());
            if (curProject[0] == null) {
                curProject[0] = ProjectUtil.guessProjectForFile(newFile);
            }
            newFile.refresh(false, false);
        } catch (URISyntaxException | IOException e) {
            LOG.error("VFS file creation failed: " + fileUri);
        }
    }

    /**
     * Executes localized text substitutions safely by opening text buffering zones beforehand.
     *
     * @param curProject    Array container enclosing the active project.
     * @param docEdit       LSP specific compilation text modification set descriptor.
     * @param openedEditors Collector listing documents currently evaluated.
     * @param name          The targeted operation label mapping text.
     */
    private static void performApplyTextEdit(Project[] curProject, TextDocumentEdit docEdit,
                                             List<VirtualFile> openedEditors, String name) {
        String uri = docEdit.getTextDocument().getUri();
        VirtualFile file = FileUtils.virtualFileFromURI(uri);
        if (file == null || curProject[0] == null) {
            return;
        }

        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileEditorManager manager = FileEditorManager.getInstance(curProject[0]);
            Editor editor = manager.openTextEditor(new OpenFileDescriptor(curProject[0], file), false);
            if (editor != null) {
                EditorEventManager eventManager = EditorEventManagerBase.forEditor(editor);
                if (eventManager != null) {
                    Integer docVersion = docEdit.getTextDocument().getVersion();
                    int version = docVersion == null ? Integer.MAX_VALUE : docVersion;
                    Runnable r = eventManager.getEditsRunnable(version, docEdit.getEdits(), name, false);
                    if (r != null) {
                        r.run();
                    }
                }
                openedEditors.add(file);
            }
        });
    }

    private static boolean checkClosed(Map<String, List<TextEdit>> changes) {
        Project curProj = null;
        Editor curEditor = null;
        boolean hasClosed = false;
        for (String key : changes.keySet()) {
            URI uri = URI.create(key);
            String realUri;
            try {
                realUri = Path.of(uri).toRealPath().toUri().toString();
            } catch (IOException exception) {
                return false;
            }

            realUri = FileUtils.sanitizeURI(realUri);
            EditorEventManager manager = EditorEventManagerBase.forUri(realUri);
            if (manager == null) {
                hasClosed = true;
                continue;
            }
            if (manager.editor != null && curEditor == null) {
                curProj = manager.editor.getProject();
                curEditor = getSelectedEditor(curProj);
            }
        }
        if (hasClosed) {
            if (curProj != null) {
                CommonRefactoringUtil.showErrorHint(curProj,
                        curEditor,
                        "Cannot perform this operation while the file is closed."
                                + " Open the file first and try again.", "Refactoring Failed",
                        null);
            }
            return false;
        }
        return true;
    }

    private static void closeFilesSafely(FileEditorManager manager, Collection<VirtualFile> files) {
        for (VirtualFile f : files) {
            if (f != null && f.isValid()) {
                manager.closeFile(f);
            }
        }
    }

    @Nullable
    private static Editor getSelectedEditor(Project project) {
        if (project == null) {
            return null;
        }
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        return (selectedEditor instanceof TextEditor textEditor) ? textEditor.getEditor() : null;
    }
}
