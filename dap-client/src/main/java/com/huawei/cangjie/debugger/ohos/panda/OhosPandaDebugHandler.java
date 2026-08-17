/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.panda;

import com.huawei.deveco.ace.debug.utils.AceDebuggerHelper;
import com.huawei.deveco.hdclib.ohos.devices.DebugClient;
import com.huawei.deveco.ohos.debugcommon.socket.forward.ArkTsPortForwardOperation;
import com.huawei.deveco.ohos.debugcommon.socket.forward.ForwardInfo;
import com.huawei.deveco.ohos.debugcommon.socket.forward.PortForwardManager;
import com.huawei.deveco.ohos.debugcommon.socket.forward.SocketPortForwardOperation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;

/**
 * When the application is started in debug mode(-D), the program continues to run after Cangjie debugging is complete.
 * create hdc fport
 *
 * @since 2023-11-7
 */
public class OhosPandaDebugHandler extends PandaDebugHandlerBase {
    private ArkTsPortForwardOperation openForwardOperation;
    private final Project myProject;

    /**
     * create websocket
     *
     * @param debugClient debugClient
     * @param tid         tid
     * @param project     project
     */
    public OhosPandaDebugHandler(DebugClient debugClient, String tid, Project project) {
        myProject = project;
        String softwareVersion = debugClient.getDevice().getSoftwareVersion();
        if (AceDebuggerHelper.isRomImageVersionFailed(project, softwareVersion)) {
            return;
        }
        boolean isNewSocketProtocol = AceDebuggerHelper.isNewSocketProtocol(softwareVersion);
        if (isNewSocketProtocol) {
            String serviceName = ForwardInfo.ServiceName.DEBUGGER.getServiceName();
            ForwardInfo forwardInfo = new ForwardInfo(project, pandaDebuggerServerPort,
                    debugClient.getDevice(), debugClient, serviceName)
                    .setTid(tid);
            openForwardOperation = new SocketPortForwardOperation(forwardInfo);
        } else {
            String serviceName = ForwardInfo.ServiceName.PANDA.getServiceName();
            ForwardInfo forwardInfo = new ForwardInfo(project, pandaDebuggerServerPort,
                    debugClient.getDevice(), debugClient, serviceName)
                    .setTid(tid);
            openForwardOperation = new ArkTsPortForwardOperation(forwardInfo);
        }
    }

    @Override
    public ArkTsPortForwardOperation getArkTsPortForwardOperation() {
        return openForwardOperation;
    }

    /**
     * delete hdc fport
     */
    @Override
    public void dispose() {
        super.dispose();
        ApplicationManager.getApplication().invokeLater(() -> {
            // close the IDE during debugging,
            // handleEventWhenStopSession is called after the IDE is closed
            if (myProject.isDisposed() || ProjectManager.getInstance().getOpenProjects().length == 0
                    || XDebuggerManagerImpl.getInstance(myProject).getDebugSessions().length == 0) {
                PortForwardManager.removeForward(myProject);
            }
        });
    }
}
