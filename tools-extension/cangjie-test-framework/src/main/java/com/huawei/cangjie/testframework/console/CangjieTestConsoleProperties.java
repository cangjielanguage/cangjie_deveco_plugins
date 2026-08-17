/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.console;

import com.huawei.deveco.ohos.testframework.run.testrunner.StackTraceHyperLinkFilter;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * CangjieTestConsoleProperties
 *
 * @since 2025/09/01
 */
public class CangjieTestConsoleProperties extends SMTRunnerConsoleProperties {
    /**
     * testLocatorMap
     */
    private static final Map<String, SMTestLocator> TEST_LOCATOR_MAP = Map.of(
            CangjieOhosTestLocationProvider.PROTOCOL_ID, CangjieOhosTestLocationProvider.INSTANCE,
            CangjieLocalTestLocationProvider.PROTOCOL_ID, CangjieLocalTestLocationProvider.INSTANCE
    );

    private final RunConfiguration configuration;
    private final String protocolId;

    public CangjieTestConsoleProperties(@NotNull RunConfiguration configuration,
                                        @NotNull Executor executor,
                                        @NotNull String protocolId) {
        super(configuration, protocolId, executor);
        this.protocolId = protocolId;
        this.configuration = configuration;
    }

    /**
     * getTestLocatorInstance
     *
     * @param protocolId protocolId
     * @return TestLocatorInstance
     */
    public static SMTestLocator getTestLocatorInstance(@NotNull String protocolId) {
        return TEST_LOCATOR_MAP.getOrDefault(protocolId, CangjieOhosTestLocationProvider.INSTANCE);
    }

    @Override
    public SMTestLocator getTestLocator() {
        return TEST_LOCATOR_MAP.get(protocolId);
    }

    @Override
    protected GlobalSearchScope initScope() {
        return super.initScope();
    }

    @Override
    @Nullable
    public TestProxyFilterProvider getFilterProvider() {
        return new MyTestProxyFilterProvider(configuration);
    }

    static class MyTestProxyFilterProvider implements TestProxyFilterProvider {
        RunConfiguration configuration;

        public MyTestProxyFilterProvider(RunConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        @Nullable
        public Filter getFilter(@NotNull String nodeType, @NotNull String nodeName,
                                @Nullable String nodeArguments) {
            Project project = configuration.getProject();
            return new StackTraceHyperLinkFilter(project, project.getBasePath());
        }
    }
}
