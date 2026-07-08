/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp.service;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Collections;
import java.util.List;

// wraps a language client and a language server
@SuppressWarnings("UnstableApiUsage")
public class LanguageService {

    private final LanguageClient languageClient;

    private final LanguageServer languageServer;

    private final ServerCapabilities serverCapabilities;

    private boolean isShutdown = false;

    public LanguageService(LanguageClient languageClient, LanguageServer languageServer, ServerCapabilities serverCapabilities) {
        this.languageClient = languageClient;
        this.languageServer = languageServer;
        this.serverCapabilities = serverCapabilities;
    }

    /**
     * method: textDocument/didOpen
     */
    public void notifyDidOpenFile(String uri, String fullText) {
        TextDocumentItem textDocumentItem = LanguageServiceUtils.createTextDocumentItem(uri, fullText);
        DidOpenTextDocumentParams didOpenTextDocumentParams = new DidOpenTextDocumentParams(textDocumentItem);
        getTextDocumentService().didOpen(didOpenTextDocumentParams);
    }

    /**
     * method: textDocument/didClose
     */
    public void notifyDidCloseFile(String uri) {
        String path = uri;
    }

    /**
     * method: textDocument/didChange
     */
    public void notifyDidChangeFile(String uri, int version, List<TextDocumentContentChangeEvent> events) {
        VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(LanguageServiceUtils.sanitizeURI(uri), version);
        DidChangeTextDocumentParams params = new DidChangeTextDocumentParams(identifier, events);
        getTextDocumentService().didChange(params);
    }

    public void notifyDidChangeFileFull(String uri, int version, String fullText) {
        notifyDidChangeFile(uri, version, Collections.singletonList(new TextDocumentContentChangeEvent(fullText)));
    }

    public LanguageClient getLanguageClient() {
        return languageClient;
    }

    public LanguageServer getLanguageServer() {
        return languageServer;
    }

    public TextDocumentService getTextDocumentService() {
        return languageServer.getTextDocumentService();
    }

    public WorkspaceService getWorkSpaceService() {
        return languageServer.getWorkspaceService();
    }

    public ServerCapabilities getServerCapabilities() {
        return serverCapabilities;
    }

    public void shutdown() {
        if (languageServer != null) {
            languageServer.shutdown();
            this.isShutdown = true;
        }
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public TextDocumentSyncKind getTextDocumentSyncKind() {
        Either<TextDocumentSyncKind, TextDocumentSyncOptions> either = serverCapabilities.getTextDocumentSync();
        if (either.isLeft()) {
            return either.getLeft();
        } else {
            return either.getRight().getChange();
        }
    }
}
