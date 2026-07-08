<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

package ${cjPackageName}_test

import std.unittest.TestClass
import std.unittest.TestPackage
import std.unittest.TestSuite
import std.unittest.UnitTestCase
import std.unittest.common.Configuration
import std.unittest.testmacro.Test
import std.unittest.testmacro.TestCase

@Test
class TestExample00 {
    @TestCase
    func test00(): Unit {
        unittest()
    }
}
