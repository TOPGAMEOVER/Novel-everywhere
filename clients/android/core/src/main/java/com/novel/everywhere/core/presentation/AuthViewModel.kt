package com.novel.everywhere.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novel.everywhere.core.data.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: NovelRepository) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repository.login(email, password)
                _state.value = AuthUiState.Success
            } catch (ex: Exception) {
                _state.value = AuthUiState.Error(ex.localizedMessage ?: "登录失败")
            }
        }
    }

    companion object {
        fun provideFactory(repository: NovelRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository) as T
                }
            }
    }
}
