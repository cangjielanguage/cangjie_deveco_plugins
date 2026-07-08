/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

class BigInt {
    let sign: String
    let value: Array<Int64>
    let len: Int64

    prop IS_ZERO: Bool {
        get() {
            value.size == 1 && value[0] == 0
        }