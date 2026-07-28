/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

interface A9 <T>{}
class AM{
    public var a: Int64
    public var b: Int64
    // fix classInit callee Hierarchy etc.
    public AM(x: Int64){
        print("b: Bool")
        a = x
        b = x
    }
    public operator func +( index: AM): AM{
        return index
    }
    // fix [] callee Hierarchy
    public operator func []( index: Int64, value!: Int64): Unit{
        this.b = value
        print("b: Bool")
    }
}
struct  b<T> <: A9<T> {}
func f4(): Int64{
    var a = AM(2)
    var b  = AM(7)
    var c = a+b
    a [2] = 4
    return 1
}