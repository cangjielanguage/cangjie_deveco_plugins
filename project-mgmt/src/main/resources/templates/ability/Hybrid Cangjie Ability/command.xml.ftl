<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->
<?xml version="1.0"?>
<command>
    <#if enableCangjieAction == "YES">
        <#include "cangjie/command.xml.ftl" />
    <#else>
        <#include "ets/command.xml.ftl" />
        <#include "cangjie/command.xml.ftl" />
    </#if>
</command>
