/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.ability.arkmemory.parser;

import com.huawei.cjprofiler.ability.arkmemory.entity.cj.EdgeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.NodeTypeEnum;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.MemTypeEnum;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.ObjectInfo;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.PrimitiveArrayDump;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.StructArrayDump;
import com.huawei.cjprofiler.ability.arkmemory.entity.cj.dump.TreeInfo;
import com.huawei.deveco.insight.ohos.utils.LogPrinter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将snapshot信息构建到一棵树中
 *
 * @since 2025-02-10
 */
public class CjBuildTreeService {
    // 虚拟根节点的Type值
    private static final int ROOT_NODE_TYPE = 3;

    private static final LogPrinter LOGGER = LogPrinter.createLogger(CjBuildTreeService.class);

    private static final int NODE_FIELD_LENGTH = 8;

    private static final int ROOT_NODE_ID = 1;

    private static volatile CjBuildTreeService cjBuildTreeService = null;

    private final List<NodeInfo> nodeList = new ArrayList<>();

    private final Set<Integer> visited = new HashSet<>();

    private final Map<Integer, Integer> sizeCache = new HashMap<>();

    private final Map<Integer, Integer> idToIndex = new HashMap<>();

    private Map<Integer, ObjectInfo> instanceDumpMap;

    private Map<Integer, Integer> classSizeMap;

    private Map<Integer, Integer> loadClassMap;

    private Set<Integer> rootSet;

    /**
     * Get single instance of CjBuildTreeService
     *
     * @return single instance of CjBuildTreeService
     */
    public static CjBuildTreeService getInstance() {
        if (cjBuildTreeService == null) {
            synchronized (CjBuildTreeService.class) {
                if (cjBuildTreeService == null) {
                    cjBuildTreeService = new CjBuildTreeService();
                }
            }
        }
        return cjBuildTreeService;
    }

    /**
     * 构建内存树
     *
     * @param instanceDumpMap instanceDumpMap
     * @param classSizeMap    classSizeMap
     * @param loadClassMap    loadClassMap
     * @param rootSet         rootSet
     * @return TreeInfo 内存树
     */
    public TreeInfo buildMemTree(Map<Integer, ObjectInfo> instanceDumpMap, Map<Integer, Integer> classSizeMap,
        Map<Integer, Integer> loadClassMap, Set<Integer> rootSet) {
        init(instanceDumpMap, classSizeMap, loadClassMap, rootSet);
        return generateTreeInfo();
    }

    private void init(Map<Integer, ObjectInfo> instanceDumpMap, Map<Integer, Integer> classSizeMap,
        Map<Integer, Integer> loadClassMap, Set<Integer> rootSet) {
        this.instanceDumpMap = instanceDumpMap;
        this.classSizeMap = classSizeMap;
        this.loadClassMap = loadClassMap;
        this.rootSet = rootSet;
    }

    /**
     * clear cache
     */
    public void clearAllCollection() {
        this.nodeList.clear();
        this.visited.clear();
        this.sizeCache.clear();
        this.idToIndex.clear();
    }

    /**
     * getNodeIndex
     *
     * @param id id
     * @return Integer
     */
    public Integer getNodeIndex(int id) {
        return idToIndex.get(id);
    }

    // 生成树形结构的节点信息和边信息
    private TreeInfo generateTreeInfo() {
        LOGGER.info("start to generate tree info");
        // 1. 计算所有节点的真实大小 并 获取节点名称
        getSizeAndName();

        // 2. 处理可达节点
        processReachableNodes();

        // 3. 处理不可达节点
        processUnreachableNodes();

        // 4. 按节点顺序生成边信息
        List<EdgeInfo> edgeInfoList = generateEdgeInfoInOrder();

        return new TreeInfo(edgeInfoList, nodeList);
    }

    // 按节点顺序生成边信息：nodeList中第i个节点的所有边连续排列在edgeList中
    private List<EdgeInfo> generateEdgeInfoInOrder() {
        List<EdgeInfo> edgeList = new ArrayList<>();

        // 按nodeList顺序遍历每个节点
        for (NodeInfo node : nodeList) {
            int nodeId = node.getId();
            int nodeType = node.getType();  // 获取当前节点的Type
            List<Integer> children = getChildren(nodeId);

            // 为当前节点的所有子节点生成边信息，连续添加到列表
            for (int childId : children) {
                Integer childIndex = idToIndex.get(childId);
                if (childIndex != null) {
                    int name = instanceDumpMap.containsKey(childId) ? getName(childId) : 0;
                    EdgeInfo edge = new EdgeInfo(nodeType, name, childIndex * NODE_FIELD_LENGTH);

                    edgeList.add(edge);
                }
            }
        }

        return edgeList;
    }

    private void getSizeAndName() {
        for (Map.Entry<Integer, ObjectInfo> entry : instanceDumpMap.entrySet()) {
            int nodeId = entry.getKey();
            ObjectInfo curNode = entry.getValue();
            // 计算所有节点的真实大小
            calculateAllNodeSizes(nodeId);
            // 设置name
            setNodeNameIndex(curNode);
        }
    }

    private void calculateAllNodeSizes(int nodeId) {
        if (sizeCache.containsKey(nodeId)) {
            return;
        }

        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> processing = new HashSet<>();

        stack.push(nodeId);
        processing.add(nodeId);

        while (!stack.isEmpty()) {
            int currentId = stack.peek();
            ObjectInfo currentObject = instanceDumpMap.get(currentId);

            // 处理空对象的情况
            if (currentObject == null) {
                handleNullObject(currentId, stack, processing);
                continue;
            }

            // 处理子节点
            boolean allChildrenProcessed = processChildren(currentObject, stack, processing);

            // 处理所有子节点已完成的情况
            if (allChildrenProcessed) {
                calculateAndCacheNodeSize(currentId, currentObject, stack, processing);
            }
        }
    }

    /**
     * 处理空对象的情况
     *
     * @param currentId 当前正在处理的对象ID
     * @param stack 用于深度优先搜索的栈
     * @param processing 正在处理中的节点集合
     */
    private void handleNullObject(int currentId, Deque<Integer> stack, Set<Integer> processing) {
        sizeCache.put(currentId, 0);
        stack.pop();
        processing.remove(currentId);
    }

    /**
     * 处理当前节点的子节点，并判断所有子节点是否已处理。
     *
     * @param currentObject 当前正在处理的对象信息。
     * @param stack 用于深度优先搜索的栈。
     * @param processing 正在处理中的节点集合。
     * @return 如果所有子节点都已处理，则返回true；否则返回false。
     */
    private boolean processChildren(ObjectInfo currentObject, Deque<Integer> stack, Set<Integer> processing) {
        boolean allChildrenProcessed = true;

        for (int childId : currentObject.getRefIdList()) {
            // 子节点未缓存时的处理
            if (!sizeCache.containsKey(childId)) {
                allChildrenProcessed = false;
                handleUncachedChild(childId, stack, processing);
            }
        }

        return allChildrenProcessed;
    }

    /**
     * 处理未缓存的子节点
     *
     * @param childId 子节点的ID
     * @param stack 用于深度优先搜索的栈
     * @param processing 正在处理中的节点集合
     */
    private void handleUncachedChild(int childId, Deque<Integer> stack, Set<Integer> processing) {
        if (processing.contains(childId)) {
            // 处理循环引用：直接设为0避免死循环
            sizeCache.put(childId, 0);
        } else {
            // 正常入栈处理
            stack.push(childId);
            processing.add(childId);
        }
    }

    /**
     * 计算并缓存当前节点的大小
     *
     * @param currentId currentId
     * @param currentObject currentObject
     * @param stack stack
     * @param processing processing
     */
    private void calculateAndCacheNodeSize(int currentId, ObjectInfo currentObject, Deque<Integer> stack,
        Set<Integer> processing) {
        sizeCache.put(currentId, getSelfSize(currentId));
        stack.pop();
        processing.remove(currentId);
    }

    // 处理可达节点
    private void processReachableNodes() {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(CjBuildTreeService.ROOT_NODE_ID);

        while (!stack.isEmpty()) {
            int currentId = stack.pop();

            if (visited.contains(currentId)) {
                continue;
            }

            // 获取节点类型（Type）
            int type = (currentId == ROOT_NODE_ID)
                ? ROOT_NODE_TYPE
                : instanceDumpMap.get(currentId) != null
                    ? instanceDumpMap.get(currentId).getObjectType().getValue()
                    : -1;

            // 获取子节点列表
            List<Integer> children = getChildren(currentId);
            int edgeCount = children.size();
            int realSize = (currentId == ROOT_NODE_ID) ? 0 : sizeCache.getOrDefault(currentId, 0);
            int name = getName(currentId);
            // 添加当前节点到结果列表
            addNodeToResult(NodeInfo.builder()
                .id(currentId)
                .name(name)
                .edgeCount(edgeCount)
                .selfSize(realSize)
                .type(type)  // 存储节点类型
                .build(), currentId);

            // 逆序添加子节点，保证DFS顺序
            for (int i = children.size() - 1; i >= 0; i--) {
                int childId = children.get(i);
                if (!visited.contains(childId)) {
                    stack.push(childId);
                }
            }
        }
    }

    // 处理不可达节点
    private void processUnreachableNodes() {
        Set<Integer> unreachableNodes = new HashSet<>();
        for (ObjectInfo object : instanceDumpMap.values()) {
            int nodeId = object.getId();
            if (!visited.contains(nodeId)) {
                unreachableNodes.add(nodeId);
            }
        }

        if (unreachableNodes.isEmpty()) {
            return;
        }

        Set<Integer> unreachableRoots = findUnreachableRoots(unreachableNodes);

        for (int rootId : unreachableRoots) {
            iterativeUnreachableDfs(rootId, unreachableNodes);
        }
    }

    /**
     * 处理不可达节点
     *
     * @param startId startId
     * @param unreachableNodes unreachableNodes
     */
    private void iterativeUnreachableDfs(int startId, Set<Integer> unreachableNodes) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(startId);

        while (!stack.isEmpty()) {
            int currentId = stack.pop();

            if (!unreachableNodes.contains(currentId) || visited.contains(currentId)) {
                continue;
            }

            ObjectInfo object = instanceDumpMap.get(currentId);
            if (object == null) {
                continue;
            }

            int type = object.getObjectType().getValue();
            int name = getName(currentId);
            // 添加当前节点到结果列表
            addNodeToResult(NodeInfo.builder()
                .id(currentId)
                .name(name)
                .edgeCount(object.getRefIdList().size())
                .selfSize(sizeCache.getOrDefault(currentId, 0))
                .type(type)  // 存储节点类型
                .build(), currentId);

            // 逆序添加子节点
            List<Integer> children = object.getRefIdList();
            for (int i = children.size() - 1; i >= 0; i--) {
                int childId = children.get(i);
                if (unreachableNodes.contains(childId) && !visited.contains(childId)) {
                    stack.push(childId);
                }
            }
        }
    }

    private int getName(int currentId) {
        int name = 0;
        if (instanceDumpMap.containsKey(currentId)) {
            name = instanceDumpMap.get(currentId).getName();
        }
        return name;
    }

    /**
     * 添加节点到结果列表
     *
     * @param nodeInfo nodeInfo
     * @param nodeId nodeId
     */
    private void addNodeToResult(NodeInfo nodeInfo, int nodeId) {
        nodeList.add(nodeInfo);

        // 记录节点索引
        int nodeIndex = nodeList.size() - 1;
        idToIndex.put(nodeId, nodeIndex);

        // 标记为已访问
        visited.add(nodeId);
    }

    /**
     * 找出不可达节点中的根节点
     *
     * @param unreachableNodes 不可达节点的集合
     * @return 不可达节点中的根节点集合
     */
    private Set<Integer> findUnreachableRoots(Set<Integer> unreachableNodes) {
        Set<Integer> referencedNodes = new HashSet<>();

        for (int nodeId : unreachableNodes) {
            ObjectInfo object = instanceDumpMap.get(nodeId);
            if (object == null) {
                continue;
            }
            for (int childId : object.getRefIdList()) {
                if (unreachableNodes.contains(childId)) {
                    referencedNodes.add(childId);
                }
            }
        }

        Set<Integer> unreachableRoots = new HashSet<>();
        for (int nodeId : unreachableNodes) {
            if (!referencedNodes.contains(nodeId)) {
                unreachableRoots.add(nodeId);
            }
        }

        if (unreachableRoots.isEmpty()) {
            unreachableRoots.add(unreachableNodes.iterator().next());
        }

        return unreachableRoots;
    }

    /**
     * 获取节点的子节点列表
     *
     * @param currentId currentId
     * @return List<Integer> refIdList
     */
    private List<Integer> getChildren(int currentId) {
        if (currentId == ROOT_NODE_ID) {
            return new ArrayList<>(rootSet);
        }

        ObjectInfo object = instanceDumpMap.get(currentId);
        if (object != null) {
            return new ArrayList<>(object.getRefIdList());
        }

        return new ArrayList<>();
    }

    private void setNodeNameIndex(ObjectInfo node) {
        if (node instanceof PrimitiveArrayDump) {
            // 基本类型数组名称
            node.setName(0);
            return;
        }
        if (!loadClassMap.containsKey(node.getCls())) {
            LOGGER.warn("node does not have class name, classId:{}", node.getCls());
            return;
        }
        node.setName(loadClassMap.get(node.getCls()));
    }

    private int getSelfSize(int id) {
        int size = 0;
        ObjectInfo objectInfo = instanceDumpMap.get(id);
        if (objectInfo == null) {
            return size;
        }
        if (objectInfo.getObjectType() == NodeTypeEnum.OBJECT) {
            size = classSizeMap.get(objectInfo.getCls());
            objectInfo.setSize(size);
            return size;
        }
        if (objectInfo instanceof PrimitiveArrayDump primitiveArrayDump) {
            size = primitiveArrayDump.getNum() * MemTypeEnum.getSize(primitiveArrayDump.getType());
            objectInfo.setSize(size);
            return size;
        }
        if (objectInfo instanceof StructArrayDump structArrayDump) {
            // STRUCT ARRAY DUMP的num存储了总共有多少个基本类型以及引用对象的指针
            size += structArrayDump.getNum() * NODE_FIELD_LENGTH;
        } else {
            // OBJECT ARRAY DUMP需要加上指针的大小
            int refNum = objectInfo.getRefIdList().size();
            size += refNum * NODE_FIELD_LENGTH;
        }
        objectInfo.setSize(size);
        return size;
    }
}
