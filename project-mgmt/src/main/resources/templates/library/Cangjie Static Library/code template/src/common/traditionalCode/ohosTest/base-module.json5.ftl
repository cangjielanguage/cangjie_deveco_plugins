<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

{
  "module": {
    "name": "${moduleName}_test",
    "type": "feature",
    "description": "$string:module_desc",
    "mainElement": "EntryAbility",
    "deviceTypes":  [${deviceTypes}],
    "deliveryWithInstall": true,
    "installationFree": ${isInstallationFree?c},
    "srcEntry": "${cjPackageName}_test.MyAbilityStage",
    "abilities": [
    ]
  }
}