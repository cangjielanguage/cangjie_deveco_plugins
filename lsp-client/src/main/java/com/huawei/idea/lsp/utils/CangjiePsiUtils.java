/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.idea.lsp.utils;

import com.huawei.idea.language.CangJieTypes;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * CommonUtils
 *
 * @since 2025-06-19
 */
public class CangjiePsiUtils {
    private static final Set<String> FILE_EXTENSIONS = Set.of(LanguageManager.CANGJIE_EXTENSION);

    /**
     * Skip whitespace psi element.
     *
     * @param element the element
     * @param isToRight the to right
     * @return the psi element
     */
    @Nullable
    public static PsiElement skipWhitespace(@Nullable PsiElement element, boolean isToRight) {
        if (element == null) {
            return null;
        }
        PsiElement temp = isToRight ? element.getNextSibling() : element.getPrevSibling();
        while (temp instanceof PsiWhiteSpace) {
            temp = isToRight ? temp.getNextSibling() : temp.getPrevSibling();
        }
        return temp;
    }

    /**
     * Delete comma separated element.
     *
     * @param element the element
     */
    public static void deleteCommaSeparatedElement(PsiElement element) {
        // First, check backwards. if there is a comma
        // it indicates that the element is not the last parameter
        // delete the element and the comma
        ASTNode node = element.getNode();
        ASTNode check = node;
        ASTNode foundComma = null;
        while ((check = check.getTreeNext()) != null) {
            if (check.getElementType() == CangJieTypes.COMMA) {
                foundComma = check;
                break;
            }
        }

        // Otherwise, look forwards. In this case
        // the element is the last parameter
        // delete the element and the comma of the prev element
        if (check == null) {
            check = node;
            while ((check = check.getTreePrev()) != null) {
                if (check.getElementType() == CangJieTypes.COMMA) {
                    foundComma = check;
                } else if (foundComma != null) {
                    break;
                } else {
                    continue;
                }
            }
        }

        // Delete element and any discovered comma.
        // Keep whitespaces
        ASTNode parentNode = element.getParent().getNode();
        parentNode.removeChild(node);
        if (foundComma != null) {
            parentNode.removeChild(foundComma);
        }
    }

    /**
     * 判断是否是 cangjie 文件
     *
     * @param file file
     * @return 是否是cangjie文件
     */
    public static boolean isCangjieFile(PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        String extension = virtualFile.getExtension();
        if (StringUtils.isEmpty(extension)) {
            return false;
        }
        return FILE_EXTENSIONS.contains(extension);
    }
}
