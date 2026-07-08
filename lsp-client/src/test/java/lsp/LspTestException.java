/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package lsp;

public class LspTestException extends RuntimeException {
    public LspTestException() {
        super();
    }

    public LspTestException(String message) {
        super(message);
    }

    public LspTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public LspTestException(Throwable cause) {
        super(cause);
    }

    protected LspTestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
