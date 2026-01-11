package com.silemore.sileme.data

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val agreeTerms: Boolean
)

data class RegisterResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val createdAt: String?
)

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean? = true
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: LoginUser
) {
    data class LoginUser(
        val id: Long,
        val email: String,
        val nickname: String,
        val alertDays: Int,
        val reminderTime: String,
        val isPaused: Boolean,
        val hasCheckedInToday: Boolean,
        val streakDays: Int
    )
}
