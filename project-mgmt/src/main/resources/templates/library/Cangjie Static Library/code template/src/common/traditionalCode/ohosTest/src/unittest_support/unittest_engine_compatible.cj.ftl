<#--
  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

  This source file is part of the Cangjie project, licensed under Apache-2.0
  with Runtime Library Exception.

  See https://cangjie-lang.cn/pages/LICENSE for license information.
-->

package ${cjPackageName}_test.unittest_support

import ohos.test_runner.TestRunner
import ohos.ability_delegator_registry.AbilityDelegator
import ohos.ability_delegator_registry.AbilityDelegatorRegistry
import std.unittest.XmlReporter
import std.fs.Path
import std.fs.Directory
import std.collection.filter
import std.fs.FileInfo
import std.collection.first
import std.fs.File
import std.collection.forEach
import std.convert.Parsable
import ohos.bundle_manager.BundleManager
import ohos.bundle_manager.BundleFlag
import std.collection.any
import std.collection.HashMap
import std.unittest.TestClass
import std.collection.ArrayList
import std.unittest.TestSuite
import ohos.ability.Want
import std.unittest.common.Configuration
import kit.PerformanceAnalysisKit.Hilog
import std.env.setVariable

@When[coverage == "true"]
foreign func __gcov_dump(): Unit

@When[coverage == "true"]
public func gcov_dump(coveragePath: String): Unit {
    Hilog.info(0, "Cangjie-Test", "execute test with coverage")
    unsafe { __gcov_dump() }
    let abilityDelegator = AbilityDelegatorRegistry.getAbilityDelegator()
    abilityDelegator.print("GENERATE_COVERAGE: <#noparse>${coveragePath}</#noparse>")
}

@When[coverage == "false"]
public func gcov_dump(coveragePath: String): Unit {
    Hilog.info(0, "Cangjie-Test", "execute test without coverage")
}

public func registerTestSuite(testClassInstanceGenerator: () -> TestClass): Unit {
    OpenHarmonyTestRunner
        .testClassInstanceGenerators
        .add(testClassInstanceGenerator)
}

public class OpenHarmonyTestRunner <: TestRunner {
    public static let testSuiteMap = HashMap<String, TestSuite>()
    public static let testClassInstanceGenerators = ArrayList<() -> TestClass>()
    private static func getDetailedInfoOf(e: Exception): String {
        let sb = StringBuilder("exception: <#noparse>${e.toString()}</#noparse>\n")
        e.getStackTrace() |>
            forEach {
            s => sb.append("         at <#noparse>${s.declaringClass}</#noparse>.<#noparse>${s.methodName}</#noparse>(<#noparse>${s.fileName}</#noparse>:<#noparse>${s.lineNumber}</#noparse>)\n")
        }
        sb.toString()
    }

    private static func joinStrings(list: ArrayList<String>, separator: String): String {
        if (list.isEmpty()) {
            return ""
        }
        let sb = StringBuilder()
        for (i in 0..list.size) {
            if (i > 0) {
                sb.append(separator)
            }
            sb.append(list[i])
        }
        sb.toString()
    }

    private func executeTestClass(testClassName: String): Unit {
        let abilityDelegator = AbilityDelegatorRegistry.getAbilityDelegator()
        Hilog.info(0, "Cangjie-Test", "EXECUTE_CLASS_START: \"<#noparse>${testClassName}</#noparse>\"")
        abilityDelegator.print("EXECUTE_CLASS_START: \"<#noparse>${testClassName}</#noparse>\"")
        let testSuite = match (OpenHarmonyTestRunner
            .testSuiteMap
            .get(testClassName)) {
            case None => return
            case Some(testClassInstanceGenerator) => testClassInstanceGenerator
        }

        testSuite
            .runTests()
            .reportTo(XmlReporter(Path("/data/storage/el2/base")))

        let testReportXmlFileName = "test-default.<#noparse>${testClassName}</#noparse>.xml"
        match (Directory.readFrom("/data/storage/el2/base/tests") |>
            filter {fileInfo => fileInfo.name == testReportXmlFileName} |> first) {
            case None => Hilog.error(1, "Cangjie-Test", "<#noparse>${testReportXmlFileName}</#noparse> not found.")
            case Some(testReportXmlFileInfo) =>
                let testReportXmlFilePath = testReportXmlFileInfo.path
                let testReportXmlFileContent = File.readFrom(testReportXmlFilePath)
                let xmlReport = String.fromUtf8(testReportXmlFileContent)
                reportTestReport("test-default.<#noparse>${testClassName}</#noparse>.xml")
                Hilog.info(0, "Cangjie-Test", "XML_REPORT_CONTENT_LENGTH: <#noparse>${xmlReport.size}</#noparse>")
                abilityDelegator.print("XML_REPORT_CONTENT_LENGTH: <#noparse>${xmlReport.size}</#noparse>")
                splitPrint(xmlReport, abilityDelegator)
                Hilog.info(0, "Cangjie-Test", "EXECUTE_CLASS_END: \"<#noparse>${testClassName}</#noparse>\"")
                abilityDelegator.print("EXECUTE_CLASS_END: \"<#noparse>${testClassName}</#noparse>\"")
        }
    }
    private func executeAllTestClasses(): Unit {
        for (testClassName in OpenHarmonyTestRunner
                .testSuiteMap
                .keys()) {
            executeTestClass(testClassName)
        }
    }
    private func executeTestCase(testClassName: String, testCaseName: String): Unit {
        let abilityDelegator = AbilityDelegatorRegistry.getAbilityDelegator()
        abilityDelegator.print("EXECUTE_CLASS_START: \"<#noparse>${testClassName}</#noparse>\"")
        Hilog.info(0, "Cangjie-Test", "EXECUTE_METHOD_START: \"<#noparse>${testClassName}</#noparse>#<#noparse>${testCaseName}</#noparse>\"")
        let testSuite = match (OpenHarmonyTestRunner
            .testSuiteMap
            .get(testClassName)) {
            case None => return
            case Some(testClassInstanceGenerator) => testClassInstanceGenerator
        }
        let configuration = Configuration()
        configuration.setByName("filter", "<#noparse>${testClassName}</#noparse>.<#noparse>${testCaseName}</#noparse>")
        testSuite
            .runTests(configuration)
            .reportTo(XmlReporter(Path("/data/storage/el2/base")))

        let testReportXmlFileName = "test-default.<#noparse>${testClassName}</#noparse>.xml"
        match (Directory.readFrom("/data/storage/el2/base/tests") |>
            filter {fileInfo => fileInfo.name == testReportXmlFileName} |> first) {
            case None => Hilog.error(1, "Cangjie-Test", "<#noparse>${testReportXmlFileName}</#noparse> not found.")
            case Some(testReportXmlFileInfo) =>
                let testReportXmlFilePath = testReportXmlFileInfo.path
                let testReportXmlFileContent = File.readFrom(testReportXmlFilePath)
                let xmlReport = String.fromUtf8(testReportXmlFileContent)
                reportTestReport("test-default.<#noparse>${testClassName}</#noparse>.xml")
                Hilog.info(0, "Cangjie-Test", "XML_REPORT_CONTENT_LENGTH: <#noparse>${xmlReport.size}</#noparse>")
                abilityDelegator.print("XML_REPORT_CONTENT_LENGTH: <#noparse>${xmlReport.size}</#noparse>")
                splitPrint(xmlReport, abilityDelegator)
                Hilog.info(0, "Cangjie-Test", "EXECUTE_METHOD_END: \"<#noparse>${testClassName}</#noparse>#<#noparse>${testCaseName}</#noparse>\"")
                abilityDelegator.print("EXECUTE_CLASS_END: \"<#noparse>${testClassName}</#noparse>\"")
        }
    }
    private func splitPrint(xmlReport: String, abilityDelegator: AbilityDelegator): Unit {
        let lines = xmlReport.split("\n", removeEmpty: true)
        let tempArray = ArrayList<String>()
        for (line in lines) {
            let trimmedLine = line.trimAscii()
            if (trimmedLine.isEmpty()) {
                continue
            }
            tempArray.add(line)
            if (tempArray.size == 10) {
                let output = OpenHarmonyTestRunner.joinStrings(tempArray, "\n")
                Hilog.error(0, "Cangjie-Test", output)
                abilityDelegator.print(output)
                tempArray.clear()
            }
        }
        if (tempArray.size > 0) {
            let output = OpenHarmonyTestRunner.joinStrings(tempArray, "\n")
            Hilog.error(0, "Cangjie-Test", output)
            abilityDelegator.print(output)
        }
    }
    private func reportTestReport(testReportName: String): Unit {
        let abilityDelegator = AbilityDelegatorRegistry.getAbilityDelegator()
        let abilityDelegatorArguments = AbilityDelegatorRegistry.getArguments()
        let bundleName = abilityDelegatorArguments.bundleName
        abilityDelegator.print("REPORT_TEST_RESULT: /data/app/el2/100/base/<#noparse>${bundleName}</#noparse>/tests/<#noparse>${testReportName}</#noparse>")
    }
    public override func onPrepare(): Unit {
        Hilog.info(0, "Cangjie-Test", "OpenHarmonyTestRunner onPrepare")
        for (testClassInstanceGenerator in OpenHarmonyTestRunner.testClassInstanceGenerators) {
            let testClassInstance = try {
                testClassInstanceGenerator()
            } catch (e: Exception) {
                Hilog.error(1, "Cangjie-Test", "exception occurred during testsuite generation:\n<#noparse>${getDetailedInfoOf(e)}</#noparse>")
                continue
            }
            let testSuite = testClassInstance.asTestSuite()
            let testSuiteName = testSuite.name
            Hilog.info(0, "Cangjie-Test", "Registering TestSuite <#noparse>${testSuiteName}</#noparse>")
            OpenHarmonyTestRunner
                .testSuiteMap
                .add(testSuiteName, testSuite)
        }
    }
    public override func onRun(): Unit {
        Hilog.info(0, "Cangjie-Test", "OpenHarmonyTestRunner onRun")
        let abilityDelegatorArguments = AbilityDelegatorRegistry.getArguments()
        let abilityDelegator = AbilityDelegatorRegistry.getAbilityDelegator()
        Hilog.info(0, "Cangjie-Test", "bundleName = <#noparse>${abilityDelegatorArguments.bundleName}</#noparse>")
        Hilog.info(0, "Cangjie-Test", "abilityDelegatorArguments.testRunnerClassName = <#noparse>${abilityDelegatorArguments.testRunnerClassName}</#noparse>")
        Hilog.info(0, "Cangjie-Test", "abilityDelegatorArguments.testCaseNames = <#noparse>${abilityDelegatorArguments.testCaseNames}</#noparse>")
        Hilog.info(0, "Cangjie-Test", "abilityDelegatorArguments.parameters = <#noparse>${abilityDelegatorArguments.parameters}</#noparse>")
        Hilog.info(0, "Cangjie-Test", "Started running tests.")
        abilityDelegator.print("OpenHarmonyTestRunner onRun")
        let parameters = abilityDelegatorArguments.parameters
        let bundleName = abilityDelegatorArguments.bundleName
        let bundleInfo = BundleManager.getBundleInfoForSelf(
            BundleFlag
                .GET_BUNDLE_INFO_WITH_HAP_MODULE
                .getValue() | BundleFlag
                .GET_BUNDLE_INFO_WITH_APPLICATION
                .getValue())
        let moduleName = match (parameters.get("-m")) {
            case None =>
                abilityDelegator.finishTest("Module name was not specified.", -1)
                return
            case Some(moduleName) =>
                if (bundleInfo.hapModulesInfo |> any {hapModuleInfo => hapModuleInfo.name == moduleName}) {
                    moduleName
                } else {
                    abilityDelegator.finishTest(
                        "No module with name \"<#noparse>${moduleName}</#noparse>\" was found in bundle <#noparse>${bundleName}</#noparse>.", -1)
                    return
                }
        }
        let abilityName = match (bundleInfo.hapModulesInfo |> filter {hapModuleInfo => hapModuleInfo.name == moduleName} |>
            first) {
            case None =>
                abilityDelegator.finishTest("No module with name \"<#noparse>${moduleName}</#noparse>\" was found in bundle <#noparse>${bundleName}</#noparse>.",
                    -1)
                return
            case Some(hapModuleInfo) =>
                let mainElementName = hapModuleInfo.mainElementName
                Hilog.info(0, "Cangjie-Test", "mainElementName = <#noparse>${mainElementName}</#noparse>")
                mainElementName
        }
        let want = Want(bundleName: bundleName, moduleName: moduleName, abilityName: abilityName)
        abilityDelegator.startAbility(want)
        let timeOut = match (parameters.get("-s timeout")) {
            case None => None<Int64>
            case Some(timeOutText) => match (Int64.tryParse(timeOutText)) {
                case None =>
                    abilityDelegator.finishTest("The parameter \"timeout\": <#noparse>${timeOutText}</#noparse> is invalid", -1)
                    return
                case Some(timeOutValue) => timeOutValue
            }
        }
        spawn {
            try {
                match (parameters.get("-s class")) {
                    case None => executeAllTestClasses()
                    case Some(text) => text.split(",", removeEmpty: true) |>
                        forEach {
                        t: String =>
                        let pair = t.split("#", removeEmpty: true)
                        let testClassName = match (pair.get(0)) {
                            case None => return
                            case Some(text) => text.trimAscii()
                        }
                        match (pair.get(1)) {
                            case None => executeTestClass(testClassName)
                            case Some(testCaseName) => executeTestCase(testClassName, testCaseName)
                        }
                    }
                }
            } catch (e: Exception) {
                Hilog.error(1, "Cangjie-Test", "an exception occurred:\n<#noparse>${getDetailedInfoOf(e)}</#noparse>")
            }
            var coverage = false
            setVariable("GCOV_PREFIX", "/data/storage/el2/base/cov")
            gcov_dump("/data/app/el2/100/base/<#noparse>${bundleName}</#noparse>/cov/")

            abilityDelegator.finishTest("Test finished.", 0)
        }
    }
}

let _ = TestRunner.registerCreator("OpenHarmonyTestRunner") {OpenHarmonyTestRunner()}
