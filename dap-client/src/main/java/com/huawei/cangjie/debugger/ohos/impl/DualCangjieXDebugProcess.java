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
import com.huawei.bitfun.message.MessageAdapter;
import com.huawei.bitfun.message.MessageFilter;
import com.huawei.bitfun.protocol.extend.FunctionBreakpoint;
import com.huawei.bitfun.protocol.extend.SetFunctionBreakpointsArguments;
import com.huawei.bitfun.protocol.extend.StackFrame;
import com.huawei.bitfun.protocol.extend.StackTraceResponse;
import com.huawei.bitfun.protocol.extend.StoppedEventArguments;
import com.huawei.bitfun.request.requester.RequestCallbacks;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.DapRuntimeException;
import com.huawei.cangjie.debugger.breakpoint.handler.CangjieFunctionBreakpointHandler;
import com.huawei.cangjie.debugger.breakpoint.properties.CangjieFunctionBreakpointProperties;
import com.huawei.cangjie.debugger.breakpoint.type.CangjieFunctionBreakpointType;
import com.huawei.cangjie.debugger.ohos.CangjieAttachParamsUtils;
import com.huawei.cangjie.debugger.ohos.LldbServerManager;
import com.huawei.cangjie.debugger.ohos.attach.LaunchType;
import com.huawei.cangjie.debugger.protocol.CangjieDapToServerService;
import com.huawei.cangjie.debugger.trace.CangjieDebugTracer;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.deveco.ohos.debugcommon.debugger.DebugStateSubject;
import com.huawei.deveco.ohos.debugcommon.debugger.SetBreakpointState;
import com.huawei.deveco.panda.PandaDAPServer;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.frame.XSuspendContext;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.SetFunctionBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetInstructionBreakpointsResponse;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugNotificationMessage;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugResponseMessage;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Dual Cangjie XDebugProcess
 *
 * @since 2026-6-11
 */
public class DualCangjieXDebugProcess extends OhCangjieXDebugProcess {
    private static final Logger LOGGER = Logger.getInstance(DualCangjieXDebugProcess.class);

    /**
     * @Interop[ArkTS] 宏生成的 napi 包装函数名固定含此子串：test -> test_ArkTS_Interop_Identifier
     */
    private static final String INTEROP_WRAPPER_MARK = "_ArkTS_Interop_Identifier";

    private static final long STEP_IN_TIMEOUT_MS = 500L;
    private static final long STACK_TRACE_TIMEOUT_MS = 3000L;
    private static final long FUNCTION_BP_TIMEOUT_MS = 3000L;
    private static final long CONTINUE_TIMEOUT_MS = 3000L;

    /**
     * 当前持有的 fake dual 指令断点，每次 setDualDebugBreakpoint 覆盖。
     */
    private FakeDualInstructionBreakpoint fakeDualInstructionBreakpoint;

    public DualCangjieXDebugProcess(XDebugSession xDebugSession,
        DapConnectionLauncher<DapFromServerService, ?, CangjieDapToServerService> connectionLauncher,
        LldbServerManager lldbServerStarter,
        CangjieDebugTracer tracer) {
        super(xDebugSession, connectionLauncher, lldbServerStarter, LaunchType.DEBUGGER, tracer);
    }

    @Override
    protected void launchDapConnection(ProgressIndicator indicator) throws IOException, TimeoutException {
        super.launchDapConnection(indicator);
        this.getFromServerService().getOutputListenerRegistrar().registerListener(this::getSymbolFilesInfoEvent);
    }

    @Override
    protected void pushLldbServerToDevice(ProgressIndicator indicator)
            throws ExecutionException, IOException, TimeoutException {
        super.pushLldbServerToDevice(indicator);
        this.symbolFilesInfo.setSymbolDirs(CangjieAttachParamsUtils.getSymbolDirs());
    }

    @Override
    protected void connectToPandaDebugAndContinue(ProgressIndicator indicator) {
        DebugStateSubject.notifyObservers(
                this.lldbServerManager.getDebugClient().getDevice().getSerialNumber(),
                this.lldbServerManager.getDebugClient().getClientData().getPid());
    }

    private void getSymbolFilesInfoEvent(OutputEventArguments event) {
        if ("console".equalsIgnoreCase(event.getCategory())) {
            this.symbolFilesInfo.addSymbolFiles(event.getOutput());
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        super.startStepOut(context);
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        super.resume(context);
        this.setPandaServerFlags("Resume");
    }

    /**
     * get panda dapServer
     *
     * @return panda dapServer
     */
    @Nullable
    public PandaDAPServer getPandaDAPServer() {
        return this.getProject().getUserData(PandaDAPServer.getPandaDapServerKey());
    }

    private void setPandaServerFlags(String action) {
        boolean isStepOut = "StepOut".equals(action);
        boolean isResume = "Resume".equals(action);
        PandaDAPServer pandaDAPServer = this.getPandaDAPServer();
        if (pandaDAPServer == null) {
            LogUtils.printCangjieLogWarn(LOGGER, "panda server is null.");
            return;
        }
        pandaDAPServer.setStepOut(isStepOut);
        pandaDAPServer.setResume(isResume);
    }

    @Override
    public void handleEventWhenStopSession() {
        super.handleEventWhenStopSession();
        this.getProject().putUserData(PandaDAPServer.getNativeRangeKey(), new ArrayList());
    }

    /**
     * set dual debug breakpoint
     *
     * @param codeAddress Decimal address
     * @return 0:cangjie method and set bk failed;1:set bk success;2:not cangjie method
     */
    public int setDualDebugBreakpoint(String codeAddress) {
        final String hexAddress = String.format(Locale.ENGLISH, "0x%016x", Long.valueOf(codeAddress));
        if (!this.symbolFilesInfo.isAddressValid(hexAddress)) {
            return SetBreakpointState.NOT_USER_SIDE_FUNCTION;
        }

        final MessageAdapter messageAdapter = this.getDapConnection().getMessageAdapter();

        final MessageFilter responseSniffer = new MessageFilter() {
            @Override
            public Message doFilter(Message message) {
                if (!(message instanceof DebugResponseMessage)) {
                    return message;
                }
                DebugResponseMessage resp = (DebugResponseMessage) message;
                if (!"setInstructionBreakpoints".equals(resp.getMethod())) {
                    return message;
                }
                Object result = resp.getResult();
                if (!(result instanceof SetInstructionBreakpointsResponse)) {
                    return message;
                }
                SetInstructionBreakpointsResponse body = (SetInstructionBreakpointsResponse) result;
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
                installStoppedFilterForFakeStepIn(messageAdapter, matchedId, hexAddress);
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
            LogUtils.printCangjieLogWarn(LOGGER, "set dual breakpoint error: " + e.getMessage());
            return SetBreakpointState.FAILURE;
        }
        return SetBreakpointState.SUCCESS;
    }

    /**
     * unregister dual instruction breakpoint
     */
    private void unregisterDualInstructionBreakpoint() {
        try {
            if (this.fakeDualInstructionBreakpoint != null) {
                this.getInstructionBreakpointHandler().unregisterBreakpoint(
                        this.fakeDualInstructionBreakpoint, false);
                this.fakeDualInstructionBreakpoint = null;
            }
            this.getInstructionBreakpointHandler().setFakeDualBreakpointId(-1);
            LogUtils.printCangjieLogInfo(LOGGER, "dual instruction breakpoint unregistered");
        } catch (ProcessCanceledException | ClassCastException | NullPointerException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "unregisterDualInstructionBreakpoint error: " + e.getMessage());
        }
    }

    /**
     * install stopped filter for fake stepIn
     *
     * @param messageAdapter messageAdapter
     * @param bpId breakpoint ID
     * @param hexAddress hex address
     */
    private void installStoppedFilterForFakeStepIn(final MessageAdapter messageAdapter,
                                                   final int bpId,
                                                   final String hexAddress) {
        messageAdapter.addMessageFilter(new MessageFilter() {
            @Override
            public Message doFilter(Message message) {
                if (!(message instanceof DebugNotificationMessage event)) {
                    return message;
                }
                if (!"stopped".equals(event.getMethod())) {
                    return message;
                }
                messageAdapter.getFilters().remove(this);

                StoppedEventArguments args = CodeCheckByPassUtils.cast(event.getParams());
                if (args == null) {
                    return message;
                }
                Integer[] hitIds = args.getHitBreakpointIds();
                if (hitIds == null || hitIds.length == 0 || !Objects.equals(bpId, hitIds[0])) {
                    return message;
                }
                DualCangjieXDebugProcess.this.unregisterDualInstructionBreakpoint();
                installPostStepInInteropFilter(messageAdapter, args.getThreadId());
                stepInAsync(args.getThreadId());
                return CodeCheckByPassUtils.getNull();
            }
        });
    }

    /**
     * install post stepIn interop filter
     *
     * @param messageAdapter messageAdapter
     * @param threadId thread id
     */
    private void installPostStepInInteropFilter(final MessageAdapter messageAdapter, final int threadId) {
        messageAdapter.addMessageFilter(new MessageFilter() {
            @Override
            public Message doFilter(Message message) {
                if (!(message instanceof DebugNotificationMessage)) {
                    return message;
                }
                DebugNotificationMessage event = (DebugNotificationMessage) message;
                if (!"stopped".equals(event.getMethod())) {
                    return message;
                }
                messageAdapter.getFilters().remove(this);

                final StoppedEventArguments args = CodeCheckByPassUtils.cast(event.getParams());
                if (args == null) {
                    return message;
                }
                final int tid = args.getThreadId();

                requestTopFrameNameAsync(tid, topName -> {
                    if (topName != null && topName.contains(INTEROP_WRAPPER_MARK)) {
                        String realName = deriveRealFunctionName(topName);
                        jumpIntoRealFunctionViaFunctionBp(realName, tid);
                    } else {
                        DualCangjieXDebugProcess.this.redeliverStoppedEvent(args);
                    }
                });
                return CodeCheckByPassUtils.getNull();
            }
        });
    }

    /**
     * redeliver stoppedEvent
     *
     * @param args stoppedEventArguments
     */
    private void redeliverStoppedEvent(final StoppedEventArguments args) {
        try {
            this.handleStoppedEvent(args);
        } catch (DapRuntimeException | IllegalStateException | NullPointerException | ClassCastException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "redeliverStoppedEvent error: " + e.getMessage());
        }
    }

    private void stepInAsync(int threadId) {
        StepInArguments stepInArgs = new StepInArguments();
        stepInArgs.setThreadId(threadId);
        (this.getToServerService()).getStepInRequester()
            .requestAsync(stepInArgs, STEP_IN_TIMEOUT_MS, new RequestCallbacks<>() {
                @Override
                public void whenError(Exception ex) {
                    LogUtils.printCangjieLogWarn(LOGGER, "fake step-in error after dual bp hit: " + ex.getMessage());
                }
            });
    }

    private void requestTopFrameNameAsync(int threadId, final java.util.function.Consumer<String> consumer) {
        StackTraceArguments args = new StackTraceArguments();
        args.setThreadId(threadId);
        args.setStartFrame(0);
        args.setLevels(1);
        (this.getToServerService())
            .getStackTraceRequester()
            .requestAsync(args, STACK_TRACE_TIMEOUT_MS, new RequestCallbacks<>() {
                @Override
                public void whenSuccess(StackTraceResponse resp) {
                    StackFrame[] frames = resp == null ? null : resp.getStackFrames();
                    consumer.accept((frames == null || frames.length == 0) ? null : frames[0].getName());
                }

                @Override
                public void whenError(Exception ex) {
                    LogUtils.printCangjieLogWarn(LOGGER,
                            "stackTrace error when detecting interop wrapper: " + ex.getMessage());
                    consumer.accept(null);
                }
            });
    }

    private void jumpIntoRealFunctionViaFunctionBp(final String realFuncName, final int threadId) {
        FunctionBreakpoint transientBp = new FunctionBreakpoint();
        transientBp.setName(realFuncName);
        List<FunctionBreakpoint> merged = buildUserFunctionBreakpoints();
        merged.add(transientBp);

        SetFunctionBreakpointsArguments fbArgs = new SetFunctionBreakpointsArguments();
        fbArgs.setBreakpoints(merged.toArray(new FunctionBreakpoint[0]));

        (this.getToServerService()).getSetFunctionBreakpointsRequester()
            .requestAsync(fbArgs, FUNCTION_BP_TIMEOUT_MS, new RequestCallbacks<>() {
                @Override
                public void whenSuccess(SetFunctionBreakpointsResponse resp) {
                    installRestoreUserFunctionBpFilter();
                    continueAsync(threadId);
                }

                @Override
                public void whenError(Exception ex) {
                    LogUtils.printCangjieLogWarn(LOGGER, "set transient function bp error: " + ex.getMessage());
                }
            });
    }

    private void continueAsync(int threadId) {
        ContinueArguments args = new ContinueArguments();
        args.setThreadId(threadId);
        (this.getToServerService()).getContinueRequester()
            .requestAsync(args, CONTINUE_TIMEOUT_MS, new RequestCallbacks<>() {
                @Override
                public void whenError(Exception ex) {
                    LogUtils.printCangjieLogWarn(LOGGER,
                            "continue error when jumping into real function: " + ex.getMessage());
                }
            });
    }

    private void installRestoreUserFunctionBpFilter() {
        final MessageAdapter messageAdapter = this.getDapConnection().getMessageAdapter();
        messageAdapter.addMessageFilter(new MessageFilter() {
            @Override
            public Message doFilter(Message message) {
                if (!(message instanceof DebugNotificationMessage)) {
                    return message;
                }
                DebugNotificationMessage event = (DebugNotificationMessage) message;
                if (!"stopped".equals(event.getMethod())) {
                    return message;
                }
                messageAdapter.getFilters().remove(this);
                DualCangjieXDebugProcess.this.resyncUserFunctionBreakpoints();
                return message;
            }
        });
    }

    private void resyncUserFunctionBreakpoints() {
        try {
            for (XBreakpointHandler<?> h : this.getBreakpointHandlers()) {
                if (h instanceof CangjieFunctionBreakpointHandler) {
                    @SuppressWarnings({"rawtypes"})
                    java.util.List empty = java.util.Collections.emptyList();
                    ((CangjieFunctionBreakpointHandler) h).doSendDapRequest(empty);
                    return;
                }
            }
            LogUtils.printCangjieLogWarn(LOGGER,
                    "CangjieFunctionBreakpointHandler not found, fallback to manual reset");
        } catch (DapRuntimeException | IllegalStateException | NullPointerException | ClassCastException e) {
            LogUtils.printCangjieLogWarn(LOGGER,
                    "resync user function breakpoints  error: " + e.getMessage());
        }
        SetFunctionBreakpointsArguments fbArgs = new SetFunctionBreakpointsArguments();
        fbArgs.setBreakpoints(buildUserFunctionBreakpoints().toArray(new FunctionBreakpoint[0]));
        (this.getToServerService()).getSetFunctionBreakpointsRequester()
            .requestAsync( fbArgs, FUNCTION_BP_TIMEOUT_MS, new RequestCallbacks<>() {});
    }

    private String deriveRealFunctionName(String wrapperFrameName) {
        int idx = wrapperFrameName.indexOf(INTEROP_WRAPPER_MARK);
        String head = wrapperFrameName.substring(0, idx);
        int sep = Math.max(head.lastIndexOf("::"), head.lastIndexOf('.'));
        if (sep >= 0) {
            head = head.substring(sep + (head.charAt(sep) == ':' ? 2 : 1));
        }
        return head;
    }

    private List<FunctionBreakpoint> buildUserFunctionBreakpoints() {
        final List<FunctionBreakpoint> result = new ArrayList<>();
        try {
            final Project project = this.getSession().getProject();
            ReadAction.nonBlocking(() -> {
                XBreakpointManager mgr = XDebuggerManager.getInstance(project).getBreakpointManager();
                XBreakpointType<XBreakpoint<CangjieFunctionBreakpointProperties>, CangjieFunctionBreakpointProperties>
                        type = XBreakpointType.EXTENSION_POINT_NAME.findExtension(CangjieFunctionBreakpointType.class);
                if (type == null) {
                    return CodeCheckByPassUtils.getNull();
                }
                for (XBreakpoint<?> xbp : mgr.getBreakpoints(type)) {
                    if (!xbp.isEnabled()) {
                        continue;
                    }
                    Object props = xbp.getProperties();
                    if (!(props instanceof CangjieFunctionBreakpointProperties)) {
                        continue;
                    }
                    FunctionBreakpoint fb = new FunctionBreakpoint();
                    fb.setName(((CangjieFunctionBreakpointProperties) props).getFunctionName());
                    result.add(fb);
                }
                return CodeCheckByPassUtils.getNull();
            }).executeSynchronously();
        } catch (NullPointerException | ClassCastException | IndexOutOfBoundsException | ProcessCanceledException e) {
            LogUtils.printCangjieLogWarn(LOGGER, "build user function breakpoints error: " + e.getMessage());
        }
        return result;
    }
}
