/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package A


open class A1 {
    
}

open class A2 <: A1 {

}

open class A3 <: A2 {

}

func test() {
    let a3 = A3();
}