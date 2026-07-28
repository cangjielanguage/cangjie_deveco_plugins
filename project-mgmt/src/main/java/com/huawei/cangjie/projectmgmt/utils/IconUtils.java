/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.utils;

import com.intellij.util.ImageLoader;
import com.intellij.util.ui.JBImageIcon;

import java.awt.Image;
import java.net.URL;
import java.util.Objects;

/**
 * IconUtils
 *
 * @since 2022-10-18
 */
public class IconUtils {
    /**
     * Get icon from resource
     *
     * @param path the icon path
     * @return JBImageIcon target icon
     */
    public static JBImageIcon getIconFromResource(String path) {
        URL iconURL = Objects.requireNonNull(IconUtils.class.getClassLoader().getResource(path));
        Image iconImage = Objects.requireNonNull(ImageLoader.loadFromUrl(iconURL));
        return new JBImageIcon(iconImage);
    }
}
