/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.deveco.cjlint;

import lombok.Data;

import java.util.List;

/**
 * cjlint_rule_list.json配置文件的JSON映射类
 *
 * @since 2025-06-28
 */
@Data
public class CjlintRuleListConfig {
    private List<String> ruleList;
}
