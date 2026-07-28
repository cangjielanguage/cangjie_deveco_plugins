/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import hilog from '@ohos.hilog';

export default {
    onCreate() {
        hilog.info(0x0000, 'testTag', '%{public}s', 'Application onCreate');
    },
    onDestroy() {
        hilog.info(0x0000, 'testTag', '%{public}s', 'Application onDestroy');
    },
}