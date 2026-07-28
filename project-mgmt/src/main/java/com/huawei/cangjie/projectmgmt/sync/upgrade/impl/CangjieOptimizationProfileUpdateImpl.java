/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.OVERRIDE_COMPILE_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PACKAGE;

import com.huawei.cangjie.projectmgmt.extend.toml.Toml;
import com.huawei.cangjie.projectmgmt.sync.upgrade.vo.TomlUpdateTypeData;
import com.huawei.cangjie.projectmgmt.utils.CangjieModulePathType;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The type Cangjie sdk in ohos toml update.
 *
 * @since 2026 -05-04
 */
public class CangjieOptimizationProfileUpdateImpl extends CangjieBaseTomlUpdateImpl {
    private static final Logger LOG = Logger.getInstance(CangjieOptimizationProfileUpdateImpl.class);

    private static final List<TomlUpdateTypeData> tomlUpdateList = new ArrayList<>();

    private static final String OVERRIDE_COMPILE_OPTION_VAL = "${OVERRIDE_COMPILE_OPTION}";

    static {
        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(PACKAGE, OVERRIDE_COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.ADD, null, OVERRIDE_COMPILE_OPTION_VAL));
    }

    private final Set<String> msgList = new HashSet<>();

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        return isNeedUpdateCommon(projectModel, "compiler");
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.pgo.optimization.profile.update.text"), msgList);
        return updateMsgs;
    }

    @Override
    public boolean doUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return false;
        }
        List<CangjieModulePathType> pathTypes =
            List.of(CangjieModulePathType.MAIN, CangjieModulePathType.OHOS_TEST, CangjieModulePathType.LOCAL_TEST);
        for (ModuleModel moduleModel : moduleModelList) {
            if (!(moduleModel instanceof OhosModuleModel ohosModuleModel)) {
                continue;
            }
            for (CangjieModulePathType pathType : pathTypes) {
                if (!FileUtils.getRealCjpmFilePath(moduleModel, pathType).toFile().exists()) {
                    continue;
                }
                String overrideCompileOption = FileUtils.getCangjieOverrideCompileOption(ohosModuleModel, pathType);
                if (StringUtils.isNotEmpty(overrideCompileOption) && overrideCompileOption.contains(
                    OVERRIDE_COMPILE_OPTION_VAL)) {
                    continue;
                }
                Path tomlPath = getTomlPath(moduleModel, pathType);
                Optional<Toml> moduleTomlOpt;
                try {
                    moduleTomlOpt = new Toml().read(tomlPath.toFile());
                    if (moduleTomlOpt.isEmpty()) {
                        continue;
                    }
                    if (!doUpdateToml(tomlPath, moduleTomlOpt.get(), moduleModel)) {
                        NotificationUtil.notifyInfo(message("upgrade.toml.task.fail"), projectModel.getProject(),
                            NotificationType.ERROR);
                        return false;
                    }
                } catch (IllegalStateException e) {
                    LOG.warn("Read toml failed: " + e.getMessage());
                }
            }
        }
        return true;
    }

    @Override
    protected boolean isModuleNeedUpdateCheck(ModuleModel moduleModel) {
        if (!(moduleModel instanceof OhosModuleModel ohosModuleModel)) {
            return false;
        }
        List<CangjieModulePathType> pathTypes =
            List.of(CangjieModulePathType.MAIN, CangjieModulePathType.OHOS_TEST, CangjieModulePathType.LOCAL_TEST);
        for (CangjieModulePathType pathType : pathTypes) {
            if (!FileUtils.getRealCjpmFilePath(moduleModel, pathType).toFile().exists()) {
                continue;
            }
            String overrideCompileOption = FileUtils.getCangjieOverrideCompileOption(ohosModuleModel, pathType);
            if (StringUtils.isEmpty(overrideCompileOption) || !overrideCompileOption.contains(
                OVERRIDE_COMPILE_OPTION_VAL)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<TomlUpdateTypeData> getTomlUpdateTypeDataList() {
        return tomlUpdateList;
    }
}
