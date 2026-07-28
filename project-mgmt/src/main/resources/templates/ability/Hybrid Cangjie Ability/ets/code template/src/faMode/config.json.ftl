<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

{
  "module": {
    "abilities": [
      {
        <#if hasSkill>
        "skills": [
          {
            "entities": [
              "entity.system.home"
            ],
            "actions": [
              "action.system.home"
            ]
          }
        ],
        </#if>
        "orientation": "unspecified",
        "formsEnabled": false,
        "name": ".${abilityName}",
        "srcLanguage": "${uiSyntax?lower_case}",
        "srcPath": "${abilityName}",
        "icon": "$media:icon",
        "description": "$string:${abilityName}_desc",
        "label": "$string:${abilityName}_label",
        "type": "page",
        <#if visible>
          "visible": true,
        </#if>
        "launchType": "standard"
      }
    ]
  }
}