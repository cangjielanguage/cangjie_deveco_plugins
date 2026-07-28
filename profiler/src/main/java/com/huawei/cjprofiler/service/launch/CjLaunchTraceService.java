/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.service.launch;

import com.huawei.cjprofiler.common.constant.LaunchTraceConstants;
import com.huawei.cjprofiler.common.enums.CjLaunchPhaseEnum;
import com.huawei.deveco.insight.ohos.dao.LaunchTraceDao;
import com.huawei.deveco.insight.ohos.model.dto.request.launch.LaunchLifeCycleRequest;
import com.huawei.deveco.insight.ohos.model.po.LaunchTracePo;
import com.huawei.deveco.insight.ohos.service.event.LifeCycleEventCache;
import com.huawei.deveco.insight.ohos.service.event.LifeCycleEventCacheManager;
import com.huawei.deveco.insight.ohos.common.ProfilerException;
import com.huawei.deveco.insight.ohos.common.constant.SysEventConstants;
import com.huawei.deveco.insight.ohos.model.dto.LaunchData;
import com.huawei.deveco.insight.ohos.model.vo.LifeCycleEventVo;
import com.huawei.deveco.insight.ohos.service.launch.LaunchDataCache;
import com.huawei.deveco.insight.ohos.service.launch.LaunchDataCacheManager;
import com.huawei.deveco.insight.ohos.service.launch.LaunchTraceService;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;
import com.huawei.deveco.insight.ohos.utils.singleton.Singleton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * CjLaunchTraceService
 *
 * @since 2025-04-25
 */
@Singleton
public class CjLaunchTraceService extends LaunchTraceService {
    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjLaunchTraceService.class);

    /**
     * 纳秒/毫秒转换
     */
    private static final int NS_MS = 1000000;

    private final Map<String, Boolean> cjSessionCacheMap = new HashMap<>();

    /**
     * syncCacheLaunchLifeCycle
     *
     * @param launchLifeCycleRequest LaunchLifeCycleRequest
     */
    protected void syncCacheLaunchLifeCycle(LaunchLifeCycleRequest launchLifeCycleRequest) {
        launchCacheLock.lock();
        try {
            if (cjSessionCacheMap.containsKey(launchLifeCycleRequest.getSessionId())
                    && cjSessionCacheMap.get(launchLifeCycleRequest.getSessionId())) {
                return;
            }
            cjSessionCacheMap.put(launchLifeCycleRequest.getSessionId(),
                    cacheCjLaunchLifeCycleTrace(launchLifeCycleRequest));
        } finally {
            launchCacheLock.unlock();
        }
    }

    /**
     * Cangjie cacheLaunchLifeCycleTrace
     *
     * @param launchLifeCycleRequest request params
     * @return is cacheLaunchLifeCycleTrace success
     */
    public Boolean cacheCjLaunchLifeCycleTrace(LaunchLifeCycleRequest launchLifeCycleRequest) {
        List<String> taskNameList = List.of(LaunchTraceConstants.TRACE_START_ABILITY,
                LaunchTraceConstants.TRACE_START_ABILITY_LOCKED,
                LaunchTraceConstants.TRACE_LOAD_ABILITY,
                LaunchTraceConstants.TRACE_ATTACH_APPLICATION,
                LaunchTraceConstants.TRACE_LAUNCH_ABILITY,
                LaunchTraceConstants.TRACE_FOREGROUND_ABILITY,
                LaunchTraceConstants.TRACE_RECEIVE_VSYNC,
                LaunchTraceConstants.TRACE_APP_RENDERING,
                LaunchTraceConstants.TRACE_RS_RENDERING,
                LaunchTraceConstants.TRACE_CANGJIE_RUNTIME,
                LaunchTraceConstants.TRACE_LOAD_EXTENSION,
                LaunchTraceConstants.TRACE_HANDLE_LAUNCH,
                LaunchTraceConstants.TRACE_LOAD_CANGJIE_APP_LIBRARY,
                LaunchTraceConstants.TRACE_UI_START,
                LaunchTraceConstants.TRACE_ABILITY_TRANSACTION,
                LaunchTraceConstants.TRACE_APP_RENDERING_SEND_COMMANDS);
        List<LaunchTracePo> launchTracePoList;
        try {
            // 一次性查询相关的trace数据,避免多次查询数据库耗时
            launchTracePoList = LaunchTraceDao.getInstance()
                    .queryLaunchTraceList(launchLifeCycleRequest.getSessionId(), launchLifeCycleRequest.getStartTime(),
                            launchLifeCycleRequest.getEndTime(), taskNameList);
        } catch (ProfilerException e) {
            LOGGER.warn("Failed to queryCjLaunchTraceList, ProfilerException occurred: {}", e.getErrorMessage());
            return Boolean.FALSE;
        }
        if (launchTracePoList == null || launchTracePoList.isEmpty()) {
            LOGGER.warn("Failed to cacheCjLaunchLifeCycleTrace, launch trace po list is empty.");
            return Boolean.FALSE;
        }
        // 对一次性查询出来的数据进行解析后并进行缓存
        parseCjLaunchLifeCycle(launchLifeCycleRequest.getSessionId(), launchLifeCycleRequest.getBundleName(),
                launchLifeCycleRequest.getAbilityName(), launchLifeCycleRequest.getProcessId(), launchTracePoList);
        // 对MainAbility生命周期数据计算并进行缓存
        parseAbilityLifeCycle(launchLifeCycleRequest);
        return Boolean.TRUE;
    }

    private void parseCjLaunchLifeCycle(String sessionId, String bundleName, String abilityName, Integer processId,
                                        List<LaunchTracePo> launchTracePoList) {
        // 应用启动6个阶段的解析结果
        boolean[] hasTransitArray = new boolean[CjLaunchPhaseEnum.values().length];
        // 存储应用启动和trace事件有关的6个生命周期, 0表示Process Creating阶段, 依次类推
        LaunchData[] launchDataArray = new LaunchData[CjLaunchPhaseEnum.values().length];
        // 4个线程id
        int[] threadIdArray = new int[4];
        threadIdArray[2] = processId;
        boolean isSearchRenderServiceTid = false;
        List<LaunchTracePo> sendCommandsTracePoList = launchTracePoList.stream()
            .filter(launchTracePo -> Objects.equals(launchTracePo.getThreadId(), processId) && launchTracePo.getName()
                .contains(LaunchTraceConstants.TRACE_APP_RENDERING_SEND_COMMANDS))
            .toList();
        // trace可以会有丢失,每个阶段的trace解析不依赖于上一个阶段,所以会有重复解析的情况
        for (LaunchTracePo data : launchTracePoList) {
            if (!isSearchRenderServiceTid) {
                isSearchRenderServiceTid = searchRenderServiceTid(data, threadIdArray);
            }
            // 如果解析 Process Creating 成功, trace解析跳过该阶段
            if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_PROCESS_CREATING.ordinal()]) {
                hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_PROCESS_CREATING.ordinal()] =
                        parseCreateProcess(data, bundleName, launchDataArray, threadIdArray);
            }
            // 如果解析 Application Launching 成功, trace解析跳过该阶段
            if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_APPLICATION_LAUNCHING.ordinal()]) {
                hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_APPLICATION_LAUNCHING.ordinal()] =
                        parseApplicationLaunching(data, bundleName, hasTransitArray, launchDataArray, threadIdArray);
            }
            parseCjApplicationLaunchSubPhase(processId, data, hasTransitArray, launchDataArray);
            parseCjAbilityLaunchingPhase(processId, data, hasTransitArray, launchDataArray);
            parseCjAbilityOnForegoundPhase(abilityName, processId, data, hasTransitArray, launchDataArray);
            // 如果解析app首帧成功, trace解析跳过该阶段
            if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING.ordinal()]) {
                hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING.ordinal()] =
                        parseAppFirstFrameRending(sendCommandsTracePoList, processId, data, launchDataArray);
            }
            // rs帧依赖app帧的关联信息[processId, index]
            if (hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING.ordinal()]
                    && !hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING.ordinal()]) {
                hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING.ordinal()] =
                        parseRsFirstFrameRending(sessionId, data, launchDataArray);
            }
            if (hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING.ordinal()]) {
                // 找到了rs侧的首帧, 不需要继续解析后续的trace事件
                break;
            }
        }
        // 计算启动阶段的duration
        calcLaunchLifeCycleDuration(hasTransitArray, launchDataArray);
        // 缓存启动的生命周期数据,提供给后续接口获取相关信息
        cacheCjLaunchLifeCycle(sessionId, launchDataArray, hasTransitArray, threadIdArray);
    }

    private void parseCjAbilityOnForegoundPhase(String abilityName, Integer processId, LaunchTracePo data,
                                                boolean[] hasTransitArray, LaunchData[] launchDataArray) {
        // 如果解析 Ability Foreground 成功, trace解析跳过该阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND.ordinal()] =
                    parseCjUiAbilityForeground(processId, data, launchDataArray);
        }
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal()] =
                    parseCjAbilityLifecycleCallback(processId, abilityName, data, launchDataArray);
        }
        // 解析Ability Foreground阶段下子阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal()] =
                    parseReceiveVsync(processId, data, launchDataArray);
        }
    }

    private void parseCjAbilityLaunchingPhase(Integer processId, LaunchTracePo data, boolean[] hasTransitArray,
                                              LaunchData[] launchDataArray) {
        // 如果解析 Ability Launching 成功, trace解析跳过该阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING.ordinal()] =
                    parseCjUiAbilityLaunching(processId, data, launchDataArray);
        }
        // 解析Ability Launching阶段下的onCreate Execution子阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION.ordinal()] =
                    parseCjOnCreateExecution(processId, data, launchDataArray);
        }
    }

    private void parseCjApplicationLaunchSubPhase(Integer processId, LaunchTracePo data, boolean[] hasTransitArray,
                                                  LaunchData[] launchDataArray) {
        // 解析Application Launching阶段下的Runtime Initialization子阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION.ordinal()] =
                    parseRuntimeInitialization(processId, data, launchDataArray);
        }
        // 解析Application Launching阶段下的.so File Loading子阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOADING_SO_FILE.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOADING_SO_FILE.ordinal()] =
                    parseCjSoFileLoading(processId, data, launchDataArray);
        }
        // 解析Application Launching阶段下的Load Extension子阶段
        if (!hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_EXTENSION.ordinal()]) {
            hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_EXTENSION.ordinal()] =
                    parseCjLoadExtension(processId, data, launchDataArray);
        }
    }

    private boolean searchRenderServiceTid(LaunchTracePo data, int[] threadIdArray) {
        if (LaunchTraceConstants.PROCESS_RENDER_SERVICE.equals(data.getThreadName())) {
            threadIdArray[3] = data.getThreadId();
            return true;
        }
        return false;
    }

    private boolean parseCreateProcess(LaunchTracePo data, String bundleName, LaunchData[] launchDataArray,
                                       int[] threadIdArray) {
        if (data.getName().contains(LaunchTraceConstants.TRACE_START_ABILITY)) {
            // Process Creating阶段用StartAbility事件的时间
            setLaunchData(data, launchDataArray, threadIdArray);
            return false;
        }
        if (data.getName().contains(LaunchTraceConstants.TRACE_START_ABILITY_LOCKED)
                && data.getName().contains(bundleName)) {
            dealCreateProcess(data, launchDataArray, threadIdArray);
            return true;
        }
        if (data.getName().contains(LaunchTraceConstants.TRACE_LOAD_ABILITY)) {
            // StartAbilityLocked函数变更（KLV还保留）去掉，保持Process Creating阶段完整
            dealCreateProcess(data, launchDataArray, threadIdArray);
        }
        return false;
    }

    private void dealCreateProcess(LaunchTracePo data, LaunchData[] launchDataArray, int[] threadIdArray) {
        if (isSameEvent(data, launchDataArray)) {
            // 打点线程相同, 确认Process Creating阶段
            return;
        }
        // 没有找到对应startAbility的trace, 用当前阶段的时间
        setLaunchData(data, launchDataArray, threadIdArray);
    }

    private void setLaunchData(LaunchTracePo data, LaunchData[] launchDataArray, int[] threadIdArray) {
        launchDataArray[0] = createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_PROCESS_CREATING, data);
        threadIdArray[0] = data.getThreadId();
    }

    /**
     * 找到了StartAbility事件 && 和StartAbility是同一个线程打点 && 在StartAbility时间范围内
     *
     * @param data            data
     * @param launchDataArray launchDataArray
     * @return isSameEvent isSameEvent
     */
    private boolean isSameEvent(LaunchTracePo data, LaunchData[] launchDataArray) {
        return launchDataArray[0] != null && launchDataArray[0].getThreadId().equals(data.getThreadId())
                && data.getStartTime() < (launchDataArray[0].getStartTime() + launchDataArray[0].getTraceDuration());
    }

    private boolean parseApplicationLaunching(LaunchTracePo data, String bundleName, boolean[] hasTransitArray,
                                              LaunchData[] launchDataArray, int[] threadIdArray) {
        if (data.getName().contains(LaunchTraceConstants.TRACE_ATTACH_APPLICATION)
                && data.getName().contains(bundleName)) {
            if (launchDataArray[0] != null) {
                hasTransitArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_PROCESS_CREATING.ordinal()] = true;
            }
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_APPLICATION_LAUNCHING.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_APPLICATION_LAUNCHING, data);
            // Runtime Initialization起始点和Application Launching相同
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION, data);
            threadIdArray[1] = data.getThreadId();
            return true;
        }
        return false;
    }

    private boolean parseRuntimeInitialization(Integer processId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_CANGJIE_RUNTIME)) {
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOADING_SO_FILE.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_LOADING_SO_FILE, data);
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOADING_SO_FILE.ordinal()]
                    .setStartTime(data.getStartTime() + data.getDuration());
            // Runtime Initialization阶段的结束时间为Initialize cangjie runtime and namespace的结束点
            LaunchData runTimeData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION.ordinal()];
            runTimeData.setLifeCycleDuration(data.getStartTime() + data.getDuration() - runTimeData.getStartTime());
            return true;
        }
        return false;
    }

    private boolean parseCjSoFileLoading(Integer processId, LaunchTracePo data,
                                         LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_LOAD_CANGJIE_APP_LIBRARY)) {
            // .so File Loading阶段的结束时间和Load Extension 阶段的开始时间均为OHOS::AbilityRuntime::CJRuntime::LoadCJAppLibrary的结束点
            LaunchData soData = launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOADING_SO_FILE.ordinal()];
            soData.setLifeCycleDuration(data.getStartTime() + data.getDuration() - soData.getStartTime());
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_EXTENSION.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_LOAD_EXTENSION, data);
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_EXTENSION.ordinal()]
                    .setStartTime(data.getStartTime() + data.getDuration());
            return true;
        }
        return false;
    }

    private boolean parseCjLoadExtension(Integer processId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_LOAD_EXTENSION)) {
            // Load Extension 阶段的结束时间为H:void OHOS::AppExecFwk::MainThread::LoadAllExtensions的结束点
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION, data);
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION.ordinal()]
                    .setStartTime(data.getStartTime() + data.getDuration());
            // Load Extension 阶段的结束时间为H:void OHOS::AppExecFwk::MainThread::LoadAllExtensions的结束点
            LaunchData loadData = launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_EXTENSION.ordinal()];
            if (loadData != null) {
                loadData.setLifeCycleDuration(data.getStartTime() + data.getDuration() - loadData.getStartTime());
            }
            return true;
        }
        return false;
    }

    private boolean parseCjUiAbilityLaunching(Integer processId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_LAUNCH_ABILITY)) {
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING, data);
            // UI Ability Initialization的起始点和UI Ability Launching相同
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION, data);
            // Module Information Acquisition的的结束时间为H:void OHOS::AppExecFwk::MainThread::HandleLaunchAbility起始点
            LaunchData moduleInfoAcquisitionData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION.ordinal()];
            if (moduleInfoAcquisitionData != null) {
                moduleInfoAcquisitionData.setLifeCycleDuration(
                        data.getStartTime() - moduleInfoAcquisitionData.getStartTime());
            }
            return true;
        }
        return false;
    }

    private boolean parseCjOnCreateExecution(Integer processId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_UI_START)) {
            // onCreate Execution的开始时间和UI Ability Initialization的结束时间均为OHOS::AbilityRuntime::UIAbilityImpl::Start的起始点
            LaunchData uiAbilityInitializationData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION.ordinal()];
            uiAbilityInitializationData
                    .setLifeCycleDuration(data.getStartTime() - uiAbilityInitializationData.getStartTime());
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION, data);
            return true;
        }
        return false;
    }

    private boolean parseCjUiAbilityForeground(Integer processId, LaunchTracePo data,
                                               LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_ABILITY_TRANSACTION)) {
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND, data);
            // Ability Lifecycle Callback阶段的开始时间刷新
            LaunchData abilityData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal()];
            if (abilityData != null) {
                Long endTime = abilityData.getStartTime() + abilityData.getLifeCycleDuration();
                abilityData.setStartTime(data.getStartTime());
                abilityData.setLifeCycleDuration(endTime - abilityData.getStartTime());
            }
            LaunchData onCreateData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION.ordinal()];
            onCreateData.setLifeCycleDuration(data.getStartTime() - onCreateData.getStartTime());
            return true;
        }
        return false;
    }

    private boolean parseCjAbilityLifecycleCallback(Integer processId, String abilityName, LaunchTracePo data,
                                                    LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_FOREGROUND_ABILITY)
                && data.getName().contains(abilityName)) {
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK, data);
            return true;
        }
        return false;
    }

    private boolean parseReceiveVsync(Integer processId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (data.getThreadId().equals(processId)
                && data.getName().contains(LaunchTraceConstants.TRACE_RECEIVE_VSYNC)) {
            launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal()] =
                    createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE, data);
            // onCreate Execution的开始时间为和.so File Loading的结束时间均为H:SourceTextModule::Evaluate结束点
            LaunchData abilityData =
                    launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal()];
            if (abilityData != null) {
                launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal()]
                        .setStartTime(abilityData.getStartTime() + abilityData.getLifeCycleDuration());
                launchDataArray[CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal()]
                        .setLifeCycleDuration(data.getStartTime()
                                - (abilityData.getStartTime() + abilityData.getLifeCycleDuration()));
            }
            return true;
        }
        return false;
    }

    private boolean parseAppFirstFrameRending(List<LaunchTracePo> sendCommandsTracePoList, Integer processId,
        LaunchTracePo data, LaunchData[] launchDataArray) {
        if (!Objects.equals(data.getThreadId(), processId)) {
            return false;
        }
        if (data.getName().contains(LaunchTraceConstants.TRACE_RECEIVE_VSYNC)) {
            launchDataArray[4] = createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_FIRST_APP_FRAME_RENDERING, data);
            return false;
        }
        // 找到Receive Vsync事件 && 当前为MarshRSTransactionData 或 HybridRender 事件 && 当前事件在Receive Vsync时间范围内
        if (launchDataArray[4] != null && data.getName().contains(LaunchTraceConstants.TRACE_APP_RENDERING)
                && data.getStartTime() < (launchDataArray[4].getStartTime() + launchDataArray[4].getTraceDuration())) {
            // 判断是否在sendCommands下
            if (getSendCommands(data, sendCommandsTracePoList).isEmpty()) {
                return false;
            }
            // 判断是否包含帧关联信息
            Optional<String> opt = getFrameRelateKey(data.getName());
            if (opt.isEmpty()) {
                return false;
            }
            // 存MarshRSTransactionData里面的[processId, index]
            launchDataArray[4].setTraceName(opt.get());
            return true;
        }
        return false;
    }

    private Optional<LaunchTracePo> getSendCommands(LaunchTracePo data, List<LaunchTracePo> sendCommandsTracePoList) {
        for (LaunchTracePo launchTracePo : sendCommandsTracePoList) {
            if (!Objects.equals(launchTracePo.getThreadId(), data.getThreadId())) {
                continue;
            }
            if (!launchTracePo.getName().contains(LaunchTraceConstants.TRACE_APP_RENDERING_SEND_COMMANDS)) {
                continue;
            }
            Long startTime = data.getStartTime();
            Long endTime = startTime + data.getDuration();
            Long sendCCommandsStartTime = launchTracePo.getStartTime();
            Long sendCCommandsEndTime = sendCCommandsStartTime + launchTracePo.getDuration();
            if (startTime >= sendCCommandsStartTime && endTime <= sendCCommandsEndTime) {
                return Optional.of(launchTracePo);
            }
        }
        return Optional.empty();
    }

    private boolean parseRsFirstFrameRending(String sessionId, LaunchTracePo data, LaunchData[] launchDataArray) {
        if (!LaunchTraceConstants.PROCESS_RENDER_SERVICE.equals(data.getThreadName())) {
            return false;
        }
        if (data.getName().contains(LaunchTraceConstants.TRACE_RECEIVE_VSYNC)) {
            launchDataArray[5] = createLaunchData(LaunchTraceConstants.LIFE_CYCLE_NAME_FIRST_RS_FRAME_RENDERING, data);
            return false;
        }
        // 包含MarshRSTransactionData里面的[processId, index]的ProcessCommandUni
        if (launchDataArray[5] != null && data.getName().contains(launchDataArray[4].getTraceName())
                && data.getName().contains(LaunchTraceConstants.TRACE_RS_RENDERING)) {
            // 修正为对应frame的持续时间
            Long duration = getTraceFrameSliceDuration(sessionId, launchDataArray[5].getTraceName(),
                    launchDataArray[5].getThreadId(), launchDataArray[5].getStartTime());
            if (duration != null && !LaunchTraceConstants.MAX_LIFE_CYCLE_DURATION.equals(duration)) {
                launchDataArray[5].setLifeCycleDuration(duration);
            }
            return true;
        }
        return false;
    }

    private Long getTraceFrameSliceDuration(String sessionId, String traceName, Integer threadId, Long startTime) {
        try {
            return LaunchTraceDao.getInstance().queryFrameSliceDuration(sessionId, traceName, threadId, startTime);
        } catch (ProfilerException e) {
            LOGGER.warn("Failed to getTraceFrameSliceDuration, ProfilerException occurred: {}",
                    e.getErrorMessage());
            return LaunchTraceConstants.MAX_LIFE_CYCLE_DURATION;
        }
    }

    private void calcLaunchLifeCycleDuration(boolean[] hasTransitArray, LaunchData[] launchDataArray) {
        // app / rs frame rendering的duration已经单独计算过
        for (int i = 3; i >= 0; i--) {
            if (launchDataArray[i] == null) {
                continue;
            }
            if (launchDataArray[i].getLifeCycleDuration() < LaunchTraceConstants.MAX_LIFE_CYCLE_DURATION) {
                // duration比-1还要小, 说明下一个阶段早于当前, 需要抛弃
                launchDataArray[i] = null;
                continue;
            }
            if (!hasTransitArray[i]
                && !LaunchTraceConstants.MAX_LIFE_CYCLE_DURATION.equals(launchDataArray[i].getLifeCycleDuration())) {
                // 未解析成功 && 持续时长不为-1, 使用默认的持续时长
                continue;
            }
            for (int j = i + 1; j < 5; j++) {
                if (launchDataArray[j] == null) {
                    continue;
                }
                // 当前阶段持续时间为下一个阶段的开始时间
                launchDataArray[i]
                        .setLifeCycleDuration(launchDataArray[j].getStartTime() - launchDataArray[i].getStartTime());
                if (launchDataArray[i].getLifeCycleDuration() < 0) {
                    launchDataArray[i] = null;
                }
                break;
            }
        }
    }

    private void cacheCjLaunchLifeCycle(String sessionId, LaunchData[] launchDataArray, boolean[] hasTransitArray,
                                        int[] threadIdArray) {
        if (!hasTransitArray[4]) {
            launchDataArray[4] = null;
        }
        if (!hasTransitArray[5]) {
            launchDataArray[5] = null;
        }
        LaunchDataCache launchDataCache = LaunchDataCacheManager.getInstance().getLaunchDataCache(sessionId);
        List<LaunchData> list = new ArrayList<>();
        for (int i = 0; i < LaunchTraceConstants.LAUNCH_MAIN_PHASE_NUM; i++) {
            LaunchData data = launchDataArray[i];
            // 如果trace有丢失, 对应的生命周期阶段没有数据
            if (data == null) {
                continue;
            }
            cacheCjLaunchSubPhaseData(launchDataArray, i, data);
            list.add(data);
        }
        // 缓存trace解析出来的生命周期
        launchDataCache.addLaunchDataList(list);
        launchDataCache.setThreadIdList(threadIdArray);
    }

    private static void cacheCjLaunchSubPhaseData(LaunchData[] launchDataArray, int index, LaunchData data) {
        if (index == CjLaunchPhaseEnum.LIFE_CYCLE_NAME_APPLICATION_LAUNCHING.ordinal()) {
            List<LaunchData> children = new ArrayList<>();
            for (int j = CjLaunchPhaseEnum.LIFE_CYCLE_NAME_RUNTIME_INITIALIZATION.ordinal();
                j <= CjLaunchPhaseEnum.LIFE_CYCLE_NAME_MODULE_INFORMATION_ACQUISITION.ordinal(); j++) {
                if (launchDataArray[j] == null) {
                    continue;
                }
                children.add(launchDataArray[j]);
            }
            data.setChildren(children);
        }
        if (index == CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_LAUNCHING.ordinal()) {
            List<LaunchData> children = new ArrayList<>();
            for (int j = CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_INITIALIZATION.ordinal();
                j <= CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ON_CREATE_EXECUTION.ordinal(); j++) {
                if (launchDataArray[j] == null) {
                    continue;
                }
                children.add(launchDataArray[j]);
            }
            data.setChildren(children);
        }
        if (index == CjLaunchPhaseEnum.LIFE_CYCLE_NAME_UI_ABILITY_ON_FOREGROUND.ordinal()) {
            List<LaunchData> children = new ArrayList<>();
            for (int j = CjLaunchPhaseEnum.LIFE_CYCLE_NAME_ABILITY_LIFECYCLE_CALLBACK.ordinal();
                j <= CjLaunchPhaseEnum.LIFE_CYCLE_NAME_LOAD_CONTENT_AND_PAGE.ordinal(); j++) {
                if (launchDataArray[j] == null) {
                    continue;
                }
                children.add(launchDataArray[j]);
            }
            data.setChildren(children);
        }
    }

    private LaunchData createLaunchData(String lifeCycleName, LaunchTracePo data) {
        return new LaunchData(lifeCycleName, data.getStartTime(), data.getDuration(), data.getName(),
                data.getDuration(), data.getThreadId(), null);
    }

    private Optional<String> getFrameRelateKey(@NotNull String name) {
        Matcher matcher = LaunchTraceConstants.PATTERN_FRAME_TRACE_RELATE.matcher(name);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private void parseAbilityLifeCycle(LaunchLifeCycleRequest launchLifeCycleRequest) {
        Optional<LifeCycleEventCache> lifeCycleEventCacheOpt = LifeCycleEventCacheManager.getInstance()
                .getLifeCycleEventCache(launchLifeCycleRequest.getSessionId());
        if (lifeCycleEventCacheOpt.isEmpty()) {
            LOGGER.warn("Failed calcAbilityLifeCycle, LifeCycleEventCache not exist.");
            return;
        }
        List<LaunchData> launchDataList = LaunchDataCacheManager.getInstance()
                .getLaunchDataCache(launchLifeCycleRequest.getSessionId()).getLaunchDataLinkedList();
        if (launchDataList == null || launchDataList.isEmpty()) {
            LOGGER.warn("Failed calcAbilityLifeCycle, launchDataList is empty.");
            return;
        }
        LaunchData lastTraceLaunchData = launchDataList.get(launchDataList.size() - 1);
        // 前端传入的开始泳道开始录制的时间 单位为毫秒
        Long startRecordTime = launchLifeCycleRequest.getStartRecordTime();
        // 将查询时间(相对时间轴的纳秒时间)转换为pc时间(单位为毫秒)
        Long msStartTime = getMsTimeByNsWithStartRecord(startRecordTime,
                lastTraceLaunchData.getStartTime() + lastTraceLaunchData.getLifeCycleDuration());
        Long msEndTime = getMsTimeByNsWithStartRecord(startRecordTime, launchLifeCycleRequest.getEndTime());
        List<LifeCycleEventVo> lifeCycleEventVoList = lifeCycleEventCacheOpt.get().queryData(msStartTime, msEndTime,
                launchLifeCycleRequest.getBundleName());

        LaunchDataCacheManager.getInstance().getLaunchDataCache(launchLifeCycleRequest.getSessionId())
                .addLaunchDataList(calcAbilityLifeCycle(launchLifeCycleRequest,
                        lastTraceLaunchData, lifeCycleEventVoList));
    }

    private List<LaunchData> calcAbilityLifeCycle(LaunchLifeCycleRequest launchLifeCycleRequest,
                                                  LaunchData lastTraceLaunchData,
                                                  List<LifeCycleEventVo> lifeCycleEventVoList) {
        Long startRecordTime = launchLifeCycleRequest.getStartRecordTime();
        String mainAbilityName = launchLifeCycleRequest.getAbilityName();
        Long endTime = getMsTimeByNsWithStartRecord(startRecordTime, launchLifeCycleRequest.getEndTime());
        Integer processId = launchLifeCycleRequest.getProcessId();
        String lastAbilityLifeCycleName = "";
        List<LaunchData> abilityLaunchList = new ArrayList<>();
        if (lifeCycleEventVoList != null && lifeCycleEventVoList.size() > 0) {
            // 倒序计算ability生命周期持续时长
            for (int i = lifeCycleEventVoList.size() - 1; i >= 0; i--) {
                LifeCycleEventVo data = lifeCycleEventVoList.get(i);
                // 过滤非MainAbility的生命周期
                if (data.getAbilityName() == null || !data.getAbilityName().equals(mainAbilityName)) {
                    continue;
                }
                abilityLaunchList.add(createAbilityLaunchData(data, startRecordTime, endTime, processId));
                endTime = data.getTimestamp();
                lastAbilityLifeCycleName = data.getName();
            }
        }
        Long startTime = lastTraceLaunchData.getStartTime() + lastTraceLaunchData.getLifeCycleDuration();
        Long duration = (endTime - startRecordTime) * NS_MS - startTime;
        // 补充一个OnForeground生命周期
        String lifeCycleName = mainAbilityName;
        if (SysEventConstants.SYS_EVENT_NAME_ABILITY_ONFOREGROUND.equals(lastAbilityLifeCycleName)) {
            int size = abilityLaunchList.size();
            if (size > 0) {
                abilityLaunchList.get(size - 1).setStartTime(startTime);
                long abilityDuration = duration + abilityLaunchList.get(size - 1).getTraceDuration();
                abilityLaunchList.get(size - 1).setLifeCycleDuration(abilityDuration);
                abilityLaunchList.get(size - 1).setTraceDuration(abilityDuration);
            }
        } else {
            abilityLaunchList.add(new LaunchData(lifeCycleName, startTime, duration, "",
                    duration, processId, null));
        }
        Collections.reverse(abilityLaunchList);
        return abilityLaunchList;
    }

    private LaunchData createAbilityLaunchData(LifeCycleEventVo lifeCycleEventVo, Long startRecordTime,
                                               Long endTime, Integer processId) {
        String lifeCycleName = lifeCycleEventVo.getAbilityName();
        if (!SysEventConstants.SYS_EVENT_NAME_ABILITY_ONFOREGROUND.equals(lifeCycleEventVo.getName())) {
            // 转换实时监控显示应用生命周期
            switch (lifeCycleEventVo.getName()) {
                case SysEventConstants.SYS_EVENT_NAME_START_ABILITY:
                    lifeCycleName = LaunchTraceConstants.LIFE_CYCLE_NAME_START;
                    break;
                case SysEventConstants.SYS_EVENT_NAME_ABILITY_ONBACKGROUND:
                    lifeCycleName = LaunchTraceConstants.LIFE_CYCLE_NAME_BACKGROUND;
                    break;
                case SysEventConstants.SYS_EVENT_NAME_TERMINATE_ABILITY:
                    lifeCycleName = LaunchTraceConstants.LIFE_CYCLE_NAME_TERMINATE;
                    break;
                case SysEventConstants.SYS_EVENT_NAME_ABILITY_ONINACTIVE:
                    lifeCycleName = LaunchTraceConstants.LIFE_CYCLE_NAME_INACTIVE;
                    break;
                case SysEventConstants.SYS_EVENT_NAME_ABILITY_ONACTIVE:
                    lifeCycleName = LaunchTraceConstants.LIFE_CYCLE_NAME_ACTIVE;
                    break;
            }
        }
        Long duration = (endTime - lifeCycleEventVo.getTimestamp()) * NS_MS;
        return new LaunchData(lifeCycleName, (lifeCycleEventVo.getTimestamp() - startRecordTime) * NS_MS,
                duration, "", duration, processId, null);
    }

    private Long getMsTimeByNsWithStartRecord(Long startRecordTime, Long nsTime) {
        return startRecordTime + nsTime / NS_MS;
    }

    /**
     * clean session cache
     *
     * @param sessionId session id
     */
    public void cleanSessionCache(String sessionId) {
        cjSessionCacheMap.remove(sessionId);
    }
}
