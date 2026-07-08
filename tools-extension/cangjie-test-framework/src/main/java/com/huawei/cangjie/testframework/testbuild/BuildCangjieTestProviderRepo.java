/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.testbuild;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Key;

import java.util.List;
import java.util.Optional;

/**
 * BuildCangjieTestProviderRepo
 *
 * @since 2025/02/20
 */
public interface BuildCangjieTestProviderRepo {
    /**
     * 构建测试用例任务扩展提供点 build_test_task_provider_extension_point_name
     */
    ExtensionPointName<BuildCangjieTestProviderRepo> BUILD_TEST_TASK_PROVIDER_EXTENSION_POINT_NAME
            = ExtensionPointName.create("com.huawei.cangjie.ohos.buildCangjieTestTaskProvider");

    /**
     * OHOS_TEST_BUILD_HVIGOR_BEFORE_RUN_TASK
     */
    String CANGJIE_TEST_BUILD_HVIGOR_BEFORE_RUN_TASK = "CangjieTest.Build.Hvigor.BeforeRunTask";

    /**
     * getProvider
     *
     * @param configuration RunConfiguration
     * @return BuildTestTaskProvider
     */
    static BuildCangjieTestProviderRepo getProvider(RunConfiguration configuration) {
        List<BuildCangjieTestProviderRepo> extensionList =
                BUILD_TEST_TASK_PROVIDER_EXTENSION_POINT_NAME.getExtensionList();
        BuildCangjieTestProviderRepo testBuildTaskProvider = null;
        if (configuration instanceof CangjieTestRunConfiguration) {
            testBuildTaskProvider = getTestBuildTaskProviderByKey(extensionList,
                    CANGJIE_TEST_BUILD_HVIGOR_BEFORE_RUN_TASK).orElse(null);
        }
        return testBuildTaskProvider;
    }

    /**
     * getBuildTaskProviderByKey
     *
     * @param extensionList List<BuildCangjieTestProvider>
     * @param key String
     * @return BuildTestTaskProvider
     */
    static Optional<BuildCangjieTestProviderRepo> getTestBuildTaskProviderByKey(
            List<BuildCangjieTestProviderRepo> extensionList, String key) {
        for (BuildCangjieTestProviderRepo extension : extensionList) {
            if (extension.getKey().toString().equals(key)) {
                return Optional.of(extension);
            }
        }
        return Optional.empty();
    }

    /**
     * getKey
     *
     * @return Key<CangjieTestBuildTask>
     */
    Key<CangjieTestBuildTask> getKey();
}
