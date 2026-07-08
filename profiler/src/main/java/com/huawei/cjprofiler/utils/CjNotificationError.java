/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.utils;

import com.huawei.deveco.insight.ohos.common.NotificationError;

/**
 * CjNotificationError
 *
 * @since 2026-06-04
 */
public class CjNotificationError extends NotificationError {
    /**
     * CJ_IMPORT_EXCEEDS_QUANTITY_LIMIT_ERROR
     */
    public static final NotificationError CJ_IMPORT_EXCEEDS_QUANTITY_LIMIT_ERROR =
        new CjNotificationError("00702211", "dlg.import.exceeds.quantity.type.limit.cj");

    private CjNotificationError(String code, String errorDescription) {
        super(code, errorDescription);
    }
}