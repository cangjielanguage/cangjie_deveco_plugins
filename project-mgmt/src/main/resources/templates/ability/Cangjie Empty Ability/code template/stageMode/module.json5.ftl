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
        "name": "${abilityName}",
        "srcEntry": "${cjPackageName}.MainAbility",
        "description": "$string:${abilityName}_desc",
        "icon": "$media:layered_image",
        "label": "$string:${abilityName}_label",
        "startWindowIcon" : "$media:startIcon",
        "startWindowBackground" : "$color:start_window_background"
        <#if visible>
          ,
        "exported": true
        </#if>
        <#if hasSkill>
          ,
        "skills": [
          {
            "entities": [
              "entity.system.home"
            ],
            "actions": [
              "action.system.home"
            ]
          }
        ]
        </#if>
      }
    ]
  }
}
