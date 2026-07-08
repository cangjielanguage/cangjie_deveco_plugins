/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.connect.DapConnectionLauncher;
import com.huawei.bitfun.intellij.breakpoint.handler.BreakpointHandlerBase;
import com.huawei.bitfun.intellij.breakpoint.handler.DataBreakpointHandlerBase;
import com.huawei.bitfun.intellij.breakpoint.handler.InstructionBreakpointHandlerBase;
import com.huawei.bitfun.intellij.breakpoint.handler.SourceBreakpointHandlerBase;
import com.huawei.bitfun.intellij.breakpoint.type.DataBreakpointTypeBase;
import com.huawei.bitfun.intellij.breakpoint.type.InstructionBreakpointTypeBase;
import com.huawei.bitfun.intellij.breakpoint.type.SourceBreakpointTypeBase;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.ex.DapXSuspendContext;
import com.huawei.bitfun.intellij.start.DebugStartType;
import com.huawei.bitfun.intellij.timetravel.TimeTravelProcess;
import com.huawei.bitfun.intellij.utils.BalloonNotificationsUtils;
import com.huawei.bitfun.intellij.utils.DapClientTimeouts;
import com.huawei.bitfun.intellij.utils.IntellijThreadUtils;
import com.huawei.bitfun.message.MessageListener;
import com.huawei.bitfun.protocol.extend.StoppedEventArguments;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.DapRuntimeException;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.bitfun.utils.StartUpTimeStatistics;
import com.huawei.cangjie.debugger.breakpoint.handler.CangjieFunctionBreakpointHandler;
import com.huawei.cangjie.debugger.breakpoint.type.CangjieDataBreakpointType;
import com.huawei.cangjie.debugger.breakpoint.type.CangjieInstructionBreakpointType;
import com.huawei.cangjie.debugger.breakpoint.type.CangjieSourceBreakpointType;
import com.huawei.cangjie.debugger.breakpoint.type.CangjieFunctionBreakpointType;
import com.huawei.cangjie.debugger.console.CommandConsoleTab;
import com.huawei.cangjie.debugger.deveco.lsp.LspUtils;
import com.huawei.cangjie.debugger.ohos.impl.LocalSymbolFilesInfo;
import com.huawei.cangjie.debugger.ohos.impl.OhCangjieXDebugProcess;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.trace.CangjieDebugTracer;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.cangjie.debugger.utils.OhCangjieBalloonNotificationsUtils;
import com.huawei.deveco.ohos.debugcommon.CommonConstants;

import com.intellij.debugger.ui.DebuggerContentInfo;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;

import lombok.Setter;

import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugRequestMessage;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugResponseMessage;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Cangjie XDebugProcess implementation
 *
 * @since 2022-10-19
 */
public class CangjieXDebugProcess extends DapXDebugProcess<DapFromServerService, CangjieDapToServerService> {
    /**
     * BALLOON_NOTIFICATION_TITLE
     */
    public static final String BALLOON_NOTIFICATION_TITLE = "Cangjie Debug";

    /**
     * Debug Language Id
     */
    public static final String LANGUAGE_ID = "Cangjie";

    private static final Logger LOGGER = Logger.getInstance(CangjieXDebugProcess.class);

    private static final String DEBUG_IN_CONSOLE_REQ = "debugInConsole";

    /**
     * symbolFilesInfo
     */
    protected LocalSymbolFilesInfo symbolFilesInfo;

    /**
     * tracer
     */
    protected final CangjieDebugTracer tracer;

    /**
     * cangjieXTimeTravelProcess
     */
    private final TimeTravelProcess<CangjieXDebugProcess, DapFromServerService, CangjieDapToServerService>
        cangjieXTimeTravelProcess = new CangjieXTimeTravelProcess<>(this);

    @Setter
    private DebugStartType debugStartType = DebugStartType.ATTACH;

    /**
     * CangjieXdebugProcess
     *
     * @param xDebugSession debug session
     * @param launchOrAttachArgs launch oro attach args
     * @param startType start type
     * @param connectionLauncher connection launcher
     * @param tracer tracer
     */
    public CangjieXDebugProcess(XDebugSession xDebugSession, Object launchOrAttachArgs, DebugStartType startType,
        DapConnectionLauncher<DapFromServerService, ?, CangjieDapToServerService> connectionLauncher,
        CangjieDebugTracer tracer) {
        super(xDebugSession, launchOrAttachArgs, startType, connectionLauncher);

        symbolFilesInfo = new LocalSymbolFilesInfo();
        addUpdateNotificationListeners(xDebugSession);
        this.tracer = tracer;
    }

    private void addUpdateNotificationListeners(XDebugSession xDebugSession) {
        xDebugSession.addSessionListener(new XDebugSessionListener() {
            private boolean isFirstStopped = true;

            @Override
            public void beforeSessionResume() {
                Project project = getProject();
                if (project.isDisposed()) {
                    return;
                }
                IntellijThreadUtils.invokeLaterIfNeeded(() ->
                        EditorNotifications.getInstance(project).updateNotifications(disassemblyFile));
            }
            @Override
            public void sessionPaused() {
                Project project = getProject();
                if (project.isDisposed()) {
                    return;
                }
                IntellijThreadUtils.invokeLaterIfNeeded(() ->
                        EditorNotifications.getInstance(project).updateNotifications(disassemblyFile));
                IntellijThreadUtils.invokeAndWait(this::handleFirstStop);
            }

            private void handleFirstStop() {
                if (!this.isFirstStopped) {
                    return;
                }
                RunnerLayoutUi ui = xDebugSession.getUI();
                Content frameContent = ui.findContent(DebuggerContentInfo.FRAME_CONTENT);
                if (frameContent != null) {
                    ui.selectAndFocus(frameContent, true, true);
                }
                this.isFirstStopped = false;
            }

            @Override
            public void stackFrameChanged() {
                Project project = getProject();
                if (project.isDisposed()) {
                    return;
                }
                IntellijThreadUtils.invokeLaterIfNeeded(() ->
                        EditorNotifications.getInstance(project).updateNotifications(disassemblyFile));
            }

            @Override
            public void sessionStopped() {
                XDebugProcess xDebugProcess = xDebugSession.getDebugProcess();
                if (xDebugProcess instanceof OhCangjieXDebugProcess ohCangjieXDebugProcess) {
                    ohCangjieXDebugProcess.terminateProcess();
                }
            }
        });
    }

    @Override
    protected void handleInitException(Exception e, boolean isFirstInitFailed) {
        String errorMsg = ExceptionUtils.getNonNullMsg(e);
        getDebugTracer().startDebugging(errorMsg);
        LogUtils.printCangjieLogInfo(LOGGER, errorMsg);
        if (e.getCause() instanceof DapRuntimeException) {
            BalloonNotificationsUtils.showErrorNotification(getProject(),
                    OhCangjieBalloonNotificationsUtils.BALLOON_NOTIFICATION_TITLE,
                    "Failed to start debug server: " + e.getCause().getMessage());
        }
        if (isFirstInitFailed) {
            try {
                XDebugSession session = getSession();
                if (session != null) {
                    session.stop();
                }
            } catch (IllegalStateException | RejectedExecutionException | NullPointerException ex) {
                // do nothing
            }
        }
    }

    @Override
    protected void launchDapConnection(ProgressIndicator indicator) throws IOException, TimeoutException {
        super.launchDapConnection(indicator);
        registerStartUpTimeStatisticsListeners();
        registerUsageMessageListeners();
    }

    private void registerUsageMessageListeners() {
        getDapConnection().getMessageAdapter().addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof DebugRequestMessage) {
                    handleDebugInConsoleRequest((DebugRequestMessage) message);
                }
            }

            private void handleDebugInConsoleRequest(DebugRequestMessage message) {
                if (DEBUG_IN_CONSOLE_REQ.equals(message.getMethod())) {
                    getUsageObject().commandLineUsageCountPlusOne();
                }
            }
        });
    }

    /**
     * Register statistic points of start up time
     */
    protected void registerStartUpTimeStatisticsListeners() {
        dapConnection.getMessageAdapter().addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof DebugRequestMessage) {
                    String command = ((DebugRequestMessage) message).getMethod();
                    StartUpTimeStatistics.recordPoint("send " + command + " request");
                }
                if (message instanceof DebugResponseMessage) {
                    handleResponseMsg((DebugResponseMessage) message);
                }
            }

            private void handleResponseMsg(DebugResponseMessage message) {
                String command = message.getMethod();
                StartUpTimeStatistics.recordPoint("receive " + command + " response");
                if ("stackTrace".equals(command)) {
                    // print result and remove listener
                    dapConnection.getMessageAdapter().getListeners().remove(this);
                    StartUpTimeStatistics.printResult();
                    StartUpTimeStatistics.reset();
                }
            }
        });
        dapConnection.getFromRemoteService().getInitializedListenerRegistrar().registerOccurOnceListener(unused ->
                StartUpTimeStatistics.recordPoint("receive initialized event"));
        dapConnection.getFromRemoteService().getStoppedListenerRegistrar().registerOccurOnceListener(ev ->
                StartUpTimeStatistics.recordPoint("receive stopped event"));
    }

    @Override
    public @NotNull TimeTravelProcess<? extends DapXDebugProcess<DapFromServerService, CangjieDapToServerService>,
        DapFromServerService, CangjieDapToServerService> getTimeTravelProcess() {
        return this.cangjieXTimeTravelProcess;
    }

    @Override
    protected List<BreakpointHandlerBase<?, ?>> createBreakpointHandlers() {
        List<BreakpointHandlerBase<?, ?>> list = new ArrayList<>();
        list.add(new SourceBreakpointHandlerBase(CangjieSourceBreakpointType.class, this));
        list.add(new CangjieFunctionBreakpointHandler(CangjieFunctionBreakpointType.class, this));
        list.add(new DataBreakpointHandlerBase(CangjieDataBreakpointType.class, this));
        list.add(new InstructionBreakpointHandlerBase(CangjieInstructionBreakpointType.class, this));
        return list;
    }

    @Override
    public DapXSuspendContext<? extends DapXDebugProcess<DapFromServerService, CangjieDapToServerService>, ?>
    createXSuspendContext(StoppedEventArguments stoppedEventArguments) {
        return new CangjieXSuspendContext(stoppedEventArguments, this);
    }

    @Override
    public boolean isVirtualFileSupported(VirtualFile virtualFile) {
        return LspUtils.isVirtualFileSupportedByLsp(virtualFile);
    }

    @Override
    @NotNull
    public String getBalloonNotificationTitle() {
        return BALLOON_NOTIFICATION_TITLE;
    }

    @Override
    protected void createDebugTabViews() {
        debugTabViews.add(new CommandConsoleTab(this.getProject(), "cjdb", this));
    }

    @Override
    @NotNull
    public String getLanguageId() {
        return LANGUAGE_ID;
    }

    @Override
    @Nullable
    public DataBreakpointTypeBase getDataBreakpointTypeInstance() {
        return CangjieDataBreakpointType.getInstance();
    }

    @Override
    @Nullable
    public SourceBreakpointTypeBase getSourceBreakpointTypeInstance() {
        return CangjieSourceBreakpointType.getInstance();
    }

    @Override
    @Nullable
    public InstructionBreakpointTypeBase getInstructionBreakpointTypeInstance() {
        return CangjieInstructionBreakpointType.getInstance();
    }

    @Override
    public boolean isSupportDataBreakpoint() {
        return false;
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return CodeCheckByPassUtils.cannotBeNull(LspUtils.getXDebuggerEditorsProvider());
    }

    @Override
    protected InitializeRequestArguments createInitializeArgs() {
        InitializeRequestArguments initializeRequestArguments = super.createInitializeArgs();
        initializeRequestArguments.setAdapterID("DAP-CANGJIE");
        if (debugStartType == DebugStartType.LAUNCH && SystemInfo.isWindows) {
            initializeRequestArguments.setSupportsRunInTerminalRequest(Boolean.TRUE);
        }
        return initializeRequestArguments;
    }

    @Override
    protected DapClientTimeouts createDefaultTimeouts() {
        return new CangjieDapClientTimeouts();
    }

    private static class CangjieDapClientTimeouts implements DapClientTimeouts {
        @Override
        public long attach() {
            // timeout interval is 300000 ms
            return 300000L;
        }

        @Override
        public long scopes() {
            // 停在断点存在表达式时，先发送scopes请求，再发送evaluate请求场景下，evaluate执行较慢时，scopes请求超时
            return this.evaluate() + 2000L;
        }
    }

    @Override
    public void handleEventWhenStopSession() {
        // Set this value to false when the debug process is closed.
        getProject().putUserData(CommonConstants.IS_DEBUG, Boolean.FALSE);
        super.handleEventWhenStopSession();
    }
}