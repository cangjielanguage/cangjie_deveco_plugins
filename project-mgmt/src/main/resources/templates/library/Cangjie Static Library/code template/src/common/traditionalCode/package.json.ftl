<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

{
  "name": "@ohos/${moduleName?lower_case}",
  "description": "a npm package which contains arkUI2.0 page",
  "ohos": {
    "org": ""
  },
  "version": "1.0.0",
  "main": "${(uiSyntax?lower_case=='ets')?then('index.ets', 'index.js')}",
  "types": "",
  "license": "ISC",
  "type": "module",
  "dependencies": {}
}
