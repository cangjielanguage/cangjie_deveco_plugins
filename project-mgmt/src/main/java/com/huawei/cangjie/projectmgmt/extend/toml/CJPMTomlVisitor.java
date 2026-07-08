/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.extend.toml;

import com.intellij.openapi.diagnostic.Logger;

import groovy.json.StringEscapeUtils;
import toml.parser.CJPMTomlParser;
import toml.parser.CJPMTomlParserBaseVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * CJPMTomlVisitor
 *
 * @since 2025/11/20
 */
public class CJPMTomlVisitor extends CJPMTomlParserBaseVisitor<HashMap<String, Object>> {
    private static final Logger LOG = Logger.getInstance(CJPMTomlVisitor.class);

    private int errorCnt;

    private HashMap<String, Object> root;

    private HashMap<String, Object> currentTable;

    private ArrayList<Object> currentList;

    private VisitMode visitMode = VisitMode.NORMAL;

    public CJPMTomlVisitor() {
        root = new HashMap<>();
        currentTable = root;
        errorCnt = 0;
    }

    /**
     * VisitMode
     */
    public enum VisitMode {
        NORMAL,
        INLINE_TABLE,
        ARRAY
    }

    public int getErrorCnt() {
        return errorCnt;
    }

    @Override
    public HashMap<String, Object> visitCjpmtoml(CJPMTomlParser.CjpmtomlContext ctx) {
        visitChildren(ctx);
        return root;
    }

    @Override
    public HashMap<String, Object> visitKeyValue(CJPMTomlParser.KeyValueContext ctx) {
        if (ctx.value().inlineTable() != null && !ctx.value().inlineTable().isEmpty()) {
            if (ctx.key() == null || ctx.key().isEmpty()) {
                return null;
            }
            var prevTable = currentTable;
            if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
                var tmpTable = new HashMap<String, Object>();
                currentTable.put(removeQuotes(ctx.key().getText()), tmpTable);
                currentTable = tmpTable;
            }
            if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
                var keys = Arrays.stream(ctx.key().dottedKey().getText().split("\\."))
                        .map(CJPMTomlVisitor::removeQuotes).toArray(String[]::new);
                currentTable = root;
                buildTable(keys, 0);
            }
            visitChildren(ctx);
            currentTable = prevTable;
            return currentTable;
        }
        visitCommonData(ctx);
        return currentTable;
    }

    private void visitCommonData(CJPMTomlParser.KeyValueContext ctx) {
        if (ctx.value().stringVal() != null && !ctx.value().stringVal().isEmpty()) {
            String content = StringEscapeUtils.unescapeJava(ctx.value().stringVal().getText());
            setKeyValue(ctx, content.substring(1, content.length() - 1));
            return;
        }
        if (ctx.value().boolVal() != null && !ctx.value().boolVal().isEmpty()) {
            setKeyValue(ctx, ctx.value().boolVal().getText().equals("true"));
            return;
        }
        if (ctx.value().integerVal() != null && !ctx.value().integerVal().isEmpty()) {
            setKeyValue(ctx, Long.valueOf(ctx.value().integerVal().getText()));
            return;
        }
        if (ctx.value().floatVal() != null && !ctx.value().floatVal().isEmpty()) {
            setKeyValue(ctx, Double.valueOf(ctx.value().floatVal().getText()));
            return;
        }
        if (ctx.value().arrayVal() != null && !ctx.value().arrayVal().isEmpty()) {
            var arrayValues = ctx.value().arrayVal().arrayValues();
            if (arrayValues != null && !arrayValues.isEmpty()) {
                visitMode = VisitMode.ARRAY;
                currentList = new ArrayList<>();
                visitChildren(ctx);
                setKeyValue(ctx, currentList);
                visitMode = VisitMode.NORMAL;
            }
        }
    }

    @Override
    public HashMap<String, Object> visitValue(CJPMTomlParser.ValueContext ctx) {
        if (visitMode == VisitMode.ARRAY && ctx.stringVal() != null && !ctx.stringVal().isEmpty()) {
            String content = StringEscapeUtils.unescapeJava(ctx.stringVal().getText());
            currentList.add(content.substring(1, content.length() - 1));
            return currentTable;
        }
        return visitChildren(ctx);
    }

    @Override
    public HashMap<String, Object> visitStandardTable(CJPMTomlParser.StandardTableContext ctx) {
        if (ctx.key() == null || ctx.key().isEmpty()) {
            return null;
        }
        if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
            var tmpTable = new HashMap<String, Object>();
            root.put(removeQuotes(ctx.key().getText()), tmpTable);
            currentTable = tmpTable;
            return currentTable;
        }
        if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
            var keys = Arrays.stream(ctx.key().dottedKey().getText().split("\\."))
                    .map(CJPMTomlVisitor::removeQuotes).toArray(String[]::new);
            currentTable = root;
            buildTable(keys, 0);
        }
        return currentTable;
    }

    @Override
    public HashMap<String, Object> visitArrayTable(CJPMTomlParser.ArrayTableContext ctx) {
        if (ctx.key() == null || ctx.key().isEmpty()) {
            return null;
        }
        if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
            var keyName = removeQuotes(ctx.key().simpleKey().getText());
            if (currentTable.get(keyName) == null) {
                var tmpTable = new HashMap<String, Object>();
                ArrayList<HashMap<String, Object>> tempList = new ArrayList<>();
                tempList.add(tmpTable);
                root.put(removeQuotes(ctx.key().getText()), tempList);
                currentTable = tmpTable;
            } else {
                if (currentTable.get(keyName) instanceof ArrayList) {
                    var tmpTable = new HashMap<String, Object>();
                    var tempList = (ArrayList<HashMap<String, Object>>) currentTable.get(keyName);
                    if (tempList == null) {
                        tempList = new ArrayList<>();
                    }
                    tempList.add(tmpTable);
                    root.put(removeQuotes(ctx.key().getText()), tempList);
                    currentTable = tmpTable;
                } else {
                    errorCnt++;
                }
            }
        }
        if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
            var keys = Arrays.stream(ctx.key().dottedKey().getText().split("\\."))
                    .map(CJPMTomlVisitor::removeQuotes).toArray(String[]::new);
            currentTable = root;
            buildArrayTable(keys, 0);
        }
        return currentTable;
    }

    private void buildTable(String[] keys, int index) {
        if (index >= keys.length) {
            return;
        }
        if (currentTable.get(keys[index]) == null) {
            var tmpTable = new HashMap<String, Object>();
            currentTable.put(keys[index], tmpTable);
            currentTable = tmpTable;
        } else if (currentTable.get(keys[index]) instanceof HashMap) {
            currentTable = (HashMap<String, Object>) currentTable.get(keys[index]);
        } else {
            LOG.info("current key is not table.");
        }
        buildTable(keys, index + 1);
    }

    private void buildArrayTable(String[] keys, int index) {
        if (index >= keys.length) {
            return;
        }
        if (index + 1 == keys.length) {
            var keyName = keys[index];
            if (currentTable.get(keyName) == null) {
                var tmpTable = new HashMap<String, Object>();
                ArrayList<HashMap<String, Object>> tempList = new ArrayList<>();
                tempList.add(tmpTable);
                currentTable.put(keys[index], tempList);
                currentTable = tmpTable;
            } else {
                if (currentTable.get(keyName) instanceof ArrayList) {
                    var tmpTable = new HashMap<String, Object>();
                    var tempList = (ArrayList<HashMap<String, Object>>) currentTable.get(keyName);
                    if (tempList == null) {
                        tempList = new ArrayList<>();
                    }
                    tempList.add(tmpTable);
                    currentTable.put(keys[index], tempList);
                    currentTable = tmpTable;
                } else {
                    errorCnt++;
                }
            }
            return;
        }
        if (currentTable.get(keys[index]) == null) {
            var tmpTable = new HashMap<String, Object>();
            currentTable.put(keys[index], tmpTable);
            currentTable = tmpTable;
        } else if (currentTable.get(keys[index]) instanceof HashMap) {
            currentTable = (HashMap<String, Object>) currentTable.get(keys[index]);
        } else {
            LOG.info("current key is not table.");
        }
        buildTable(keys, index + 1);
    }

    private void setKeyValue(CJPMTomlParser.KeyValueContext ctx, Object value) {
        HashMap<String, Object> temp = currentTable;
        if (ctx.key().simpleKey() != null && !ctx.key().simpleKey().isEmpty()) {
            currentTable.put(removeQuotes(ctx.key().getText()), value);
        }
        if (ctx.key().dottedKey() != null && !ctx.key().dottedKey().isEmpty()) {
            var keys = Arrays.stream(ctx.key().dottedKey().getText().split("\\."))
                    .map(CJPMTomlVisitor::removeQuotes).toArray(String[]::new);
            buildTableAndPutValue(keys, 0, value);
        }
        currentTable = temp;
    }

    private void buildTableAndPutValue(String[] keys, int index, Object value) {
        if (index >= keys.length) {
            return;
        }

        if (index == keys.length - 1) {
            currentTable.put(keys[index], value);
            return;
        }

        if (currentTable.get(keys[index]) == null) {
            var tmpTable = new HashMap<String, Object>();
            currentTable.put(keys[index], tmpTable);
            currentTable = tmpTable;
        } else if (currentTable.get(keys[index]) instanceof HashMap) {
            currentTable = (HashMap<String, Object>) currentTable.get(keys[index]);
        } else {
            LOG.info("current key is not table.");
        }
        buildTableAndPutValue(keys, index + 1, value);
    }

    private static String removeQuotes(String key) {
        if (key != null && key.length() >= 2
                && ((key.startsWith("\"") && key.endsWith("\""))
                || (key.startsWith("'") && key.endsWith("'")))) {
            return key.substring(1, key.length() - 1);
        }
        return key;
    }
}
