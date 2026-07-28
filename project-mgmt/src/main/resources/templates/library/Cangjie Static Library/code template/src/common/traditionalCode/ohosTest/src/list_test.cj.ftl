<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

package ${cjPackageName}_test

import ${cjPackageName}_test.unittest_support.registerTestSuite

private let register = testsuite()

func testsuite() {
    registerTestSuite {TestExample00()}
}
