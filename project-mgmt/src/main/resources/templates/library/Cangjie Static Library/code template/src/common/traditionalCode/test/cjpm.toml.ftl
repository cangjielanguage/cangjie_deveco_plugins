<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

[package]
  cjc-version = "${cjcVersion}"
  compile-option = "--dy-std --cfg=\"<#noparse>${COMPILE_CONDITION}</#noparse>\""
  override-compile-option = "<#noparse>${OVERRIDE_COMPILE_OPTION}</#noparse>"
  description = "CangjieUI Application"
  name = "${cjPackageName}_local_test"
  output-type = "dynamic"
  src-dir = "."
  target-dir = ""
  version = "1.0.0"

[profile]
  [profile.build]
    incremental = true
    lto = ""
    [profile.build.combined]
      ${cjPackageName}_local_test = "dynamic"
  [profile.customized-option]
    debug = "-g -Woff all -Won apilevel-check"
    release = "--fast-math -O2 -s -Woff all -Won apilevel-check"
    asan = "--sanitize=address -lclang_rt.asan"
  [profile.test]