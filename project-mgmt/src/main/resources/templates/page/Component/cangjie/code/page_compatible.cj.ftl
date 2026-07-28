<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

package ${cjPackageName}

import ohos.component.Text
import ohos.component.Button
import ohos.component.Column
import ohos.component.CustomView
import ohos.component.ReuseParams
import ohos.state_manage.LocalStorage
import ohos.state_manage.ObservedProperty
import ohos.state_manage.SubscriberManager
import ohos.state_manage.ViewStackProcessor
import ohos.state_macro_manage.State
import ohos.state_macro_manage.Component
import ohos.state_macro_manage.HybridComponentEntry
import ohos.hybrid_base.CJPageEntry
import ohos.hybrid_base.HybridComponentBase

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
