package com.silemore.sileme.data

data class ErrorResponse(
    val code: Int,
    val message: String,
    val errors: List<FieldErrorDetail>?,
    val timestamp: String?
)
