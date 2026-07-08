<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

<#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 18>
import { testCJ } from 'lib${cjPackageName}.so';
<#else>
import { requireCJLib } from "libark_interop_loader.so";
</#if>
<#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi lt 18>

interface CJLib {
  testCJ(src: string): string
}
</#if>

@Entry
@Component
struct Index {
  @State message: string = 'Hello World';

  build() {
    RelativeContainer() {
      Text(this.message)
        .fontSize(40)
        .fontWeight(FontWeight.Bold)
        .alignRules({
          center: { anchor: '__container__', align: VerticalAlign.Center },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .onClick(() => {
<#if cangjieCompatibleBaseApi?? && cangjieCompatibleBaseApi gte 18>
          this.message = testCJ("Cangjie")
<#else>
          const lib = requireCJLib("lib${cjPackageName}.so") as CJLib
          this.message = lib.testCJ("Cangjie")
</#if>
        })
    }
    .height('100%')
    .width('100%')
  }
}