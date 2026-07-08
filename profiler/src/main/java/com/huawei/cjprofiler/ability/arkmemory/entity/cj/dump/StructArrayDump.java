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

import java.util.List;

/**
 * 该子记录存放一个Struct数组
 *
 * @since 2025-02-10
 */
@Getter
public class StructArrayDump extends ObjectInfo {
    // 数组中对象元素个数
    private int num;

    /**
     * PrimitiveArrayDump构造函数
     *
     * @param id id
     * @param cls cls
     * @param refIdList refIdList
     * @param num num
     * @since 2025-02-10
     */
    public StructArrayDump(int id, int cls, List<Integer> refIdList, int num) {
        super(id, cls, refIdList);
        this.num = num;
        setObjectType(NodeTypeEnum.ARRAY);
    }
}
