package com.silemore.sileme.data

data class ContactCreateRequest(
    val name: String,
    val email: String,
    val relationship: String? = null,
    val message: String? = null
)

data class ContactCreateResponse(
    val id: Long,
    val name: String,
    val email: String,
    val relationship: String?,
    val isVerified: Boolean,
    val verifyEmailSentAt: String?,
    val createdAt: String?
)

data class ContactListResponse(
    val contacts: List<ContactItem>,
    val total: Int,
    val limit: Int,
    val remaining: Int
) {
    data class ContactItem(
        val id: Long,
        val name: String,
        val email: String,
        val relationship: String? = null,
        val isVerified: Boolean,
        val createdAt: String?
    )
}

data class ContactVerifyRequest(
    val token: String
)
