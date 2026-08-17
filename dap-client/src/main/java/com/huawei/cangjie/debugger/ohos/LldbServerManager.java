/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import static com.huawei.cangjie.debugger.ohos.OhFilePathUtils.REMOTE_TEMP_DIR;
import static com.huawei.cangjie.debugger.ohos.OhFilePathUtils.getLldbServer;
import static com.huawei.cangjie.debugger.ohos.OhFilePathUtils.getLldbStartScript;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.StartUpTimeStatistics;
import com.huawei.bitfun.utils.ThreadUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.hdclib.ohos.devices.DebugClientData;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * lldb server manager tool
 *
 * @since 2022-12-1
 */
public class LldbServerManager {
    private static final int TIMEOUT = 10000;

    private static final Logger LOGGER = Logger.getInstance(LldbServerManager.class);

    private static final int MIN_DIR_LENGTH = 70;

    /**
     * targetSocketDir
     */
    protected final String myTargetSocketDir;

    /**
     * platformSocketName
     */
    protected final String myPlatformSocketName;

    private final DebugClient myDebugClient;

    private final DebugClientData myDebugClientData;

    private final OpenHarmonyConsolePrinter myPrinter;

    private final NativeDebuggerState myDebuggerState;

    private final ProjectModel projectModel;

    private final String myTargetRootDir;

    private final String myTargetBinDir;

    private final Abi ohAbi;

    /**
     * Instantiates a new Lldb server launcher.
     *
     * @param debugClient debugClient
     * @param debuggerState debuggerState
     * @param printer printer
     * @param projectModel projectModel
     * @throws TimeoutException e
     */
    public LldbServerManager(DebugClient debugClient, NativeDebuggerState debuggerState,
                             OpenHarmonyConsolePrinter printer, ProjectModel projectModel) throws TimeoutException {
        this.myDebugClient = debugClient;
        this.myDebugClientData = (debugClient == null ? null : debugClient.getClientData());
        this.myPrinter = printer;
        this.myDebuggerState = debuggerState;
        this.projectModel = projectModel;
        this.myTargetSocketDir = getNamedSocketDirectory();
        this.myPlatformSocketName = String.format(Locale.ENGLISH, "platform-%d.sock", System.currentTimeMillis());
        this.myTargetRootDir = getTargetRootDir();
        this.myTargetBinDir = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR, myTargetRootDir, "bin");
        this.ohAbi = getDeviceAbi();
    }

    /**
     * launchServerAndGetConnectAddress
     *
     * @return addr
     * @throws ExecutionException e
     * @throws TimeoutException e
     */
    public String launchServerAndGetConnectAddress() throws ExecutionException, TimeoutException {
        StartUpTimeStatistics.recordPoint("push files to device");
        pushFilesToDevice(myDebuggerState.getModuleWrapper().getOhosModule());
        StartUpTimeStatistics.recordPoint("start lldb-server");
        return startLldbServer();
    }

    /**
     * kill lldb-server
     *
     * @throws TimeoutException e
     */
    public void killLldbServer() throws TimeoutException {
        LogUtils.printCangjieLogInfo(LOGGER, "Cangjie Debug Stopped");
        myPrinter.stdout(LogUtils.generateTimeStampForConsole("Cangjie Debug Stopped"));
        // kill lldb-server
        String killServerCmd = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR, myTargetBinDir, "lldb-server");
        String killLldbResult = myDebugClient.getDevice().getClient()
                .sendSyncShellCommand(String.format(Locale.ENGLISH, "kill $(pgrep -f %s)", killServerCmd), TIMEOUT);
        LogUtils.printCangjieLogInfo(LOGGER, "kill lldb-server process: " + killLldbResult);
    }

    /**
     * terminate Process
     *
     * @throws TimeoutException e
     */
    public void terminateProcess() throws TimeoutException {
        String pid = myDebugClientData.getPid();
        String output = myDebugClient.getDevice().getClient()
                .sendSyncShellCommand("kill -9 " + pid, 3000);
        LogUtils.printCangjieLogWarn(LOGGER, "force kill process: " + output);
        if (output.contains("error")) {
            LogUtils.printCangjieLogWarn(LOGGER, "stop " + pid + " error: " + output);
        }
    }

    private String getTargetRootDir() {
        String separator = CodeCheckByPassUtils.FORWARD_SLASH_STR;
        String prefix = separator + "data" + separator + "data";
        return String.join(separator, prefix, myDebugClientData != null ? myDebugClientData.getPackageName() : "",
                "lldb");
    }

    private String getNamedSocketDirectory() {
        if (myDebugClientData == null) {
            LogUtils.printCangjieLogWarn(LOGGER, "myClientData is null");
            return "";
        }
        String dir = myDebugClientData.getPackageName();
        if (dir.length() >= MIN_DIR_LENGTH) {
            dir = dir.substring(dir.length() - MIN_DIR_LENGTH);
        }
        return CodeCheckByPassUtils.FORWARD_SLASH_STR + dir;
    }

    /**
     * push files to device, for example, core so, lldb server file...
     *
     * @param ohosModuleModel ohosModuleModel
     * @throws ExecutionException e
     * @throws TimeoutException e
     */
    protected void pushFilesToDevice(OhosModuleModel ohosModuleModel) throws ExecutionException, TimeoutException {
        LogUtils.printCangjieLogInfo(LOGGER, "Pushing files to the device...");
        File localLldbServer = getLldbServer(ohosModuleModel, ohAbi);
        if (localLldbServer == null) {
            throw new ExecutionException(
                    "Unable to obtain local lldb-server.Project name:" + projectModel.getProjectName());
        }
        File localStartServerScript = getLldbStartScript();
        // Push lldb-server and startup script to the temporary directory of the target device
        String lldbServerTempPath = pushFileToTargetTempDir(localLldbServer);
        String startServerScriptTempPath = pushFileToTargetTempDir(localStartServerScript);

        String mkdirOutput = createCataLog();
        if (!mkdirOutput.isEmpty()) {
            LogUtils.printCangjieLogWarn(LOGGER, mkdirOutput);
        }
        // Copy the temporary directory file to the official directory
        copyTargetTempFileToBinDirectory(lldbServerTempPath);
        copyTargetTempFileToBinDirectory(startServerScriptTempPath);
    }

    /**
     * start remote lldb Server
     *
     * @return Connect Url
     */
    protected String startLldbServer() {
        String startCmd = getStartCommand();
        String message = "Starting LLDB server: " + startCmd;
        LogUtils.printCangjieLogInfo(LOGGER, message);
        myPrinter.stdout(LogUtils.generateTimeStampForConsole(message));
        ExecutorService executorService = ThreadUtils.createSingleThreadExecutor("lldb-server executor");
        try {
            ThreadUtils.createSingleThreadExecutor("lldb-server executor").execute(() -> {
                try {
                    myDebugClient.getDevice().getClient().sendSyncShellCommand(startCmd, 500);
                } catch (TimeoutException e) {
                    LogUtils.printCangjieLogWarn(LOGGER, "startLldbServer error:" + e.getMessage());
                }
            });
        } finally {
            executorService.shutdown();
        }

        return getConnectUrl();
    }

    /**
     * get Start CommandLine
     *
     * @return the String CommandLine
     */
    private String getStartCommand() {
        String startScript = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR, myTargetBinDir,
                "start_lldb_server.sh");
        return String.format(Locale.ENGLISH, "%s %s %s %s %s %s \"%s\";", startScript, myTargetRootDir, "unix-abstract",
                myTargetSocketDir, myPlatformSocketName, REMOTE_TEMP_DIR, myDebuggerState.getLoggingTargetChannels());
    }

    /**
     * get connect url
     *
     * @return connect url
     */
    protected String getConnectUrl() {
        return String.format(Locale.ENGLISH, "%s-connect://[%s]%s/%s", "unix-abstract",
                myDebugClient.getDevice().getSerialNumber(), myTargetSocketDir, myPlatformSocketName);
    }

    private void copyTargetTempFileToBinDirectory(String tempTargetFilePath) throws TimeoutException {
        String fileName = (new File(tempTargetFilePath)).getName();
        String destFilePath = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR, myTargetBinDir, fileName);
        // Copy command
        String copyChmodCommand = "cat " + tempTargetFilePath + " | " + String.format(Locale.ENGLISH,
                "cat > %s && chmod 500 %s", destFilePath, destFilePath);
        String output = myDebugClient.getDevice().getClient().sendSyncShellCommand(copyChmodCommand, TIMEOUT);
        LogUtils.printCangjieLogInfo(LOGGER, "Copying to app folder: " + tempTargetFilePath + " => " + destFilePath);
        LogUtils.printCangjieLogInfo(LOGGER, "Command: " + copyChmodCommand);
        if (!output.isEmpty()) {
            LogUtils.printCangjieLogWarn(LOGGER, "Warning! result => " + output);
        }
    }

    private String createCataLog() throws TimeoutException {
        String command = String.format("mkdir -p %s; mkdir -p %s", myTargetRootDir, myTargetBinDir);
        return myDebugClient.getDevice().getClient().sendSyncShellCommand(command, TIMEOUT);
    }

    private String pushFileToTargetTempDir(File localFile) throws TimeoutException {
        String fileName = localFile.getName();
        String tmpDestFile = String.join(CodeCheckByPassUtils.FORWARD_SLASH_STR, REMOTE_TEMP_DIR,
                fileName);
        String message = "Pushing the file from " + localFile + " to " + tmpDestFile
                + " on the target device...";
        LogUtils.printCangjieLogInfo(LOGGER, message);
        String sendFileCommend = String.format("file send \"%s\" \"%s\"", localFile, tmpDestFile);
        String result = myDebugClient.getDevice().getClient().sendSyncCommand(sendFileCommend, TIMEOUT);
        if (!result.startsWith("FileTransfer finish")) {
            LogUtils.printCangjieLogWarn(LOGGER, result);
        }
        return tmpDestFile;
    }

    @Nullable
    private Abi getDeviceAbi() throws TimeoutException {
        String output = myDebugClient.getDevice().getClient().sendSyncShellCommand
                ("param get const.product.cpu.abilist", 3000);
        LogUtils.printCangjieLogInfo(LOGGER, "device abi: " + output);
        return Abi.getEnum(output, DeviceOs.OHOS);
    }

    public DebugClient getDebugClient() {
        return myDebugClient;
    }

    public NativeDebuggerState getDebuggerState() {
        return myDebuggerState;
    }

    public ProjectModel getProjectModel() {
        return projectModel;
    }

    public Abi getOhAbi() {
        return ohAbi;
    }

    public OpenHarmonyConsolePrinter getMyPrinter() {
        return myPrinter;
    }

    @Override
    public String toString() {
        return "LldbServerLauncher{}";
    }
}
