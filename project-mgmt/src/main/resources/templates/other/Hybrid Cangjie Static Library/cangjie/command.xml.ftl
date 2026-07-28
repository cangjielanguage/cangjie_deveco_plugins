<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<mkdir dest="${rootOut}/libs"/>

<#--  cangjie  -->
<instantiate src="cangjie/code template/code/src/index.cj.ftl"
             dest="${rootOut}/src/main/cangjie/index.cj"/>
<instantiate src="cangjie/code template/code/cjpm.toml.ftl"
             dest="${rootOut}/cjpm.toml"/>

<#-- config -->
<merge src="cangjie/code template/stageMode/module-build-profile.json5.ftl" dest="${rootOut}/build-profile.json5"/>
<merge src="cangjie/code template/stageMode/module-oh-package.json5.ftl" dest="${rootOut}/oh-package.json5"/>

<#-- loader -->
<#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 18>
    <copy src="cangjie/code template/types/Index.d.ts" dest="${rootOut}/src/main/cangjie/types/lib${cjPackageName}/Index.d.ts"/>
    <instantiate src="cangjie/code template/types/oh-package.json5.ftl" dest="${rootOut}/src/main/cangjie/types/lib${cjPackageName}/oh-package.json5"/>
<#else>
    <copy src="cangjie/code template/loader/libark_interop_loader.d.ts" dest="${rootOut}/src/main/cangjie/loader/libark_interop_loader.d.ts"/>
    <copy src="cangjie/code template/loader/oh-package.json5" dest="${rootOut}/src/main/cangjie/loader/oh-package.json5"/>
</#if>

<open file="${rootOut}/src/main/cangjie/index.cj"/>