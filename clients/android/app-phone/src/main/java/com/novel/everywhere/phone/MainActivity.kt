package com.novel.everywhere.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novel.everywhere.core.data.NovelRepository
import com.novel.everywhere.core.presentation.AuthViewModel
import com.novel.everywhere.core.presentation.LibraryViewModel
import com.novel.everywhere.core.presentation.ReaderViewModel
import com.novel.everywhere.core.presentation.SettingsViewModel
import com.novel.everywhere.phone.ui.PhoneAppRoot

class MainActivity : ComponentActivity() {

    private lateinit var narrator: Narrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        narrator = Narrator(this)
        val repository = (application as PhoneApp).graph.repository
        setContent {
            val authVm: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory(repository))
            val libraryVm: LibraryViewModel = viewModel(factory = LibraryViewModel.provideFactory(repository))
            val readerVm: ReaderViewModel = viewModel(factory = ReaderViewModel.provideFactory(repository))
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.provideFactory(repository))
            PhoneAppRoot(
                authViewModel = authVm,
                libraryViewModel = libraryVm,
                readerViewModel = readerVm,
                settingsViewModel = settingsVm,
                narrator = narrator,
                repository = repository,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        narrator.release()
    }
}
