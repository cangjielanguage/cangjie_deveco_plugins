/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp.service;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// create language service
public class LanguageServiceFactory {

    // should be called in background thread
    public LanguageService createLanguageService(LanguageServiceConfig config, LanguageClient languageClient) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(config.getExecPath());
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        if (!process.isAlive()) {
            throw new Exception("process start failed");
        }
        Launcher<LanguageServer> launcher = Launcher.createLauncher(
                languageClient,
                LanguageServer.class, process.getInputStream(), process.getOutputStream(),
                Executors.newCachedThreadPool(), fun -> fun);
        launcher.startListening();
        LanguageServer languageServer = launcher.getRemoteProxy();
        Thread.sleep(1000);
        InitializeParams initParams = new InitializeParams();
        initParams.setProcessId(111);
        initParams.setRootUri(LanguageServiceUtils.sanitizeURI(config.getRootUri()));
        try {
            initParams.setRootPath(new URL(config.getRootUri()).getPath());
        } catch (MalformedURLException ignored) {
        }
        initParams.setCapabilities(config.getClientCapabilities());
        CompletableFuture<InitializeResult> resultFuture = languageServer.initialize(initParams);
        InitializeResult result = resultFuture.get(3000, TimeUnit.MILLISECONDS);
        languageServer.initialized(new InitializedParams());
        ServerCapabilities serverCapabilities = result.getCapabilities();
        return new LanguageService(languageClient, languageServer, serverCapabilities);
    }

}
