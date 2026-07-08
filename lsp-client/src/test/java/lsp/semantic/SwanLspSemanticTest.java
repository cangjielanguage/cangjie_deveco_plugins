/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp.semantic;
/*
import org.wso2.lsp4intellij.contributors.semantic.SemanticTokenData;
import lsp.LanguageServiceTestBase;
import lsp.LspTestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("UnstableApiUsage")
public class SwanLspSemanticTest extends LanguageServiceTestBase {
    @Test
    public void test_semantic_full() throws IOException, InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
        String srcPath = "lsp/semantic/SemanticTestSource.cj";
        String uri = LspTestUtils.resourcePathToUri(srcPath);
        languageService.notifyDidOpenFile(uri, IOUtils.toString(new URI(uri), StandardCharsets.UTF_8));
        Thread.sleep(1500);
        List<SemanticTokenData> dataList = languageService.requestSemanticTokensFull(uri).get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(dataList);
        assertFalse(dataList.isEmpty());
        URL url = LspTestUtils.class.getClassLoader().getResource(srcPath);
        String full = IOUtils.toString(url, StandardCharsets.UTF_8);
        String[] lines = full.split("\r\n");
        for (SemanticTokenData data : dataList) {
            System.out.println(lines[data.getLine()].substring(data.getCharacter(), data.getCharacter() + data.getLength()) + " - " + data.getTokenType());
        }
    }
}
*/