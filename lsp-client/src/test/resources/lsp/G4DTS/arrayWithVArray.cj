/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

public class WithinVArray{

    public var varray1 :VArray<Int8, $3> = [1, 2, 3]
    public var varray2 :VArray<Int16, $3> = [1, 2, 3]
    public var varray3 :VArray<Int32, $3> = [1, 2, 3]
    public var varray4 :VArray<Int64, $3> = [1, 2, 3]
    public var varray5 :VArray<IntNative, $3> = [1, 2, 3]
    public var varray6 :VArray<UInt8, $3> = [1, 2, 3]
    public var varray7 :VArray<UInt16, $3> = [1, 2, 3]
    public var varray8 :VArray<UInt32, $3> = [1, 2, 3]
    public var varray9 :VArray<UInt64, $3> = [1, 2, 3]
    public let varray11  = VArray<Int64, $10>({ i => i})
    public let varray12  = VArray<Int64, $5>(item: 0)

}