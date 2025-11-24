package com.novel.everywhere.tablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novel.everywhere.core.presentation.AuthViewModel
import com.novel.everywhere.core.presentation.LibraryViewModel
import com.novel.everywhere.core.presentation.ReaderViewModel
import com.novel.everywhere.core.presentation.SettingsViewModel
import com.novel.everywhere.tablet.ui.TabletAppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as TabletApp).graph.repository
        setContent {
            val authVm: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory(repository))
            val libraryVm: LibraryViewModel = viewModel(factory = LibraryViewModel.provideFactory(repository))
            val readerVm: ReaderViewModel = viewModel(factory = ReaderViewModel.provideFactory(repository))
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.provideFactory(repository))
            TabletAppRoot(
                authViewModel = authVm,
                libraryViewModel = libraryVm,
                readerViewModel = readerVm,
                settingsViewModel = settingsVm,
                repository = repository,
            )
        }
    }
}
