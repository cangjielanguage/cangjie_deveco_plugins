/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeTypeEnum;

import lombok.Data;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 该子记录存放一个对象实例
 *
 * @since 2025-02-10
 */
@Data
public class ObjectInfo {
    // 对象 ID
    private int id;

    // 对象大小
    private int size;

    // 类/结构体 ID
    private int cls;

    private NodeTypeEnum objectType = NodeTypeEnum.OBJECT;

    private int name;

    // 引用类型成员变量的值，其值为引用对象的 ID
    private List<Integer> refIdList;

    public ObjectInfo(int id, int cls, List<Integer> refIdList) {
        this.id = id;
        this.cls = cls;
        this.refIdList = CollectionUtils.isNotEmpty(refIdList) ? refIdList : new ArrayList<>();
    }
}
