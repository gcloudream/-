package com.silemore.sileme.network

class ApiException(
    message: String,
    val code: Int? = null
) : Exception(message)
