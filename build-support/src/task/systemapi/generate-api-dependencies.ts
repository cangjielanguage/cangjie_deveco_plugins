/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import {AbstractModuleHookTask} from '@ohos/hvigor-ohos-plugin/src/tasks/hook/abstract-module-hook-task';
import type {ModuleTaskService} from '@ohos/hvigor-ohos-plugin/src/tasks/service/module-task-service';
import type {TargetTaskService} from '@ohos/hvigor-ohos-plugin/src/tasks/service/target-task-service';
import {CangjieTaskNames} from '../cangjie-task-names';

/**
 * generate api dependencies
 *
 * @since 2024/12/16
 */
export class GenerateApiDependencies extends AbstractModuleHookTask {
  constructor(moduleTaskService: ModuleTaskService) {
    super(moduleTaskService, false, CangjieTaskNames.GENERATE_API_DEPENDENCIES);
  }

  initTaskDepends(taskTargetService: TargetTaskService): void {
    this.dependsOn(
      `${taskTargetService.getTargetData().getTargetName()}@${CangjieTaskNames.ADD_API_DEPENDENCIES.name}`);
  }
}
