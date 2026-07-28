<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<#if isCompatible?? && isCompatible>
    <instantiate src="ets/src/ets/page/page11_compatible.ets.ftl" dest="${rootOut}/src/main/ets/pages/${pkgDir}/${pageFileName}.ets"/>
<#else>
    <instantiate src="ets/src/ets/page/page11.ets.ftl" dest="${rootOut}/src/main/ets/pages/${pkgDir}/${pageFileName}.ets"/>
</#if>
<#--  res  -->
<#if moduleType = "entry" || moduleType = "feature">
    <merge src="ets/res/profile/main_pages.json.ftl" dest="${rootOut}/src/main/resources/base/profile/${pageProfileFileName}"/>
</#if>
<open file="${rootOut}/src/main/ets/pages/${pkgDir}/${pageFileName}.ets"/>
