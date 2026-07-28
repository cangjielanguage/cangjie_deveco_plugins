<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

package ${cjPackageName}

import kit.ArkUI.Text
import kit.ArkUI.Button
import kit.ArkUI.Column
import kit.ArkUI.CustomView
import kit.ArkUI.LocalStorage
import kit.ArkUI.ObservedProperty
import kit.ArkUI.SubscriberManager
import kit.ArkUI.ViewStackProcessor
import kit.ArkUI.CJPageEntry
import kit.ArkUI.HybridComponentBase
import ohos.arkui.state_macro_manage.State
import ohos.arkui.state_macro_manage.Component
import ohos.arkui.state_macro_manage.HybridComponentEntry

@HybridComponentEntry
@Component
class ${pageName?cap_first} {
    @State
    var msg: String = "Hello"

    public func build() {
        Column {
            Text(msg)
            Button("click to change Text").onClick ({
                evt => msg = "world"
            })
        }
    }
}
