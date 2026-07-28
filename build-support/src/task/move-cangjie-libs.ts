/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import {FileSet} from '@ohos/hvigor';
import {FileUtil} from '@ohos/hvigor-ohos-plugin/src/utils/file-util';
import path from 'path';
import {OhosLogger} from '@ohos/hvigor-ohos-plugin/src/utils/log/ohos-logger';
import type {TargetTaskService} from '@ohos/hvigor-ohos-plugin/src/tasks/service/target-task-service';
import {CangjieTaskNames} from './cangjie-task-names';
import fs from 'fs';
import {retry} from '../utils/common-utils';
import {ApiType} from '@ohos/hvigor-ohos-plugin/src/project/data/hap-extra-info';
import {CangjieCommonPath} from '../common/cangjie-common-path';
import {OhosHapTask} from '@ohos/hvigor-ohos-plugin/src/tasks/task/ohos-hap-task';
import {copyLibsCommon, linkFile} from '../utils/cangjie-file-util';
import {AbiEnum} from '../enums/cangjie-cpu-abi-enum';
import {InjectUtil} from '@ohos/hvigor-ohos-plugin/src/utils/inject-util';
import {CangjieLogger} from '../log/cangjie-logger';
import {TaskNames} from '@ohos/hvigor-ohos-plugin/src/tasks/common/task-names';

/**
 * move cangjie libs
 *
 * @since 2024/4/13
 */
export class MoveCangjieLibs extends OhosHapTask {
  private logger: OhosLogger = CangjieLogger.getLogger(MoveCangjieLibs.name);

  constructor(taskService: TargetTaskService) {
    super(taskService, CangjieTaskNames.MOVE_CANGJIE_LIBS);
  }

  declareInputFiles(): FileSet {
    return new FileSet();
  }

  declareOutputFiles(): FileSet {
    if (!this.pathInfo.getIntermediatesProcessLibs()) {
      return new FileSet();
    }
    return new FileSet().addEntry(this.pathInfo.getIntermediatesProcessLibs(), { isDirectory: true });
  }

  taskShouldDo(): boolean {
    return this.moduleModel !== undefined && this.moduleModel.getApiType() === ApiType.STAGE;
  }

  initTaskDepends(): void {
    this.declareDepends(TaskNames.Task.PROCESS_LIB.name);
  }

  protected async doTaskAction(): Promise<void> {
    if (this.moduleModel.isHarModule() && !InjectUtil.isOhosTest()) {
      return;
    }
    for (const key in AbiEnum) {
      if (!Object.prototype.hasOwnProperty.call(AbiEnum, key)) {
        continue;
      }
      const abiFilter = AbiEnum[key as keyof typeof AbiEnum];
      const cangjiePathImpl = new CangjieCommonPath(this.targetName, this.pathInfo, abiFilter);
      await retry(() => this.moveHarBinLibs(cangjiePathImpl));
    }
  }

  protected async moveHarBinLibs(cangjieCommonPath: CangjieCommonPath): Promise<void> {
    const processBinLibs = cangjieCommonPath.getIntermediatesProcessBinLibs();
    if (!fs.existsSync(processBinLibs)) {
      return;
    }
    const processLibs = cangjieCommonPath.getIntermediatesProcessLibs();
    const files = fs.readdirSync(processBinLibs);
    for (const file of files) {
      const filePath = path.join(processBinLibs, file);
      if (fs.statSync(filePath).isDirectory()) {
        await copyLibsCommon(filePath, processLibs);
      }
    }
    for (const file of files) {
      const filePath = path.join(processBinLibs, file);
      if (!fs.statSync(filePath).isDirectory()) {
        await linkFile(filePath, path.resolve(processLibs, file));
      }
    }
    fs.rmSync(processBinLibs, {recursive: true, force: true, maxRetries: 3});
  }
}
