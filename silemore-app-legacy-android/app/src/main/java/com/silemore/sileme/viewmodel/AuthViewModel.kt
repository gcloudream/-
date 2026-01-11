package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import com.silemore.sileme.data.LoginRequest
import com.silemore.sileme.data.RegisterRequest
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    suspend fun login(email: String, password: String, rememberMe: Boolean): Boolean {
        _state.value = AuthUiState(isLoading = true)
        return try {
            repository.login(LoginRequest(email, password, rememberMe))
            _state.value = AuthUiState()
            true
        } catch (ex: ApiException) {
            _state.value = AuthUiState(error = ex.message ?: "登录失败")
            false
        } catch (ex: Exception) {
            _state.value = AuthUiState(error = "登录失败，请稍后重试")
            false
        }
    }

    suspend fun register(email: String, password: String, nickname: String): Boolean {
        _state.value = AuthUiState(isLoading = true)
        return try {
            repository.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    nickname = nickname,
                    agreeTerms = true
                )
            )
            _state.value = AuthUiState()
            true
        } catch (ex: ApiException) {
            _state.value = AuthUiState(error = ex.message ?: "注册失败")
            false
        } catch (ex: Exception) {
            _state.value = AuthUiState(error = "注册失败，请稍后重试")
            false
        }
    }

    suspend fun logout() {
        _state.value = AuthUiState(isLoading = true)
        try {
            repository.logout()
        } finally {
            _state.value = AuthUiState()
        }
    }
}
