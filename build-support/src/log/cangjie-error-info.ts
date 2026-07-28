/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import {
  MoreInfo,
  TCangjieErrorInfo,
  UNDEFINED_CAUSE,
  UNDEFINED_CODE,
  UNDEFINED_DESC,
  UNDEFINED_POS
} from './cangjie-types';

/**
 * cangjie的错误信息描述类。
 * 提供数据的输入、校验、获取。
 */
export class CangjieErrorDetail {
  private readonly code: string;
  private readonly description: string;
  private readonly cause: string;
  private readonly position: string;
  private readonly solutions: string[] = [];
  private readonly moreInfo?: MoreInfo;

  constructor(errorInfo?: TCangjieErrorInfo) {
    if (errorInfo) {
      this.code = errorInfo.code;
      this.description = errorInfo.description;
      this.cause = errorInfo.cause;
      this.position = errorInfo.position;
      this.solutions = errorInfo.solutions;
      this.moreInfo = errorInfo.moreInfo;
    } else {
      this.code = UNDEFINED_CODE;
      this.description = UNDEFINED_DESC;
      this.cause = UNDEFINED_CAUSE;
      this.position = UNDEFINED_POS;
    }
  }

  public getCode(): string {
    return this.code;
  }

  public getDescription(): string {
    return this.description;
  }

  public getCause(): string {
    return this.cause;
  }

  public getPosition(): string {
    return this.position;
  }

  public getSolutions(): string[] {
    return this.solutions;
  }

  public getMoreInfo(): MoreInfo | undefined {
    return this.moreInfo;
  }

  /**
   * 校验HvigorErrorInfo数据是否合法
   * @returns
   */
  public checkInfo(): boolean {
    return this.checkCode() && this.checkCause();
  }

  /**
   * 校验错误码的8位数字组成的字符串
   * @returns
   */
  private checkCode(): boolean {
    if (!this.code) {
      return false;
    }

    if (this.code.length !== 8) {
      return false;
    }

    const exp = /[0-9]{8}/g;
    if (exp.test(this.code)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 校验错误原因是否存在
   * @returns
   */
  private checkCause(): boolean {
    if (!this.cause) {
      return false;
    }

    if (this.code === UNDEFINED_CODE) {
      return this.cause === UNDEFINED_CAUSE;
    }

    return this.cause !== '' && this.cause !== undefined && this.cause !== null;
  }
}
