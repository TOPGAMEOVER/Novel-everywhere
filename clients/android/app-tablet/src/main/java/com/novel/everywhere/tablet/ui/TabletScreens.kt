package com.novel.everywhere.tablet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.novel.everywhere.core.data.NovelRepository
import com.novel.everywhere.core.presentation.AuthUiState
import com.novel.everywhere.core.presentation.AuthViewModel
import com.novel.everywhere.core.presentation.LibraryUiState
import com.novel.everywhere.core.presentation.LibraryViewModel
import com.novel.everywhere.core.presentation.ReaderViewModel
import com.novel.everywhere.core.presentation.SettingsViewModel

@Composable
fun TabletAppRoot(
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    readerViewModel: ReaderViewModel,
    settingsViewModel: SettingsViewModel,
    repository: NovelRepository,
) {
    val authState by authViewModel.state.collectAsState()
    var selectedId by remember { mutableIntStateOf(-1) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (authState is AuthUiState.Success) {
            Row(modifier = Modifier.fillMaxSize()) {
                LibraryPane(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    stateFlow = libraryViewModel.state,
                    onSelect = {
                        selectedId = it
                        readerViewModel.loadProgress(it)
                    },
                )
                ReaderPane(
                    modifier = Modifier.weight(0.65f),
                    readerViewModel = readerViewModel,
                    selectedNovelId = selectedId,
                )
            }
        } else {
            LoginPane(onLogin = authViewModel::login, state = authState)
        }
    }
}

@Composable
private fun LoginPane(onLogin: (String, String) -> Unit, state: AuthUiState) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("平板端登录", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.padding(12.dp))
        androidx.compose.material3.OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("邮箱") })
        Spacer(modifier = Modifier.padding(6.dp))
        androidx.compose.material3.OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("密码") })
        Spacer(modifier = Modifier.padding(12.dp))
        Button(onClick = { onLogin(email, password) }) { Text("登录") }
        if (state is AuthUiState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LibraryPane(
    modifier: Modifier,
    stateFlow: kotlinx.coroutines.flow.StateFlow<LibraryUiState>,
    onSelect: (Int) -> Unit,
) {
    val state by stateFlow.collectAsState()
    Surface(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        when (state) {
            is LibraryUiState.Loading -> Text("加载中...", modifier = Modifier.padding(16.dp))
            is LibraryUiState.Error -> Text(
                (state as LibraryUiState.Error).message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
            is LibraryUiState.Loaded -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items((state as LibraryUiState.Loaded).novels) { novel ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(novel.id) }
                                .padding(12.dp),
                        ) {
                            Text(novel.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                novel.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderPane(
    modifier: Modifier,
    readerViewModel: ReaderViewModel,
    selectedNovelId: Int,
) {
    val state by readerViewModel.state.collectAsState()
    Surface(modifier = modifier.fillMaxSize()) {
        if (selectedNovelId == -1) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("请选择一本小说开始阅读")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                Text("章节：${state.chapter}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "大屏阅读体验带来更接近纸质书的沉浸感。",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Button(onClick = { readerViewModel.syncProgress(selectedNovelId, state.chapter, state.offset + 1) }) {
                    Text(if (state.isSyncing) "同步中..." else "手动同步")
                }
            }
        }
    }
}
