package com.novel.everywhere.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novel.everywhere.core.data.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReaderUiState(
    val chapter: String = "开篇",
    val offset: Int = 0,
    val isSyncing: Boolean = false,
    val error: String? = null,
)

class ReaderViewModel(private val repository: NovelRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    fun loadProgress(novelId: Int) {
        viewModelScope.launch {
            try {
                val progress = repository.getProgress(novelId)
                _state.value = ReaderUiState(progress.chapter, progress.offset, false, null)
            } catch (_: Exception) {
                // fallback to default progress
            }
        }
    }

    fun syncProgress(novelId: Int, chapter: String, offset: Int) {
        _state.value = _state.value.copy(isSyncing = true, error = null)
        viewModelScope.launch {
            try {
                repository.updateProgress(novelId, chapter, offset)
                _state.value = ReaderUiState(chapter, offset, false, null)
            } catch (ex: Exception) {
                _state.value = _state.value.copy(isSyncing = false, error = ex.localizedMessage)
            }
        }
    }

    companion object {
        fun provideFactory(repository: NovelRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReaderViewModel(repository) as T
                }
            }
    }
}
