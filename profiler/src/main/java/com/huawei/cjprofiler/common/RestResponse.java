/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cjprofiler.common;

/**
 * 智慧化调优接口统一返回值类型。如果返回的 code 不为 200，证明操作失败，可以查看 message 查看具体信息
 *
 * @param <T>
 * @since 2024-12
 */
public class RestResponse<T> {
    private int code;

    private T data;

    private String message;

    public RestResponse() {
    }

    public RestResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * success
     *
     * @param data T
     * @param message String
     * @return RestResponse<></>
     */
    public static <T> RestResponse<T> success(T data, String message) {
        return new RestResponse<>(RestCode.SUCCESS.code, data, message);
    }

    /**
     * success
     *
     * @param message String
     * @return RestResponse<></>
     */
    public static RestResponse<String> success(String message) {
        return new RestResponse<>(RestCode.SUCCESS.code, "", message);
    }

    /**
     * failure
     *
     * @param data T
     * @param message String
     * @return RestResponse<></>
     */
    public static <T> RestResponse<T> failure(T data, String message) {
        return new RestResponse<>(RestCode.FAILURE.code, data, message);
    }

    /**
     * failure
     *
     * @param message String
     * @return RestResponse<></>
     */
    public static RestResponse<String> failure(String message) {
        return new RestResponse<>(RestCode.FAILURE.code, "", message);
    }

    /**
     * isFailed
     *
     * @return isFailed boolean
     */
    public boolean isFailed() {
        return this.code != RestCode.SUCCESS.code;
    }

    /**
     * getCode
     *
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     * setCode
     *
     * @param code int
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * getCode
     *
     * @return data
     */
    public T getData() {
        return data;
    }

    /**
     * setData
     *
     * @param data T
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * getMessage
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage
     *
     * @param message String
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * RestCode
     *
     * @since 2025/4/22
     */
    public enum RestCode {
        SUCCESS(200),
        FAILURE(400);

        private final int code;

        RestCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }
}
