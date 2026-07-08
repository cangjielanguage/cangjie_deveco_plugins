/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.ASAN;
import static com.huawei.cangjie.projectmgmt.utils.Constants.CUSTOMIZED_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.KEY_PROFILE;

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

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The type Cangjie sdk in ohos toml update.
 *
 * @since 2026-03-03
 */
public class CangjieAsanTomlUpdateImpl extends CangjieBaseTomlUpdateImpl {
    private static final List<TomlUpdateTypeData> tomlUpdateList = new ArrayList<>();

    // AARCH64
    private static final String ASAN_OPTIONS = "--sanitize=address -lclang_rt.asan";

    static {
        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(KEY_PROFILE, CUSTOMIZED_OPTION, ASAN),
            TomlUpdateTypeData.OperationEnum.ADD, null, ASAN_OPTIONS));
    }

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        return isNeedUpdateCommon(projectModel, "compiler");
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.asan.update.text"), Collections.EMPTY_SET);
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
                Optional<Toml> customizedOptionOpt = FileUtils.getCustomizedOption(ohosModuleModel, pathType);
                if (customizedOptionOpt.isPresent() && customizedOptionOpt.get().contains(ASAN)) {
                    continue;
                }
                Path tomlPath = getTomlPath(moduleModel, pathType);
                Optional<Toml> moduleTomlOpt;
                try {
                    moduleTomlOpt = new Toml().read(tomlPath.toFile());
                } catch (IllegalStateException e) {
                    continue;
                }
                if (moduleTomlOpt.isEmpty()) {
                    continue;
                }
                if (!doUpdateToml(tomlPath, moduleTomlOpt.get(), moduleModel)) {
                    NotificationUtil.notifyInfo(message("upgrade.toml.task.fail"), projectModel.getProject(),
                        NotificationType.ERROR);
                    return false;
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
            Optional<Toml> customizedOptionOpt = FileUtils.getCustomizedOption(ohosModuleModel, pathType);
            if (customizedOptionOpt.isEmpty()) {
                return true;
            }
            Toml toml = customizedOptionOpt.get();
            if (toml.contains(ASAN)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    protected List<TomlUpdateTypeData> getTomlUpdateTypeDataList() {
        return tomlUpdateList;
    }

    @Override
    protected void doAddTomlValue(Map<String, Object> lastKeyObjMap, String lastKey, Object updateValue) {
        lastKeyObjMap.putIfAbsent(lastKey, updateValue);
    }
}
