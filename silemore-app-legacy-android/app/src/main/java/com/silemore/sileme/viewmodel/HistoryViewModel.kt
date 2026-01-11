package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silemore.sileme.data.CheckInHistoryResponse
import com.silemore.sileme.data.CheckInTodayResponse
import com.silemore.sileme.data.UserProfileResponse
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.repository.AppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val history: CheckInHistoryResponse? = null,
    val today: CheckInTodayResponse? = null,
    val profile: UserProfileResponse? = null,
    val error: String? = null
)

class HistoryViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val historyDeferred = async { repository.history(page = 0, size = 30) }
                val profileDeferred = async { repository.getProfile() }
                val todayDeferred = async { repository.today() }
                val history = historyDeferred.await()
                _state.value = HistoryUiState(
                    isLoading = false,
                    history = history,
                    today = todayDeferred.await(),
                    profile = profileDeferred.await()
                )
            } catch (ex: ApiException) {
                _state.value = HistoryUiState(
                    isLoading = false,
                    error = ex.message ?: "加载失败"
                )
            } catch (ex: Exception) {
                _state.value = HistoryUiState(
                    isLoading = false,
                    error = "加载失败，请稍后重试"
                )
            }
        }
    }
}
