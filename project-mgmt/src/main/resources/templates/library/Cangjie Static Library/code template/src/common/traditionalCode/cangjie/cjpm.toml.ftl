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
  name = "${cjPackageName}"
  output-type = "dynamic"
  src-dir = "./src/main/cangjie"
  target-dir = ""
  version = "1.0.0"

[profile]
  [profile.build]
    incremental = true
    lto = ""
    [profile.build.combined]
      ${cjPackageName} = "dynamic"
  [profile.customized-option]
    debug = "-g -Woff all -Won apilevel-check"
    release = "--fast-math -O2 -s -Woff all -Won apilevel-check"
    asan = "--sanitize=address -lclang_rt.asan"
  [profile.test]

[target.aarch64-linux-ohos]
  compile-option = "-B \"${DEVECO_CANGJIE_HOME}/build-tools/third_party/llvm/bin\" -B \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot/usr/lib/aarch64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot/usr/lib/aarch64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/llvm/lib/clang/15.0.4/lib/aarch64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/llvm/lib/aarch64-linux-ohos\" --sysroot \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot\""
[target.aarch64-linux-ohos.bin-dependencies]
  path-option = ["<#noparse>${AARCH64_LIBS}</#noparse>", "<#noparse>${AARCH64_MACRO_LIBS}</#noparse>", "<#noparse>${AARCH64_KIT_LIBS}</#noparse>"]
  package-option = {}

[target.x86_64-linux-ohos]
  compile-option = "-B \"${DEVECO_CANGJIE_HOME}/build-tools/third_party/llvm/bin\" -B \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot/usr/lib/x86_64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot/usr/lib/x86_64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/llvm/lib/clang/15.0.4/lib/x86_64-linux-ohos\" -L \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/llvm/lib/x86_64-linux-ohos\" --sysroot \"<#noparse>${DEVECO_OH_NATIVE_HOME}</#noparse>/sysroot\""
[target.x86_64-linux-ohos.bin-dependencies]
  path-option = ["<#noparse>${X86_64_OHOS_LIBS}</#noparse>", "<#noparse>${X86_64_OHOS_MACRO_LIBS}</#noparse>", "<#noparse>${X86_64_OHOS_KIT_LIBS}</#noparse>"]

<#if osType == "windows">
[target.x86_64-w64-mingw32.bin-dependencies]
  path-option = ["<#noparse>${X86_64_LIBS}</#noparse>", "<#noparse>${X86_64_MACRO_LIBS}</#noparse>", "<#noparse>${X86_64_KIT_LIBS}</#noparse>"]
  package-option = {}
<#else>
[target.aarch64-apple-darwin]
  compile-option = "--link-options=\"-rpath @executable_path/../../../sdk/cangjie/build-tools/runtime/lib/darwin_aarch64_cjnative\" --link-options=\"-rpath @executable_path/../../../sdk/cangjie/api/lib/darwin_aarch64_cjnative/kit\" --link-options=\"-rpath @executable_path/../../../sdk/cangjie/api/lib/darwin_aarch64_cjnative/ohos\" --link-options=\"-rpath @executable_path/../../../sdk/cangjie/api/macro/ohos\" --link-options=\"-rpath @executable_path/module/cj_sdk_libraries\""
[target.aarch64-apple-darwin.bin-dependencies]
  path-option = ["<#noparse>${AARCH64_LIBS}</#noparse>", "<#noparse>${AARCH64_MACRO_LIBS}</#noparse>", "<#noparse>${AARCH64_KIT_LIBS}</#noparse>"]
  package-option = {}
</#if>