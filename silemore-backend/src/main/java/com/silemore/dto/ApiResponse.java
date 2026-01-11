package com.silemore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.silemore.util.TimeUtil;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private OffsetDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, TimeUtil.nowOffset());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(200, "success", data);
    }

    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(200, message, null, TimeUtil.nowOffset());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
