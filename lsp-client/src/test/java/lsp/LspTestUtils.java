/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp;

import lsp.service.LanguageService;
import lsp.service.LanguageServiceConfig;
import lsp.service.LanguageServiceFactory;
import lsp.service.LanguageServiceUtils;

public class LspTestUtils {

    public static final String TEST_EXEC_PATH = "D:\\quickapp_demo\\SWANUI_HOME\\6.1.0\\new_version\\LSPServer.exe";

    public static final String TEST_PROJECT_URI = "file:/D:/projects/lsp-temp/LSPClient_IDEA";

    public static LanguageService createTestLanguageService() throws Exception {
        LanguageServiceConfig config = new LanguageServiceConfig();
        config.setExecPath(TEST_EXEC_PATH);
        config.setRootUri(TEST_PROJECT_URI);
        config.setTimeout(1000);
        config.setClientCapabilities(LanguageServiceUtils.CLIENT_CAPABILITIES);
        return new LanguageServiceFactory().createLanguageService(config, new TestLanguageClient());
    }

    public static String resourcePathToUri(String srcPath) {
        try {
            return LspTestUtils.class.getClassLoader().getResource(srcPath).toURI().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
