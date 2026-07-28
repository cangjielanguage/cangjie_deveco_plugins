/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.testbuild;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.RunConfiguration;

import java.util.Objects;

/**
 * CangjieTestBuildTask
 *
 * @since 2025/02/20
 */
public class CangjieTestBuildTask extends BeforeRunTask<CangjieTestBuildTask> {
    public CangjieTestBuildTask(RunConfiguration runConfiguration) {
        super(Objects.requireNonNull(BuildCangjieTestProviderRepo.getProvider(runConfiguration)).getKey());
        setEnabled(true);
    }
}
