<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<#if isCompatible?? && isCompatible>
    <instantiate src="cangjie/code/page_compatible.cj.ftl" dest="${targetDir}/${pageFileName}.cj"/>
<#else>
    <instantiate src="cangjie/code/page.cj.ftl" dest="${targetDir}/${pageFileName}.cj"/>
</#if>
<open file="${targetDir}/${pageFileName}.cj"/>
<#if isCompatible?? && isCompatible>
    <merge src="cangjie/config/module-oh-package_compatible.json5.ftl" dest="${rootOut}/oh-package.json5"/>
<#else>
    <copy src="cangjie/types/Index.d.ts" dest="${rootOut}/src/main/cangjie/types/lib${cjPackageName}/Index.d.ts"/>
    <instantiate src="cangjie/types/oh-package.json5.ftl" dest="${rootOut}/src/main/cangjie/types/lib${cjPackageName}/oh-package.json5"/>
    <merge src="cangjie/config/module-oh-package.json5.ftl" dest="${rootOut}/oh-package.json5"/>
</#if>