/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package core

public interface CharExtension {}

const asciiUpperToLowerDiff: UInt32 = 32

extend Char <: CharExtension {
    public func toAsciiUpper(): Char {
        if (this >= 'a' && this <= 'z') {
            return Char(UInt32(this) - asciiUpperToLowerDiff)