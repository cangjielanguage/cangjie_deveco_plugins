/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.connect.DapConnectionLauncher;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.start.XDebugSessionStartUtils;
import com.huawei.bitfun.intellij.utils.IntellijThreadUtils;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.bitfun.utils.StartUpTimeStatistics;
import com.huawei.cangjie.debugger.ohos.attach.LaunchType;
import com.huawei.cangjie.debugger.ohos.impl.DualCangjieXDebugProcess;
import com.huawei.cangjie.debugger.ohos.impl.OhCangjieXDebugProcess;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.protocol.CangjieIDebugProtocolServer;
import com.huawei.cangjie.debugger.start.CangjieDapConnectionUtils;
import com.huawei.cangjie.debugger.trace.CangjieDebugTracer;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.cangjie.debugger.utils.MessageFormatUtil;
import com.huawei.cangjie.sdkconfig.support.SdkConfig;
import com.huawei.cangjie.sdkmanager.idea.api.CangjieIdeaSdkInfoHandler;
import com.huawei.cangjie.sdkmanager.idea.constants.CangjieComponentPath;
import com.huawei.deveco.ace.debug.utils.UpdateWatchesUtil;
import com.huawei.deveco.debugger.ohos.run.configuration.OpenHarmonyRunConfiguration;
import com.huawei.deveco.debugger.ohos.util.DebuggerUtil;
import com.huawei.deveco.hdclib.ohos.client.Client;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.LaunchInfo;
import com.huawei.deveco.ohos.debugcommon.debugger.ConnectDebuggerTask;
import com.huawei.deveco.ohos.debugcommon.debugger.DebuggerState;
import com.huawei.deveco.ohos.debugcommon.debugger.NativeDebuggerState;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyConsolePrinter;
import com.huawei.deveco.ohos.debugcommon.processhandler.OpenHarmonyLaunchStatus;
import com.huawei.deveco.panda.util.PandaConstants;
import com.huawei.deveco.panda.watch.AceProjectManager;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.sdkmanager.core.util.ComponentVersionUtil;
import com.huawei.deveco.sourcemap.util.ProjectUtils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.intellij.xdebugger.impl.ui.XDebugSessionTab;

import com.jgoodies.common.base.Strings;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OhConnectDebuggerTask
 *
 * @since 2022-12-5
 */
public class OhConnectDebuggerTask extends ConnectDebuggerTask {
    private static final Logger LOGGER = Logger.getInstance(OhConnectDebuggerTask.class);

    private static final String VERSION_PATTERN = "[0-9]+([\\\\.][0-9]+){3}";

    private static final Pattern VERSION_PATTERN_COMPILE = Pattern.compile(VERSION_PATTERN);

    private static final String FLAG_VERSION_PHONE = "4.1.0.52";

    private static final String FLAG_VERSION_RK = "4.1.5.5";

    private final ExecutionEnvironment myEnv;

    private final NativeDebuggerState debuggerState;

    private final ProjectModel projectModel;

    private final OhosModuleModel ohosModuleModel;

    private final Project project;

    private final String debugType;

    private final LaunchType launchType;

    private final CangjieDebugTracer tracer = new CangjieDebugTracer();

    private SdkConfig sdkConfig = null;

    /**
     * constructor
     *
     * @param device device
     * @param packageName packageName
     * @param executionEnvironment executionEnvironment
     * @param debuggerState debuggerState
     */
    public OhConnectDebuggerTask(Devices device, String packageName, ExecutionEnvironment executionEnvironment,
        DebuggerState debuggerState) {
        this(device, packageName, executionEnvironment, debuggerState, LaunchType.DEBUGGER);
    }

    /**
     * constructor
     *
     * @param device device
     * @param packageName packageName
     * @param executionEnvironment executionEnvironment
     * @param debuggerState debuggerState
     * @param launchType launchType
     */
    public OhConnectDebuggerTask(Devices device, String packageName, ExecutionEnvironment executionEnvironment,
        DebuggerState debuggerState, LaunchType launchType) {
        super(device, packageName);
        this.myEnv = executionEnvironment;
        this.debuggerState = CodeCheckByPassUtils.cast(debuggerState);
        ohosModuleModel = debuggerState.getModuleWrapper().getOhosModule();
        projectModel = debuggerState.getModuleWrapper().getOhosModule().getProjectModel();
        project = projectModel.getProject();
        this.debugType = getDebugType(false);
        this.launchType = launchType;
    }

    @Override
    public ProcessHandler launchDebugger(LaunchInfo launchInfo, OpenHarmonyConsolePrinter openHarmonyConsolePrinter,
        OpenHarmonyLaunchStatus openHarmonyLaunchStatus, DebugClient debugClient) {
        StartUpTimeStatistics.setEnabled(true);
        StartUpTimeStatistics.reset();
        StartUpTimeStatistics.recordPoint("start Cangjie debug on OHOS");
        AceProjectManager.getInstance().setProject(project);
        ProcessHandler processHandler = openHarmonyLaunchStatus.getProcessHandler();
        try {
            LogUtils.printCangjieLogInfo(LOGGER, "Cangjie Debug Started");
            // stop old session when rerun attach
            stopExistingXDebugSession(project, debugClient);
            // Whether it meets the preconditions for commissioning and startup
            OhFilePathUtils.validateCangjieDir(ohosModuleModel);
            // Register process listener
            processHandler.addProcessListener(new LaunchTerminatedProcessListener(null));
            String serverPath = OhFilePathUtils.getServerInstallPath();
            DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService>
                connectionLauncher =
                CangjieDapConnectionUtils.createDapConnectionLauncher(serverPath, getDynamicLibPath(),
                        getOtherDynamicLibPath(), getCangjieHomePath(), null);
            LldbServerManager lldbServerLauncher = !isUerModule(debugClient.getDevice().getSoftwareVersion())
                ? new LldbServerManager(debugClient, debuggerState, openHarmonyConsolePrinter, projectModel)
                : new UserModelLldbServerStarter(debugClient, debuggerState, openHarmonyConsolePrinter, projectModel);
            Function<XDebugSession, DapXDebugProcess<?, ?>> createProcessFunction =
                getCreateProcessFunction(connectionLauncher, lldbServerLauncher);
            // The descriptor of the handler and other startup-related information have been set in the program runner
            RunContentDescriptor descriptor = myEnv.getContentToReuse();
            // Initialize operating environment information
            ExecutionEnvironment env = new ExecutionEnvironmentBuilder(myEnv).executor(launchInfo.executor)
                    .runner(launchInfo.runner).contentToReuse(descriptor).build();
            synchronized (this) {
                String pid = debugClient.getClientData().getPid();
                XDebugSession session = getXDebugSession(env, createProcessFunction, pid);
                if (session instanceof XDebugSessionImpl xDebugSession) {
                    UpdateWatchesUtil.updateWatchesFromPersistence(session, env, false);
                    return getProcessHandler(openHarmonyLaunchStatus, processHandler, openHarmonyConsolePrinter, env,
                            xDebugSession);
                }
            }
        } catch (IOException | TimeoutException | ExecutionException e) {
            try {
                doStopForError(debugClient, processHandler);
            } catch (TimeoutException ex) {
                LogUtils.printCangjieLogWarn(LOGGER, "launch debugger time out:" + ex.getMessage());
            }
            return nullHandlerAndTerminate(openHarmonyLaunchStatus, ExceptionUtils.getNonNullMsg(e));
        }
        return CodeCheckByPassUtils.getNull();
    }

    private XDebugSession getXDebugSession(ExecutionEnvironment env, Function<XDebugSession, DapXDebugProcess<?, ?>>
            createProcessFunction, String processId) throws ExecutionException {
        String moduleName = ProjectUtils.getModuleName(debuggerState.getModuleWrapper());
        String sessionName = moduleName + "(Cangjie-" + processId + ")";
        return XDebugSessionStartUtils.createXDebugSessionAndInit(project, sessionName, env, createProcessFunction);
    }

    /**
     * 获取调试类型（当调试类型为Detect Automatically时，根据isOriginal取值来决定取值是Detect Automatically，还是转换后的调试类型）
     *
     * @param isOriginal 是否是原始值
     * @return 调试类型
     */
    private String getDebugType(boolean isOriginal) {
        RunProfile runProfile = this.myEnv.getRunProfile();
        if (!(runProfile instanceof OpenHarmonyRunConfiguration)) {
            return OhConstants.DEBUGGER_NAME_CANGJIE;
        }
        OpenHarmonyRunConfiguration configuration = (OpenHarmonyRunConfiguration) runProfile;
        if (isOriginal) {
            return configuration.getCurrentDebugType();
        }
        return configuration.getDebugType();
    }

    /**
     * return sdkPath
     *
     * @return java.lang.String sdkPath
     */
    @Nullable
    private String getDynamicLibPath() {
        try {
            SdkConfig config = getSdkConfig();
            File dynamicLibFile;
            if (SystemUtils.IS_OS_WINDOWS) {
                dynamicLibFile = Path.of(config.getBuildToolsBinPath()).toFile();
            } else {
                dynamicLibFile = Path.of(config.getBuildToolsThirdPartyLibPath()).toFile();
            }
            if (dynamicLibFile.exists()) {
                return dynamicLibFile.getCanonicalPath();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Dynamic lib directory is not exist");
        }
        return CodeCheckByPassUtils.getNull();
    }

    /**
     * get other libPath
     * eg: libsecurec
     *
     * @return other libPath
     */
    @Nullable
    private String getOtherDynamicLibPath() {
        try {
            SdkConfig config = getSdkConfig();
            if (SystemUtils.IS_OS_WINDOWS) {
                StringBuilder filePath = new StringBuilder();
                File liblldbPath = Path.of(config.getBuildToolsThirdPartyLibPath()).toFile();
                if (liblldbPath.exists()) {
                    filePath.append(liblldbPath.getCanonicalPath()).append(';');
                }
                File runtimePath = Path.of(config.getBuildToolsWinRuntimePath()).toFile();
                if (runtimePath.exists()) {
                    filePath.append(runtimePath.getCanonicalPath()).append(';');
                }
                return filePath.toString();
            } else {
                StringBuilder filePath = new StringBuilder();
                File runtimePath = Path.of(config.getBuildToolsMacX86RuntimePath())
                        .toFile();
                if (runtimePath.exists()) {
                    filePath.append(runtimePath.getCanonicalPath()).append(':');
                }
                File toolsLib = Path.of(config.getBuildToolsLibPath()).toFile();
                if (toolsLib.exists()) {
                    filePath.append(toolsLib.getCanonicalPath());
                }
                return filePath.toString();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Other lib directory is not exist");
        }
        return null;
    }

    /**
     * get CANGJIE_HOME path
     *
     * @return CANGJIE_HOME path
     */
    private String getCangjieHomePath() {
        try {
            SdkConfig config = getSdkConfig();
            File dynamicLibFile = Path.of(config.getBuildToolsRootPath()).toFile();
            if (dynamicLibFile.exists()) {
                return dynamicLibFile.getCanonicalPath();
            }
        } catch (IOException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "Cangjie home directory is not exist");
        }
        return CodeCheckByPassUtils.getNull();
    }

    private SdkConfig getSdkConfig() {
        if (sdkConfig == null) {
            String apiVersion = ohosModuleModel.getProjectModel().getFullCompileSdkVersion().getValue();
            boolean isHarmony = DebuggerUtil.isHarmonyRunTime(ohosModuleModel);
            sdkConfig = new CangjieIdeaSdkInfoHandler()
                    .getLocalSdks(isHarmony, apiVersion)
                    .get(CangjieComponentPath.CANGJIE.value())
                    .getSdkConfig();
        }
        return sdkConfig;
    }

    private Function<XDebugSession, DapXDebugProcess<?, ?>> getCreateProcessFunction(
        DapConnectionLauncher<DapFromServerService, CangjieIDebugProtocolServer, CangjieDapToServerService>
        connectionLauncher, LldbServerManager lldbServerLauncher) throws TimeoutException {
        Function<XDebugSession, DapXDebugProcess<?, ?>> createProcessFunction;
        tracer.setDebugType(debuggerState.getDebugType());
        setDeviceAndProjectInfoToTracer(lldbServerLauncher);
        if (OhConstants.DUAL_JS_CANGJIE_DEBUG_TYPE.equals(debugType)) {
            createProcessFunction = xDebugSession -> {
                printDebugStartMessage(xDebugSession);
                return new DualCangjieXDebugProcess(xDebugSession, connectionLauncher,
                        lldbServerLauncher, tracer);
            };
        } else {
            createProcessFunction = xDebugSession -> {
                printDebugStartMessage(xDebugSession);
                return new OhCangjieXDebugProcess(xDebugSession, connectionLauncher,
                        lldbServerLauncher, launchType, tracer);
            };
        }
        return CodeCheckByPassUtils.cannotBeNull(createProcessFunction);
    }

    private void printDebugStartMessage(XDebugSession xDebugSession) {
        IntellijThreadUtils.invokeLater(() ->
                xDebugSession.getConsoleView().print(
                        "************** start cangjie debugger process ****************"
                                + CodeCheckByPassUtils.NEXT_LINE
                                + LogUtils.generateTimeStampForConsole(
                                "Cangjie Debug Started" + CodeCheckByPassUtils.NEXT_LINE),
                        ConsoleViewContentType.SYSTEM_OUTPUT));
    }

    private void doStopForError(DebugClient debugClient, ProcessHandler processHandler) throws TimeoutException {
        if (processHandler != null && !processHandler.isProcessTerminated() && !processHandler.isProcessTerminating()) {
            if (processHandler.detachIsDefault()) {
                processHandler.detachProcess();
            } else {
                processHandler.destroyProcess();
            }
        }
        String packageName1 = debugClient.getClientData().getPackageName();
        String output = debugClient.getDevice().getClient().sendSyncShellCommand("aa force-stop " + packageName1, 3000);
        if (output.contains("error")) {
            LogUtils.printCangjieLogWarn(LOGGER, MessageFormatUtil.removeCrlf(output));
        }
    }

    @Nullable
    private ProcessHandler nullHandlerAndTerminate(OpenHarmonyLaunchStatus openHarmonyLaunchStatus, String reason) {
        openHarmonyLaunchStatus.terminateLaunch(reason, true);
        return CodeCheckByPassUtils.getNull();
    }

    @Nullable
    private ProcessHandler getProcessHandler(@NotNull OpenHarmonyLaunchStatus launchStatus,
        ProcessHandler oldProcessHandler, @NotNull OpenHarmonyConsolePrinter printer, ExecutionEnvironment env,
        XDebugSessionImpl session) {
        Optional<RunContentDescriptor> debugDescriptor = Optional.of(session)
                .map(XDebugSessionImpl::getRunContentDescriptor);
        Optional<ProcessHandler> processHandler = debugDescriptor.map(RunContentDescriptor::getProcessHandler);
        if (processHandler.isEmpty()) {
            return CodeCheckByPassUtils.getNull();
        }
        ProcessHandler debugProcessHandler = processHandler.get();
        debugProcessHandler.addProcessListener(new LaunchTerminatedProcessListener(session));

        OpenHarmonySessionInfo oldInfo = oldProcessHandler.getUserData(OpenHarmonySessionInfo.KEY);
        if (oldInfo != null) {
            new OpenHarmonySessionInfo.OpenHarmonySessionInfoBuilder().setProcessHandler(debugProcessHandler)
                    .setDescriptor(oldInfo.getMyDescriptor())
                    .setExecutorId(oldInfo.getMyExecutorId())
                    .setExecutorActionName(oldInfo.getMyExecutorActionName())
                    .setRunConfiguration(oldInfo.getMyRunConfiguration())
                    .setExecutionTarget(oldInfo.getMyExecutionTarget())
                    .build();
        } else {
            RunProfile runProfile = env.getRunProfile();
            RunConfiguration runConfiguration = runProfile instanceof RunConfiguration
                    ? (RunConfiguration) runProfile
                    : null;
            Executor executor = env.getExecutor();
            new OpenHarmonySessionInfo.OpenHarmonySessionInfoBuilder().setProcessHandler(debugProcessHandler)
                    .setDescriptor(debugDescriptor.get())
                    .setExecutorId(executor.getId())
                    .setExecutorActionName(executor.getActionName())
                    .setRunConfiguration(runConfiguration)
                    .setExecutionTarget(env.getExecutionTarget())
                    .build();
        }
        launchStatus.setProcessHandler(debugProcessHandler);
        printer.setProcessHandler(debugProcessHandler);
        session.getConsoleView().attachToProcess(debugProcessHandler);
        RunContentDescriptor contentToReuse = myEnv.getContentToReuse();
        if (contentToReuse != null) {
            Disposer.register(contentToReuse, session.getConsoleView());
        }
        return debugProcessHandler;
    }

    private void stopExistingXDebugSession(Project project, DebugClient client) {
        for (XDebugSession session : XDebuggerManager.getInstance(project).getDebugSessions()) {
            XDebugProcess debugProcess = session.getDebugProcess();
            if (!(debugProcess instanceof OhCangjieXDebugProcess)) {
                continue;
            }
            OhCangjieXDebugProcess cangjieAppDebugProcess = (OhCangjieXDebugProcess) debugProcess;
            if (Objects.equals(cangjieAppDebugProcess.getDeviceAppPid(), client.getClientData().getPid())) {
                session.stop();
            }
        }
    }

    private class LaunchTerminatedProcessListener implements ProcessListener {
        private final Optional<XDebugSession> mySession;

        /**
         * Instantiates a new Launch terminated process listener.
         *
         * @param session the session
         */
        LaunchTerminatedProcessListener(XDebugSession session) {
            this.mySession = Optional.ofNullable(session);
        }

        /**
         * Instantiates a new Launch terminated process listener.
         *
         * @param event the session
         */
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
            if (mySession.isPresent()) {
                XDebugSession session = this.mySession.get();
                UpdateWatchesUtil.updateWatchOfStopSession(this.mySession.get().getProject(), false);
                closeDebugWindow(session);
            }
            event.getProcessHandler().destroyProcess();
        }

        @Override
        public void processWillTerminate(@NotNull ProcessEvent event, boolean isDestroyed) {
            mySession.ifPresent(XDebugSession::stop);
        }
    }

    private void closeDebugWindow(XDebugSession xDebugSession) {
        if (xDebugSession instanceof XDebugSessionImpl debugSession) {
            XDebugSessionTab sessionTab = debugSession.getSessionTab();
            if (sessionTab != null) {
                RunContentDescriptor runContentDescriptor =
                        sessionTab.getRunContentDescriptor();
                if (runContentDescriptor != null) {
                    IntellijThreadUtils.invokeLaterIfNeeded(() -> RunContentManager.getInstance(this.project)
                            .removeRunContent(DefaultDebugExecutor.getDebugExecutorInstance(), runContentDescriptor));
                    return;
                }
            }

            AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
                XDebugSessionTab retryTab = debugSession.getSessionTab();
                if (retryTab != null) {
                    RunContentDescriptor desc = retryTab.getRunContentDescriptor();
                    if (desc != null) {
                        IntellijThreadUtils.invokeLaterIfNeeded(() -> RunContentManager.getInstance(this.project)
                                .removeRunContent(DefaultDebugExecutor.getDebugExecutorInstance(), desc));
                        return;
                    }
                }
                IntellijThreadUtils.invokeLaterIfNeeded(() -> {
                    try {
                        if (!debugSession.isStopped()) {
                            debugSession.stop();
                        }
                    } catch (IllegalStateException e) {
                        // do nothing
                    }
                });
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private boolean isUerModule(String softwareVersion) {
        // softwareVersion:OpenHarmony x.x.x.x or xxxxx x.x.x.x(xxxxxx)
        if (Strings.isEmpty(softwareVersion)) {
            throw CodeCheckByPassUtils.createRuntimeException("get software version error");
        }
        LogUtils.printCangjieLogInfo(LOGGER, "get software version:" + softwareVersion);
        Matcher matcher = VERSION_PATTERN_COMPILE.matcher(softwareVersion);
        if (matcher.find()) {
            String version = matcher.group();
            if (softwareVersion.startsWith(PandaConstants.OPEN_HARMONY)) {
                // rk
                return ComponentVersionUtil.compareVersion(version, FLAG_VERSION_RK) > 0;
            }
            return ComponentVersionUtil.compareVersion(version, FLAG_VERSION_PHONE) > 0;
        }
        return false;
    }

    private void setDeviceAndProjectInfoToTracer(LldbServerManager lldbServerStarter) throws TimeoutException {
        tracer.setDeviceType("OHOS");
        Client deviceClient = lldbServerStarter.getDebugClient().getDevice().getClient();
        String paramGetResult = deviceClient.sendSyncShellCommand("param get", 1000);
        ProjectModel model = lldbServerStarter.getProjectModel();
        if (model != null) {
            tracer.setApiVersion(model.getFullCompileSdkVersion().getValue());
        }
        String osVersion = parseParamGetResult(paramGetResult, "const.product.software.version = ");
        if (osVersion != null) {
            tracer.setDeviceImage(osVersion);
        }
    }

    private String parseParamGetResult(String paramGetResult, String key) {
        String[] split = paramGetResult.split("\\r?\\n");
        for (String line : split) {
            line = line.trim();
            if (line.startsWith(key)) {
                return line.substring(key.length());
            }
        }
        return CodeCheckByPassUtils.getNull();
    }
}
