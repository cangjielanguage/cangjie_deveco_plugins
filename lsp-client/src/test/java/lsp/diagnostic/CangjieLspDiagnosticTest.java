/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

//package lsp.diagnostic;
//
//import lsp.service.LanguageService;
//import lsp.service.LanguageServiceConfig;
//import lsp.service.LanguageServiceFactory;
//import lsp.service.LanguageServiceUtils;
//import lsp.LspTestException;
//import lsp.LspTestUtils;
//import lsp.TestLanguageClient;
//import org.apache.commons.io.IOUtils;
//import org.eclipse.lsp4j.PublishDiagnosticsParams;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.charset.StandardCharsets;
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.TimeUnit;
//
//import static lsp.LspTestUtils.TEST_EXEC_PATH;
//import static lsp.LspTestUtils.TEST_PROJECT_URI;
//import static org.junit.Assert.assertFalse;
//
//public class CangjieLspDiagnosticTest {
//
//    private static final BlockingDeque<PublishDiagnosticsParams> resultQueue = new LinkedBlockingDeque<>();
//
//    private static LanguageService languageService;
//
//    @BeforeClass
//    public static void beforeAll() throws Exception {
//        LanguageServiceConfig config = new LanguageServiceConfig();
//        config.setExecPath(TEST_EXEC_PATH);
//        config.setRootUri(TEST_PROJECT_URI);
//        config.setTimeout(1000);
//        config.setClientCapabilities(LanguageServiceUtils.CLIENT_CAPABILITIES);
//        languageService = new LanguageServiceFactory().createLanguageService(config, new TestLanguageClient(){
//            @Override
//            public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
//                resultQueue.push(diagnostics);
//            }
//        });
//    }
//
//    @Test
//    public void test_diagnostic() throws URISyntaxException, IOException {
//        String uri = LspTestUtils.resourcePathToUri("lsp/diagnostic/DiagnosticTestSource.cj");
//        languageService.notifyDidOpenFile(uri, IOUtils.toString(new URI(uri), StandardCharsets.UTF_8));
//        PublishDiagnosticsParams publishDiagnosticsParams = testDiagnostic();
//        assertFalse(publishDiagnosticsParams.getDiagnostics().isEmpty());
//    }
//
//    private static PublishDiagnosticsParams testDiagnostic() {
//        try {
//            return resultQueue.pollFirst(5000, TimeUnit.MILLISECONDS);
//        } catch (Exception e) {
//            throw new LspTestException(e);
//        }
//    }
//}
