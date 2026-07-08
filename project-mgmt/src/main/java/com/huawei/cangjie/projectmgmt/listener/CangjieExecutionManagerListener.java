/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.listener;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.cangjie.projectmgmt.settings.ProjectOptimizationSettingsService;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.debugger.ohos.deployment.DeviceItem;
import com.huawei.deveco.debugger.ohos.deployment.DeviceSelector;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.hdclib.ohos.client.Client;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.hdclib.ohos.devices.DebugClientData;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.hdclib.ohos.hdc.HarmonyDebugConnector;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * The type Cangjie execution manager listener.
 *
 * @since 2026 -04-24
 */
public class CangjieExecutionManagerListener implements ExecutionListener {
    /**
     * The constant TIME_OUT.
     */
    public static final int TIME_OUT = 1000 * 3;

    private static final Logger LOG = Logger.getInstance(CangjieExecutionManagerListener.class);

    private static final String SLASH = "/";

    private static final String[] ILLEGAL_01 = new String[]{"CON", "PRN", "AUX", "NUL"};

    private static final String[] ILLEGAL_02 = {"COM", "LPT"};

    private static final int FILE_RECV_TIME_OUT = 1000 * 60;

    private final Project project;

    private boolean isGenerateOptimizationProfile = false;

    /**
     * Instantiates a new Cangjie execution manager listener.
     *
     * @param project the project
     */
    public CangjieExecutionManagerListener(Project project) {
        this.project = project;
    }

    /**
     * Gets profile path.
     *
     * @param projectPath the project path
     * @param moduleName the module name
     * @return the profile path
     */
    public static Path getProfilePath(String projectPath, String moduleName) {
        return getCjpgoPath(projectPath).resolve(moduleName).resolve("merged.profdata");
    }

    /**
     * Gets devices.
     *
     * @return the devices
     */
    public static Optional<Devices> getSelectedDevice() {
        HarmonyDebugConnector hdc = HarmonyDebugConnector.getHdcConnector();
        if (hdc == null) {
            return Optional.empty();
        }
        DeviceItem deviceItem = DeviceSelector.getSelectedDeviceItem();
        if (deviceItem == null) {
            return Optional.empty();
        }
        String serialNumber = deviceItem.getSerialNumber();
        Devices[] devices = hdc.getDevices();
        if (devices == null || devices.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(devices).filter(device -> serialNumber.equals(device.getSerialNumber())).findFirst();
    }

    @Override
    public void processNotStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        com.huawei.cangjie.projectmgmt.utils.FileUtils.resetProfileFlag(project);
    }

    /**
     * 在进程启动后立即调用。
     *
     * @param executorId 执行器ID (如 "Run" 或 "Debug")
     * @param env 正在运行的环境，包含了 RunProfile 和其他信息
     * @param handler 正在运行的进程的 ProcessHandler
     */
    @Override
    public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, // 使用 ExecutionEnvironment
        @NotNull ProcessHandler handler) {
        // 从 ExecutionEnvironment 中获取 RunProfile
        RunProfile runProfile = env.getRunProfile();

        String configurationName = runProfile.getName();
        LOG.info("ExecutionManagerListener: Process started for configuration: %s (Executor: %s)".formatted(
            configurationName, executorId));
        ProjectOptimizationSettingsService.State state =
            ProjectOptimizationSettingsService.getInstance(project).getState();
        if (state != null) {
            isGenerateOptimizationProfile = state.isGenerateOptimizationProfile();
        }
        com.huawei.cangjie.projectmgmt.utils.FileUtils.resetProfileFlag(project);
    }

    @Override
    public void processTerminating(@NotNull String executorId, @NotNull ExecutionEnvironment env,
        @NotNull ProcessHandler handler) {
        if (!isGenerateOptimizationProfile) {
            return;
        }
        RunProfile runProfile = env.getRunProfile();
        if (!(runProfile instanceof OpenHarmonyRunConfiguration openHarmonyRunConfiguration)) {
            return;
        }
        String moduleName = openHarmonyRunConfiguration.getModuleName();
        String bundleName = openHarmonyRunConfiguration.getModule().getProjectModel().getBundleName();
        Optional<Devices> selectedDeviceOpt = getSelectedDevice();
        if (selectedDeviceOpt.isEmpty()) {
            return;
        }
        Devices device = selectedDeviceOpt.get();
        DebugClient appClient = device.getAppClient(bundleName);
        if (appClient == null) {
            return;
        }
        DebugClientData clientData = appClient.getClientData();
        if (clientData == null) {
            return;
        }
        String pid = clientData.getPid();
        if (StringUtils.isEmpty(pid)) {
            return;
        }
        Client client = device.getClient();
        String devicePgoPath = String.format(Locale.ENGLISH, "/data/app/el2/100/base/%s/cjpgo", bundleName);
        deleteDeviceCjpgoFolder(devicePgoPath, client);
        String stopCmd = String.format(Locale.ENGLISH, "devicedebug kill -12 %s", pid);
        try {
            client.sendSyncShellCommand(stopCmd, TIME_OUT);
            String basePath = project.getBasePath();
            if (StringUtils.isEmpty(basePath)) {
                return;
            }
            // 每隔500ms检测cjpgo文件夹
            boolean isGenFinish = monitorFolder(client, devicePgoPath);
            if (!isGenFinish) {
                // 提示检测文件失败
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                    message("cangjie.pgo.run.action.unavailable"), project, NotificationType.ERROR);
                return;
            }
            // 接收文件
            Path profrawFilePath = getCjpgoPath(basePath).resolve(moduleName).resolve("profraws");
            try {
                FileUtils.deleteDirectory(profrawFilePath.toFile());
            } catch (IOException e) {
                LOG.warn(String.format(Locale.ENGLISH, "delete %s pgo files failed: %s", moduleName, e.getMessage()));
            }
            String recvFileResult = recvFile(client, devicePgoPath, profrawFilePath.toString());
            if (StringUtils.isEmpty(recvFileResult) || !recvFileResult.contains("FileTransfer finish")) {
                LOG.warn("Failed to receive the optimization profile: " + recvFileResult);
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"), message("cangjie.pgo.recv.fail"),
                    project, NotificationType.ERROR);
                return;
            }
            String profdataPath = getProfilePath(basePath, moduleName).toString();
            // 合并文件
            boolean mergeFilesResult = mergeProfrawFiles(profrawFilePath.toString(), profdataPath);
            if (mergeFilesResult) {
                ProjectOptimizationSettingsService.State state =
                    ProjectOptimizationSettingsService.getInstance(project).getState();
                if (state != null) {
                    state.setProfdataFilePath(profdataPath);
                    project.save();
                }
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                    message("cangjie.pgo.generate.optimization.profile.success"), project,
                    NotificationType.INFORMATION);
            }
            deleteDeviceCjpgoFolder(devicePgoPath, client);
        } catch (TimeoutException e) {
            LOG.warn("Failed to generate the optimization profile: " + e.getMessage());
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"), message("cangjie.pgo.generate.fail"),
                project, NotificationType.ERROR);
        }
    }

    /**
     * 合并指定路径下的所有 .profraw 文件，生成一个 .profdata 文件。
     *
     * @param profrawDir 包含 .profraw 文件的目录路径。
     * @param outputFilePath 生成的 .profdata 文件的完整路径（包括文件名）。
     * @return true 如果合并成功，false 如果失败。
     */
    public boolean mergeProfrawFiles(String profrawDir, String outputFilePath) {
        ProjectModel projectModel = CommonProjectUtil.getProjectModel(project);
        String sdkPath = SdkUtils.getSdkPath(projectModel);
        if (StringUtils.isEmpty(sdkPath)) {
            return false;
        }
        String llvmProfdataExePath = Paths.get(sdkPath, "build-tools/third_party/llvm/bin",
            (SystemInfo.isWindows ? "llvm-profdata.exe" : "llvm-profdata")).toString();
        Path profrawDirPath = Paths.get(profrawDir);

        if (!Files.exists(profrawDirPath) || !Files.isDirectory(profrawDirPath)) {
            LOG.warn("Profraw directory does not exist or is not a directory: " + profrawDir);
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                message("cangjie.pgo.profdata.directory.not.found", profrawDir), project, NotificationType.ERROR);
            return false;
        }

        File llvmProfdataExe = new File(llvmProfdataExePath);
        if (!llvmProfdataExe.exists() || !llvmProfdataExe.isFile() || !llvmProfdataExe.canExecute()) {
            LOG.warn("llvm-profdata not found or not executable at: " + llvmProfdataExePath);
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                message("cangjie.pgo.llvm.profdata.not.found", llvmProfdataExePath), project, NotificationType.ERROR);
            return false;
        }

        List<String> profrawFiles;
        try {
            profrawFiles = Files.list(profrawDirPath).filter(path -> path.toString().endsWith(".profraw"))
                .map(Path::toAbsolutePath).map(Path::toString).toList();
        } catch (IOException e) {
            LOG.warn("Error listing profraw files in directory " + profrawDir + ": " + e.getMessage());
            return false;
        }

        if (profrawFiles.isEmpty()) {
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"),
                message("cangjie.pgo.no.profdata.found", profrawDir), project, NotificationType.ERROR);
            // 没有文件可合并，但也算成功完成操作
            return true;
        }

        // 构建命令
        List<String> command = new ArrayList<>();
        command.add(llvmProfdataExePath);
        command.add("merge");
        command.addAll(profrawFiles);
        command.add("-o");
        command.add(outputFilePath);
        LOG.info("Executing merge command: %s".formatted(String.join(" ", command)));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // 合并标准错误流到标准输出流
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            // 读取进程输出
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            // 等待进程完成并获取退出码
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                FileUtils.deleteDirectory(profrawDirPath.toFile());
                return true;
            } else {
                LOG.warn("llvm-profdata failed to merge profraw files, exit code: " + exitCode + ", message: "
                    + stringBuilder);
                NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"), message("cangjie.pgo.merge.fail"),
                    project, NotificationType.ERROR);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            LOG.warn("Error executing llvm-profdata merge command: " + e.getMessage());
            NotificationUtil.notifyInfo(message("cangjie.pgo.message.title"), message("cangjie.pgo.merge.fail"),
                project, NotificationType.ERROR);
            return false;
        }
    }

    private String recvFile(Client client, String devicePath, String local) throws TimeoutException {
        if (SystemInfo.isWindows && this.illegalWindowFile(devicePath)) {
            return "Illegal file at Window";
        } else {
            String command = "file recv " + devicePath + " " + local;
            return client.sendSyncCommand(command, FILE_RECV_TIME_OUT);
        }
    }

    private void deleteDeviceCjpgoFolder(String devicePgoPath, Client client) {
        // 删除文件
        try {
            String deleteCmd = String.format(Locale.ENGLISH, "rm -rf %s", devicePgoPath);
            client.sendSyncShellCommand(deleteCmd, TIME_OUT);
        } catch (TimeoutException e) {
            LOG.warn("Failed to delete the .profraw file on the device:" + e.getMessage());
        }
    }

    private boolean illegalWindowFile(String file) {
        int length = file.split(SLASH).length;
        Locale englishLocale = Locale.ENGLISH;
        String temp = file.split(SLASH)[length - 1].toUpperCase(englishLocale);
        for (String s : ILLEGAL_01) {
            if (temp.equals(s) || temp.startsWith(s + ".")) {
                return true;
            }
        }
        for (String s : ILLEGAL_02) {
            for (int fileCount = 0; fileCount <= 9; fileCount++) {
                if (temp.equals(s + fileCount) || temp.startsWith(s + fileCount + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean monitorFolder(Client client, String devicePgoPath) {
        long interval = 500L;
        long maxDuration = 10 * 1000;
        long startTime = System.currentTimeMillis();
        String lastResult = null;
        String checkCmd = String.format(Locale.ENGLISH, "ls -al --full-time %s", devicePgoPath);
        try {
            while (System.currentTimeMillis() - startTime <= maxDuration) {
                String currentResult = client.sendSyncShellCommand(checkCmd, TIME_OUT);
                if (lastResult == null) {
                    lastResult = currentResult;
                } else if (lastResult.equals(currentResult)) {
                    return true;
                } else {
                    lastResult = currentResult;
                }
                // 3. 休眠 500ms
                Thread.sleep(interval);
            }
            LOG.warn("The detection task has ended, and the maximum duration of 10 seconds has been reached. "
                + "The folder may still be changing.");
            return true;
        } catch (InterruptedException e) {
            LOG.warn("The detection thread is interrupted.");
            return false;
        } catch (TimeoutException e) {
            LOG.warn("Sampling file generation timed out.");
            return false;
        }
    }

    private static Path getCjpgoPath(String projectPath) {
        return Paths.get(projectPath, ".idea", ".deveco", "cangjie", "cjpgos");
    }
}