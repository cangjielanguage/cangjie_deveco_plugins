/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

/**
 * cangjie constant
 *
 * @since 2022-12-1
 */
public interface OhConstants {
    /**
     * Cangjie debugger name in UI
     */
    String DEBUGGER_NAME_CANGJIE = "Cangjie";

    /**
     * Dual(Js + Cangjie) debugger name in UI
     */
    String DUAL_JS_CANGJIE_DEBUG_TYPE = "Dual (ArkTS/JS + Cangjie)";

    /**
     * INSTALL_NATIVE_CHECK_ERROR
     */
    String CONNECT_LLDB_SERVER_ERROR = "Connection shut down by remote side while waiting for "
            + "reply to initial handshake packet";

    /**
     * CANGJIE_PLUGIN_ID
     */
    String CANGJIE_PLUGIN_ID = "com.huawei.cangjie-support-plugin";
}
