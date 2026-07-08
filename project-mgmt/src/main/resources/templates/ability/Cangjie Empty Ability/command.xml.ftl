<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<?xml version="1.0"?>
<command>

    <mkdir dest="${rootOut}/libs"/>

    <#--  resources  -->
    <copy src="../../common/media/background.png" dest="${rootOut}/src/main/resources/base/media/background.png"/>
    <copy src="../../common/media/foreground.png" dest="${rootOut}/src/main/resources/base/media/foreground.png"/>
    <copy src="../../common/media/layered_image.json" dest="${rootOut}/src/main/resources/base/media/layered_image.json"/>
    <merge src="code template/resources/element/color.json.ftl" dest="${rootOut}/src/main/resources/base/element/color.json"/>
    <#include "commands/stringCommand.xml.ftl" />
    <mkdir dest="${rootOut}/src/main/resources/rawfile"/>

    <#--  cangjie  -->
    <#if isCompatible?? && isCompatible>
        <instantiate src="code template/cangjie/src/index_compatible.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/index.cj"/>
        <instantiate src="code template/cangjie/src/ability_stage_compatible.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/ability_stage.cj"/>
        <instantiate src="code template/cangjie/src/main_ability_compatible.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/main_ability.cj"/>
    <#else>
        <instantiate src="code template/cangjie/src/index.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/index.cj"/>
        <instantiate src="code template/cangjie/src/ability_stage.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/ability_stage.cj"/>
        <instantiate src="code template/cangjie/src/main_ability.cj.ftl"
                     dest="${rootOut}/src/main/cangjie/main_ability.cj"/>
    </#if>

    <instantiate src="code template/cangjie/cjpm.toml.ftl"
                 dest="${rootOut}/cjpm.toml"/>
    <#if apiType == "stageMode">
        <#include  "commands/stageCommand.xml.ftl"/>
    </#if>

    <#--  ohosTest  -->
    <#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 19>
        <instantiate src="code template/ohosTest/cjpm.toml.ftl" dest="${rootOut}/src/ohosTest/cangjie/cjpm.toml"/>
        <instantiate src="code template/ohosTest/src/example_test.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/example_test.cj"/>
        <instantiate src="code template/ohosTest/src/list_test.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/list_test.cj"/>
        <instantiate src="code template/ohosTest/src/example.cj.ftl" dest="${rootOut}/src/ohosTest/cangjie/example.cj"/>
        <#if isCompatible?? && isCompatible>
            <instantiate src="code template/ohosTest/src/unittest_support/unittest_engine_compatible.cj.ftl" dest = "${rootOut}/src/ohosTest/cangjie/unittest_support/unittest_engine.cj"/>
        <#else>
            <instantiate src="code template/ohosTest/src/unittest_support/unittest_engine.cj.ftl" dest = "${rootOut}/src/ohosTest/cangjie/unittest_support/unittest_engine.cj"/>
        </#if>
    </#if>

    <#--  localTest  -->
    <instantiate src="code template/test/cjpm.toml.ftl" dest="${rootOut}/src/test/cangjie/cjpm.toml"/>
    <instantiate src="code template/test/src/example_test.cj.ftl" dest="${rootOut}/src/test/cangjie/example_test.cj"/>
    <instantiate src="code template/test/src/example.cj.ftl" dest="${rootOut}/src/test/cangjie/example.cj"/>

    <#include "commands/createAbilityCommand.xml.ftl" />

    <#-- CJLint支持code-linter.json5配置 -->
    <#if codeLinterJsonHasCj?? && !codeLinterJsonHasCj>
        <merge src="../../common/code-linter.json.ftl" dest="${projectPath}/code-linter.json5"/>
    </#if>
</command>
