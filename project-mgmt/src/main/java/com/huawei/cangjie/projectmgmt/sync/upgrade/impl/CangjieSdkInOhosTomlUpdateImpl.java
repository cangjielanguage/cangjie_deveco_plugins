/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.Constants.AARCH64_LINUX_OHOS;
import static com.huawei.cangjie.projectmgmt.utils.Constants.BIN_DEPENDENCIES;
import static com.huawei.cangjie.projectmgmt.utils.Constants.COMPILE_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PATH_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.TARGET;
import static com.huawei.cangjie.projectmgmt.utils.Constants.X86_64_LINUX_OHOS;

import com.huawei.cangjie.projectmgmt.sync.upgrade.vo.TomlUpdateTypeData;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import org.jetbrains.annotations.NotNull;

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
 * @since 2025 -07-05
 */
public class CangjieSdkInOhosTomlUpdateImpl extends CangjieBaseTomlUpdateImpl {
    private static final List<TomlUpdateTypeData> tomlUpdateList = new ArrayList<>();

    private static final String DEVECO_CANGJIE_HOME = "${DEVECO_CANGJIE_HOME}";

    // AARCH64
    private static final String AARCH64_OLD_B_OPTION =
        "-B \"" + DEVECO_CANGJIE_HOME + "/compiler/third_party/llvm/bin\"";

    private static final String AARCH64_OLD_L_OPTION =
        "-L \"" + DEVECO_CANGJIE_HOME + "/build/linux_ohos_aarch64_llvm/openssl\"";

    private static final String AARCH64_NEW_OPTIONS =
        "-B \"" + DEVECO_CANGJIE_HOME + "/build-tools/third_party/llvm/bin\"";

    private static final String AARCH64_KIT_OPTIONS = "${AARCH64_KIT_LIBS}";

    // X86_64
    private static final String X86_64_OLD_B_OPTION =
        "-B \"" + DEVECO_CANGJIE_HOME + "/compiler/third_party/llvm/bin\"";

    private static final String X86_64_OLD_L_OPTION =
        "-L \"" + DEVECO_CANGJIE_HOME + "/build/linux_ohos_x86_64_llvm/openssl\"";

    private static final String X86_64_KIT_OPTIONS = "${X86_64_OHOS_KIT_LIBS}";

    static {
        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, AARCH64_LINUX_OHOS, COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.REPLACE, Arrays.asList(AARCH64_OLD_B_OPTION, AARCH64_OLD_L_OPTION),
            AARCH64_NEW_OPTIONS));

        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, AARCH64_LINUX_OHOS, BIN_DEPENDENCIES,
            PATH_OPTION), TomlUpdateTypeData.OperationEnum.ADD, null, AARCH64_KIT_OPTIONS));

        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, X86_64_LINUX_OHOS, COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.REPLACE, Arrays.asList(X86_64_OLD_B_OPTION, X86_64_OLD_L_OPTION),
            AARCH64_NEW_OPTIONS));

        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, X86_64_LINUX_OHOS, BIN_DEPENDENCIES,
            PATH_OPTION), TomlUpdateTypeData.OperationEnum.ADD, null, X86_64_KIT_OPTIONS));
    }

    private Set<String> msgList = new HashSet<>();

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        return isNeedUpdateCommon(projectModel, "compiler");
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.sdk.in.ohos.update.text"), msgList);
        return updateMsgs;
    }

    @Override
    protected boolean isModuleNeedUpdateCheck(ModuleModel moduleModel) {
        Optional<String> compileOptionOptional = getCompileOption(moduleModel);
        if (compileOptionOptional.isEmpty()) {
            return false;
        }
        boolean isNeedUpdate = false;
        String cangjieOption = compileOptionOptional.get();
        if (isMatched(cangjieOption, "${DEVECO_CANGJIE_HOME}/compiler/")) {
            msgList.add(message("upgrade.sdk.compiler.renamed.text"));
            isNeedUpdate = true;
        }
        if (isMatched(cangjieOption, "${DEVECO_CANGJIE_HOME}/build/")) {
            msgList.add(message("upgrade.sdk.build.renamed.text"));
            isNeedUpdate = true;
        }
        return isNeedUpdate;
    }

    @Override
    protected List<TomlUpdateTypeData> getTomlUpdateTypeDataList() {
        return tomlUpdateList;
    }
}
