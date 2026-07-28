<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

import { CJHybridComponentV2 } from '@cangjie/cjhybridview';

<#if moduleType = "entry" || moduleType = "feature">
@Entry
@Component
struct ${pageName?cap_first} {
  @State message: string = 'Hello World';

  build() {
    Column() {
      CJHybridComponentV2({
        library: '${cjDynamicName}',
        component: '${pageName?cap_first}'
      })
    }
    .height('100%')
    .width('100%')
  }
}
<#else>
@Component
export struct ${pageName?cap_first} {
  @State message: string = 'Hello World';

  build() {
    Column() {
      CJHybridComponentV2({
        library: '${cjDynamicName}',
        component: '${pageName?cap_first}'
      })
    }
    .height('100%')
    .width('100%')
  }
}
</#if>