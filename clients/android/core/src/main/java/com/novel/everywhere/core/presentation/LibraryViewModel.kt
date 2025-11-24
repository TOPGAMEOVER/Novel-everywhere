package com.novel.everywhere.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novel.everywhere.core.data.Novel
import com.novel.everywhere.core.data.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Loaded(val novels: List<Novel>) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel(private val repository: NovelRepository) : ViewModel() {

    private val _state = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = LibraryUiState.Loading
        viewModelScope.launch {
            try {
                val novels = repository.fetchNovels()
                _state.value = LibraryUiState.Loaded(novels)
            } catch (ex: Exception) {
                _state.value = LibraryUiState.Error(ex.localizedMessage ?: "加载失败")
            }
        }
    }

    companion object {
        fun provideFactory(repository: NovelRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(repository) as T
                }
            }
    }
}
