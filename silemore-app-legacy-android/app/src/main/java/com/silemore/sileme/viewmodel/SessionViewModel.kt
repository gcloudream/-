package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silemore.sileme.storage.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SessionState(
    val initialized: Boolean = false,
    val token: String? = null
) {
    val isAuthenticated: Boolean = !token.isNullOrBlank()
}

class SessionViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStore.tokenFlow.collectLatest { token ->
                _state.value = SessionState(initialized = true, token = token)
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            tokenStore.clear()
        }
    }

    fun setToken(token: String) {
        viewModelScope.launch {
            tokenStore.saveToken(token)
        }
    }
}
