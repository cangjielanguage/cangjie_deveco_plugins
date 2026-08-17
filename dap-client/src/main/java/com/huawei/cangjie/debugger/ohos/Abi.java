/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;

import org.jetbrains.annotations.Nullable;

/**
 * Abi
 *
 * @since 2022-12-1
 */
public enum Abi {
    OHOS_ARMEABI_V7A("armeabi-v7a", 4, "arm-linux-ohos", DeviceOs.OHOS),
    OHOS_ARM64_V8A("arm64-v8a", 8, "aarch64-linux-ohos", DeviceOs.OHOS),
    OHOS_X86_64("x86_64", 8, "x86_64-linux-ohos", DeviceOs.OHOS),
    HOS_ARMEABI_V7A("armeabi-v7a", 4, "arm-linux-hos", DeviceOs.HOS),
    HOS_ARM64_V8A("arm64-v8a", 8, "aarch64-linux-hos", DeviceOs.HOS),
    HOS_X86_64("x86_64", 8, "x86_64-linux-hos", DeviceOs.HOS);

    private final String mAbi;
    private final int mAddressSizeInBytes;
    private final String mLldbType;
    private final DeviceOs deviceOs;

    Abi(String abi, int addrSizeInBytes, String lldbType, DeviceOs deviceOs) {
        this.mAbi = abi;
        this.mAddressSizeInBytes = addrSizeInBytes;
        this.mLldbType = lldbType;
        this.deviceOs = deviceOs;
    }

    /**
     * get enum in abi (HOS interface)
     *
     * @param abi abi
     * @param deviceOs device operating system
     * @return Abi
     */
    @Nullable
    public static Abi getEnum(String abi, DeviceOs deviceOs) {
        if ("default".equalsIgnoreCase(abi)) {
            return OHOS_ARMEABI_V7A;
        }
        for (Abi value : values()) {
            if (value.deviceOs == deviceOs && value.mAbi.equals(abi)) {
                return value;
            }
        }
        return CodeCheckByPassUtils.getNull();
    }

    /**
     * get lldb path
     *
     * @return java.lang.String
     */
    public String getLldbType() {
        return this.mLldbType;
    }

    /**
     * get address size bytes
     *
     * @return address size bytes
     */
    public int getAddressSizeInBytes() {
        return this.mAddressSizeInBytes;
    }

    @Override
    public String toString() {
        return this.mAbi;
    }
}
