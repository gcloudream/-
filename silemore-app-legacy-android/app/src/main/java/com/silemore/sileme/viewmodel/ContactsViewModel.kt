package com.silemore.sileme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silemore.sileme.data.ContactCreateRequest
import com.silemore.sileme.data.ContactListResponse
import com.silemore.sileme.network.ApiException
import com.silemore.sileme.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactsUiState(
    val isLoading: Boolean = true,
    val contacts: ContactListResponse? = null,
    val error: String? = null
)

class ContactsViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ContactsUiState())
    val state: StateFlow<ContactsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val contacts = repository.contacts()
                _state.value = ContactsUiState(
                    isLoading = false,
                    contacts = contacts
                )
            } catch (ex: ApiException) {
                _state.value = ContactsUiState(
                    isLoading = false,
                    error = ex.message ?: "加载失败"
                )
            } catch (ex: Exception) {
                _state.value = ContactsUiState(
                    isLoading = false,
                    error = "加载失败，请稍后重试"
                )
            }
        }
    }

    fun addContact(request: ContactCreateRequest, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = try {
                repository.addContact(request)
                refresh()
                true
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(isLoading = false, error = ex.message)
                false
            } catch (ex: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "添加失败")
                false
            }
            onResult(result)
        }
    }

    fun deleteContact(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = try {
                repository.deleteContact(id)
                refresh()
                true
            } catch (ex: ApiException) {
                _state.value = _state.value.copy(error = ex.message)
                false
            } catch (ex: Exception) {
                _state.value = _state.value.copy(error = "删除失败")
                false
            }
            onResult(result)
        }
    }
}
