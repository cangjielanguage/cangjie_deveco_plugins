/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.connect.DapConnectionLauncher;
import com.huawei.bitfun.intellij.breakpoint.utils.FakeDualInstructionBreakpoint;
import com.huawei.bitfun.intellij.ex.DapXSuspendContext;
import com.huawei.bitfun.intellij.start.DebugStartType;
import com.huawei.bitfun.intellij.trace.DebugTracer;
import com.huawei.bitfun.intellij.treemodel.DapSuspendContextNode;
import com.huawei.bitfun.message.MessageAdapter;
import com.huawei.bitfun.message.MessageFilter;
import com.huawei.bitfun.protocol.extend.StoppedEventArguments;
import com.huawei.bitfun.utils.ThreadUtils;
import com.huawei.cangjie.debugger.impl.CangjieXDebugProcess;
import com.huawei.cangjie.debugger.ohos.CangjieAttachParamsUtils;
import com.huawei.cangjie.debugger.ohos.LldbServerManager;
import com.huawei.cangjie.debugger.ohos.OhCangjieDebugClientListener;
import com.huawei.cangjie.debugger.ohos.attach.LaunchType;
import com.huawei.cangjie.debugger.ohos.panda.OhosPandaDebugHandler;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.trace.CangjieDebugTracer;
import com.huawei.cangjie.debugger.utils.FeatureEnableUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.ohos.debugcommon.CommonConstants;
import com.huawei.deveco.ohos.debugcommon.debugger.SetBreakpointState;
import com.huawei.deveco.ohos.debugcommon.processhandler.DebugSessionManager;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.SetInstructionBreakpointsResponse;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugResponseMessage;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * OHOS device Cangjie debug process
 *
 * @since 2022-12-5
 */
public class OhCangjieXDebugProcess extends CangjieXDebugProcess {
    private static final Set<Character> FORBIDDEN_CHAR_SET = Set.of('|', ';', '&', '$', '>', '<', '`', '\\', '!', '\n');

    private static final OhCangjieDebugClientListener DEBUG_CLIENT_LISTENER =
        OhCangjieDebugClientListener.getInstance();

    private static final Logger LOGGER = Logger.getInstance(OhCangjieXDebugProcess.class);

    /**
     * LldbServerStarter
     */
    protected final LldbServerManager lldbServerManager;

    private OhosPandaDebugHandler pandaDebugHandler;

    private final LaunchType launchType;

    /**
     * constructor
     *
     * @param xDebugSession xDebugSession
     * @param connectionLauncher connectionLauncher
     * @param lldbServerManager lldbServerManager
     * @param launchType launchType
     * @param tracer tracer
     */
    public OhCangjieXDebugProcess(XDebugSession xDebugSession,
        DapConnectionLauncher<DapFromServerService, ?, CangjieDapToServerService> connectionLauncher,
        LldbServerManager lldbServerManager, LaunchType launchType, CangjieDebugTracer tracer) {
        // start type is always attach, attach args is set after lldb-server is started
        super(xDebugSession, null, DebugStartType.ATTACH, connectionLauncher, tracer);
        this.lldbServerManager = lldbServerManager;
        this.launchType = launchType;
        traceWhenSessionPaused(xDebugSession);
    }

    @Override
    public DapXSuspendContext<CangjieXDebugProcess, DapSuspendContextNode> createXSuspendContext(
        StoppedEventArguments arguments) {
        return new OhCangjieXSuspendContext(arguments, this);
    }

    /**
     * 1. push lldb server
     * 2. create connection
     * 3. initialize
     * 4. attach
     * 5. wait initialized event
     * 6. set breakpoints
     * 7. configuration done
     *
     * @param indicator indicator
     * @throws Exception e
     */
    @Override
    protected void doInit(ProgressIndicator indicator) throws Exception {
        LogUtils.printCangjieLogInfo(LOGGER, "Prepare to start DAP Server...");
        Devices device = lldbServerManager.getDebugClient().getDevice();
        DEBUG_CLIENT_LISTENER.addToAppStatusList(device, getDeviceAppPid(), this);
        if (launchType == LaunchType.DEBUGGER) {
            pandaDebugHandler = new OhosPandaDebugHandler(lldbServerManager.getDebugClient(), "", getProject());
        }
        CompletableFuture<Void> future = launchDapConnectionAsync(indicator);
        pushLldbServerToDevice(indicator);
        future.get();
        LogUtils.printCangjieLogInfo(LOGGER, "DAP Server started");
        this.checkNotCancelledAndDo(this::initialize, indicator);
        this.checkNotCancelledAndDo(this::launchOrAttach, indicator);
        this.checkNotCancelledAndDo(this::waitInitialized, indicator);
        this.checkNotCancelledAndDo(this::setBreakpoints, indicator);
        this.checkNotCancelledAndDo(this::configurationDone, indicator);
        // the appfreezed happened exception isn't thrown by faultLog when the value is true
        getProject().putUserData(CommonConstants.IS_DEBUG, Boolean.TRUE);
        if (launchType == LaunchType.DEBUGGER) {
            connectToPandaDebugAndContinue(indicator);
        }
    }

    /**
     * launch DAP connection async
     *
     * @param indicator indicator
     * @return CompletableFuture
     */
    protected CompletableFuture<Void> launchDapConnectionAsync(ProgressIndicator indicator) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ExecutorService executorService = ThreadUtils.createSingleThreadExecutor("launch dap connection");
        try {
            executorService.execute(() -> {
                try {
                    this.checkNotCancelledAndDo(this::launchDapConnection, indicator);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
                future.complete(null);
            });
        } finally {
            executorService.shutdown();
        }
        return future;
    }

    /**
     * connect to panda debug and continue
     *
     * @param indicator indicator
     */
    protected void connectToPandaDebugAndContinue(ProgressIndicator indicator) {
        OhosModuleModel module = lldbServerManager.getDebuggerState().getModuleWrapper().getOhosModule();
        String cangjieProjectType = module.getExtraBuildOptionByName("CangjieProjectType");
        if ("Cangjie".equals(cangjieProjectType)) {
            return;
        }
        indicator.setText("Sending connect to panda server request");
        pandaDebugHandler.createSocketAndStartListener();
    }

    /**
     * pushLldbServerToDevice
     *
     * @param indicator indicator
     * @throws ExecutionException e
     * @throws IOException e
     * @throws TimeoutException e
     */
    protected void pushLldbServerToDevice(ProgressIndicator indicator)
        throws ExecutionException, IOException, TimeoutException {
        String packageName = lldbServerManager.getDebugClient().getClientData().getPackageName();
        for (char ch : packageName.toCharArray()) {
            if (FORBIDDEN_CHAR_SET.contains(ch)) {
                throw new ExecutionException("Forbidden package name： " + ch);
            }
        }
        indicator.setText("Pushing LLDB server to device");
        String remoteAddress = lldbServerManager.launchServerAndGetConnectAddress();
        launchOrAttachArgs =
            CangjieAttachParamsUtils.createOhCangjieAttachRequestParams(lldbServerManager.getDebugClient(),
                remoteAddress, lldbServerManager.getDebuggerState(), lldbServerManager.getOhAbi());
    }

    /**
     * call TraceUtils.trace() when paused the first time
     *
     * @param xDebugSession session
     */
    private void traceWhenSessionPaused(XDebugSession xDebugSession) {
        xDebugSession.addSessionListener(new XDebugSessionListener() {
            @Override
            public void sessionPaused() {
                xDebugSession.removeSessionListener(this);
            }
        });
    }

    @Override
    public boolean isSupportDataBreakpoint() {
        return FeatureEnableUtils.IS_DATA_BP_ENABLED;
    }

    /**
     * stop session
     */
    public void stopSession() {
        super.stopXDebugSession(false);
    }

    /**
     * get device app pid
     *
     * @return pid
     */
    public String getDeviceAppPid() {
        return lldbServerManager.getDebugClient().getClientData().getPid();
    }

    @Override
    public void handleEventWhenStopSession() {
        try {
            super.handleEventWhenStopSession();
            lldbServerManager.killLldbServer();
            if (!lldbServerManager.getDebuggerState().isAttach()) {
                // don't kill process when attach to process
                lldbServerManager.terminateProcess();
            }
            DebugSessionManager.removeDebugSession(getSession());
            if (pandaDebugHandler != null) {
                pandaDebugHandler.dispose();
            }
        } catch (TimeoutException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "handle event when stop session error:" + e.getMessage());
        }
    }

    /**
     * set dual debug breakpoint
     *
     * @param codeAddress Decimal address
     * @return 0:cangjie method and set bk failed;1:set bk success;2:not cangjie method
     */
    public int setDualDebugBreakpoint(String codeAddress) {
        String hexAddress = String.format(Locale.ENGLISH, "0x%016x", Long.valueOf(codeAddress));
        boolean isAddressValid = symbolFilesInfo.isAddressValid(hexAddress);
        if (!isAddressValid) {
            return SetBreakpointState.NOT_USER_SIDE_FUNCTION;
        }

        MessageAdapter messageAdapter = this.getDapConnection().getMessageAdapter();
        MessageFilter responseSniffer = new MessageFilter() {
            @Override
            public Message doFilter(Message message) {
                if (!(message instanceof DebugResponseMessage resp)) {
                    return message;
                }
                if (!"setInstructionBreakpoints".equals(resp.getMethod())) {
                    return message;
                }
                if (!(resp.getResult() instanceof SetInstructionBreakpointsResponse body)) {
                    return message;
                }
                Breakpoint[] bps = body.getBreakpoints();
                if (bps == null || bps.length == 0) {
                    return message;
                }
                Integer matchedId = null;
                for (Breakpoint bp : bps) {
                    if (bp == null || !bp.isVerified() || bp.getId() == null) {
                        continue;
                    }
                    if (hexAddress.equalsIgnoreCase(bp.getInstructionReference())) {
                        matchedId = bp.getId();
                        break;
                    }
                }
                if (matchedId == null) {
                    for (Breakpoint bp : bps) {
                        if (bp != null && bp.isVerified() && bp.getId() != null) {
                            matchedId = bp.getId();
                            break;
                        }
                    }
                }
                if (matchedId == null) {
                    return message;
                }
                messageAdapter.getFilters().remove(this);
                return message;
            }
        };
        messageAdapter.addMessageFilter(responseSniffer);
        this.fakeDualInstructionBreakpoint = new FakeDualInstructionBreakpoint(
                this.disassemblyFile.getPath(), hexAddress, this.getInstructionBreakpointTypeInstance());
        this.getInstructionBreakpointHandler().registerBreakpoint(this.fakeDualInstructionBreakpoint);

        CompletableFuture<Void> future = this.getInstructionBreakpointHandler().getCurPendingBreakpointsFuture();
        if (future == null) {
            messageAdapter.getFilters().remove(responseSniffer);
            return SetBreakpointState.FAILURE;
        }
        try {
            future.get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            messageAdapter.getFilters().remove(responseSniffer);
            LOGGER.warn(String.format(Locale.ENGLISH, "set dual breakpoint error: %s", e));
            return SetBreakpointState.FAILURE;
        }
        return SetBreakpointState.SUCCESS;
    }


    @Override
    public DebugTracer getDebugTracer() {
        return tracer;
    }

    /**
     * terminate process
     */
    public void terminateProcess() {
        if (!lldbServerManager.getDebuggerState().isAttach()) {
            ExecutorService executorService = ThreadUtils.createSingleThreadExecutor("terminateProcess");
            try {
                // don't kill process when attach to process
                executorService.execute(() -> {
                            try {
                                lldbServerManager.terminateProcess();
                            } catch (TimeoutException e) {
                                LogUtils.printCangjieLogWarn(LOGGER, "stop process error", e);
                            }
                        }
                );
            } finally {
                executorService.shutdown();
            }
        }
    }
}
