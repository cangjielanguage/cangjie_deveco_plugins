/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.dts2cj;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;

import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;
import com.huawei.deveco.projectmodel.ohos.sync.syncinterface.PostSync;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * The type Dts2cj copy sync.
 *
 * @since 2025 -01-18
 */
public class Dts2cjCopySync implements PostSync {
    private static final Logger LOGGER = Logger.getInstance(Dts2cjCopySync.class);

    @Override
    public void checkStatus(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        doCopyDts2cjResults(projectModel, true);
    }

    /**
     * Do copy dts 2 cj results.
     *
     * @param projectModel the project model
     * @param isNeedDelete the is need delete
     */
    public void doCopyDts2cjResults(ProjectModel projectModel, boolean isNeedDelete) {
        Project project = projectModel.getProject();
        Map<Path, Path> copyMap = Dts2cjCopyManager.getInstance().popSyncCopy(project);
        if (copyMap.isEmpty()) {
            return;
        }
        copyMap.forEach((srcPath, destPath) -> {
            if (!Files.exists(srcPath) || !Files.exists(destPath) || !Files.isDirectory(srcPath) || !Files.isDirectory(
                destPath)) {
                return;
            }
            if (isNeedDelete) {
                deleteIndexCjFiles(destPath);
            }
            copyCjFiles(srcPath, destPath);
            VirtualFile cangjieDir = LocalFileSystem.getInstance().findFileByPath(destPath.toString());
            LocalFileSystem.getInstance().refreshFiles(Collections.singletonList(cangjieDir), false, true, null);
        });
        NotificationUtil.notifyInfo(message("dts2cj.generate.cangjie.bindings.finished"), project,
            NotificationType.INFORMATION);
    }

    private void deleteIndexCjFiles(Path destPath) {
        Path indexCjPath = destPath.resolve("index.cj");
        if (!Files.exists(indexCjPath)) {
            return;
        }
        try {
            Files.delete(indexCjPath);
        } catch (IOException e) {
            LOGGER.warn(String.format(Locale.ROOT, "delete cangjie library cj files failed: %s", e.getMessage()));
        }
    }

    private void copyCjFiles(Path srcDir, Path destDir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir, "*.cj")) {
            for (Path srcFile : stream) {
                Path destFile = destDir.resolve(srcFile.getFileName());
                Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn(String.format(Locale.ROOT, "copy dts2cj files failed: %s", e.getMessage()));
        }
    }
}
