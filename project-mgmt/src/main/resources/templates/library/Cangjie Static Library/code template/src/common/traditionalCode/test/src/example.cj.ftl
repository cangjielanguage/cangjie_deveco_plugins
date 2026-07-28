<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

/**
 * Created on 2025/2/26
 */
package ${cjPackageName}_local_test

import std.unittest.expectEqual
import std.unittest.testmacro.Expect

func unittest() {
    let a = "abc"
    let b = "b"
    @Expect(a.contains(b))
    @Expect(a, a)
}