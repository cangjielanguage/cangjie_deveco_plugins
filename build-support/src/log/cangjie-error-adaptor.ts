/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import {CangjieAdaptorErrorMessage, ERROR_NOT_MATCH, MatchFieldType} from './cangjie-types';
import path from 'path';
import {CangjieError} from './cangjie-error';

/**
 * hvigor的适配器
 */
export class CangjieErrorAdaptor {
  protected _hvigorError: CangjieError;

  constructor(value: any, field: MatchFieldType = 'id') {
    this._hvigorError = new CangjieError(this.getErrorCodeJsonPath(), {field, value});
  }

  getErrorMessage(): CangjieAdaptorErrorMessage {
    return {
      timestamp: this._hvigorError.timestamp,
      id: this._hvigorError.id,
      code: this._hvigorError.code,
      originMessage: this._hvigorError.originMessage,
      originSolutions: this._hvigorError.originSolutions,
      moreInfo: this._hvigorError.moreInfo,
      stack: this._hvigorError.stack,
      message: this._hvigorError.message ?? ERROR_NOT_MATCH,
      solutions: this._hvigorError.solutions,
      checkMessage: this._hvigorError.checkMessage,
    };
  }

  getErrorCodeJsonPath(): string[] {
    return [path.resolve(__dirname, '../../res/error/cangjie-build-msg.json')];
  }

  formatMessage(...args: unknown[]): this {
    this._hvigorError.formatMessage(...args);
    return this;
  }

  formatSolutions(index: number, ...args: unknown[]): this {
    this._hvigorError.formatSolutions(index, ...args);
    return this;
  }
}
