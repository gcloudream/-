package com.silemore.sileme.network

import com.silemore.sileme.data.ApiResponse
import com.silemore.sileme.data.CheckInHistoryResponse
import com.silemore.sileme.data.CheckInResponse
import com.silemore.sileme.data.CheckInTodayResponse
import com.silemore.sileme.data.ContactCreateRequest
import com.silemore.sileme.data.ContactCreateResponse
import com.silemore.sileme.data.ContactListResponse
import com.silemore.sileme.data.ContactVerifyRequest
import com.silemore.sileme.data.LoginRequest
import com.silemore.sileme.data.LoginResponse
import com.silemore.sileme.data.PauseRequest
import com.silemore.sileme.data.PauseResponse
import com.silemore.sileme.data.RegisterRequest
import com.silemore.sileme.data.RegisterResponse
import com.silemore.sileme.data.SettingsResponse
import com.silemore.sileme.data.UpdateProfileRequest
import com.silemore.sileme.data.UpdateProfileResponse
import com.silemore.sileme.data.UpdateSettingsRequest
import com.silemore.sileme.data.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("users/logout")
    suspend fun logout(): Response<Unit>

    @GET("users/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfileResponse>>

    @PATCH("users/me/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<ApiResponse<SettingsResponse>>

    @PATCH("users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UpdateProfileResponse>>

    @POST("users/me/pause")
    suspend fun pause(@Body request: PauseRequest): Response<ApiResponse<PauseResponse>>

    @POST("check-ins")
    suspend fun checkIn(): Response<ApiResponse<CheckInResponse>>

    @GET("check-ins/today")
    suspend fun today(): Response<ApiResponse<CheckInTodayResponse>>

    @GET("check-ins")
    suspend fun history(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<CheckInHistoryResponse>>

    @GET("contacts")
    suspend fun contacts(): Response<ApiResponse<ContactListResponse>>

    @POST("contacts")
    suspend fun addContact(
        @Body request: ContactCreateRequest
    ): Response<ApiResponse<ContactCreateResponse>>

    @DELETE("contacts/{id}")
    suspend fun deleteContact(@Path("id") id: Long): Response<Unit>

    @POST("contacts/verify")
    suspend fun verifyContact(
        @Body request: ContactVerifyRequest
    ): Response<ApiResponse<Unit>>
}
