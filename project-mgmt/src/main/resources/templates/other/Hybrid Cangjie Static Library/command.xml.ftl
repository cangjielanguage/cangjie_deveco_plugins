<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<?xml version="1.0"?>
<command>
    <#include "cangjie/command.xml.ftl" />

    <#-- CJLint支持code-linter.json5配置 -->
    <#if codeLinterJsonHasCj?? && !codeLinterJsonHasCj>
        <merge src="../../common/code-linter.json.ftl" dest="${projectPath}/code-linter.json5"/>
    </#if>
</command>
