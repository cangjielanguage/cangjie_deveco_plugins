/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.runner;

import static com.huawei.deveco.ohos.testframework.utils.CommonUtil.isHarModule;

import com.huawei.cangjie.testframework.configuration.CangjieTestRunConfiguration;
import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.cangjie.testframework.utils.TestUtil;
import com.huawei.deveco.debugger.ohos.CommonConstants;
import com.huawei.deveco.debugger.ohos.DebugMessageBundle;
import com.huawei.deveco.debugger.ohos.build.BuildPackageInfo;
import com.huawei.deveco.debugger.ohos.build.HapMetadataModel;
import com.huawei.deveco.debugger.ohos.constants.ErrorCodeConstants;
import com.huawei.deveco.debugger.ohos.deployment.DeviceSelector;
import com.huawei.deveco.debugger.ohos.run.app.info.LaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.app.info.OpenHarmonyLaunchAppInfo;
import com.huawei.deveco.debugger.ohos.run.tasks.LaunchTask;
import com.huawei.deveco.debugger.ohos.util.ErrorCodeUtils;
import com.huawei.deveco.debugger.ohos.util.HapMetadataParse;
import com.huawei.deveco.debugger.ohos.util.PathUtil;
import com.huawei.deveco.debugger.ohos.util.ProjectUtil;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.errormsg.MessageModel;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.utils.CommonUtil;
import com.huawei.deveco.ohos.testframework.utils.CreateTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OhosTestUtils;
import com.huawei.deveco.ohos.testframework.utils.OhosTraceHelper;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyHintBundle;
import com.huawei.deveco.ohos.testframework.utils.OpenHarmonyLogBundle;
import com.huawei.deveco.ohos.testframework.utils.TestProjectUtils;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProductManager;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModelManager;
import com.huawei.deveco.projectmodel.ohos.model.TargetManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosTarget;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;
import com.huawei.deveco.projectmodel.ohos.v2.impl.HvigorProductV2;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * CangjieTestDeployHapTask
 *
 * @since 2025/02/20
 */
public class CangjieOhosTestDeployHapTask implements LaunchTask {
    private static final String REMOTE_OUTPUT_PATH = String.join(File.separator, "data", "local", "tmp");

    private static final String DEVICE_NOT_MATCH = "The deviceType or apiVersion of the target device "
            + "does not match that configured";

    private static final Logger LOG = Logger.getInstance(CangjieOhosTestDeployHapTask.class);

    private static final int DEPLOY_HAP = 20;

    private static final int TIME_OUT = 1000 * 3 * 60;

    private static final int TIME_OUT_10 = 1000 * 10 * 60;

    private static final String DEPLOYING_HAP = "Deploying Cangjie HAP";

    private static final String INSTALL_FAILED = ": Install Failed: ";

    private static final String UNINSTALL_FAILED = ": Uninstall failed: ";

    private static final String MODULE = "module";

    private static final String NAME = "name";

    private static final long MAX_FILE_COUNT = 1000 * 1000L;

    /**
     * 限制hap包大小为2G
     */
    private static final long MAX_TOTAL_FILE_SIZE = 100 * 1024L * 1024L * 1024L;

    private final Devices device;

    private final OhosModuleModel module;

    private final OpenHarmonyLaunchAppInfo launchAppInfo;

    private final OpenHarmonyConsolePrinter printer;

    private final String debugBuildPath;

    private String testBuildPath;

    private CangjieTestRunConfiguration myConfiguration;

    private OhosTraceHelper mTraceHelper;

    private Project project;

    /**
     * hap包部署任务
     *
     * @param project Project
     * @param configuration RunConfiguration
     * @param device Devices
     * @param appInfo OpenHarmonyLaunchAppInfo
     * @param consolePrinter OpenHarmonyConsolePrinter
     */
    public CangjieOhosTestDeployHapTask(Project project, RunConfiguration configuration, Devices device,
                                        OpenHarmonyLaunchAppInfo appInfo, OpenHarmonyConsolePrinter consolePrinter) {
        this.device = device;
        module = appInfo.getModule();
        launchAppInfo = appInfo;
        printer = consolePrinter;
        debugBuildPath = PathUtil.getBuildHapOutputDir(module);
        String productName = "default";
        HvigorProductV2 product =
                ProductManager.getInstance().getCurrentProduct(CommonProjectUtil.getProjectModel(project));
        if (product != null) {
            productName = product.getName();
        }
        // find target name
        String targetName = "default";
        if (!ModuleType.HAR.toString().equalsIgnoreCase(module.getModuleType())) {
            OhosTarget currentTarget = TargetManager.getInstance().getCurrentTarget(module);
            if (currentTarget != null) {
                targetName = currentTarget.getName();
            }
        }
        testBuildPath = Path.of(module.getModulePath(), Constant.BUILD, productName, Constant.OUTPUTS,
                targetName).toString();
        if (module.isHarLibrary()) {
            testBuildPath = Path.of(module.getModulePath(), Constant.BUILD, productName, Constant.OUTPUTS,
                    Constant.OHOS_TEST).toString();
        }
        if (configuration instanceof CangjieTestRunConfiguration) {
            myConfiguration = (CangjieTestRunConfiguration) configuration;
        }
        this.project = project;
    }

    @Override
    @NotNull
    public String getDescription() {
        return DEPLOYING_HAP;
    }

    @Override
    public int getDuration() {
        return DEPLOY_HAP;
    }

    @Override
    public boolean perform() {
        // 当前测试框架只支持default target
        if (!isDefaultTarget()) {
            printer.stderr(OpenHarmonyHintBundle.message("testFramework.only.supports.default.target"));
            OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.TARGET_NOT_DEFAULT);
            mTraceHelper.traceRun(OhosTraceHelper.TraceAction.RUN_OHOS_TEST.getActionName(),
                    OhosTraceHelper.TraceStage.ERROR.getStageName());
            return false;
        }
        if (!myConfiguration.getKeepDataSelected()) {
            uninstallHap(launchAppInfo, printer);
        }
        String tempDir = ProjectUtil.getRandomUUID();
        String path = String.join(File.separator, REMOTE_OUTPUT_PATH, tempDir);
        String remotePath = FileUtil.toSystemIndependentName(path);
        ProjectModel projectModel = ProjectModelManager.getInstance().getTargetProjectModel(project);
        Map<String, String> hapNamePathMap = null;
        try {
            hapNamePathMap = getHapNamePathMap(projectModel);
        } catch (ExecutionException exception) {
            String errorMessage = exception.getMessage();
            if (errorMessage.contains(DEVICE_NOT_MATCH)) {
                MessageModel messageModel = new MessageModel(ErrorCodeUtils.APP_LAUNCH_TITLE,
                        ErrorCodeConstants.ERROR_00401026, errorMessage);
                CommonUtil.notifyError(messageModel, project);
                printer.stderr(messageModel.toString());
                return false;
            }
            if (CommonConstants.NO_HAP_EXISTS.equals(exception.getMessage())) {
                MessageModel messageModel = new MessageModel(ErrorCodeUtils.APP_LAUNCH_TITLE,
                        ErrorCodeConstants.ERROR_00401022, CommonConstants.NO_HAP_EXISTS);
                CommonUtil.notifyError(messageModel, project);
                printer.stderr(messageModel.toString());
            }
            return false;
        }
        if (hapNamePathMap.isEmpty()) {
            LOG.warn(OpenHarmonyLogBundle.message("hapNamePathMap.is.empty"));
            return false;
        }
        if (!isCreateTemDirSuccess(remotePath)) {
            return false;
        }
        if (!isSuccessPushAllHaps(hapNamePathMap, remotePath)) {
            return false;
        }
        boolean isSuccessInstall = isSuccessInstallAllHaps(remotePath);
        // remove temp dir no matter what the installation result is
        removeTempDir(remotePath);
        // cjpm clean build cache
        if (!myConfiguration.getIsRunWithCoverage()) {
            OhosModuleModel curmodule = myConfiguration.getModule();
            if (curmodule == null) {
                return isSuccessInstall;
            }
            Path testPath = Path.of(curmodule.getModulePath(), Constant.SRC, Constant.OHOS_TEST, Constant.CANGJIE)
                    .toAbsolutePath().normalize();
            TestUtil.executeCjCmd(testPath, "&&" + Constant.CJPM + " " + Constant.CLEAN, curmodule);
            Path buildCoverageOutputPath = Path.of(testPath.toString(), Constant.BUILD_GCNO_OUTPUT_DIR);
            Path testCoverageOutputPath = Path.of(testPath.toString(), Constant.TEST_GCNO_OUTPUT_DIR);
            try {
                if (testCoverageOutputPath.toFile().exists()) {
                    FileUtils.deleteDirectory(testCoverageOutputPath.toFile());
                }
                if (buildCoverageOutputPath.toFile().exists()) {
                    FileUtils.deleteDirectory(buildCoverageOutputPath.toFile());
                }
            } catch (IOException e) {
                LOG.warn("Failed to clean up test coverage output");
            }
        }
        return isSuccessInstall;
    }

    /**
     * 判断是否是default target
     *
     * @return boolean
     */
    private boolean isDefaultTarget() {
        if (isHarModule(module)) {
            return true;
        }
        OhosTarget ohosTarget = TargetManager.getInstance().getCurrentTarget(module);
        if (ohosTarget == null) {
            LOG.warn("get target null");
            return true;
        }
        return Constant.DEFAULT.equals(ohosTarget.getName());
    }

    private void uninstallHap(LaunchAppInfo launchAppInfo, OpenHarmonyConsolePrinter printer) {
        String bundleName = launchAppInfo.getBundleName();
        printer.stdout("$ hdc uninstall " + bundleName);
        String uninstallRes = null;
        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        try {
            uninstallRes = device.getClient().uninstallHap(StringUtils.EMPTY, bundleName, 5000);
        } catch (TimeoutException e) {
            LOG.warn("Uninstall hap timed out");
            printer.stderr(dateFormat.format(new Date()) + UNINSTALL_FAILED + e.getMessage());
        }
        if (uninstallRes == null) {
            return;
        }
        if (!uninstallRes.contains(CommonConstants.UNINSTALL_SUCCESSFULLY_OUTPUT) && !uninstallRes.contains(
                CommonConstants.NO_INSTALL_BUNDLE_OUTPUT)) {
            OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.HAP_UNINSTALL_FAILED);
            mTraceHelper.traceRun(OhosTraceHelper.TraceAction.RUN_OHOS_TEST.getActionName(),
                    OhosTraceHelper.TraceStage.ERROR.getStageName());
            printer.stderr(dateFormat.format(new Date()) + UNINSTALL_FAILED + uninstallRes);
        }
    }

    private Map<String, String> getHapNamePathMap(ProjectModel projectModel) throws ExecutionException {
        List<BuildPackageInfo> allBuildPackageInfoList = getAllBuildPackageInfoList(projectModel);
        Map<String, String> hapNamePathMap = new HashMap<>();
        List<HapMetadataModel.DependRemoteHsp> dependRemoteHspList = new ArrayList<>();
        for (BuildPackageInfo buildPackageInfo : allBuildPackageInfoList) {
            if (StringUtils.isEmpty(buildPackageInfo.getHapName())) {
                continue;
            }
            HapMetadataParse.getDependRemoteHsp(dependRemoteHspList, buildPackageInfo.getDependRemoteHspList());
            hapNamePathMap.put(buildPackageInfo.getHapName(), buildPackageInfo.getHapPath());
        }
        for (HapMetadataModel.DependRemoteHsp dependRemoteHsp : dependRemoteHspList) {
            hapNamePathMap.put(dependRemoteHsp.getHspName(), dependRemoteHsp.getHspPath());
        }
        if (myConfiguration.getOnlyOhosTestPackage()) {
            hapNamePathMap.remove(OhosTestUtils.getHapOrHspName(debugBuildPath));
        }
        return hapNamePathMap;
    }

    private List<BuildPackageInfo> getAllBuildPackageInfoList(ProjectModel projectModel) throws ExecutionException {
        List<OhosModuleModel> moduleModelList = new ArrayList<>();
        if (myConfiguration.isAutoDependencies()) {
            moduleModelList.addAll(ProjectUtil.getSelectMultiModules(projectModel, myConfiguration));
        } else {
            moduleModelList.add(module);
        }
        List<BuildPackageInfo> buildPackageInfoList = getBuildPackageInfoList(moduleModelList);
        List<HapMetadataModel> ohosTestHapMetadataModelList =
            HapMetadataParse.getOhosTestHapMetadataModelList(project, module);
        if (CollectionUtils.isNotEmpty(ohosTestHapMetadataModelList)) {
            buildPackageInfoList.add(getOhosTestBuildPackageInfo(module));
        } else {
            myConfiguration.setOhosTestModuleName(module.getModuleName());
        }
        return buildPackageInfoList;
    }

    private List<BuildPackageInfo> getBuildPackageInfoList(List<OhosModuleModel> moduleModelList)
            throws ExecutionException {
        List<BuildPackageInfo> buildPackageInfoList = new ArrayList<>();
        for (OhosModuleModel moduleModel : moduleModelList) {
            if (isHarModule(moduleModel)) {
                continue;
            }
            BuildPackageInfo buildPackageInfo =
                    TestProjectUtils.getBuildPackageInfo(project, moduleModel, DeviceSelector.getSelectedDeviceItem());
            if (buildPackageInfo == null) {
                continue;
            }
            buildPackageInfoList.add(buildPackageInfo);
        }
        return buildPackageInfoList;
    }

    private BuildPackageInfo getOhosTestBuildPackageInfo(OhosModuleModel module) {
        BuildPackageInfo ohosTestBuildPackageInfo =
            HapMetadataParse.getOhosTestBuildPackageInfo(project, module, DeviceSelector.getSelectedDeviceItem());
        String hapOrHspName;
        if (ohosTestBuildPackageInfo != null) {
            hapOrHspName = ohosTestBuildPackageInfo.getHapName();
        } else {
            hapOrHspName = OhosTestUtils.getHapOrHspName(testBuildPath);
        }
        String ohosTestHapPath = PathUtil.getHapPath(testBuildPath, hapOrHspName);
        String ohosTestModuleName = getOhosTestModuleName(ohosTestHapPath);
        myConfiguration.setOhosTestModuleName(ohosTestModuleName);
        return new BuildPackageInfo(hapOrHspName, ohosTestModuleName, ohosTestHapPath, new ArrayList<>());
    }

    private String getOhosTestModuleName(String hapPath) {
        Optional<JSONObject> configInfoJson = getConfigInfoByHap(hapPath, Constant.MODULE_JSON);
        if (configInfoJson.isEmpty()) {
            return StringUtils.EMPTY;
        }
        JSONObject moduleObj = configInfoJson.get().getJSONObject(MODULE);
        return moduleObj.getString(NAME);
    }

    private Optional<JSONObject> getConfigInfoByHap(String path, String fileName) {
        if (path == null || path.isEmpty()) {
            printer.stderr(Constant.NO_HAP_EXISTS);
            return Optional.empty();
        }
        File file = new File(path);
        if (!file.exists()) {
            printer.stderr(Constant.NO_HAP_EXISTS);
            return Optional.empty();
        }
        Optional<String> configStr = getStringByHap(path, fileName);
        if (configStr.isEmpty()) {
            printer.stderr(
                    String.format(
                            Locale.ENGLISH, OpenHarmonyHintBundle.message("file.in.hap.file.not.exist"), fileName
                    )
            );
            OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.MODULE_JSON_DOES_NOT_EXIST);
            mTraceHelper.traceRun(OhosTraceHelper.TraceAction.RUN_OHOS_TEST.getActionName(),
                    OhosTraceHelper.TraceStage.ERROR.getStageName());
            return Optional.empty();
        }
        JSONObject configJson = null;
        try {
            configJson = JSONObject.parseObject(configStr.get());
        } catch (JSONException e) {
            LOG.warn("parse config.json failed.");
        }
        if (configJson == null) {
            printer.stderr(String.format(Locale.ENGLISH, OpenHarmonyHintBundle.message("file.is.empty"), fileName));
            OhosTraceHelper.setErrorMsg(OhosTraceHelper.TraceConstant.MODULE_JSON_EMPTY);
            mTraceHelper.traceRun(OhosTraceHelper.TraceAction.RUN_OHOS_TEST.getActionName(),
                    OhosTraceHelper.TraceStage.ERROR.getStageName());
            return Optional.empty();
        }
        return Optional.of(configJson);
    }

    private Optional<String> getStringByHap(String path, String fileName) {
        long fileCount = 0L;
        long totalFileSize = 0L;
        ZipInputStream zipInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(path);
            zipInputStream = new ZipInputStream(new FileInputStream(path));
            ZipEntry zipEntry;
            int length;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                fileCount++;
                judgeFileCount(fileCount);
                while ((length = zipInputStream.read(new byte[10240])) != -1) {
                    totalFileSize += length;
                    judgeTotalFileSize(totalFileSize);
                }
                if (fileName.equals(zipEntry.toString())) {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(zipFile.getInputStream(zipEntry), CreateTestUtils.CHARSET));
                    stringBuilder = new StringBuilder();
                    getLineContent(bufferedReader, stringBuilder);
                    break;
                }
            }
        } catch (IOException e) {
            LOG.warn("zip file read failed.");
            return Optional.empty();
        } finally {
            OhosTestUtils.closeStream(zipFile);
            OhosTestUtils.closeStream(bufferedReader);
            OhosTestUtils.closeStream(zipInputStream);
        }
        return stringBuilder == null ? Optional.empty() : Optional.of(stringBuilder.toString());
    }

    private void getLineContent(BufferedReader bufferedReader, StringBuilder stringBuilder) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
    }

    private void judgeFileCount(long fileCount) throws IOException {
        if (fileCount > MAX_FILE_COUNT) {
            throw new IOException(OpenHarmonyHintBundle.message("zip.package.contains.many.files"));
        }
    }

    private void judgeTotalFileSize(long totalFileSize) throws IOException {
        if (totalFileSize > MAX_TOTAL_FILE_SIZE) {
            throw new IOException(OpenHarmonyHintBundle.message("file.size.too.large"));
        }
    }

    private boolean isCreateTemDirSuccess(String remotePath) {
        try {
            String tempDirCreateCmd = String.format(Locale.ENGLISH, "shell mkdir %s", remotePath);
            String result = device.getClient().sendSyncCommand(tempDirCreateCmd, TIME_OUT);
            printer.stdout(String.format("$ hdc %s", tempDirCreateCmd));
            if (StringUtils.isNotEmpty(result)) {
                printer.stderr(String.format(Locale.ENGLISH, "%s", result));
                return false;
            }
            return true;
        } catch (TimeoutException exception) {
            printer.stderr(String.format("Create Directory Timeout: %s", exception.getMessage()));
            return false;
        }
    }

    private boolean isSuccessPushAllHaps(Map<String, String> hapNamePathMap, String remotePath) {
        for (String packagePath : hapNamePathMap.values()) {
            if (!isSuccessPushSingleHap(remotePath, packagePath)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSuccessPushSingleHap(String remotePath, String packagePath) {
        try {
            String sendFileCommand = String.format(Locale.ENGLISH, "file send %s %s", packagePath,
                    CommonConstants.QUOTATION_MARKS + remotePath + CommonConstants.QUOTATION_MARKS);
            String result = device.getClient().sendSyncCommand(sendFileCommand, TIME_OUT_10);
            if (result.startsWith("FileTransfer finish")) {
                printer.stdout(String.format(Locale.ENGLISH, "$ hdc %s", sendFileCommand));
                return true;
            }
            printer.stderr(String.format(Locale.ENGLISH, "FileTransfer Failed: %s", result));
        } catch (TimeoutException exception) {
            printer.stderr(String.format("Push Hap Timeout: %s", exception.getMessage()));
        }
        return false;
    }

    private boolean isSuccessInstallAllHaps(String remotePath) {
        boolean isInstallSuccess;
        String installRes;
        try {
            String commonCommand = String.format(Locale.ENGLISH, "bm install %s %s",
                    CommonConstants.ARGV_P, remotePath);
            installRes = device.getClient().sendSyncShellCommand(commonCommand, TIME_OUT_10);
            printer.stdout(String.format(Locale.ENGLISH, "$ hdc shell %s", commonCommand));
            isInstallSuccess = installRes.contains(CommonConstants.INSTALL_SUCCESSFULLY_OUTPUT);
        } catch (TimeoutException exception) {
            isInstallSuccess = false;
            installRes = "Install Timeout";
            printer.stderr(DebugMessageBundle.message("task.error.install.hap.timeout"));
        }
        if (!isInstallSuccess) {
            printer.stderr(String.format("Install Failed: %s", installRes));
        }
        return isInstallSuccess;
    }

    private void removeTempDir(String remotePath) {
        try {
            String rmCmd = String.format(Locale.ENGLISH, "shell rm -rf %s", remotePath);
            device.getClient().sendSyncCommand(rmCmd, TIME_OUT);
            printer.stdout(String.format("$ hdc %s", rmCmd));
        } catch (TimeoutException exception) {
            printer.stderr("Remove Directory Timeout: " + exception.getMessage());
        }
    }

    private void activeDevice() {

    }

    @Override
    @NotNull
    public String getId() {
        return DEPLOYING_HAP;
    }

    @Override
    public void processError() {
    }

    public void setTraceHelper(OhosTraceHelper mTraceHelper) {
        this.mTraceHelper = mTraceHelper;
    }
}
