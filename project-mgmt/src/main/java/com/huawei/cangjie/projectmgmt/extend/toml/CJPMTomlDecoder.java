/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.extend.toml;

import toml.parser.CJPMTomlLexer;
import toml.parser.CJPMTomlParser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Optional;

/**
 * CJPMTomlDecoder
 *
 * @since 2025/11/20
 */
public class CJPMTomlDecoder {
    private static int errorCnt = 0;

    private CommonTokenStream tokens;

    private ParseTree parseTree;

    /**
     * toml parser
     *
     * @param toml target toml content
     * @return toml obj result
     */
    public Optional<HashMap<String, Object>> decode(String toml) {
        errorCnt = 0;

        CharStream charStream = CharStreams.fromString(toml);
        CJPMTomlLexer lexer = new CJPMTomlLexer(charStream);
        tokens = new CommonTokenStream(lexer);
        CJPMTomlParser parser = new CJPMTomlParser(tokens);
        parser.setTokenStream(tokens);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line,
                                    int charPositionInLine,
                                    String msg,
                                    RecognitionException e) {
                errorCnt++;
            }
        });
        parseTree = parser.cjpmtoml();
        if (errorCnt > 0) {
            return Optional.empty();
        }
        CJPMTomlVisitor visitor = new CJPMTomlVisitor();
        var result = parseTree.accept(visitor);
        if (visitor.getErrorCnt() > 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(result);
    }

    /**
     * get tokens
     *
     * @return tokens
     */
    protected CommonTokenStream getTokenStream() {
        return tokens;
    }

    /**
     * get parsed tree
     *
     * @return parse tree
     */
    protected ParseTree getParseTree() {
        return parseTree;
    }
}
