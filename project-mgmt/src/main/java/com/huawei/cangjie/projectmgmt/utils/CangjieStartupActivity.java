/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.huawei.cangjie.projectmgmt.listener.CangjieExecutionManagerListener;
import com.huawei.cangjie.projectmgmt.trace.TraceKind;
import com.huawei.cangjie.projectmgmt.trace.TraceUtils;
import com.huawei.deveco.build.ohos.util.ModuleUtils;
import com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;

import com.intellij.execution.ExecutionManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CangjieStartupActivity
 *
 * @since 2024/12/09
 */
public class CangjieStartupActivity implements StartupActivity {
    private static final String BUILD_APP_ID = "OhosBuildAppAction";
    private static final String BUILD_HAP_ID = "OhosBuildHapAction";
    private static final String MAKE_MODULE_ID = "com.huawei.deveco.build.ohos.actions.MakeModuleAction";
    private static final AtomicBoolean LISTENER_REGISTERED = new AtomicBoolean(false);

    @Override
    public void runActivity(@NotNull Project project) {
        if (LISTENER_REGISTERED.compareAndSet(false, true)) {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(
                    AnActionListener.TOPIC, new AnActionListener() {
                        @Override
                        public void beforeActionPerformed(AnAction action, AnActionEvent event) {
                            ApplicationManager.getApplication().executeOnPooledThread(
                                    () -> compileBuildTrace(action, event));
                        }
                    });
        }
        Disposable projectDisposable = Disposer.newDisposable(project, "CangjiePluginConnectionsDisposable");
        MessageBusConnection connection = project.getMessageBus().connect(projectDisposable);
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, new CangjieExecutionManagerListener(project));
    }

    private void compileBuildTrace(AnAction action, AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        ProjectModel projectModel = CommonProjectUtil.getProjectModel(project);
        if (!FileUtils.isCangjieProject(projectModel)) {
            return;
        }
        String id = event.getActionManager().getId(action);
        if (BUILD_APP_ID.equals(id)) {
            TraceUtils.trace(TraceKind.CJ_COMPILE_APP);
        }
        if (BUILD_HAP_ID.equals(id)) {
            TraceUtils.trace(TraceKind.CJ_COMPILE_HAP);
        }
        if (MAKE_MODULE_ID.equals(id)) {
            VirtualFile[] virtualFilesArray = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            if (virtualFilesArray == null || virtualFilesArray.length == 0) {
                return;
            }
            List<ModuleModel> selectedModulesList = ModuleUtils.getSelectedModulesList(project, virtualFilesArray);
            if (selectedModulesList.isEmpty()) {
                selectedModulesList = projectModel.getModuleModelList();
            }
            boolean isContainsHap = false;
            boolean isContainsHar = false;
            for (ModuleModel model : selectedModulesList) {
                if (ModuleType.ENTRY.toString().equalsIgnoreCase(model.getModuleType())
                        || ModuleType.FEATURE.toString().equalsIgnoreCase(model.getModuleType())) {
                    isContainsHap = true;
                } else {
                    isContainsHar = true;
                }
            }
            if (isContainsHap) {
                TraceUtils.trace(TraceKind.CJ_COMPILE_HAP);
            }
            if (isContainsHar) {
                TraceUtils.trace(TraceKind.CJ_COMPILE_HAR);
            }
        }
    }
}
