/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.ohos.impl;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.cangjie.debugger.utils.LogUtils;

import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * OHOS symbol files info
 *
 * @since 2024-3-12
 */
public class LocalSymbolFilesInfo {
    private static final Logger LOGGER = Logger.getInstance(LocalSymbolFilesInfo.class);

    private Set<File> symbolDirs = Sets.newLinkedHashSet();

    private Set<SymbolFileInfo> symbolFileInfoSet = new CopyOnWriteArraySet<>();

    /**
     * add symbol files
     *
     * @param consoleOutput the output from server
     */
    public void addSymbolFiles(String consoleOutput) {
        String[] libraryOutputList = consoleOutput.split("\n");
        for (String libraryOutput : libraryOutputList) {
            if (!libraryOutput.contains("library-loaded")) {
                continue;
            }
            File soFile = CodeCheckByPassUtils.createFile(getTargetNameFromOutput(libraryOutput));
            if (!soFile.exists()) {
                continue;
            }
            symbolDirs.stream().filter(symbolDir -> {
                try {
                    return soFile.getCanonicalPath().startsWith(symbolDir.getCanonicalPath()) && !isContainFile(soFile);
                } catch (IOException e) {
                    LogUtils.printCangjieLogWarn(LOGGER, "get path error occurred: " + ExceptionUtils.getNonNullMsg(e));
                }
                return false;
            }).forEach(symbolDir -> {
                String codeRange = getCodeRangeFromOutput(libraryOutput);
                String[] codeRangeArr = codeRange.split("~");
                SymbolFileInfo symbolFileInfo = new SymbolFileInfo(soFile, codeRangeArr[0], codeRangeArr[1]);
                symbolFileInfoSet.add(symbolFileInfo);
            });
        }
    }

    /**
     * check the address is valid
     *
     * @param address assembly Address
     * @return true if the address is in range
     */
    public boolean isAddressValid(String address) {
        for (SymbolFileInfo symbolFile : symbolFileInfoSet) {
            if (address.compareTo(symbolFile.getMinCodeRange()) >= 0
                    && address.compareTo(symbolFile.getMaxCodeRange()) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * set symbol dirs
     *
     * @param symbolDirs symbol dirs
     */
    public void setSymbolDirs(Set<File> symbolDirs) {
        this.symbolDirs = symbolDirs;
    }

    private String getTargetNameFromOutput(String output) {
        String[] outputArray = output.split("target-name=");
        String soFilePath = outputArray[1].split("host-name")[0];
        return soFilePath.substring(1, soFilePath.length() - 2);
    }

    private String getCodeRangeFromOutput(String output) {
        String[] outputArray = output.split("codeRange=");
        if (outputArray.length < 2 || outputArray[1].length() < 2) {
            LogUtils.printCangjieLogWarn(LOGGER, "Invalid codeRange format in output: " + output);
            return "";
        }
        return outputArray[1].substring(1, outputArray[1].length() - 1);
    }

    private boolean isContainFile(File file) throws IOException {
        for (SymbolFileInfo symbolFileInfo : symbolFileInfoSet) {
            if (symbolFileInfo.getSoFile().getCanonicalPath().equals(file.getCanonicalPath())) {
                return true;
            }
        }
        return false;
    }

    private static class SymbolFileInfo {
        private File soFile;

        private String minCodeRange;

        private String maxCodeRange;

        private SymbolFileInfo(File soFile, String minCodeRange, String maxCodeRange) {
            this.soFile = soFile;
            this.minCodeRange = minCodeRange;
            this.maxCodeRange = maxCodeRange;
        }

        public String getMinCodeRange() {
            return minCodeRange;
        }

        public String getMaxCodeRange() {
            return maxCodeRange;
        }

        public File getSoFile() {
            return soFile;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
