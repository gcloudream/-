package com.silemore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.silemore.util.TimeUtil;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int code;
    private String message;
    private List<FieldErrorDetail> errors;
    private OffsetDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(int code, String message, List<FieldErrorDetail> errors, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(int code, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(code, message, errors, TimeUtil.nowOffset());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorDetail> getErrors() {
        return errors;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
