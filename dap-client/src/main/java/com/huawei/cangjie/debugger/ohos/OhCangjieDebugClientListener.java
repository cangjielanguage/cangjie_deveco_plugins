/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos;

import com.huawei.bitfun.intellij.ex.DapXDebugProcess;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;
import com.huawei.deveco.hdclib.ohos.devices.Devices;
import com.huawei.deveco.hdclib.ohos.hdc.HarmonyDebugConnector;

import com.intellij.openapi.diagnostic.Logger;

import java.util.ListIterator;
import java.util.Vector;

/**
 * OhCppDebugClientListener
 *
 * @since 2024-3-12
 */
public class OhCangjieDebugClientListener implements HarmonyDebugConnector.IJSChangeListener {
    private static final OhCangjieDebugClientListener INSTANCE = new OhCangjieDebugClientListener();

    private static final Logger LOGGER = Logger.getInstance(OhCangjieDebugClientListener.class);

    private Vector<AppStatus> appStatusList = new Vector<>();

    /**
     * getInstance
     *
     * @return DebugClientListener instance
     */
    public static OhCangjieDebugClientListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void jsDebugAdd(Devices device, String pid, String packageName) {
        CodeCheckByPassUtils.doNothing();
    }

    /**
     * addToAppStatusList
     *
     * @param device device
     * @param pid pid
     * @param dapXDebugProcess dapXDebugProcess
     */
    public final void addToAppStatusList(Devices device, String pid, DapXDebugProcess<?, ?> dapXDebugProcess) {
        boolean isContain = false;
        for (AppStatus appStatus : appStatusList) {
            if (appStatus.device == device && appStatus.pid.equals(pid)) {
                isContain = true;
                break;
            }
        }
        if (!isContain) {
            LogUtils.printCangjieLogInfo(LOGGER, "add pid to AppStatus: " + pid);
            appStatusList.add(new AppStatus(device, pid, dapXDebugProcess));
        }
    }

    @Override
    public void jsDebugRemove(Devices device, String pid, String packageName) {
        ListIterator<AppStatus> iterator = appStatusList.listIterator();
        while (iterator.hasNext()) {
            AppStatus appStatus = iterator.next();
            boolean isRemove = appStatus.device == device && appStatus.pid.equals(pid);
            if (isRemove) {
                LogUtils.printCangjieLogInfo(LOGGER, "remove pid from AppStatus: " + pid);
                iterator.remove();
                appStatus.dapXDebugProcess.stopXDebugSession(false);
                break;
            }
        }
    }

    /**
     * isDebuggeeCrashed
     *
     * @param device device
     * @param pid    pid
     * @return is app crashed
     */
    public boolean isDebuggeeCrashed(Devices device, String pid) {
        return appStatusList.stream().noneMatch(appStatus -> appStatus.device == device && appStatus.pid.equals(pid));
    }


    private static class AppStatus {
        private final Devices device;

        private final String pid;

        private final DapXDebugProcess<?, ?> dapXDebugProcess;

        AppStatus(Devices device, String packageName, DapXDebugProcess<?, ?> dapXDebugProcess) {
            this.device = device;
            this.pid = packageName;
            this.dapXDebugProcess = dapXDebugProcess;
        }
    }
}
