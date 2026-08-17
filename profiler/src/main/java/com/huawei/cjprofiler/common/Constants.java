/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common;

/**
 * Constants
 *
 * @since 2025/4/22
 */
public class Constants {
    /**
     * package root name
     */
    public static final String PACKAGE_ROOT_NAME = "com.huawei.cjprofiler";

    /**
     * debug mode
     */
    public static final boolean DEBUG_MODE = true;

    /**
     * root path of controller
     */
    public static final String CONTROLLER_ROOT_PATH = "com/huawei/cjprofiler/controller";

    /**
     * jar url separator
     */
    public static final String JAR_URL_SEPARATOR = "!/";

    /**
     * cjprof file type
     */
    public static final String CJPROF_FILE_TYPE = "cjheapdump";

    /**
     * URL静态类
     */
    public static final class URL {
        /**
         * scheme name
         */
        public static final String SCHEME_NAME = "http";

        /**
         * url prefix
         */
        public static final String URL_PREFIX = SCHEME_NAME + "://";

        /**
         * domain name
         */
        public static final String DOMAIN_NAME = "localhost";

        /**
         * cangjie profiler prefix
         */
        public static final String CANGJIE_PROFILER_PREFIX = "cjprofiler";
    }
}
