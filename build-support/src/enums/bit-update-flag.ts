/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

export enum BitUpdateFlag {
  NONE = 0, // 000 —— 都未设置
  FIRST = 1 << 0, // 001 —— 第 1 位
  SECOND = 1 << 1, // 010 —— 第 2 位
  THIRD = 1 << 2, // 100 —— 第 3 位
}

/**
 * 通用工具函数
 */
export class BitUpdateFlagUtils {
  /**
   * 设置（可以一次设置多位）
   * @param {BitUpdateFlag} src
   * @param {BitUpdateFlag} flags
   * @returns {BitUpdateFlag}
   */
  static set(src: BitUpdateFlag, ...flags: BitUpdateFlag[]): BitUpdateFlag {
    return flags.reduce((acc, f) => acc | f, src);
  }

  /**
   * 清除（可以一次清除多位）
   * @param {BitUpdateFlag} src
   * @param {BitUpdateFlag} flags
   * @returns {BitUpdateFlag}
   */
  static clear(src: BitUpdateFlag, ...flags: BitUpdateFlag[]): BitUpdateFlag {
    return flags.reduce((acc, f) => acc & ~f, src);
  }

  /**
   *  取反（toggle）
   * @param {BitUpdateFlag} src
   * @param {BitUpdateFlag} flags
   * @returns {BitUpdateFlag}
   */
  static toggle(src: BitUpdateFlag, ...flags: BitUpdateFlag[]): BitUpdateFlag {
    return flags.reduce((acc, f) => acc ^ f, src);
  }

  /**
   * 判断是否全部包含（AND 检测）
   * @param {BitUpdateFlag} src
   * @param {BitUpdateFlag} flag
   * @returns {boolean}
   */
  static has(src: BitUpdateFlag, flag: BitUpdateFlag): boolean {
    return (src & flag) === flag;
  }
}
