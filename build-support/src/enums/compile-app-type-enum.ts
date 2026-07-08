/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

export enum CompileAppTypeEnum {

  // 1、compileSdkVersion <= 21 && Regardless of the value of compatibleSDKVersion
  COMPILE_SDK_VER_LTE_21 = 'COMPILE_SDK_VER_LTE_21',

  // 2、compatibleSdkVersion == 22
  COMPATIBLE_SDK_VER_EQ_22 = 'COMPATIBLE_SDK_VER_EQ_22',

  // 3、compatibleSdkVersion < 23
  COMPATIBLE_SDK_VER_LT_23 = 'COMPATIBLE_SDK_VER_LT_23',

  // normal app
  NORMAL_APP = 'NORMAL_APP',
}
