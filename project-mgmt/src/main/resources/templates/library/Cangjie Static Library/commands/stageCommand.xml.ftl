<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<instantiate src="code template/src/stageMode/module.json5.ftl" dest="${rootOut}/src/main/module.json5"/>
<copy src="code template/src/common/traditionalCode/module-build-profile.json5" dest="${rootOut}/build-profile.json5"/>
<#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 19>
    <merge src="code template/src/common/traditionalCode/ohosTest/module-build-profile.json5.ftl" dest="${rootOut}/build-profile.json5"/>
</#if>
<instantiate src="code template/src/common/traditionalCode/oh-package.json5.ftl" dest="${rootOut}/oh-package.json5"/>
<copy src="code template/src/stageMode/Index.d.ets" dest="${rootOut}/Index.d.ets"/>
<#--  res  -->
<copy src="code template/src/common/traditionalCode/res/element/string.json" dest="${rootOut}/src/main/resources/base/element/string.json"/>
<copy src="code template/src/common/traditionalCode/res/element/string.json" dest="${rootOut}/src/main/resources/en_US/element/string.json"/>
<copy src="code template/src/common/traditionalCode/res/element/string.json" dest="${rootOut}/src/main/resources/zh_CN/element/string.json"/>