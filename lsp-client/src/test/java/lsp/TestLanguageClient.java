/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestLanguageClient implements LanguageClient {
    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        System.out.println(params);
        return CompletableFuture.supplyAsync(ApplyWorkspaceEditResponse::new);
    }

    @Override
    public CompletableFuture<Void> registerCapability(RegistrationParams params) {
        System.out.println(params);
        return CompletableFuture.supplyAsync(() -> null);
    }

    @Override
    public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
        System.out.println(params);
        return CompletableFuture.supplyAsync(() -> null);
    }

    @Override
    public void telemetryEvent(Object object) {
        System.out.println(object);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        System.out.println(diagnostics);
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        System.out.println(messageParams);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        System.out.println(requestParams);
        return CompletableFuture.supplyAsync(MessageActionItem::new);
    }

    @Override
    public void logMessage(MessageParams message) {
        System.out.println(message);
    }

    @Override
    public CompletableFuture<List<WorkspaceFolder>> workspaceFolders() {
        return CompletableFuture.supplyAsync(Collections::emptyList);
    }

    @Override
    public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
        System.out.println(configurationParams);
        return CompletableFuture.supplyAsync(Collections::emptyList);
    }
}
