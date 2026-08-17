/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.model.dto.response;

import com.huawei.cjprofiler.ability.arkmemory.entity.CjStack;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * cangjie stack list
 *
 * @since 2025/03/04/18:54
 */
@AllArgsConstructor
@Data
public class CjStackList {
    private List<CjStack> cjStackList;
}
