/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.cjprof.jni;

import com.huawei.cjprofiler.utils.ConfigUtil;

import com.cjprof.jni.model.ConstructorDiffNode;
import com.cjprof.jni.model.ConstructorNode;
import com.cjprof.jni.model.HeapSnapshot;
import com.cjprof.jni.model.InstanceDiffNode;
import com.cjprof.jni.model.InstanceNode;
import com.cjprof.jni.model.ThreadInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Cjprof
 *
 * @since 2026-04-20
 */
public class Cjprof {
    static {
        String cangjieHomePath = ConfigUtil.getCangjieHomePath();
        Path toolsLibPath = Path.of(cangjieHomePath, "tools", "lib");

        System.load(toolsLibPath.resolve("cjprof.dll").toString());
        System.load(toolsLibPath.resolve("cjprof-jni.dll").toString());
    }

    /**
     * initialize
     */
    public static native void initialize();

    /**
     * parseHeapSnapshotFiles
     *
     * @param filePaths filePaths
     * @return boolean
     */
    public static native boolean parseHeapSnapshotFiles(List<String> filePaths);

    /**
     * cleanHeapSnapshotFiles
     *
     * @param ids ids
     */
    public static native void cleanHeapSnapshotFiles(List<Long> ids);

    /**
     * queryAllHeapSnapshot
     *
     * @return List<HeapSnapshot>
     */
    public static native List<HeapSnapshot> queryAllHeapSnapshot();

    /**
     * getSnapshotIDByFilePath
     *
     * @param filePath filePath
     * @return long
     */
    public static native long getSnapshotIDByFilePath(String filePath);

    /**
     * getConstructorNodesBySnapshotID 构造函数节点
     *
     * @param id id
     * @return List<ConstructorNode>
     */
    public static native List<ConstructorNode> getConstructorNodesBySnapshotID(long id);

    /**
     * getRootNodesBySnapshotID
     *
     * @param id id
     * @param rootTypes rootTypes
     * @return List<ConstructorNode>
     */
    public static native List<ConstructorNode> getRootNodesBySnapshotID(long id, Set<Byte> rootTypes);

    /**
     * getRootDiffNodesBySnapshotID
     *
     * @param baseSnapshotId baseSnapshotId
     * @param targetSnapshotId targetSnapshotId
     * @param rootTypes rootTypes
     * @return List<ConstructorDiffNode>
     */
    public static native List<ConstructorDiffNode> getRootDiffNodesBySnapshotID(long baseSnapshotId,
            long targetSnapshotId, Set<Byte> rootTypes);

    /**
     * expandConstructorNode 节点展开
     *
     * @param snapshotId snapshotId
     * @param nodeId nodeId
     * @param startIndex startIndex
     * @param length length
     * @return ConstructorNode
     */
    public static native ConstructorNode expandConstructorNode(long snapshotId, long nodeId, int startIndex,
            int length);

    /**
     * expandConstructorDiffNode
     *
     * @param baseSnapshotId baseSnapshotId
     * @param targetSnapshotId targetSnapshotId
     * @param nodeId nodeId
     * @param startIndex startIndex
     * @param length length
     * @return ConstructorDiffNode
     */
    public static native ConstructorDiffNode expandConstructorDiffNode(long baseSnapshotId, long targetSnapshotId,
            long nodeId, int startIndex, int length);

    /**
     * expandInstanceNode
     *
     * @param snapshotId snapshotId
     * @param nodeId nodeId
     * @param startIndex startIndex
     * @param length length
     * @return InstanceNode
     */
    public static native InstanceNode expandInstanceNode(long snapshotId, long nodeId, int startIndex, int length);

    /**
     * expandInstanceDiffNode
     *
     * @param baseSnapshotId baseSnapshotId
     * @param targetSnapshotId targetSnapshotId
     * @param nodeId nodeId
     * @param startIndex startIndex
     * @param length length
     * @return InstanceDiffNode
     */
    public static native InstanceDiffNode expandInstanceDiffNode(long baseSnapshotId, long targetSnapshotId,
            long nodeId, int startIndex, int length);

    /**
     * expandDetailNode
     *
     * @param snapshotId snapshotId
     * @param nodeId nodeId
     * @param isReference isReference
     * @param startIndex startIndex
     * @param length length
     * @return InstanceNode
     */
    public static native InstanceNode expandDetailNode(long snapshotId, long nodeId, boolean isReference,
            int startIndex, int length);

    /**
     * expandDetailDiffNode
     *
     * @param baseSnapshotId baseSnapshotId
     * @param targetSnapshotId targetSnapshotId
     * @param nodeId nodeId
     * @param isReference isReference
     * @param startIndex startIndex
     * @param length length
     * @return InstanceDiffNode
     */
    public static native InstanceDiffNode expandDetailDiffNode(long baseSnapshotId, long targetSnapshotId, long nodeId,
            boolean isReference, int startIndex, int length);

    /**
     * querySnapshotComparison 快照比较
     *
     * @param baseId baseId
     * @param targetId targetId
     * @return List<ConstructorDiffNode>
     */
    public static native List<ConstructorDiffNode> querySnapshotComparison(long baseId, long targetId);

    /**
     * getNodeRootpaths 根路径
     *
     * @param snapshotId snapshotId
     * @param nodeId nodeId
     * @param pathNum pathNum
     * @return java.util.List<java.util.List<InstanceNode>>
     */
    public static native java.util.List<java.util.List<InstanceNode>> getNodeRootpaths(long snapshotId, long nodeId,
            int pathNum);

    /**
     * getThreadInfos 线程信息
     *
     * @param snapshotId snapshotId
     * @return List<ThreadInfo>
     */
    public static native List<ThreadInfo> getThreadInfos(long snapshotId);

    /**
     * querySnapshotCountOfResults 查询功能
     *
     * @param keyword keyword
     * @param isIgnoreCase isIgnoreCase
     * @param snapshotId snapshotId
     * @return int
     */
    public static native int querySnapshotCountOfResults(String keyword, boolean isIgnoreCase, long snapshotId);

    /**
     * querySnapshotNodeByIndex
     *
     * @param keyword keyword
     * @param isIgnoreCase isIgnoreCase
     * @param snapshotId snapshotId
     * @param length length
     * @param index index
     * @return ConstructorNode
     */
    public static native ConstructorNode querySnapshotNodeByIndex(String keyword, boolean isIgnoreCase, long snapshotId,
            int length, int index);

    /**
     * queryComparisonCountOfResults
     *
     * @param keyword keyword
     * @param isIgnoreCase isIgnoreCase
     * @param baseId baseId
     * @param targetId targetId
     * @return int
     */
    public static native int queryComparisonCountOfResults(String keyword, boolean isIgnoreCase, long baseId,
            long targetId);

    /**
     * queryComparisonNodeByIndex
     *
     * @param keyword keyword
     * @param isIgnoreCase isIgnoreCase
     * @param baseId baseId
     * @param targetId targetId
     * @param length length
     * @param index index
     * @return ConstructorDiffNode
     */
    public static native ConstructorDiffNode queryComparisonNodeByIndex(String keyword, boolean isIgnoreCase,
            long baseId, long targetId, int length, int index);
}