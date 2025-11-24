package com.novel.everywhere.phone.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.novel.everywhere.core.data.NovelRepository
import com.novel.everywhere.core.data.ReadingSettingsDto
import com.novel.everywhere.core.presentation.AuthUiState
import com.novel.everywhere.core.presentation.AuthViewModel
import com.novel.everywhere.core.presentation.LibraryUiState
import com.novel.everywhere.core.presentation.LibraryViewModel
import com.novel.everywhere.core.presentation.ReaderViewModel
import com.novel.everywhere.core.presentation.SettingsUiState
import com.novel.everywhere.core.presentation.SettingsViewModel
import com.novel.everywhere.phone.Narrator
import kotlinx.coroutines.launch

@Composable
fun PhoneAppRoot(
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    readerViewModel: ReaderViewModel,
    settingsViewModel: SettingsViewModel,
    narrator: Narrator,
    repository: NovelRepository,
) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            navController.navigate("library") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login",
    ) {
        composable("login") {
            LoginScreen(
                state = authState,
                onLogin = { email, password -> authViewModel.login(email, password) },
            )
        }
        composable("library") {
            LibraryScreen(
                stateFlow = libraryViewModel.state,
                onSelect = { id ->
                    readerViewModel.loadProgress(id)
                    navController.navigate("reader/$id")
                },
                onOpenSettings = { navController.navigate("settings") },
                repository = repository,
            )
        }
        composable(
            route = "reader/{novelId}",
            arguments = listOf(navArgument("novelId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val novelId = backStackEntry.arguments?.getInt("novelId") ?: return@composable
            ReaderScreen(
                stateFlow = readerViewModel.state,
                novelId = novelId,
                onSync = readerViewModel::syncProgress,
                onSpeak = narrator::speak,
                onBack = { navController.navigateUp() },
            )
        }
        composable("settings") {
            SettingsScreen(
                stateFlow = settingsViewModel.state,
                onBack = { navController.navigateUp() },
            )
        }
    }
}

@Composable
fun LoginScreen(state: AuthUiState, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = state is AuthUiState.Loading
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "欢迎登录", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = if (isLoading) "登录中..." else "登录")
        }
        if (state is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    stateFlow: kotlinx.coroutines.flow.StateFlow<LibraryUiState>,
    onSelect: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    repository: NovelRepository,
) {
    val state by stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    repository.uploadNovel(
                        resolver = context.contentResolver,
                        uri = uri,
                        title = null,
                        author = null,
                        description = null,
                    )
                } catch (ex: Exception) {
                    snackbarHostState.showSnackbar(ex.localizedMessage ?: "导入失败")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的书架") },
                actions = {
                    TextButton(onClick = onOpenSettings) { Text("设置") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch(arrayOf("*/*")) }) {
                Icon(Icons.Default.List, contentDescription = null)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (state) {
            is LibraryUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "加载中...",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                    )
                }
            }
            is LibraryUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = (state as LibraryUiState.Error).message,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            is LibraryUiState.Loaded -> {
                val novels = (state as LibraryUiState.Loaded).novels
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(novels) { novel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(novel.id) },
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(novel.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "作者：${novel.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                novel.description?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderScreen(
    stateFlow: kotlinx.coroutines.flow.StateFlow<com.novel.everywhere.core.presentation.ReaderUiState>,
    novelId: Int,
    onSync: (Int, String, Int) -> Unit,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by stateFlow.collectAsState()
    var currentText by remember { mutableStateOf("这是章节内容示例...") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4EFE2))
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("返回书架") }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "章节：${state.chapter}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = currentText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(onClick = { onSpeak(currentText) }) {
                Icon(Icons.Default.Headphones, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("朗读")
            }
            Button(onClick = { onSync(novelId, state.chapter, state.offset + 1) }) {
                Text(if (state.isSyncing) "同步中..." else "同步进度")
            }
        }
        state.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SettingsScreen(
    stateFlow: kotlinx.coroutines.flow.StateFlow<SettingsUiState>,
    onBack: () -> Unit,
) {
    val state by stateFlow.collectAsState()
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextButton(onClick = onBack) { Text("返回") }
            Spacer(modifier = Modifier.height(12.dp))
            when (state) {
                is SettingsUiState.Loading -> Text("加载中...")
                is SettingsUiState.Error -> Text(
                    (state as SettingsUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
                is SettingsUiState.Loaded -> SettingsSummary((state as SettingsUiState.Loaded).settings)
            }
        }
    }
}

@Composable
private fun SettingsSummary(settings: ReadingSettingsDto) {
    Text(text = "字体：${settings.fontFamily} / ${settings.fontSize}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "主题：${settings.theme}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "背景色：${settings.bgColor}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "朗读音色：${settings.ttsVoice}")
}
