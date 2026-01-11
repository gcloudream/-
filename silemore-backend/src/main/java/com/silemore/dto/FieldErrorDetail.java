package com.silemore.dto;

public class FieldErrorDetail {
    private String field;
    private String message;

    public FieldErrorDetail() {
    }

    public FieldErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
