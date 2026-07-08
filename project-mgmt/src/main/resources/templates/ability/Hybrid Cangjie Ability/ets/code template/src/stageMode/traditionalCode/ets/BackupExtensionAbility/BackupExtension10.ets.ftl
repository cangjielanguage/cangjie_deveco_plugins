<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

import hilog from '@ohos.hilog';
import BackupExtensionAbility, { BundleVersion } from '@ohos.application.BackupExtensionAbility';

export default class ${backupAbilityName} extends BackupExtensionAbility {
  async onBackup () {
    hilog.info(0x0000, 'testTag', 'onBackup ok');
  }

  async onRestore (bundleVersion : BundleVersion) {
    hilog.info(0x0000, 'testTag', 'onRestore ok %{public}s', JSON.stringify(bundleVersion));
  }
}