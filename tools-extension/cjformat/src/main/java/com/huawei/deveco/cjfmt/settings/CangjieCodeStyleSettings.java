/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjfmt.settings;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * class CangjieCodeStyleSettings
 *
 * @since 2024 -11-7
 */
public class CangjieCodeStyleSettings extends CustomCodeStyleSettings {
    /**
     * The constant INDENT_WIDTH_DEFAULT.
     */
    public static final int INDENT_WIDTH_DEFAULT = 4;

    /**
     * The constant LINE_LIMIT_LENGTH_DEFAULT.
     */
    public static final int LINE_LIMIT_LENGTH_DEFAULT = 120;

    /**
     * The constant MULTIPLE_LINE_METHOD_CHAIN_LEVEL_DEFAULT.
     */
    public static final int MULTIPLE_LINE_METHOD_CHAIN_LEVEL_DEFAULT = 2;

    /**
     * INDENT_WIDTH
     */
    public int indentWidth = INDENT_WIDTH_DEFAULT;

    /**
     * LINE_LIMIT_LENGTH
     */
    public int lineLimitLength = LINE_LIMIT_LENGTH_DEFAULT;

    /**
     * LINE_BREAK_TYPE
     */
    public LineBreakTypeStyle lineBreakType = LineBreakTypeStyle.LF;

    /**
     * ALLOW_MULTI_LINE_METHOD_CHAIN
     */
    public boolean allowMultiLineMethodChain = true;

    /**
     * MULTIPLE_LINE_METHOD_CHAIN_LEVEL
     */
    public int methodChainLevel = MULTIPLE_LINE_METHOD_CHAIN_LEVEL_DEFAULT;

    /**
     * MULTIPLE_LINE_METHOD_CHAIN_OVER_LINE_LENGTH
     */
    public boolean methodChainOverLength = true;

    /**
     * Instantiates a new Cangjie code style settings.
     *
     * @param settings the settings
     */
    public CangjieCodeStyleSettings(CodeStyleSettings settings) {
        super("CangjieCodeStyleSettings", settings);
    }

    /**
     * Copy settings.
     *
     * @param lang the lang
     * @param rootSettings the root settings
     * @param targetSettings the target settings
     */
    public static void copySettings(Language lang, CodeStyleSettings rootSettings, CodeStyleSettings targetSettings) {
        CommonCodeStyleSettings sourceCommonSettings = rootSettings.getCommonSettings(lang);
        CangjieCodeStyleSettings targetCommonSettings =
            targetSettings.getCustomSettings(CangjieCodeStyleSettings.class);
        if (sourceCommonSettings.getIndentOptions() != null) {
            if (CangjieCodeStyleSettings.validateIndentWidth(sourceCommonSettings.getIndentOptions().INDENT_SIZE)) {
                targetCommonSettings.indentWidth = sourceCommonSettings.getIndentOptions().INDENT_SIZE;
            }
        }
        if (CangjieCodeStyleSettings.validateLineLimitLength(sourceCommonSettings.RIGHT_MARGIN)) {
            targetCommonSettings.lineLimitLength = sourceCommonSettings.RIGHT_MARGIN;
        }
    }

    /**
     * Validate indent width boolean.
     *
     * @param indentWidthInput the indent width input
     * @return the boolean
     */
    public static boolean validateIndentWidth(int indentWidthInput) {
        return indentWidthInput >= 0 && indentWidthInput <= 8;
    }

    /**
     * Validate line limit length boolean.
     *
     * @param lineLimitLengthInput the line limit length input
     * @return the boolean
     */
    public static boolean validateLineLimitLength(int lineLimitLengthInput) {
        return lineLimitLengthInput >= 1 && lineLimitLengthInput <= 120;
    }

    /**
     * Validate chain level boolean.
     *
     * @param chainLevelInput the chain level input
     * @return the boolean
     */
    public static boolean validateChainLevel(int chainLevelInput) {
        return chainLevelInput >= 2 && chainLevelInput <= 10;
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indentWidth, lineLimitLength, lineBreakType, allowMultiLineMethodChain, methodChainLevel,
            methodChainOverLength);
    }

    /**
     * Write config to file.
     *
     * @param settings the settings
     * @param cjfmtTomlPath the cjfmt toml path
     * @throws IOException the io exception
     */
    public static void writeConfigToFile(CangjieCodeStyleSettings settings, Path cjfmtTomlPath) throws IOException {
        if (cjfmtTomlPath == null) {
            return;
        }
        List<String> configList = new ArrayList<>();
        configList.add("indentWidth = " + settings.indentWidth);
        configList.add("linelimitLength = " + settings.lineLimitLength);
        configList.add("lineBreakType = \"" + settings.lineBreakType.getBreakType() + "\"");
        configList.add("allowMultiLineMethodChain = " + settings.allowMultiLineMethodChain);
        configList.add("multipleLineMethodChainLevel = " + settings.methodChainLevel);
        configList.add("multipleLineMethodChainOverLineLength = " + settings.methodChainOverLength);
        if (cjfmtTomlPath.getParent() != null) {
            Files.createDirectories(cjfmtTomlPath.getParent());
        }
        Files.write(cjfmtTomlPath, configList);
    }
}
