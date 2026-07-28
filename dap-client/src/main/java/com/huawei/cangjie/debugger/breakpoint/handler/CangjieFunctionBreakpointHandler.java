/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.handler;

import com.huawei.bitfun.DapFromServerService;
import com.huawei.bitfun.DapToServerService;
import com.huawei.bitfun.intellij.breakpoint.handler.BreakpointHandlerBase;
import com.huawei.bitfun.intellij.breakpoint.utils.BreakpointUpdate;
import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.intellij.utils.EdtRequestCallbacks;
import com.huawei.bitfun.intellij.utils.IntellijThreadUtils;
import com.huawei.bitfun.protocol.extend.FunctionBreakpoint;
import com.huawei.bitfun.protocol.extend.SetFunctionBreakpointsArguments;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.cangjie.debugger.breakpoint.properties.CangjieFunctionBreakpointProperties;

import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.SetFunctionBreakpointsResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * cangjie function breakpoint handler
 *
 * @since 2022-11-26
 */
public class CangjieFunctionBreakpointHandler extends
    BreakpointHandlerBase<XBreakpoint<CangjieFunctionBreakpointProperties>, CangjieFunctionBreakpointProperties> {
    public CangjieFunctionBreakpointHandler(
        Class<? extends XBreakpointType<XBreakpoint<CangjieFunctionBreakpointProperties>, ?>> bpTypeClass,
        DapXDebugProcess<? extends DapFromServerService, ? extends DapToServerService<?>> process) {
        super(bpTypeClass, process);
    }

    @Override
    public void doSendDapRequest(
        List<BreakpointUpdate<XBreakpoint<CangjieFunctionBreakpointProperties>>> pendingUpdates) {
        XDebuggerManager xDebuggerManager = XDebuggerManager.getInstance(debugProcess.getSession().getProject());
        List<XBreakpoint<CangjieFunctionBreakpointProperties>> bpsInManager = IntellijThreadUtils.invokeAndGet(
            () -> new ArrayList<>(xDebuggerManager.getBreakpointManager().getBreakpoints(getBreakpointTypeClass())));
        debugProcess.getToServerService()
            .getSetFunctionBreakpointsRequester()
            .requestAsync(createSetFunctionBreakpointArgs(bpsInManager), debugProcess.getTimeouts().setBreakpoints(),
                new EdtRequestCallbacks<>() {
                    @Override
                    public void whenSuccessEdt(SetFunctionBreakpointsResponse setFunctionBreakpointsResponse) {
                        handleSetFunctionBreakpointsResponse(setFunctionBreakpointsResponse, bpsInManager);
                    }

                    @Override
                    public void whenErrorEdt(Exception ex) {
                        debugProcess.reportError("set function breakpoint error:" + ExceptionUtils.getNonNullMsg(ex));
                    }
                });
    }

    private void handleSetFunctionBreakpointsResponse(SetFunctionBreakpointsResponse setFunctionBreakpointsResponse,
        List<XBreakpoint<CangjieFunctionBreakpointProperties>> bpsInManager) {
        Breakpoint[] breakpoints = setFunctionBreakpointsResponse.getBreakpoints();
        List<Integer> bpsInManagerEnableIndex = bpsInManager.stream()
                .filter(XBreakpoint::isEnabled)
                .map(bpsInManager::indexOf)
                .toList();
        if (bpsInManagerEnableIndex.size() != breakpoints.length) {
            debugProcess.reportError("set function breakpoints response doesn't match request size");
            return;
        }
        for (int i = 0; i < breakpoints.length; i++) {
            Integer id = breakpoints[i].getId();
            if (id != null) {
                bpsInManager.get(bpsInManagerEnableIndex.get(i)).putUserData(DAP_BREAKPOINT_ID, id);
            }
        }
    }

    /**
     * createSetFunctionBreakpointsArguments
     *
     * @param bpsInManager allFunctionBreakpoints
     * @return SetFunctionBreakpointsArguments
     */
    private SetFunctionBreakpointsArguments createSetFunctionBreakpointArgs(
        List<XBreakpoint<CangjieFunctionBreakpointProperties>> bpsInManager) {
        List<FunctionBreakpoint> functionBreakpointList = new ArrayList<>();
        Boolean isSupportsConditionalBreakpoints =
            debugProcess.getServerCapabilities().getSupportsConditionalBreakpoints();
        for (XBreakpoint<CangjieFunctionBreakpointProperties> xBreakpoint : bpsInManager) {
            CangjieFunctionBreakpointProperties properties = xBreakpoint.getProperties();
            if (properties != null && xBreakpoint.isEnabled()) {
                FunctionBreakpoint functionBreakpoint = new FunctionBreakpoint();
                functionBreakpoint.setName(properties.getFunctionName());
                XExpression conditionExpression = xBreakpoint.getConditionExpression();
                if (conditionExpression != null && isSupportsConditionalBreakpoints) {
                    functionBreakpoint.setCondition(conditionExpression.getExpression());
                }
                if (properties.isHitCountEnabled()) {
                    functionBreakpoint.setHitCondition(properties.getHitCount());
                }
                functionBreakpointList.add(functionBreakpoint);
            }
        }
        SetFunctionBreakpointsArguments arguments = new SetFunctionBreakpointsArguments();
        arguments.setBreakpoints(functionBreakpointList.toArray(FunctionBreakpoint[]::new));
        return arguments;
    }
}
