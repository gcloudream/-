package com.silemore.sileme.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "暂无"
    return runCatching { LocalDate.parse(value).format(dateFormatter) }.getOrDefault(value)
}

fun formatTime(value: String?): String {
    if (value.isNullOrBlank()) return "暂无"
    val asDateTime = runCatching { LocalDateTime.parse(value).toLocalTime() }.getOrNull()
    if (asDateTime != null) {
        return asDateTime.format(timeFormatter)
    }
    return runCatching { LocalTime.parse(value).format(timeFormatter) }.getOrDefault(value)
}

fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "暂无"
    return runCatching { LocalDateTime.parse(value).format(dateTimeFormatter) }.getOrDefault(value)
}
