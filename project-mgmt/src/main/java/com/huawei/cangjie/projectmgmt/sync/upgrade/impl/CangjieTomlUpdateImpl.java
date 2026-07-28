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
import static com.huawei.cangjie.projectmgmt.utils.Constants.KEY_DEPENDENCIES;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PACKAGE;
import static com.huawei.cangjie.projectmgmt.utils.Constants.PATH_OPTION;
import static com.huawei.cangjie.projectmgmt.utils.Constants.TARGET;
import static com.huawei.cangjie.projectmgmt.utils.Constants.X86_64_LINUX_OHOS;

import com.huawei.cangjie.projectmgmt.sync.upgrade.vo.TomlUpdateTypeData;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.openapi.diagnostic.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The type Cangjie toml update.
 *
 * @since 2025 -02-07
 */
public class CangjieTomlUpdateImpl extends CangjieBaseTomlUpdateImpl {
    private static final Logger LOG = Logger.getInstance(CangjieTomlUpdateImpl.class);

    private static final List<TomlUpdateTypeData> tomlUpdateList = new ArrayList<>();

    private static final String DEVECO_OH_NATIVE_HOME = "${DEVECO_OH_NATIVE_HOME}";

    private static final String DEVECO_CANGJIE_HOME = "${DEVECO_CANGJIE_HOME}";

    private static final String AARCH64_KIT_LIBS = "${AARCH64_KIT_LIBS}";

    private static final String X86_64_OHOS_KIT_LIBS = "${X86_64_OHOS_KIT_LIBS}";

    // AARCH64
    private static final String AARCH64_OLD_B_OPTION =
        "-B \"" + DEVECO_CANGJIE_HOME + "/musl/usr/lib/aarch64-linux-ohos\"";

    private static final String AARCH64_OLD_L_OPTION =
        "-L \"" + DEVECO_CANGJIE_HOME + "/musl/usr/lib/aarch64-linux-ohos\"";

    private static final String AARCH64_OLD_SYSROOT = "--sysroot \"" + DEVECO_CANGJIE_HOME + "/musl\"";

    private static final String AARCH64_NEW_OPTIONS =
        "-B \"" + DEVECO_OH_NATIVE_HOME + "/sysroot/usr/lib/aarch64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/sysroot/usr/lib/aarch64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/llvm/lib/clang/15.0.4/lib/aarch64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/llvm/lib/aarch64-linux-ohos\" " + "--sysroot \"" + DEVECO_OH_NATIVE_HOME + "/sysroot\"";

    // X86_64
    private static final String X86_64_OLD_B_OPTION =
        "-B \"" + DEVECO_CANGJIE_HOME + "/musl/usr/lib/x86_64-linux-ohos\"";

    private static final String X86_64_OLD_L_OPTION =
        "-L \"" + DEVECO_CANGJIE_HOME + "/musl/usr/lib/x86_64-linux-ohos\"";

    private static final String X86_64_OLD_SYSROOT = "--sysroot \"" + DEVECO_CANGJIE_HOME + "/musl\"";

    private static final String X86_64_NEW_OPTIONS =
        "-B \"" + DEVECO_OH_NATIVE_HOME + "/sysroot/usr/lib/x86_64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/sysroot/usr/lib/x86_64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/llvm/lib/clang/15.0.4/lib/x86_64-linux-ohos\" " + "-L \"" + DEVECO_OH_NATIVE_HOME
            + "/llvm/lib/x86_64-linux-ohos\" " + "--sysroot \"" + DEVECO_OH_NATIVE_HOME + "/sysroot\"";

    private static final String DY_STD_OPTION = "--dy-std";

    static {
        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, AARCH64_LINUX_OHOS, COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.REPLACE,
            Arrays.asList(AARCH64_OLD_B_OPTION, AARCH64_OLD_L_OPTION, AARCH64_OLD_SYSROOT), AARCH64_NEW_OPTIONS));

        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(TARGET, X86_64_LINUX_OHOS, COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.REPLACE,
            Arrays.asList(X86_64_OLD_B_OPTION, X86_64_OLD_L_OPTION, X86_64_OLD_SYSROOT), X86_64_NEW_OPTIONS));

        tomlUpdateList.add(new TomlUpdateTypeData(Arrays.asList(PACKAGE, COMPILE_OPTION),
            TomlUpdateTypeData.OperationEnum.FORCED_REPLACE, null, DY_STD_OPTION));

        tomlUpdateList.add(
            new TomlUpdateTypeData(Arrays.asList(TARGET, AARCH64_LINUX_OHOS, BIN_DEPENDENCIES, PATH_OPTION),
                TomlUpdateTypeData.OperationEnum.REPLACE, null, AARCH64_KIT_LIBS));

        tomlUpdateList.add(
            new TomlUpdateTypeData(Arrays.asList(TARGET, X86_64_LINUX_OHOS, BIN_DEPENDENCIES, PATH_OPTION),
                TomlUpdateTypeData.OperationEnum.REPLACE, null, X86_64_OHOS_KIT_LIBS));
    }

    private Set<String> msgList = new HashSet<>();

    @Override
    public boolean isNeedUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        return isNeedUpdateCommon(projectModel, "musl");
    }

    @Override
    @NotNull
    public Map<String, Set<String>> getUpdateMessage(@NotNull ProjectModel projectModel,
        @NotNull SyncRequest syncRequest) {
        Map<String, Set<String>> updateMsgs = new HashMap<>();
        updateMsgs.put(message("upgrade.toml.update.text"), msgList);
        return updateMsgs;
    }

    @Override
    protected boolean isModuleNeedUpdateCheck(ModuleModel moduleModel) {
        Optional<String> compileOptionOptional = getCompileOption(moduleModel);
        if (compileOptionOptional.isEmpty()) {
            return false;
        }
        String cangjieOption = compileOptionOptional.get();
        if (isMatched(cangjieOption, "${DEVECO_CANGJIE_HOME}/musl")) {
            msgList.add(message("upgrade.sdk.musl.deleted.text"));
            return true;
        }
        return false;
    }

    @Override
    protected List<TomlUpdateTypeData> getTomlUpdateTypeDataList() {
        return tomlUpdateList;
    }

    @Override
    protected void doOtherUpdate(Map<String, Object> tomlMap, ModuleModel moduleModel) {
        deleteCjRes(tomlMap, moduleModel);
    }

    private void deleteCjRes(Map<String, Object> tomlMap, ModuleModel moduleModel) {
        final String resKey = "cj_res_" + moduleModel.getModuleName();
        Map<String, Object> dependencies = FileUtils.getTomlUnitMap(tomlMap, List.of(KEY_DEPENDENCIES, resKey),
            TomlUpdateTypeData.OperationEnum.REPLACE);
        if (dependencies == null) {
            return;
        }
        Object removedObj = dependencies.remove(resKey);
        if (!(removedObj instanceof Map<?, ?> removedMap)) {
            return;
        }
        Object pathObj = ((Map<String, Object>) removedMap).get("path");
        if (!(pathObj instanceof String path) || path.isBlank()) {
            return;
        }
        Path absPath = getTomlPath(moduleModel).getParent().resolve(path).normalize();
        if (!Files.isDirectory(absPath)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(absPath)) {
            paths.sorted(Comparator.reverseOrder()).forEach(filePath -> {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    LOG.warn("Failed to delete the cj_res file:%s".formatted(filePath));
                }
            });
        } catch (IOException e) {
            LOG.warn("Failed to delete the cj_res directory.");
        }
    }
}
