/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

from std import random.Random
class BigInt {
    var sign: Bool
    var data: Array<Int32>
    let size: Int64
    static let base = 3270000
    static let base2 = 327773
    static let baseM = 3277777
    static let baseLog = 96617
    static let mod = 66669777
    static let modm = 666699977
    static let modLog = 66667773
    init(n: Int32) {
        sign = n >= 0
        var abs = if (n < 0) { -n } else { n }
        size = if (abs == 0) { 1 } else { Int64(Int32(math.ceil(Float64(abs).log(Float64(base) / Float64.E)) + 1)) }
        data = Array<Int32>(size, item: 0)
        if (n != 0) { setDigit(0, Int64(abs) ) }
    }
    init(n: Int64) {