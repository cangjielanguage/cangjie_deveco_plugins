/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package pkg_composite

from std import collection.*
import pkg_class.*
import pkg_struct.*
import pkg_enum.*
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
    public var varray10 :VArray<UIntNative, $3> = [1, 2, 3]
//    public let varray11  = VArray<Int64, $10>({ i => i})
//    public let varray12  = VArray<Int64, $5>(item: 0)
    public let varray13 :VArray<Float16, $3> = [0.23, 0.45, 0.56]
    public let varray14 :VArray<Float32, $3> = [0.24, 0.46, 0.57]
    public let varray15 :VArray<Float64, $3> = [0.222, 0.333, 0.444]
    public let varray16 :VArray<Bool, $3> = ['a' != 'a', true, false]
    public let varray17 :VArray<Char, $10> = ['a', 'b', '\\', '\u{597d}', '\"', 'f', 'g', 'h', 'i', 'j']
//   public var varray18 :VArray<String, $3> = ["100", "cangjie", "仓颉"]
    //（非）泛型类做数组元素
//    public var varray19 : VArray<ClassViaInherit, $2> =[ClassViaInherit(32,par_2:21), ClassViaInherit()]
    //（非）泛型struct做数组元素
    public var varray20 : VArray<StructViaExtend, $2> = [StructViaExtend(32,21), StructViaExtend(69,457)]
    //（非）泛型enum做数组元素
//    public var varray21 : VArray<TimeUnit1, $2> = [TimeUnit1.Year(19), TimeUnit1.Year]

}