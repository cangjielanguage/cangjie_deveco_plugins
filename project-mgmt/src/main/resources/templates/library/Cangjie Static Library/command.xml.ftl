<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->
<?xml version="1.0"?>
<command>
  <copy src="code template/gitignore_file" dest="${rootOut}/.gitignore"/>
  <merge src="code template/project-build-profile.json5.ftl" dest="${projectPath}/build-profile.json5"/>
  <mkdir dest="${rootOut}/libs/arm64-v8a" />

  <#--  cangjie  -->
  <instantiate src="code template/src/common/traditionalCode/cangjie/src/index.cj.ftl"
    dest="${rootOut}/src/main/cangjie/index.cj"/>
  <instantiate src="code template/src/common/traditionalCode/cangjie/cjpm.toml.ftl"
    dest="${rootOut}/cjpm.toml"/>

  <#--  ohosTest  -->
  <#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 19>
    <instantiate src="code template/src/common/traditionalCode/ohosTest/cjpm.toml.ftl" dest="${rootOut}/src/ohosTest/cangjie/cjpm.toml"/>
    <instantiate src="code template/src/common/traditionalCode/ohosTest/src/example_test.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/example_test.cj"/>
    <instantiate src="code template/src/common/traditionalCode/ohosTest/src/list_test.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/list_test.cj"/>
    <instantiate src="code template/src/common/traditionalCode/ohosTest/src/example.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/example.cj"/>
    <#if isCompatible?? && isCompatible>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/unittest_support/unittest_engine_compatible.cj.ftl" dest = "${rootOut}/src/ohosTest/cangjie/unittest_support/unittest_engine.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/ability_stage_compatible.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/ability_stage.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/index_compatible.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/index.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/main_ability_compatible.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/main_ability.cj"/>
    <#else>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/unittest_support/unittest_engine.cj.ftl" dest = "${rootOut}/src/ohosTest/cangjie/unittest_support/unittest_engine.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/ability_stage.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/ability_stage.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/index.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/index.cj"/>
      <instantiate src="code template/src/common/traditionalCode/ohosTest/src/main_ability.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/main_ability.cj"/>
    </#if>

    <#--  resources  -->
    <copy src="../../common/media/background.png" dest="${rootOut}/src/ohosTest/resources/base/media/background.png"/>
    <copy src="../../common/media/foreground.png" dest="${rootOut}/src/ohosTest/resources/base/media/foreground.png"/>
    <copy src="../../common/media/layered_image.json" dest="${rootOut}/src/ohosTest/resources/base/media/layered_image.json"/>
    <copy src="../../common/media/startIcon.png" dest="${rootOut}/src/ohosTest/resources/base/media/startIcon.png"/>
    <copy src="code template/src/common/traditionalCode/ohosTest/resources/profile/main_pages.json" dest="${rootOut}/src/ohosTest/resources/base/profile/main_pages.json"/>
    <merge src="code template/src/common/traditionalCode/ohosTest/resources/element/color.json.ftl" dest="${rootOut}/src/ohosTest/resources/base/element/color.json"/>
    <merge src="code template/src/common/traditionalCode/ohosTest/resources/element/string.json.ftl" dest="${rootOut}/src/ohosTest/resources/base/element/string.json"/>
    <merge src="code template/src/common/traditionalCode/ohosTest/resources/element/string.json.ftl" dest="${rootOut}/src/ohosTest/resources/en_US/element/string.json"/>
    <merge src="code template/src/common/traditionalCode/ohosTest/resources/zh_CN/element/string.json.ftl" dest="${rootOut}/src/ohosTest/resources/zh_CN/element/string.json"/>
    <instantiate src="code template/src/common/traditionalCode/ohosTest/base-module.json5.ftl" dest="${rootOut}/src/ohosTest/module.json5"/>
    <merge src="code template/src/common/traditionalCode/ohosTest/module.json5.ftl" dest="${rootOut}/src/ohosTest/module.json5"/>
    <mkdir dest="${rootOut}/src/ohosTest/resources/rawfile"/>
  </#if>

  <#--  localTest  -->
  <instantiate src="code template/src/common/traditionalCode/test/cjpm.toml.ftl" dest="${rootOut}/src/test/cangjie/cjpm.toml"/>
  <instantiate src="code template/src/common/traditionalCode/test/src/example_test.cj.ftl" dest="${rootOut}/src/test/cangjie/example_test.cj"/>
  <instantiate src="code template/src/common/traditionalCode/test/src/example.cj.ftl" dest="${rootOut}/src/test/cangjie/example.cj"/>

  <#-- hivgor -->
  <copy src="code template/src/stageMode/hvigorfile.ts" dest="${rootOut}/hvigorfile.ts"/>

<#if apiType == "stageMode">
    <#include  "commands/stageCommand.xml.ftl"/>
</#if>

  <#include "commands/createModuleCommand.xml.ftl" />

  <#-- CJLint支持code-linter.json5配置 -->
  <#if codeLinterJsonHasCj?? && !codeLinterJsonHasCj>
    <merge src="../../common/code-linter.json.ftl" dest="${projectPath}/code-linter.json5"/>
  </#if>
</command>
