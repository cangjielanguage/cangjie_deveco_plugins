/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.testframework.configuration;

import static com.huawei.cangjie.testframework.utils.TestUtil.isSameOrSubPath;
import static com.huawei.deveco.projectmgmt.ohos.utils.CommonProjectUtil.getSelectFileOhosModuleModel;

import com.huawei.cangjie.testframework.utils.Constant;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * CangjieOhosTestConfigurationProducer
 *
 * @since 2025/08/28
 */
public class CangjieOhosTestConfigurationProducer extends CangjieTestConfigurationProducer {
    protected CangjieOhosTestConfigurationProducer() {
        super();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull CangjieTestRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        Location<PsiElement> location = context.getLocation();
        if (location == null) {
            return false;
        }
        PsiElement element = location.getPsiElement();
        PsiFile file = element.getContainingFile();
        if (file == null || !file.getName().endsWith(Constant.CJ_TEST_FILE_SUFFIX)) {
            return false;
        }
        OhosModuleModel moduleModel = getSelectFileOhosModuleModel(element.getProject(), file.getVirtualFile());
        String ohosTestPath = Path.of(moduleModel.getModulePath(), Constant.SRC, Constant.OHOS_TEST,
                Constant.CANGJIE).toString();
        if (!isSameOrSubPath(ohosTestPath, file.getVirtualFile().getCanonicalPath())) {
            return false;
        }
        return super.setupConfigurationFromContext(configuration, context, sourceElement);
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull CangjieTestRunConfiguration configuration,
                                              @NotNull ConfigurationContext configurationContext) {
        if (configuration.getTestPathType() != CangjieTestRunConfiguration.TestPathType.OHOS_TEST_PATH) {
            return false;
        }
        return super.isConfigurationFromContext(configuration, configurationContext);
    }

    @Override
    @NotNull
    public ConfigurationFactory getConfigurationFactory() {
        return CangjieOhosTestRunConfigurationType.getInstance();
    }
}
