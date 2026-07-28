/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.utils;

import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;

/**
 * project utils
 *
 * @since 2024-05-20
 */
public class ProjectUtils {
    private static final String CANGJIE_PROJECT_OPTION_NAME = "CangjieProjectType";

    /**
     * check is cangjie project
     *
     * @param module ohosModuleModel
     * @return is cangjie project
     */
    public static boolean isCangjieProject(OhosModuleModel module) {
        CangjieProjectType cangjieProjectType = ProjectUtils.getCangjieProjectType(module);
        return cangjieProjectType != CangjieProjectType.NOT_CANGJIE;
    }

    /**
     * check is cangjie only project
     *
     * @param module ohosModuleModel
     * @return is cangjie only project
     */
    public static boolean isCangjieOnlyProject(OhosModuleModel module) {
        CangjieProjectType cangjieProjectType = ProjectUtils.getCangjieProjectType(module);
        return cangjieProjectType == CangjieProjectType.CANGJIE;
    }

    /**
     * check is cangjie mix project
     *
     * @param module ohosModuleModel
     * @return is cangjie mix project
     */
    public static boolean isCangjieMixProject(OhosModuleModel module) {
        CangjieProjectType cangjieProjectType = ProjectUtils.getCangjieProjectType(module);
        return cangjieProjectType == CangjieProjectType.ARKTS_CANGJIE;
    }

    /**
     * get cangjie project type
     *
     * @param module ohosModuleModel
     * @return cangjie project type
     */
    private static CangjieProjectType getCangjieProjectType(OhosModuleModel module) {
        String cangjieProjectType = module.getExtraBuildOptionByName(CANGJIE_PROJECT_OPTION_NAME);
        return CangjieProjectType.getCangjieProjectType(cangjieProjectType);
    }
}