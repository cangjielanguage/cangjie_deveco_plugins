/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.COMPILE_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CUSTOMIZED_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.DEBUG;
import static com.huawei.cangjie.projectmgmt.utils.Constants.KEY_PROFILE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PACKAGE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.RELEASE;
import static com.huawei.cangjie.projectmgmt.utils.FileUtils.getCangjiePackageCompileOption;
import static com.huawei.cangjie.projectmgmt.utils.FileUtils.getTomlUnitMap;

import com.huawei.cangjie.projectmgmt.extend.toml.CJPMTomlEncoder;
import com.huawei.cangjie.projectmgmt.extend.toml.Toml;
import com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider;
import com.huawei.cangjie.projectmgmt.sync.upgrade.vo.TomlUpdateTypeData;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.util.ModuleType;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The Cangjie compile condition update.
 *
 * @since 2025-06-10
 */
public class CangjieCfgUpdateImpl implements CangjieUpdateProvider {
    private static final Logger LOG = Logger.getInstance(CangjieCfgUpdateImpl.class);

    private static final String COMPILE_CONDITION = "COMPILE_CONDITION";

    private static final String APILEVEL_CHECK = "-Won apilevel-check";

    private Set<String> msgList = new HashSet<>();

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        List<ModuleModel> modelList = projectModel.getModuleModelList();
        if (modelList == null || modelList.isEmpty()) {
            return false;
        }
        for (ModuleModel moduleModel : modelList) {
            if (isNeedUpdate(moduleModel, false) || isNeedUpdate(moduleModel, true)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.compile.condition.update.text"), msgList);
        return updateMsgs;
    }

    @Override
    public boolean doUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return false;
        }
        for (ModuleModel moduleModel : moduleModelList) {
            if (isNeedUpdate(moduleModel, false) && !updateToml(moduleModel, false)) {
                NotificationUtil.notifyInfo(message("upgrade.compile.condition.task.fail",
                                moduleModel.getModuleName()), projectModel.getProject(), NotificationType.ERROR);
            }
            if (isNeedUpdate(moduleModel, true) && !updateToml(moduleModel, true)) {
                NotificationUtil.notifyInfo(message("upgrade.compile.condition.task.fail",
                                moduleModel.getModuleName() + "_ohosTest"),
                        projectModel.getProject(), NotificationType.ERROR);
            }
        }
        return true;
    }

    private boolean isNeedUpdate(ModuleModel moduleModel, boolean isOhosTest) {
        if (!FileUtils.isCangjieModule(moduleModel)) {
            return false;
        }
        Path tomlPath = getTomlPath(moduleModel, isOhosTest);
        if (!tomlPath.toFile().exists() || !(moduleModel instanceof OhosModuleModel)) {
            return false;
        }
        String compileOption = getCangjiePackageCompileOption((OhosModuleModel) moduleModel, isOhosTest);
        if (StringUtils.isEmpty(compileOption) || !compileOption.contains(COMPILE_CONDITION)) {
            msgList.add(message("upgrade.compile.condition.config.add.text"));
            return true;
        }
        return false;
    }

    @NotNull
    private Path getTomlPath(ModuleModel moduleModel, boolean isOhosTest) {
        return Path.of(FileUtils.getRealCjpmTomlDir(moduleModel, isOhosTest), "cjpm.toml");
    }

    private boolean updateToml(ModuleModel moduleModel, boolean isOhosTest) {
        List<String> cfgKeys = List.of(PACKAGE, COMPILE_OPTION);
        List<String> debugKeys = List.of(KEY_PROFILE, CUSTOMIZED_OPTION, DEBUG);
        List<String> releaseKeys = List.of(KEY_PROFILE, CUSTOMIZED_OPTION, RELEASE);
        String updateData = getCfgData(moduleModel);
        Path tomlPath = getTomlPath(moduleModel, isOhosTest);
        Optional<Toml> moduleTomlOpt;
        try {
            moduleTomlOpt = new Toml().read(tomlPath.toFile());
        } catch (IllegalStateException e) {
            return false;
        }
        if (moduleTomlOpt.isEmpty()) {
            return false;
        }
        Toml toml = moduleTomlOpt.get();
        Map<String, Object> tomlMap = toml.toMap();
        updateUnitMap(cfgKeys, tomlMap, updateData);
        updateUnitMap(debugKeys, tomlMap, APILEVEL_CHECK);
        updateUnitMap(releaseKeys, tomlMap, APILEVEL_CHECK);
        try {
            CJPMTomlEncoder encoder = new CJPMTomlEncoder(toml);
            encoder.write(tomlMap, tomlPath.toFile());
        } catch (IOException e) {
            LOG.warn("Error writing TOML file: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void updateUnitMap(List<String> keys, Map<String, Object> tomlMap, String updateData) {
        Map<String, Object> unitMap =
                getTomlUnitMap(tomlMap, keys, TomlUpdateTypeData.OperationEnum.FORCED_REPLACE);
        String lastKey = keys.get(keys.size() - 1);
        if (unitMap.isEmpty()) {
            unitMap.put(lastKey, updateData);
        } else {
            Object oldValue = unitMap.getOrDefault(lastKey, "");
            if (oldValue.toString().isEmpty()) {
                unitMap.put(lastKey, updateData);
            }
            if (!oldValue.toString().isEmpty() && !oldValue.toString().contains(updateData)) {
                unitMap.put(lastKey, oldValue + " " + updateData);
            }
        }
    }

    private String getCfgData(ModuleModel moduleModel) {
        if (ModuleType.HAR.toString().equalsIgnoreCase(moduleModel.getModuleType())) {
            return "--cfg=\"${COMPILE_CONDITION}\"";
        } else {
            String moduleName = moduleModel.getModuleName().toUpperCase();
            return String.format("--cfg=\"${COMPILE_CONDITION_%s}\"", moduleName);
        }
    }
}
