/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.extend.toml;

import com.intellij.openapi.diagnostic.Logger;

import toml.parser.CJPMTomlParser;
import toml.parser.CJPMTomlParserBaseListener;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CJPMTomlEncoder
 *
 * @since 2025/11/20
 */
public class CJPMTomlEncoder {
    /**
     * The constant INTENT_SPACE.
     */
    public static final String INTENT_SPACE = "  ";

    private static final Logger LOG = Logger.getInstance(Toml.class);

    private static final LinkedHashMap<String, String> ESCAPE_CHARACTERS = new LinkedHashMap<>();

    static {
        ESCAPE_CHARACTERS.put("\\", "\\\\");
        ESCAPE_CHARACTERS.put("\b", "\\b");
        ESCAPE_CHARACTERS.put("\n", "\\n");
        ESCAPE_CHARACTERS.put("\r", "\\r");
        ESCAPE_CHARACTERS.put("\t", "\\t");
        ESCAPE_CHARACTERS.put("\f", "\\f");
        ESCAPE_CHARACTERS.put("\"", "\\\"");
    }

    private final Toml rawToml;

    private final List<DiffEntry> diffs = new ArrayList<>();

    /**
     * CJPMTomlEncoder constructor
     *
     * @param toml toml
     */
    public CJPMTomlEncoder(Toml toml) {
        rawToml = toml;
    }

    private static class DiffEntry {
        List<String> path;
        Object newValue;
        Type type;
        boolean consumed = false;

        protected DiffEntry(List<String> path, Object value, Type type) {
            this.path = path;
            this.newValue = value;
            this.type = type;
        }

        enum Type {ADD, UPDATE, DELETE}

        /**
         * Table parts list.
         *
         * @return the list
         */
        List<String> tableParts() {
            if (path == null || path.size() < 2) {
                return Collections.emptyList();
            }
            return path.subList(0, path.size() - 1);
        }

        /**
         * To table header string.
         *
         * @return the string
         */
        String toTableHeader() {
            List<String> parts = tableParts();
            if (parts.isEmpty()) {
                return "";
            }
            return "[" + String.join(".", parts) + "]";
        }

        /**
         * Key name string.
         *
         * @return the string
         */
        String keyName() {
            if (path == null || path.isEmpty()) {
                return StringUtils.EMPTY;
            }
            return path.getLast();
        }
    }

    /**
     * update toml to file
     *
     * @param from map
     * @param target file
     * @throws IOException e
     */
    public void write(Object from, File target) throws IOException {
        diffMap((Map<String, Object>) from, rawToml.toMap(), new ArrayList<>());
        String newContent = applyDiffs();
        doWriteToml(target, newContent);
    }

    /**
     * update toml to file
     *
     * @param target file
     * @throws IOException e
     */
    public void rewriteAll(File target) throws IOException {
        String newContent = diffs.isEmpty() ? formatWithIndent() : applyDiffs();
        doWriteToml(target, newContent);
    }

    /**
     * format with indent based on raw content lines, preserving dotted key headers
     *
     * @return formatted content
     */
    private String formatWithIndent() {
        String rawContent = rawToml.getRawContent();
        if (rawContent == null || rawContent.isEmpty()) {
            return rawContent;
        }
        StringBuilder sb = new StringBuilder();
        int currentKeyValueIndentLevelContext = 0;
        String currentTopLevelTableName = null;
        boolean hasWrittenTopLevelTable = false;
        String[] lines = rawContent.split("\n|\r\n|\r");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            if (trimmedLine.startsWith("[")) {
                String tableName = trimmedLine.substring(1, trimmedLine.length() - 1);
                boolean isSubTable =
                    (currentTopLevelTableName != null && tableName.contains(".") && tableName.startsWith(
                        currentTopLevelTableName + "."));
                if (!isSubTable) {
                    if (hasWrittenTopLevelTable && !sb.isEmpty() && !sb.toString().endsWith("\n\n")) {
                        sb.append("\n");
                    }
                    sb.append(trimmedLine).append("\n");
                    currentKeyValueIndentLevelContext = 0;
                    currentTopLevelTableName = tableName;
                    hasWrittenTopLevelTable = true;
                } else {
                    String suffix = tableName.substring(currentTopLevelTableName.length() + 1);
                    int dotCountInSuffix = 0;
                    for (char c : suffix.toCharArray()) {
                        if (c == '.') {
                            dotCountInSuffix++;
                        }
                    }
                    int nestingLevel = dotCountInSuffix + 1;
                    String tableIndent = INTENT_SPACE.repeat(nestingLevel);
                    sb.append(tableIndent).append(trimmedLine).append("\n");
                    currentKeyValueIndentLevelContext = nestingLevel;
                }
            } else if (trimmedLine.contains("=")) {
                String indent = INTENT_SPACE.repeat(currentKeyValueIndentLevelContext + 1);
                sb.append(indent).append(trimmedLine).append("\n");
            } else {
                sb.append(trimmedLine).append("\n");
            }
        }
        return sb.toString();
    }

    private void doWriteToml(File target, String newContent) throws IOException {
        OutputStreamWriter writer = null;
        try (OutputStream outputStream = new FileOutputStream(target)) {
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(newContent);
            writer.flush();
        } catch (IOException e) {
            LOG.warn("write cjpm.toml failed: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void diffMap(Map<String, Object> a, Map<String, Object> b, List<String> path) {
        for (Map.Entry<String, Object> entry : a.entrySet()) {
            path.add(entry.getKey());
            if (!b.containsKey(entry.getKey())) {
                diffs.add(new DiffEntry(new ArrayList<>(path), entry.getValue(), DiffEntry.Type.ADD));
            } else if (!Objects.equals(a.get(entry.getKey()), b.get(entry.getKey()))) {
                if (entry.getValue().getClass().equals(b.get(entry.getKey()).getClass())) {
                    if (isPrimary(entry.getValue())) {
                        diffs.add(new DiffEntry(new ArrayList<>(path), entry.getValue(), DiffEntry.Type.UPDATE));
                    } else if (entry.getValue() instanceof Map<?, ?>) {
                        diffMap((Map<String, Object>) entry.getValue(),
                                (Map<String, Object>) b.get(entry.getKey()), path);
                    } else if (entry.getValue() instanceof ArrayList<?>) {
                        diffs.add(new DiffEntry(new ArrayList<>(path), entry.getValue(), DiffEntry.Type.UPDATE));
                    } else {
                        LOG.warn("unsupported type");
                        path.removeLast();
                        continue;
                    }
                } else {
                    diffs.add(new DiffEntry(new ArrayList<>(path), entry.getValue(), DiffEntry.Type.UPDATE));
                }
            } else {
                path.removeLast();
                continue;
            }
            path.removeLast();
        }

        for (String key : b.keySet()) {
            if (!a.containsKey(key)) {
                path.add(key);
                diffs.add(new DiffEntry(new ArrayList<>(path), null, DiffEntry.Type.DELETE));
                path.removeLast();
            }
        }
    }

    private String applyDiffs() {
        CommonTokenStream tokens = rawToml.getDecoder().getTokenStream();
        ParseTree parseTree = rawToml.getDecoder().getParseTree();
        TokenStreamRewriter rewriter = new TokenStreamRewriter(tokens);

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(new CJPMTomlParserBaseListener() {
            private final List<String> context = new ArrayList<>();

            private boolean inDelete = false;
            @Override
            public void exitStandardTable(CJPMTomlParser.StandardTableContext ctx) {
                inDelete = false;
                context.clear();
                context.addAll(getKeys(ctx));
                DiffEntry diff = lookupDiffFor(context);

                if (diff == null || diff.type != DiffEntry.Type.DELETE) {
                    return;
                }

                rewriter.delete(ctx.getStart(), ctx.getStop());
                inDelete = true;
            }

            @Override
            public void exitArrayTable(CJPMTomlParser.ArrayTableContext ctx) {
                inDelete = false;
                context.clear();
                context.addAll(getKeys(ctx));
                DiffEntry diff = lookupDiffFor(context);

                if (diff == null) {
                    return;
                }

                if (diff.type == DiffEntry.Type.ADD) {
                    rewriter.insertAfter(ctx.getStop(),
                            "\n" + diff.path.getLast() + " = " + objectToString(diff.newValue));
                }

                if (diff.type == DiffEntry.Type.DELETE) {
                    rewriter.delete(ctx.getStart(), ctx.getStop());
                    inDelete = true;
                }
            }

            @Override
            public void exitKeyValue(CJPMTomlParser.KeyValueContext ctx) {
                if (inDelete) {
                    rewriter.delete(ctx.getStart(), ctx.getStop());
                    return;
                }
                context.addAll(getKeys(ctx));

                DiffEntry diff = lookupDiffFor(context);
                if (diff == null) {
                    context.removeLast();
                    return;
                }

                switch (diff.type) {
                    case UPDATE -> {
                        try {
                            rewriter.replace(ctx.value().getStart(),
                                    ctx.value().getStop(), objectToString(diff.newValue));
                        } catch (UnsupportedOperationException e) {
                            LOG.warn(ctx.getText() + "in key list:" + context + "can't convert to string");
                        }
                    }

                    case DELETE -> {
                        rewriter.delete(ctx.key().getStart(), ctx.value().getStop());
                    }
                }
                context.removeLast();
            }

            @Override
            public void enterStandardTable(CJPMTomlParser.StandardTableContext ctx) {
                String tableText = ctx.getText();
                for (DiffEntry diff : diffs) {
                    if (diff.type != DiffEntry.Type.ADD || diff.consumed) {
                        continue;
                    }
                    if (diff.path == null || diff.path.size() < 2) {
                        continue;
                    }
                    String expectedTable = diff.toTableHeader();
                    if (expectedTable.equals(tableText)) {
                        int baseIndentSize = ctx.getStart().getCharPositionInLine();
                        String childIndent = " ".repeat(baseIndentSize) + "  ";
                        String injection =
                            "\n%s%s = %s".formatted(childIndent, diff.keyName(), objectToString(diff.newValue));
                        rewriter.insertAfter(ctx.getStop(), injection);
                        diff.consumed = true;
                    }
                }
            }
        }, parseTree);
        return rewriteDiff(tokens, rewriter);
    }

    private String rewriteDiff(CommonTokenStream tokens, TokenStreamRewriter rewriter) {
        Map<String, List<DiffEntry>> unconsumedByTable =
            diffs.stream().filter(diffEntry -> diffEntry.type == DiffEntry.Type.ADD && !diffEntry.consumed)
                .filter(diffEntry -> diffEntry.path != null && diffEntry.path.size() >= 2)
                .collect(Collectors.groupingBy(DiffEntry::toTableHeader, LinkedHashMap::new, Collectors.toList()));
        if (!unconsumedByTable.isEmpty()) {
            StringBuilder appendix = new StringBuilder();
            String newTableChildIndent = "  ";
            for (Map.Entry<String, List<DiffEntry>> entry : unconsumedByTable.entrySet()) {
                String tableHeader = entry.getKey();
                List<DiffEntry> tableDiffs = entry.getValue();
                appendix.append("\n\n").append(tableHeader);
                for (DiffEntry diff : tableDiffs) {
                    appendix.append("\n").append(newTableChildIndent).append(diff.keyName()).append(" = ")
                        .append(objectToString(diff.newValue));
                    diff.consumed = true;
                }
            }
            int lastTokenIndex = tokens.size() - 1;
            Token lastRealToken = tokens.get(lastTokenIndex);
            while (lastRealToken.getType() == Token.EOF && lastTokenIndex > 0) {
                lastTokenIndex--;
                lastRealToken = tokens.get(lastTokenIndex);
            }
            rewriter.insertAfter(lastRealToken, appendix.toString());
        }

        for (DiffEntry diff : diffs) {
            if (diff.type == DiffEntry.Type.ADD && !diff.consumed && diff.path != null && diff.path.size() == 1) {
                rewriter.insertBefore(tokens.get(0), diff.keyName() + " = " + objectToString(diff.newValue) + "\n");
                diff.consumed = true;
            }
        }
        return rewriter.getText();
    }

    private boolean isPrimary(Object o) {
        return o instanceof Boolean || o instanceof Number || o instanceof String || isArrayOfPrimitive(o);
    }

    private boolean isArrayOfPrimitive(Object o) {
        if (o instanceof ArrayList<?> array) {
            if (!array.isEmpty()) {
                Object first = array.getFirst();
                return isPrimary(first) || isArrayOfPrimitive(first);
            }
            return true;
        }
        return false;
    }

    private DiffEntry lookupDiffFor(List<String> path) {
        DiffEntry result = null;
        for (DiffEntry diff : diffs) {
            if (Objects.equals(diff.path, path)) {
                result = diff;
                break;
            }
        }
        return result;
    }

    private String escapeString(String str) {
        String result = str;
        for (Map.Entry<String, String> entry : ESCAPE_CHARACTERS.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String objectToString(Object o) throws UnsupportedOperationException {
        if (o instanceof Boolean v) {
            return v ? "true" : "false";
        } else if (o instanceof Number v) {
            return v.toString();
        } else if (o instanceof String v) {
            return "\"" + escapeString(v) + "\"";
        } else if (isArrayOfPrimitive(o)) {
            List<?> list = (List<Object>) o;
            return "[" + list.stream().map(this::objectToString)
                    .collect(Collectors.joining(", ")) + "]";
        } else if (o instanceof Map<?, ?> v) {
            return "{" + v.entrySet().stream().map(e -> e.getKey() + " = " + objectToString(e.getValue()))
                    .collect(Collectors.joining(", ")) + "}";
        } else {
            throw new UnsupportedOperationException("unsupported object that cannot convert ot string");
        }
    }

    private List<String> getKeys(Object o) {
        switch (o) {
            case CJPMTomlParser.StandardTableContext ctx -> {
                if (ctx.key() == null || ctx.key().isEmpty()) {
                    return List.of();
                }
                if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
                    return List.of(ctx.key().getText());
                }
                if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
                    var keys = ctx.key().dottedKey().getText().split("\\.");
                    return List.of(keys);
                }
                return List.of();
            }
            case CJPMTomlParser.ArrayTableContext ctx -> {
                if (ctx.key() == null || ctx.key().isEmpty()) {
                    return List.of();
                }
                if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
                    return List.of(ctx.key().getText());
                }
                if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
                    var keys = ctx.key().dottedKey().getText().split("\\.");
                    return List.of(keys);
                }
                return List.of();
            }
            case CJPMTomlParser.KeyValueContext ctx -> {
                if (ctx.key() == null || ctx.key().isEmpty()) {
                    return List.of();
                }
                if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
                    return List.of(ctx.key().getText());
                }
                if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
                    var keys = ctx.key().dottedKey().getText().split("\\.");
                    return List.of(keys);
                }
                return List.of();
            }
            case null, default -> {
                return List.of();
            }
        }
    }
}
