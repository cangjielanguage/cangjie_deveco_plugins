/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.build.filter;

import com.huawei.hvigor.api.HvigorFilterFactory;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for constructing console log filters
 *
 * @since 2024-03-15
 */
public class CangjieBuildFilterFactory implements HvigorFilterFactory {
    @NotNull
    @Override
    public List<Filter> createFilters(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(new CangjieErrorOutputFilter(project, project.getBasePath()));
        return filters;
    }
}
