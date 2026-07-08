/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import {FileSet, TaskInputValue} from '@ohos/hvigor';
import path from 'path';
import {OhosLogger} from '@ohos/hvigor-ohos-plugin/src/utils/log/ohos-logger';
import type {TargetTaskService} from '@ohos/hvigor-ohos-plugin/src/tasks/service/target-task-service';
import {CangjieTaskNames} from './cangjie-task-names';
import fs from 'fs';
import {createDir, deleteIfNoFiles, onlyCangjieModule, readPackageName} from '../utils/cangjie-file-util';
import {retry} from '../utils/common-utils';
import {ApiType} from '@ohos/hvigor-ohos-plugin/src/project/data/hap-extra-info';
import {BaseCangjieTask} from './base-cangjie-task';
import {DoNativeStrip} from '@ohos/hvigor-ohos-plugin/src/tasks/do-native-strip';
import {CangjieCommonPath} from '../common/cangjie-common-path';
import {InjectUtil} from '@ohos/hvigor-ohos-plugin/src/utils/inject-util';
import {CangjieLogger} from '../log/cangjie-logger';
import {BuildArtifactConst} from '@ohos/hvigor-ohos-plugin/src/const/build-directory-const';
import {CjBuildDirConst} from '../constants/cangjie-build-dir-const';

/**
 * after compile cangjie
 *
 * @since 2024/4/13
 */
export class AfterCompileCangjie extends BaseCangjieTask {
  private logger: OhosLogger = CangjieLogger.getLogger(AfterCompileCangjie.name);
  private readonly _moduleDir: string;

  constructor(taskService: TargetTaskService) {
    super(taskService, CangjieTaskNames.AFTER_COMPILE_CANGJIE);
    this._moduleDir = this.moduleModel.getProjectDir();
  }

  taskShouldDo(): boolean {
    return this.moduleModel !== undefined && this.moduleModel.getApiType() === ApiType.STAGE;
  }

  declareInputFiles(): FileSet {
    const fileSet = super.declareInputFiles();
    for (const abiFilter of this.abiFilters) {
      const cangjiePathImpl = new CangjieCommonPath(this.targetName, this.pathInfo, abiFilter,
        this.customCjpmArguments);
      const cjBuildLogDir = cangjiePathImpl.getIntermediatesCjBuildLogRelease();
      if (fs.existsSync(cjBuildLogDir)) {
        fileSet.addEntry(cjBuildLogDir, {isDirectory: true});
      }
      cangjiePathImpl.mockPath = CjBuildDirConst.MOCK;
      const cjMockBuildLogDir = cangjiePathImpl.getIntermediatesCjBuildLogRelease();
      if (fs.existsSync(cjMockBuildLogDir)) {
        fileSet.addEntry(cjMockBuildLogDir, {isDirectory: true});
      }
    }
    return fileSet;
  }

  declareInputs(): Map<string, TaskInputValue> {
    const map = super.declareInputs();
    map.set('buildProfileConfig', JSON.stringify(this.moduleModel.getProfileOpt()));
    return map;
  }

  initTaskDepends(): void {
    this.declareDepends(DoNativeStrip.name);
  }

  protected async doTaskAction(): Promise<void> {
    for (const abiFilter of this.abiFilters) {
      const cangjiePathImpl = new CangjieCommonPath(this.targetName, this.pathInfo, abiFilter,
        this.customCjpmArguments);
      await deleteIfNoFiles(cangjiePathImpl.getIntermediatesStrippedBinLibs());
      if (this.moduleModel.isHarModule() && !InjectUtil.isOhosTest()) {
        await this.processCjos(cangjiePathImpl.getIntermediatesCjBinLibs(),
          cangjiePathImpl.getIntermediatesStrippedBinLibs());
      } else {
        const tomlName = readPackageName(this.cjpmTomlPath, this.logger);
        await retry(() => this.copyUnitTests(cangjiePathImpl.getIntermediatesCjUnitLibs(),
          cangjiePathImpl.getIntermediatesStrippedLibs(), tomlName));
      }
    }

    if (onlyCangjieModule(this.moduleModel.getProjectDir()) && this.moduleModel.isHapModule()) {
      createDir(this.pathInfo.getInterMediatesSourceMapDirPath());
      const sourceMapPath = path.resolve(this.pathInfo.getInterMediatesSourceMapDirPath(),
        BuildArtifactConst.SOURCEMAPS_MAP);
      if (!fs.existsSync(sourceMapPath)) {
        fs.writeFileSync(sourceMapPath, '');
      }
    }
    this.initCangjieSdk();
    if (this.sdkCangjieComponent === undefined) {
      return;
    }
    if (this.sdkCangjieComponent.isEscapeSdk && this.moduleModel.isHarModule()) {
      fs.writeFileSync(path.join(this.pathInfo.getIntermediatesStrippedLibsDir(), 'compile-info.json'), '');
    }
  }
}
