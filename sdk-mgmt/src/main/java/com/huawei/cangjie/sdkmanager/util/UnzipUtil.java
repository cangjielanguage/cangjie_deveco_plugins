/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.sdkmanager.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Enumeration;
import java.util.Locale;

/**
 * The type Unzip util.
 *
 * @since 2024-7-6
 */
public class UnzipUtil {
    /**
     * The constant BUFFER_SIZE.
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * The type Zip verifier.
     */
    public static final class ZipVerifier {
        /**
         * 解压后的Zip文件最大数量，含目录、文件、符号链接等
         */
        private static final int MAX_ZIP_ENTRY_NUM = 100_000;

        /**
         * 解压后的zip文件总大小的最大size，20G
         */
        private static final long MAX_ZIP_SIZE = 20 * 1024 * 1024 * 1024L;

        private int num;

        private long size;

        /**
         * Instantiates a new Zip verifier.
         *
         * @param num the num
         * @param size the size
         */
        public ZipVerifier(int num, long size) {
            this.num = num;
            this.size = size;
        }

        /**
         * Inc and check num.
         *
         * @throws IOException the io exception
         */
        public void incAndCheckNum() throws IOException {
            num++;
            if (num > MAX_ZIP_ENTRY_NUM) {
                throw new IOException("Zip entry is too many, exceed max " + MAX_ZIP_ENTRY_NUM);
            }
        }

        /**
         * Gets size.
         *
         * @return the size
         */
        public long getSize() {
            return size;
        }

        /**
         * Inc and check size.
         *
         * @param size the size
         * @throws IOException the io exception
         */
        public void incAndCheckSize(long size) throws IOException {
            this.size += size;
            if (this.size > MAX_ZIP_SIZE) {
                throw new IOException(
                    String.format(Locale.ENGLISH, "Zip size is too big, exceed %s bytes", MAX_ZIP_SIZE));
            }
        }
    }

    /**
     * Unzip.
     *
     * @param srcFile the src file
     * @param target the target
     * @throws IOException the io exception
     */
    public static void unzip(File srcFile, File target) throws IOException {
        try (ZipFile zipFile = new ZipFile(srcFile)) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            ZipVerifier zipVerifier = new ZipVerifier(0, 0L);
            while (entries.hasMoreElements()) {
                processFiles(target, zipVerifier, entries, zipFile);
            }
        }
    }

    /**
     * processFiles
     *
     * @param target the target
     * @param zipVerifier ZipVerifier
     * @param entries Enumeration<ZipArchiveEntry>
     * @param zipFile ZipFile
     * @throws IOException IOException
     */
    private static void processFiles(File target, ZipVerifier zipVerifier,
                                     Enumeration<ZipArchiveEntry> entries, ZipFile zipFile) throws IOException {
        zipVerifier.incAndCheckNum();
        ZipArchiveEntry entry = entries.nextElement();
        String normalizedName = StringUtil.normalize(entry.getName())
            .orElseThrow(() -> new IOException("Invalid file name: " + entry.getName()));
        String canonicalPath = sanitizeFileName(normalizedName, target.getCanonicalPath());
        File entryFile = new File(canonicalPath);
        if (entry.isUnixSymlink()) {
            try (ByteArrayOutputStream targetByteStream = new ByteArrayOutputStream()) {
                readZipEntry(zipFile, entry, targetByteStream, zipVerifier);
                createSymbolicLink(entryFile, targetByteStream);
            }
        } else if (entry.isDirectory()) {
            createDirs(entryFile);
        } else {
            createParentDirectoryAndFile(entryFile);
            if (StringUtils.isEmpty(entryFile.getCanonicalPath())) {
                return;
            }
            readZipEntry(zipFile, entry,
                    Files.newOutputStream(Paths.get(entryFile.getCanonicalPath())),
                    zipVerifier);
        }
        copyPermission(entry, entryFile);
    }

    /**
     * windows读不到权限，暂时不设置权限
     * 如果目标文件是一个symbolic link,这个时候也不设置权限
     *
     * @param srcEntry srcEntry
     * @param targetFile targetFile
     * @throws IOException IOException
     */
    private static void copyPermission(ZipArchiveEntry srcEntry, File targetFile) throws IOException {
        if (OsTypeUtil.isWindows() || srcEntry.isUnixSymlink()) {
            return;
        }
        int unixMode = srcEntry.getUnixMode();
        int safeMode = unixMode & 0755;
        Path path = targetFile.toPath();
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(convertUnixMode2PosixString(safeMode)));
    }

    private static String convertUnixMode2PosixString(int mode) {
        return String.valueOf((mode & 0400) == 0 ? '-' : 'r') + ((mode & 0200) == 0 ? '-' : 'w') + ((mode & 0100) == 0
                ? '-'
                : 'x') + ((mode & 0040) == 0 ? '-' : 'r') + ((mode & 0020) == 0 ? '-' : 'w') + ((mode & 0010) == 0
                ? '-'
                : 'x') + ((mode & 0004) == 0 ? '-' : 'r') + ((mode & 0002) == 0 ? '-' : 'w') + ((mode & 0001) == 0
                ? '-'
                : 'x');
    }

    private static void createParentDirectoryAndFile(File entryFile) throws IOException {
        if (!entryFile.exists()) {
            createDirs(entryFile.getParentFile());
            if (!entryFile.createNewFile()) {
                throw new IOException("Failed to create file " + entryFile);
            }
        }
    }

    private static void readZipEntry(ZipFile zipFile, ZipArchiveEntry entry, OutputStream dest, ZipVerifier zipVerifier)
            throws IOException {
        int size;
        byte[] buf = new byte[BUFFER_SIZE];
        try (dest;
             BufferedOutputStream bufferedDest = new BufferedOutputStream(dest);
             InputStream is = new BufferedInputStream(zipFile.getInputStream(entry))) {
            while ((size = is.read(buf)) > -1) {
                zipVerifier.incAndCheckSize(size);
                bufferedDest.write(buf, 0, size);
            }
        }
    }

    private static void createSymbolicLink(File entryFile, ByteArrayOutputStream targetByteStream) throws IOException {
        Path linkPath = entryFile.toPath();
        Path linkTarget = new File(targetByteStream.toString(StandardCharsets.UTF_8)).toPath();
        createParentDirectory(entryFile);
        Files.createSymbolicLink(linkPath, linkTarget);
    }

    private static void createParentDirectory(File entryFile) throws IOException {
        File parent = entryFile.getParentFile();
        createDirs(parent);
    }

    private static void createDirs(File file) throws IOException {
        if (file != null && !file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("Failed to create directory of file " + file);
            }
        }
    }

    private static String sanitizeFileName(String fileName, String dir) throws IOException {
        String filePath = Paths.get(dir, fileName).normalize().toString();
        String dirPath = Paths.get(dir).normalize().toString();
        if (!dirPath.endsWith(File.separator)) {
            dirPath += File.separator;
        }
        if (filePath.startsWith(dirPath)) {
            return filePath;
        }
        throw new IOException("Invalid file: " + fileName);
    }
}
