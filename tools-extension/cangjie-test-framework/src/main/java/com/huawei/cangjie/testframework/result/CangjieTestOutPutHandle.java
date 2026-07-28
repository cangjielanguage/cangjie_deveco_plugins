/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.result;

import static com.huawei.cangjie.testframework.utils.Constant.XML_TEST_ERROR_MESSAGE;

import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestIdentifier;
import com.huawei.deveco.ohos.testframework.run.testrunner.OhosTestResult;
import com.huawei.deveco.ohos.testframework.run.testrunner.TestRunnerListener;

import com.intellij.openapi.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

/**
 * CangjieTestOutPutHandle
 *
 * @since 2025/02/20
 */
public class CangjieTestOutPutHandle extends DefaultHandler {
    private final Stack<Pair<String, CangjieTestOutputXmlBean>> stack = new Stack<>();
    private Collection<TestRunnerListener> testRunnerListeners;

    public CangjieTestOutPutHandle() {}

    public CangjieTestOutPutHandle(Collection<TestRunnerListener> testRunnerListeners) {
        this.testRunnerListeners = testRunnerListeners;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Pair<String, CangjieTestOutputXmlBean> parentElement = null;
        if (!stack.isEmpty()) {
            parentElement = stack.peek();
        }
        switch (qName) {
            case Constant.XML_TEST_SUITE: {
                String nameEle = attributes.getValue(Constant.XML_NAME);
                int index = nameEle.lastIndexOf(".");
                String testClassName = index == -1 ? nameEle : nameEle.substring(index + 1);
                String testsCount = attributes.getValue(Constant.XML_TESTS_COUNT);
                String testFailuresCount = attributes.getValue(Constant.XML_FAILURES_COUNT);
                String testErrorCount = attributes.getValue(Constant.XML_ERRORS_COUNT);
                String testSippedCount = attributes.getValue(Constant.XML_SKIPPED_COUNT);
                String testCostTime = attributes.getValue(Constant.XML_COST_TIME);
                String testTimeStamp = attributes.getValue(Constant.XML_TIME_STAMP);

                CangjieTestOutputXmlBean xmlBean = new CangjieTestOutputXmlBean();
                xmlBean.setClassName(testClassName);
                xmlBean.setTests(testsCount);
                xmlBean.setFailures(testFailuresCount);
                xmlBean.setErrors(testErrorCount);
                xmlBean.setSkipped(testSippedCount);
                xmlBean.setTime(testCostTime);
                xmlBean.setTimestamp(testTimeStamp);
                stack.push(new Pair<>(qName, xmlBean));
                break;
            }
            case Constant.XML_TEST_CASE: {
                String testName = attributes.getValue(Constant.XML_NAME);
                String classNameEle = attributes.getValue(Constant.XML_CLASS_NAME);
                int index = classNameEle.lastIndexOf(".");
                String testClassName = index == -1 ? classNameEle : classNameEle.substring(index + 1);
                String testCostTime = attributes.getValue(Constant.XML_COST_TIME);
                OhosTestIdentifier identifier = new OhosTestIdentifier(
                        testClassName, testName, (long) Double.parseDouble(testCostTime));
                for (TestRunnerListener listener : testRunnerListeners) {
                    listener.testStarted(identifier);
                }
                String assertionsCount = attributes.getValue(Constant.XML_ASSERTIONS_COUNT);

                CangjieTestOutputXmlBean xmlBean = new CangjieTestOutputXmlBean();
                xmlBean.setClassName(testClassName);
                xmlBean.setCaseName(testName);
                xmlBean.setAssertions(assertionsCount);
                xmlBean.setTime(testCostTime);
                stack.push(new Pair<>(qName, xmlBean));
                break;
            }
            case Constant.XML_TEST_SKIPPED:
            case Constant.XML_TEST_FAILURE: {
                CangjieTestOutputXmlBean xmlBean = new CangjieTestOutputXmlBean();
                if (parentElement != null) {
                    xmlBean.setClassName(parentElement.second.getClassName());
                    xmlBean.setCaseName(parentElement.second.getCaseName());
                }
                stack.push(new Pair<>(qName, xmlBean));
                break;
            }
            case Constant.XML_TEST_ERROR: {
                String errorMsg = attributes.getValue(XML_TEST_ERROR_MESSAGE);
                CangjieTestOutputXmlBean xmlBean = new CangjieTestOutputXmlBean();
                xmlBean.getCharacters().append(errorMsg);
                if (parentElement != null) {
                    xmlBean.setClassName(parentElement.second.getClassName());
                    xmlBean.setCaseName(parentElement.second.getCaseName());
                }
                stack.push(new Pair<>(qName, xmlBean));
                break;
            }
            default:
                stack.push(new Pair<>(qName, new CangjieTestOutputXmlBean()));
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (stack.isEmpty()) {
            return;
        }
        CangjieTestOutputXmlBean bean = stack.peek().getSecond();
        bean.setCharacters(String.valueOf(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (stack.isEmpty()) {
            return;
        }
        Pair<String, CangjieTestOutputXmlBean> curElement = stack.pop();
        String key = curElement.first;
        if (!key.equals(qName)) {
            return;
        }
        CangjieTestOutputXmlBean curAttributes = curElement.second;
        // end parse suit
        if (qName.equals(Constant.XML_TEST_SUITE)) {
            OhosTestResult result = new OhosTestResult();
            String testClassName = curAttributes.getClassName();
            result.setSuiteName(testClassName);
            String testsCount = curAttributes.getTests();
            result.setTestCount(Integer.parseInt(testsCount));
            long testCostTime = Math.round(Double.parseDouble(curAttributes.getTime()) * 1000);
            result.setTestDuration(testCostTime);
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.suiteEnded(result);
            }
        } else if (qName.equals(Constant.XML_TEST_CASE)) {
            String testName = curAttributes.getCaseName();
            String testClassName = curAttributes.getClassName();
            long testCostTime = Math.round(Double.parseDouble(curAttributes.getTime()) * 1000);
            OhosTestIdentifier identifier = new OhosTestIdentifier(
                    testClassName, testName, testCostTime);
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.testEnded(identifier, testCostTime, new HashMap<>());
            }
        } else if (qName.equals(Constant.XML_TEST_SKIPPED)) {
            String testName = curAttributes.getCaseName();
            String testClassName = curAttributes.getClassName();
            OhosTestIdentifier identifier = new OhosTestIdentifier(testClassName, testName, 0);
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.testIgnored(identifier);
            }
        } else if ((qName.equals(Constant.XML_TEST_FAILURE) || qName.equals(Constant.XML_TEST_ERROR))
                && !curAttributes.getCharacters().isEmpty()) {
            String testName = curAttributes.getCaseName();
            String testClassName = curAttributes.getClassName();
            OhosTestIdentifier identifier = new OhosTestIdentifier(testClassName, testName, 0);
            for (TestRunnerListener listener : testRunnerListeners) {
                listener.testFailed(identifier, curAttributes.getCharacters().toString());
            }
        } else {
            return;
        }
    }
}
