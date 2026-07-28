/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.config;

import static com.huawei.cangjie.projectmgmt.utils.Constants.CANGJIE_PACKAGE_NAME;
import static com.huawei.cangjie.projectmgmt.utils.Constants.MODULE_NAME;

import com.huawei.deveco.projectmgmt.ohos.template.render.RenderHashMap;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * 读取全局持久化文件
 *
 * @since 2025-11-20
 */
public class ConfigPathHandler {
    static String packageName;

    static boolean enabled;

    private static final Logger LOG = Logger.getInstance(ConfigPathHandler.class);

    /**
     * 获取跨操作系统的配置文件真实路径
     * Windows: .../options/windows/cangjie_settings.xml
     * macOS: .../options/mac/cangjie_settings.xml
     *
     * @return 路径
     */
    public static String getRealConfigPath() {
        String basePath = PathManager.getConfigPath();
        String osSubDir = getOSSubDirectory();
        return Paths.get(basePath, "options", osSubDir, "cangjie_settings.xml").toString();
    }

    private static String getOSSubDirectory() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        }
        if (osName.contains("mac")) {
            return "mac";
        }
        return "";
    }

    /**
     * 加载配置
     */
    public static void loadConfig() {
        // 获取真实路径
        String configPath = getRealConfigPath();
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            LOG.warn("cjPackageName config file doesn't exist");
            return;
        }

        try {
            // 创建DocumentBuilderFactory实例
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            // 禁用外部实体解析
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            // 禁用外部DTD加载
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 创建DocumentBuilder实例
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            XPath xpath = XPathFactory.newInstance().newXPath();

            String pkgExpr = "(/application/component/option[@name='packageName']/@value)[1]";
            Object pkgNameObj = xpath.evaluate(pkgExpr, doc, XPathConstants.NODE);
            Node pkgNode = null;
            if (pkgNameObj instanceof Node) {
                pkgNode = (Node) pkgNameObj;
            }
            if (pkgNode != null) {
                packageName = pkgNode.getNodeValue();
            }
            String enableExpr = "(/application/component/option[@name='enabled']/@value)[1]";
            Object enabledObj = xpath.evaluate(enableExpr, doc, XPathConstants.NODE);
            Node enableNode = null;
            if (enabledObj instanceof Node) {
                enableNode = (Node) enabledObj;
            }
            if (enableNode != null) {
                enabled = Boolean.parseBoolean(enableNode.getNodeValue());
            }
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
            LOG.error("read " + configPath + " config error");
        }
    }

    /**
     * 替换包名
     *
     * @param renderParameterMap renderParameterMap
     */
    public static void replaceCjPackageName(@NotNull RenderHashMap renderParameterMap) {
        loadConfig();
        if (enabled) {
            renderParameterMap.put(CANGJIE_PACKAGE_NAME, packageName + "_" + renderParameterMap.getString(MODULE_NAME));
        }
    }
}