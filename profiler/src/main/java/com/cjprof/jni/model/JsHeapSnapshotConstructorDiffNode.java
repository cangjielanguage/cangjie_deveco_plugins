/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.cjprof.jni.model;

import com.huawei.deveco.insight.ohos.ability.arkmemory.JsHeapSnapshotDiffNode;
import com.huawei.deveco.insight.ohos.ability.arkmemory.instancefilters.JsHeapSnapshot;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * JsHeapSnapshotConstructorDiffNode
 *
 * @since 2026-04-14
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class JsHeapSnapshotConstructorDiffNode extends JsHeapSnapshotDiffNode {
    /**
     * JsHeapSnapshotConstructorDiffNode
     *
     * @since 2026-04-14
     */
    transient String jsTargetHeapSnapshotId;

    public JsHeapSnapshotConstructorDiffNode(String className, JsHeapSnapshot.Diff diff, JsHeapSnapshot snapshot,
        JsHeapSnapshot baseSnapshot, String baseSnapshotId, String jsTargetHeapSnapshotId) {
        super(className, diff, snapshot, baseSnapshot, baseSnapshotId);
        this.jsTargetHeapSnapshotId = jsTargetHeapSnapshotId;
    }
}
