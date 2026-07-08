/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import type {HapPlugin} from '@ohos/hvigor-ohos-plugin/src/plugin/hap-plugin';
import {BaseCangjiePlugin} from './base-cangjie-plugin';

/**
 * cangjie hap plugin
 *
 * @since 2024/1/6
 */
export class CangjieHapPlugin extends BaseCangjiePlugin {
  private hapPlugin: HapPlugin;

  constructor(hapPlugin: HapPlugin) {
    super(hapPlugin);
    this.hapPlugin = hapPlugin;
  }

  async initCangjieTasks(): Promise<void> {
    await this.initTasks();
  }
}