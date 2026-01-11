package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silemore.sileme.data.PauseRequest
import com.silemore.sileme.data.UpdateSettingsRequest
import com.silemore.sileme.data.UserProfileResponse
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val profile: UserProfileResponse? = null,
    val error: String? = null
)

class SettingsViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val profile = repository.getProfile()
                _state.value = SettingsUiState(
                    isLoading = false,
                    profile = profile
                )
            } catch (ex: ApiException) {
                _state.value = SettingsUiState(
                    isLoading = false,
                    error = ex.message ?: "加载失败"
                )
            } catch (ex: Exception) {
                _state.value = SettingsUiState(
                    isLoading = false,
                    error = "加载失败，请稍后重试"
                )
            }
        }
    }

    fun updateSettings(
        alertDays: Int,
        reminderTime: String,
        reminderEnabled: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = try {
                repository.updateSettings(
                    UpdateSettingsRequest(alertDays, reminderTime, reminderEnabled)
                )
                refresh()
                true
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(error = ex.message)
                false
            } catch (ex: Exception) {
                _state.value = _state.value.copy(error = "保存失败")
                false
            }
            onResult(result)
        }
    }

    fun pause(days: Int, reason: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = try {
                repository.pause(
                    PauseRequest(action = "pause", duration = days, reason = reason)
                )
                refresh()
                true
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(error = ex.message)
                false
            } catch (ex: Exception) {
                _state.value = _state.value.copy(error = "暂停失败")
                false
            }
            onResult(result)
        }
    }

    fun resume(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = try {
                repository.pause(PauseRequest(action = "resume"))
                refresh()
                true
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(error = ex.message)
                false
            } catch (ex: Exception) {
                _state.value = _state.value.copy(error = "恢复失败")
                false
            }
            onResult(result)
        }
    }
}
