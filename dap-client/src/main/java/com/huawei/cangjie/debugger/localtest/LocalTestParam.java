/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.localtest;

import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;

import com.intellij.openapi.project.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * local test param
 *
 * @since 2025-08-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalTestParam {
    /**
     * module model
     */
    private ModuleModel moduleModel;

    /**
     * project
     */
    private Project project;

    /**
     * debuggee
     */
    private String program;

    /**
     * envs
     */
    private Map<String, String> envs;


    /**
     * args
     */
    private List<String> args;
}