/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeTypeEnum;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * 基本数据类型数组
 *
 * @since 2025-02-10
 */
@Setter
@Getter
public class PrimitiveArrayDump extends ObjectInfo {
    // 数组元素个数
    private int num;

    // 元素类型
    private int type;

    /**
     * PrimitiveArrayDump构造函数
     *
     * @param id id
     * @param num num
     * @param type type
     * @since 2025-02-10
     */
    public PrimitiveArrayDump(int id, int num, int type) {
        super(id, 0, new ArrayList<>());
        this.num = num;
        this.type = type;
        setObjectType(NodeTypeEnum.ARRAY);
    }
}
