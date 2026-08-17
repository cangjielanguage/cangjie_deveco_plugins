/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto;

import com.huawei.deveco.insight.ohos.utils.annotations.NotNull;

import com.alibaba.fastjson2.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前端发送的请求的数据格式定义
 *
 * @since 2025/11/11
 */
@Data
@AllArgsConstructor
public class JcefRequest {
    @NotNull
    private String key;

    @NotNull
    private Data data;

    /**
     * data数据格式
     */
    @lombok.Data
    @AllArgsConstructor
    public static class Data {
        @NotNull
        private JSONObject params;

        @NotNull
        private String method;
    }
}
