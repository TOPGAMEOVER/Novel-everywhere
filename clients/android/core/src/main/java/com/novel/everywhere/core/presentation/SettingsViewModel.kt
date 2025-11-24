package com.novel.everywhere.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novel.everywhere.core.data.NovelRepository
import com.novel.everywhere.core.data.ReadingSettingsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Loaded(val settings: ReadingSettingsDto) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

class SettingsViewModel(private val repository: NovelRepository) : ViewModel() {

    private val _state = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = SettingsUiState.Loading
        viewModelScope.launch {
            try {
                val settings = repository.getSettings()
                _state.value = SettingsUiState.Loaded(settings)
            } catch (ex: Exception) {
                _state.value = SettingsUiState.Error(ex.localizedMessage ?: "加载失败")
            }
        }
    }

    companion object {
        fun provideFactory(repository: NovelRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
