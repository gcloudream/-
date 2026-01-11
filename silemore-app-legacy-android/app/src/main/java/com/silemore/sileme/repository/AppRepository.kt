package com.silemore.sileme.repository

import com.silemore.sileme.data.ApiResponse
import com.silemore.sileme.data.CheckInHistoryResponse
import com.silemore.sileme.data.CheckInResponse
import com.silemore.sileme.data.CheckInTodayResponse
import com.silemore.sileme.data.ContactCreateRequest
import com.silemore.sileme.data.ContactCreateResponse
import com.silemore.sileme.data.ContactListResponse
import com.silemore.sileme.data.LoginRequest
import com.silemore.sileme.data.LoginResponse
import com.silemore.sileme.data.PauseRequest
import com.silemore.sileme.data.PauseResponse
import com.silemore.sileme.data.RegisterRequest
import com.silemore.sileme.data.RegisterResponse
import com.silemore.sileme.data.SettingsResponse
import com.silemore.sileme.data.UpdateSettingsRequest
import com.silemore.sileme.data.UserProfileResponse
import com.silemore.sileme.data.ErrorResponse
import com.silemore.sileme.network.ApiClient
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.network.ApiService
import com.silemore.sileme.storage.TokenStore
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AppRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val errorAdapter: JsonAdapter<ErrorResponse>
) {
    companion object {
        fun create(apiClient: ApiClient, tokenStore: TokenStore): AppRepository {
            val adapter = apiClient.moshi.adapter(ErrorResponse::class.java)
            return AppRepository(apiClient.service, tokenStore, adapter)
        }
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return execute { api.register(request) }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val result = execute { api.login(request) }
        tokenStore.saveToken(result.accessToken)
        return result
    }

    suspend fun logout() {
        executeWithoutBody { api.logout() }
        tokenStore.clear()
    }

    suspend fun getProfile(): UserProfileResponse {
        return execute { api.getProfile() }
    }

    suspend fun updateSettings(request: UpdateSettingsRequest): SettingsResponse {
        return execute { api.updateSettings(request) }
    }

    suspend fun pause(request: PauseRequest): PauseResponse {
        return execute { api.pause(request) }
    }

    suspend fun checkIn(): CheckInResponse {
        return execute { api.checkIn() }
    }

    suspend fun today(): CheckInTodayResponse {
        return execute { api.today() }
    }

    suspend fun history(
        page: Int,
        size: Int,
        startDate: String? = null,
        endDate: String? = null
    ): CheckInHistoryResponse {
        return execute { api.history(page, size, startDate, endDate) }
    }

    suspend fun contacts(): ContactListResponse {
        return execute { api.contacts() }
    }

    suspend fun addContact(request: ContactCreateRequest): ContactCreateResponse {
        return execute { api.addContact(request) }
    }

    suspend fun deleteContact(id: Long) {
        executeWithoutBody { api.deleteContact(id) }
    }

    private suspend fun <T> execute(
        call: suspend () -> Response<ApiResponse<T>>
    ): T = withContext(Dispatchers.IO) {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
                ?: throw ApiException("服务返回为空")
            return@withContext body.data
                ?: throw ApiException(body.message.ifBlank { "服务返回为空" }, body.code)
        }

        throw ApiException(parseError(response), response.code())
    }

    private suspend fun executeWithoutBody(
        call: suspend () -> Response<Unit>
    ) = withContext(Dispatchers.IO) {
        val response = call()
        if (!response.isSuccessful) {
            throw ApiException(parseError(response), response.code())
        }
    }

    private fun parseError(response: Response<*>): String {
        val raw = response.errorBody()?.string()
        if (raw.isNullOrBlank()) {
            return "请求失败 (${response.code()})"
        }
        val parsed = runCatching { errorAdapter.fromJson(raw) }.getOrNull()
        return parsed?.message?.ifBlank { null } ?: "请求失败 (${response.code()})"
    }
}
