/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

export const UNDEFINED_CODE = '00000000';
export const UNDEFINED_DESC = '';
export const UNDEFINED_CAUSE = 'Unknown';
export const UNDEFINED_POS = '';
export const SPLIT_TAG = '<HVIGOR_ERROR_SPLIT>';
export const DEFAULT_ERROR_CODE = '00000';
export const ERROR_NOT_MATCH = 'The error information does not match.';

export enum CountryEnum {
  CN = 'cn',
  EN = 'en',
}

export type MoreInfo = Record<CountryEnum, string>;
export type MatchFieldType = 'none' | 'id' | 'code' | 'checkMessage';


/**
 * json文件里的错误信息
 * hvigor及插件
 */
export interface CangjieErrorInfo extends BaseErrorInfo {

  // 带占位符的错误信息
  message?: string;
}

export interface BaseErrorInfo {
  code?: string;
  moreInfo?: MoreInfo;
  checkMessage?: string;
  solutions?: string[];
}

export interface ErrorInfoId {
  id: string;
}

export interface MatchOptions {
  field: MatchFieldType;
  value: any;
}

/**
 * 适配器返回的错误信息
 */
export interface CangjieAdaptorErrorMessage {
  timestamp?: Date;
  id?: string;
  code?: string;
  originMessage?: string;
  originSolutions?: string[];
  moreInfo?: MoreInfo;
  stack?: string;
  message: string;
  solutions?: string[];
  components?: string;
  checkMessage?: string;
}

export interface TCangjieErrorInfo {

  /**
   * 错误码, 8位
   */
  code: string;

  /**
   * 对错误码的描述
   */
  description: string;

  /**
   * 错误原因，描述错误发生的前提，需针对发生错误的上下文具体描述
   */
  cause: string;

  /**
   * 错误发生的位置，从用户视角指明哪个文件、哪个配置项，哪个API，哪个参数等报错
   */
  position: string;

  /**
   * 具体的解决方法，一般为多条，一条方法为一个字符串
   */
  solutions: string[];

  /**
   * 指导解决该错误的官网FAQ地址，格式：{cn: https://xxx, en: https://xxx}
   */
  moreInfo?: MoreInfo;
};

export enum ErrorCode {
  ERROR_00 = '00',
  ERROR_01 = '01',
  ERROR_02 = '02',
  ERROR_03 = '03',
  ERROR_04 = '04',
  ERROR_05 = '05',
  ERROR_06 = '06',
  ERROR_07 = '07',
  ERROR_08 = '08',
}

// 将错误码映射到描述信息
export const ErrorCodeDescription: Record<ErrorCode, string> = {
  [ErrorCode.ERROR_00]: 'Unknown Error',
  [ErrorCode.ERROR_01]: 'Dependency Error',
  [ErrorCode.ERROR_02]: 'Script Error',
  [ErrorCode.ERROR_03]: 'Configuration Error',
  [ErrorCode.ERROR_04]: 'Not Found',
  [ErrorCode.ERROR_05]: 'Syntax Error',
  [ErrorCode.ERROR_06]: 'Specification Limit Violation',
  [ErrorCode.ERROR_07]: 'Permissions Error',
  [ErrorCode.ERROR_08]: 'Operation Error',
};
