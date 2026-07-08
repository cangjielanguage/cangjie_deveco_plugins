/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.huawei.cangjie.projectmgmt.sync.dts2cj.Dts2cjViewManager;
import com.huawei.deveco.common.buildsupport.processhandler.DevEcoColoredProcessHandler;

import com.intellij.build.BuildBundle;
import com.intellij.build.BuildContentDescriptor;
import com.intellij.build.DefaultBuildDescriptor;
import com.intellij.build.events.impl.FailureResultImpl;
import com.intellij.build.events.impl.FinishBuildEventImpl;
import com.intellij.build.events.impl.StartBuildEventImpl;
import com.intellij.build.events.impl.SuccessResultImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.lang.LangBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import com.intellij.util.text.DateFormatUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Dts 2 cj exec util.
 *
 * @since 2025 -01-19
 */
public class Dts2cjExecUtil {
    private static final Logger LOGGER = Logger.getInstance(Dts2cjExecUtil.class);

    private final Project project;

    /**
     * Instantiates a new Dts 2 cj exec util.
     *
     * @param project the project
     */
    public Dts2cjExecUtil(Project project) {
        this.project = project;
    }

    /**
     * Run with console boolean.
     *
     * @param cmdLine the cmd line
     * @param name the name
     * @return the boolean
     */
    public boolean runWithConsole(GeneralCommandLine cmdLine, String name) {
        DevEcoColoredProcessHandler processHandler;
        try {
            processHandler = new DevEcoColoredProcessHandler(cmdLine);
            processHandler.setShouldKillProcessSoftly(false);
        } catch (ExecutionException exception) {
            LOGGER.error("Exec Dts2cj failed: " + exception.getMessage());
            printExceptionMessageToConsoleView(exception, name);
            return false;
        }
        ProcessTerminatedListener.attach(processHandler);
        AtomicBoolean isDts2cjNormalExit = new AtomicBoolean(false);
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                isDts2cjNormalExit.set(event.getExitCode() == 0);
            }
        });

        ConsoleViewImpl consoleView = new ConsoleViewImpl(project, GlobalSearchScope.allScope(project), true, true);
        consoleView.attachToProcess(processHandler);
        String contentName = name + " [Generate Cangjie Bindings]";
        ExternalSystemTaskId id =
            ExternalSystemTaskId.create(ProjectSystemId.IDE, ExternalSystemTaskType.EXECUTE_TASK, project);
        ProcessHandler finalProcessHandler = processHandler;
        DefaultBuildDescriptor buildDescriptor =
            createBuildDescriptor(finalProcessHandler, id, contentName, consoleView);
        Dts2cjViewManager syncViewManager = project.getService(Dts2cjViewManager.class);
        return runWithConsoleProcess(runWithConsoleAddProcess(processHandler, syncViewManager, id, buildDescriptor),
            name, isDts2cjNormalExit);
    }

    private DefaultBuildDescriptor createBuildDescriptor(@NotNull ProcessHandler processHandler,
        ExternalSystemTaskId id, String contentName, ConsoleViewImpl consoleView) {
        return new DefaultBuildDescriptor(id, contentName,
            PathUtil.toSystemDependentName(StringUtil.notNullize(project.getBasePath())),
            System.currentTimeMillis()).withContentDescriptor(() -> {
            BuildContentDescriptor buildContentDescriptor =
                new BuildContentDescriptor(consoleView, processHandler, consoleView.getComponent(),
                    "Cangjie Bindings Output");
            buildContentDescriptor.setActivateToolWindowWhenAdded(true);
            buildContentDescriptor.setActivateToolWindowWhenFailed(true);
            return buildContentDescriptor;
        });
    }

    private void printExceptionMessageToConsoleView(Exception exception, String name) {
        ConsoleViewImpl consoleView = new ConsoleViewImpl(project, GlobalSearchScope.allScope(project), true, true);
        consoleView.print(exception.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
        String contentName = name + " [Generate Cangjie Bindings]";
        ExternalSystemTaskId id =
            ExternalSystemTaskId.create(ProjectSystemId.IDE, ExternalSystemTaskType.EXECUTE_TASK, project);
        DefaultBuildDescriptor buildDescriptor =
            createBuildDescriptor(new NopProcessHandler(), id, contentName, consoleView);
        Dts2cjViewManager syncViewManager = project.getService(Dts2cjViewManager.class);
        syncViewManager.onEvent(id,
            new StartBuildEventImpl(buildDescriptor, BuildBundle.message("build.status.running")));
        syncViewManager.onEvent(id, createFailedFinishEvent(id));
    }

    private FinishBuildEventImpl createFailedFinishEvent(ExternalSystemTaskId id) {
        String eventTime = DateFormatUtil.formatDateTime(System.currentTimeMillis());
        return new FinishBuildEventImpl(id, null, System.currentTimeMillis(),
            BuildBundle.message("build.status.failed") + " " + LangBundle.message("build.event.message.at", eventTime),
            new FailureResultImpl());
    }

    private ProcessHandler runWithConsoleAddProcess(ProcessHandler processHandler, Dts2cjViewManager syncViewManager,
        ExternalSystemTaskId id, DefaultBuildDescriptor buildDescriptor) {
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                syncViewManager.onEvent(id,
                    new StartBuildEventImpl(buildDescriptor, BuildBundle.message("build.status.running")));
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                String eventTime = DateFormatUtil.formatDateTime(System.currentTimeMillis());
                if (event.getExitCode() == 0) {
                    syncViewManager.onEvent(id, new FinishBuildEventImpl(id, null, System.currentTimeMillis(),
                        BuildBundle.message("build.event.message.successful") + " " + LangBundle.message(
                            "build.event.message.at", eventTime), new SuccessResultImpl()));
                } else {
                    syncViewManager.onEvent(id, createFailedFinishEvent(id));
                }
            }
        });
        return processHandler;
    }

    private boolean runWithConsoleProcess(ProcessHandler processHandler, String name,
        AtomicBoolean isDts2cjNormalExit) {
        LOGGER.info(String.format(Locale.ENGLISH, "Dts2cj started for module %s.", name));
        final long start = System.currentTimeMillis();
        processHandler.startNotify();
        boolean isProcessEnded = processHandler.waitFor();
        if (isProcessEnded) {
            LOGGER.info(String.format(Locale.ENGLISH, "Dts2cj executed for module %s in %s ms.", name,
                (System.currentTimeMillis() - start)));
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
            return isDts2cjNormalExit.get();
        }
        LOGGER.warn(String.format(Locale.ENGLISH, "Exec dts2cj failed for module %s in %s ms.", name,
            (System.currentTimeMillis() - start)));
        return false;
    }
}
