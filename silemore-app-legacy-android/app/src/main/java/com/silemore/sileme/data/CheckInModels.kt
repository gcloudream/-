package com.silemore.sileme.data

data class CheckInResponse(
    val id: Long,
    val checkInDate: String,
    val checkInTime: String,
    val streakDays: Int,
    val isNewRecord: Boolean,
    val encouragement: String?
)

data class CheckInTodayResponse(
    val hasCheckedIn: Boolean,
    val checkIn: CheckInItem?,
    val stats: Stats
) {
    data class CheckInItem(
        val id: Long,
        val checkInDate: String,
        val checkInTime: String
    )

    data class Stats(
        val currentStreak: Int,
        val missedDays: Long,
        val alertThreshold: Int,
        val lastCheckInAt: String?
    )
}

data class CheckInHistoryResponse(
    val content: List<CheckInHistoryItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    data class CheckInHistoryItem(
        val id: Long,
        val checkInDate: String,
        val checkInTime: String
    )
}
