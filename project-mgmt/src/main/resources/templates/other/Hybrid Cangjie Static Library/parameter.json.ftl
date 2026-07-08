<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

{
  "rootOut":"${modulePath}",
  "addObfuscationConfig": <#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 10 && apiType?? && apiType == "stageMode" && uiSyntax?lower_case == "ets">true<#else>false</#if>,
  "isCrossPlatformProject": <#if category?? && (category == "cross ability" || category == "cross library") || (isArkUIXProject?? && isArkUIXProject)>true<#else>false</#if>,
  "shouldCreateOhosTest": <#if (createOhosTest?? && createOhosTest) && apiType?? && apiType == "stageMode">true<#else>false</#if>
}