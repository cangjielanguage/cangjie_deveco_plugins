/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp;

import lsp.service.LanguageService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class LanguageServiceTestBase {

    public static LanguageService languageService;

    @BeforeClass
    public static void beforeAll() throws Exception {
        if (languageService == null) {
            languageService = LspTestUtils.createTestLanguageService();
        }
    }

    @AfterClass
    public static void afterAll() {
        if (languageService != null) {
            languageService.shutdown();
            languageService = null;
        }
    }
}
