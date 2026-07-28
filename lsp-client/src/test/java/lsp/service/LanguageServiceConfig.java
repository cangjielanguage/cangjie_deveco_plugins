/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp.service;

import org.eclipse.lsp4j.ClientCapabilities;

// used to create a language service
public class LanguageServiceConfig {
    private String rootUri;
    private String execPath;
    private int timeout;
    private ClientCapabilities clientCapabilities;

    public LanguageServiceConfig() {
    }

    public LanguageServiceConfig(String rootUri, String execPath, int timeout) {
        this.rootUri = rootUri.trim();
        this.execPath = execPath.trim();
        this.timeout = timeout;
    }

    public ClientCapabilities getClientCapabilities() {
        return clientCapabilities;
    }

    public void setClientCapabilities(ClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    public String getRootUri() {
        return rootUri;
    }

    public void setRootUri(String rootUri) {
        this.rootUri = rootUri.trim();
    }

    public String getExecPath() {
        return execPath;
    }

    public void setExecPath(String execPath) {
        this.execPath = execPath.trim();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
