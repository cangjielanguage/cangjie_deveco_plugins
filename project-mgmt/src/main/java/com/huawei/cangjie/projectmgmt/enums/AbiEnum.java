/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.enums;

/**
 * abi enum
 *
 * @since 2025-02-24
 */
public enum AbiEnum {
    ARM64_V8A("arm64-v8a"),
    X86_64("x86_64");

    private final String abi;

    AbiEnum(String abi) {
        this.abi = abi;
    }

    public String getAbi() {
        return abi;
    }
}
