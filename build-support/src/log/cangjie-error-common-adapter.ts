/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import os from 'os';
import {CangjieErrorDetail} from './cangjie-error-info';
import {SPLIT_TAG} from './cangjie-types';
import {getOsLanguage} from './os-country';

/**
 * 打印报错的通用适配器
 */
export class CangjieErrorCommonAdapter {
  public static red = '\u001b[31m';

  /**
   * 组装完整三段式的错误信息
   * @private
   */
  public combinePhase(errorInfo: CangjieErrorDetail): string {
    return `${this.combinePhase1(errorInfo)}${os.EOL}${this.combinePhase2(errorInfo)}${os.EOL}${this.combinePhase3(
      errorInfo)}`;
  }

  /**
   * 组装三段式的第一段：错误码+描述
   * @param errorInfo
   * @returns
   */
  private combinePhase1(errorInfo: CangjieErrorDetail): string {
    return `${CangjieErrorCommonAdapter.red}${errorInfo.getCode()} ${errorInfo.getDescription()}`;
  }

  /**
   * 组装三段式的第二段：错误原因+报错位置
   * @param errorInfo
   * @returns
   */
  private combinePhase2(errorInfo: CangjieErrorDetail): string {
    const at = ' At ';
    const position = at + errorInfo.getPosition();
    const res = `${CangjieErrorCommonAdapter.red}Error Message: ${errorInfo.getCause()}${position === at ? '' :
      position}`;
    if (res.includes(SPLIT_TAG)) {
      return this.composeCauseAndPosition(errorInfo.getCause(), errorInfo.getPosition());
    } else {
      return `${CangjieErrorCommonAdapter.red}Error Message: ${errorInfo.getCause()}${position === at ? '' : position}`;
    }
  }

  /**
   * 对于printMergedError()的场景需要将错误原因和报错位置配对
   * 通过merge时的特殊分割点SPLIT_TAG来进行分割，有两种情况：
   * 1、cause数量和position数量一致：一一配对
   * 2、cause数量和position数量不一致：
   * 1）cause全量 + position一个： 需要将每个cause一行 + position单独放最后一行
   * 2）cause一个 + position全量： 需要将每个cause一行 + position每个单独放一行
   *
   * @param cause
   * @param position
   * @returns
   */
  private composeCauseAndPosition(cause: string, position: string): string {
    let res = `${CangjieErrorCommonAdapter.red}Error Message: `;
    const at = ' At ';

    const causeList = cause.split(SPLIT_TAG);
    const positionList = position.split(SPLIT_TAG);

    if (causeList.length === positionList.length) {
      res = this.composeCauseAndPositionWithSameLength(causeList, positionList, at, res);
    } else {
      res = this.composeCauseAndPositionWithUnSameLength(causeList, positionList, ` ${at}`, res);
    }

    return res;
  }

  /**
   * 原因和位置一一对应拼接
   * @param causeList
   * @param positionList
   * @param at
   * @param resParam
   * @returns
   */
  private composeCauseAndPositionWithSameLength(causeList: string[], positionList: string[], at: string,
    resParam: string): string {
    let res = resParam;
    for (let i = 0; i < causeList.length; i++) {
      const cau = causeList[i];
      const pos = at + positionList[i];
      res += `${cau + pos}${os.EOL}`;
    }
    return res.slice(0, -`${os.EOL}`.length);
  }

  /**
   * 原因和位置纵向排列
   * @param causeList
   * @param positionList
   * @param at
   * @param resParam
   * @returns
   */
  private composeCauseAndPositionWithUnSameLength(causeList: string[], positionList: string[], at: string,
    resParam: string): string {
    let res = resParam;
    for (let i = 0; i < causeList.length; i++) {
      const cau = causeList[i];
      res += `${cau}${os.EOL}`;
    }
    for (let i = 0; i < positionList.length; i++) {
      const pos = at + positionList[i];
      res += `${pos}${os.EOL}`;
    }
    return res.slice(0, -`${os.EOL}`.length);
  }

  /**
   * 组装三段式的第三段：解决方案
   * @param errorInfo
   * @returns
   */
  private combinePhase3(errorInfo: CangjieErrorDetail): string {
    let solutionMessage = `${CangjieErrorCommonAdapter.red}${os.EOL}* Try the following:${os.EOL}`;
    const solutions = errorInfo.getSolutions();
    if (solutions instanceof Array && solutions.length > 0) {
      solutions.forEach((solution) => {
        solutionMessage += `${CangjieErrorCommonAdapter.red}  > ${solution}${os.EOL}`;
      });
    } else {
      return '';
    }

    const moreInfo = errorInfo.getMoreInfo();
    if (moreInfo) {
      const href = moreInfo[getOsLanguage()]; // 国际化
      solutionMessage += `${CangjieErrorCommonAdapter.red}  > More info: ${href}${os.EOL}`;
    }

    return solutionMessage;
  }
}
