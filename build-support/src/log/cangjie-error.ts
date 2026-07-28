/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import util from 'util';
import {CangjieErrorInfo, DEFAULT_ERROR_CODE, ErrorInfoId, MatchOptions, MoreInfo} from './cangjie-types';
import fs from 'fs';

/**
 * cangjie的报错
 */
export class CangjieError {
  private static readonly validFields: string[] = ['id', 'checkMessage', 'code']; // 匹配错误信息支持的字段
  protected readonly _errorJsonPaths: string[]; // 保存错误信息的文件路径
  protected _timestamp: Date; // 时间戳
  protected _id: string | undefined; // 错误标识
  protected _message: string | undefined; // 完整的错误信息
  protected _solutions: string[] | undefined; // 完整的解决方法
  protected _moreInfo: MoreInfo | undefined; // 指导解决的url
  protected _code: string | undefined; // 错误码
  protected _checkMessage: string | undefined; // 匹配用的字符串，message=%s时必填
  protected _stack: string | undefined; // 堆栈
  protected _errorInfo: (CangjieErrorInfo & ErrorInfoId) | undefined; // 从json文件拿到的数据
  private readonly _originMessage: string | undefined; // 原始的错误信息，包含占位符
  private readonly _originSolutions: string[] | undefined; // 原始的解决方法，包含占位符
  private readonly _matchOptions: MatchOptions; // 匹配错误信息用的参数

  constructor(errorJsonPaths: string[], matchOptions: MatchOptions) {
    this._timestamp = new Date();
    this._errorJsonPaths = errorJsonPaths;
    this._matchOptions = matchOptions;

    this._errorInfo = this.findErrorInfo();
    if (this._errorInfo) {
      this._id = this._errorInfo.id;
      this._code = this._errorInfo.code;
      this._moreInfo = this._errorInfo.moreInfo;
      this._checkMessage = this._errorInfo.checkMessage;
      this._solutions = this._errorInfo.solutions;
      this._originMessage = this._errorInfo.message;
      this._message = this._errorInfo.message;
      this._originSolutions = this._errorInfo.solutions;
    } else {
      this._code = DEFAULT_ERROR_CODE;
    }
  }

  get originMessage(): string | undefined {
    return this._originMessage;
  }

  get originSolutions(): string[] | undefined {
    return this._originSolutions;
  }

  get timestamp(): Date {
    return this._timestamp;
  }

  get errorJsonPaths(): string[] {
    return this._errorJsonPaths;
  }

  get id(): string | undefined {
    return this._id;
  }

  get message(): string | undefined {
    return this._message;
  }

  get solutions(): string[] | undefined {
    return this._solutions;
  }

  get moreInfo(): MoreInfo | undefined {
    return this._moreInfo;
  }

  get code(): string | undefined {
    return this._code;
  }

  get stack(): string | undefined {
    return this._stack;
  }

  get checkMessage(): string | undefined {
    return this._checkMessage;
  }

  get errorInfo(): (CangjieErrorInfo & ErrorInfoId) | undefined {
    return this._errorInfo;
  }

  set id(value: string | undefined) {
    this._id = value;
  }

  set message(value: string | undefined) {
    this._message = value;
  }

  set solutions(value: string[] | undefined) {
    this._solutions = value;
  }

  set moreInfo(value: MoreInfo | undefined) {
    this._moreInfo = value;
  }

  set code(value: string | undefined) {
    this._code = value;
  }

  set stack(value: string | undefined) {
    this._stack = value;
  }

  set checkMessage(value: string | undefined) {
    this._checkMessage = value;
  }

  formatMessage(...args: unknown[]): void {
    if (this.originMessage) {
      this._message = util.format(this._originMessage, ...args);
    }
  }

  formatSolutions(index: number, ...args: unknown[]): void {
    if (this._originSolutions?.[index] && this._solutions) {
      this._solutions[index] = util.format(this._originSolutions[index], ...args);
    }
  }

  /**
   * 是否从json文件中匹配到错误信息
   * @returns {boolean}
   */
  public isMatchSuccess(): boolean {
    return !!this._errorInfo;
  }

  public findErrorInfo(): (CangjieErrorInfo & ErrorInfoId) | undefined {
    return this.match(this._matchOptions);
  }

  /**
   * 查找错误信息
   */
  private match(matchOptions: MatchOptions): (CangjieErrorInfo & ErrorInfoId) | undefined {
    if (!CangjieError.validFields.includes(matchOptions.field) || !matchOptions.value) {
      return undefined;
    }
    if (matchOptions.field === 'id') {
      return this.matchById(matchOptions.value);
    }
    return this.matchByField(matchOptions);
  }

  /**
   * 通过field字段匹配错误信息
   */
  private matchByField(matchOptions: MatchOptions): (CangjieErrorInfo & ErrorInfoId) | undefined {
    const errorInfoJson = this.getErrorInfoJson();
    if (!errorInfoJson) {
      return undefined;
    }

    for (const key of Object.keys(errorInfoJson)) {
      const errorInfo = errorInfoJson[key] as unknown as Record<string, string>;

      // 通过checkMessage匹配错误信息时，value是完整的错误信息
      if (matchOptions.field === 'checkMessage') {
        if (errorInfo.checkMessage && matchOptions.value.includes(errorInfo.checkMessage)) {
          return this.putIdIntoErrorInfo(key, errorInfoJson[key]);
        }
      } else if (matchOptions.value === errorInfo[matchOptions.field]) {
        return this.putIdIntoErrorInfo(key, errorInfoJson[key]);
      }
    }

    return undefined;
  }

  /**
   * 通过标识匹配错误信息
   */
  private matchById(id: string): (CangjieErrorInfo & ErrorInfoId) | undefined {
    const errorInfoJson = this.getErrorInfoJson();
    return this.putIdIntoErrorInfo(id, errorInfoJson?.[id]);
  }

  private putIdIntoErrorInfo(id: string, errorInfo?: CangjieErrorInfo): (CangjieErrorInfo & ErrorInfoId) | undefined {
    if (errorInfo) {
      return {...errorInfo, id};
    }
    return undefined;
  }

  /**
   * 获取json文件对象
   */
  private getErrorInfoJson(): Record<string, CangjieErrorInfo> | undefined {
    if (this._errorJsonPaths.length) {
      return this._errorJsonPaths.reduce((pre, path) => ({...pre, ...this.getJsonObj(path)}), {});
    } else {
      return undefined;
    }
  }

  private getJsonObj(jsonPath: string, encodingStr = 'utf-8'): any {
    if (!fs.existsSync(jsonPath)) {
      return undefined;
    }
    const text = fs.readFileSync(jsonPath, {encoding: encodingStr as BufferEncoding});
    try {
      return JSON.parse(text);
    } catch (e) {
      return undefined;
    }
  }
}
