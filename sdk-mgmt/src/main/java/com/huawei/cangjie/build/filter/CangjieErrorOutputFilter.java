/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.build.filter;

import com.huawei.hvigor.run.HvigorConsoleAdditionalFilter;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.execution.filters.FileHyperlinkRawDataFinder;
import com.intellij.execution.filters.PatternBasedFileHyperlinkRawDataFinder;
import com.intellij.execution.filters.PatternHyperlinkFormat;
import com.intellij.execution.filters.PatternHyperlinkPart;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Cangjie Log Parsing
 *
 * @since 2024-03-15
 */
public class CangjieErrorOutputFilter extends AbstractFileHyperlinkFilter implements DumbAware {
    private static final FileHyperlinkRawDataFinder FINDER = new OhosFileFinder();

    public CangjieErrorOutputFilter(@NotNull Project project, @Nullable String baseDir) {
        super(project, baseDir);
    }

    @Override
    @NotNull
    public List<FileHyperlinkRawData> parse(@NotNull String line) {
        return FINDER.find(line);
    }

    @Override
    @Nullable
    public VirtualFile findFile(@NotNull String filePathParam) {
        String filePath = filePathParam;
        filePath = StringUtil.trimStart(filePath, "file://");
        filePath = HvigorConsoleAdditionalFilter.convertWslPath(filePath);
        return super.findFile(filePath);
    }

    @Override
    protected boolean supportVfsRefresh() {
        return true;
    }

    private static class OhosFileFinder implements FileHyperlinkRawDataFinder {
        private static final PatternBasedFileHyperlinkRawDataFinder PATTERN_FINDER =
            new PatternBasedFileHyperlinkRawDataFinder(new PatternHyperlinkFormat[]{
                new PatternHyperlinkFormat(
                    Pattern.compile("^\\s*(?:\\[Error Detail])?\\s*==>\\s*(.+?):(\\d+)(:\\d+)?:?\\s*\\r?\\n?$"), false,
                    false, PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN)
            });

        @Override
        @NotNull
        public List<FileHyperlinkRawData> find(@NotNull String line) {
            List<FileHyperlinkRawData> result = PATTERN_FINDER.find(line);
            if (!result.isEmpty()) {
                return result;
            }
            return ContainerUtil.createMaybeSingletonList(null);
        }
    }
}
