/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.result;

/**
 * CangjieTestOutputXmlBean
 *
 * @since 2025/02/20
 */
public class CangjieTestOutputXmlBean {
    private String className;
    private String tests;
    private String failures;
    private String errors;
    private String skipped;
    private String time;
    private String timestamp;

    // for test case
    private String caseName;
    private String assertions;

    // for failure
    private final StringBuilder characters = new StringBuilder();

    public CangjieTestOutputXmlBean() {}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }

    public String getFailures() {
        return failures;
    }

    public void setFailures(String failures) {
        this.failures = failures;
    }

    public String getSkipped() {
        return skipped;
    }

    public void setSkipped(String skipped) {
        this.skipped = skipped;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getAssertions() {
        return assertions;
    }

    public void setAssertions(String assertions) {
        this.assertions = assertions;
    }

    public StringBuilder getCharacters() {
        return characters;
    }

    public void setCharacters(String str) {
        this.characters.append(str);
    }
}
