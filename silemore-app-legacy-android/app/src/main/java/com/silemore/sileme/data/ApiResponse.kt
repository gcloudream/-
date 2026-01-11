package com.silemore.sileme.data

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: String?
)
