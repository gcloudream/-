package com.silemore.sileme.data

data class UserProfileResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val alertDays: Int,
    val reminderTime: String,
    val reminderEnabled: Boolean,
    val isPaused: Boolean,
    val pauseUntil: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val stats: Stats,
    val contacts: Contacts
) {
    data class Stats(
        val totalCheckIns: Long,
        val currentStreak: Int,
        val longestStreak: Int,
        val lastCheckInAt: String?
    )

    data class Contacts(
        val total: Int,
        val verified: Int
    )
}

data class UpdateSettingsRequest(
    val alertDays: Int,
    val reminderTime: String,
    val reminderEnabled: Boolean
)

data class SettingsResponse(
    val alertDays: Int,
    val reminderTime: String,
    val reminderEnabled: Boolean,
    val isPaused: Boolean,
    val pauseUntil: String?
)

data class UpdateProfileRequest(
    val nickname: String
)

data class UpdateProfileResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val updatedAt: String?
)

data class PauseRequest(
    val action: String,
    val duration: Int? = null,
    val reason: String? = null
)

data class PauseResponse(
    val isPaused: Boolean,
    val pauseUntil: String?,
    val reason: String?
)
