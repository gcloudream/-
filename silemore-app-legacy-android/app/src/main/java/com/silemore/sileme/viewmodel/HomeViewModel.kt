package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silemore.sileme.data.CheckInTodayResponse
import com.silemore.sileme.data.UserProfileResponse
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.repository.AppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val today: CheckInTodayResponse? = null,
    val profile: UserProfileResponse? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val profileDeferred = async { repository.getProfile() }
                val todayDeferred = async { repository.today() }
                _state.value = HomeUiState(
                    isLoading = false,
                    today = todayDeferred.await(),
                    profile = profileDeferred.await(),
                    error = null
                )
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ex.message ?: "加载失败"
                )
            } catch (ex: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载失败，请稍后重试"
                )
            }
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                repository.checkIn()
                val today = repository.today()
                val profile = repository.getProfile()
                _state.value = HomeUiState(
                    isLoading = false,
                    today = today,
                    profile = profile
                )
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ex.message ?: "签到失败"
                )
            } catch (ex: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "签到失败，请稍后重试"
                )
            }
        }
    }
}
