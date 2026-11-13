package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.configureAppCheck
import com.example.data.local.RyujinDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.FirestoreSyncRepository
import com.example.data.repository.NovelRepository
import com.example.export.StudioNotificationHelper
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.navigation.RyujinRoute
import com.example.ui.screens.AppDistributionDialog
import com.example.ui.screens.ChaptersScreen
import com.example.ui.screens.CharactersScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.RyujinAiCompanionDialog
import com.example.ui.screens.StudioProfileScreen
import com.example.ui.theme.RyujinStudioTheme
import com.example.ui.viewmodel.RyujinStudioViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        StudioNotificationHelper.ensureChannel(this)
        setContent {
            RyujinAppRoot()
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    trySend(currentUser)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
private fun RyujinAppRoot() {
    val authFlow = remember { Firebase.auth.authStateFlow() }
    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val context = LocalContext.current
    val prefsRepo = remember(context.applicationContext) {
        UserPreferencesRepository(context.applicationContext)
    }
    val settings by prefsRepo.settingsFlow.collectAsStateWithLifecycle(
        initialValue = com.example.data.local.ReaderSettings()
    )

    RyujinStudioTheme(darkTheme = settings.isDarkMode) {
        val user = currentUser
        if (user == null) {
            AuthScreen(onAuthSuccess = {})
        } else {
            key(user.uid) {
                AuthenticatedStudioShell(
                    user = user,
                    prefsRepo = prefsRepo
                )
            }
        }
    }
}

private data class TopLevelNavItem(
    val route: RyujinRoute,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedStudioShell(
    user: FirebaseUser,
    prefsRepo: UserPreferencesRepository
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: RyujinStudioViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val db = RyujinDatabase.getInstance(appContext)
                val novelRepo = NovelRepository(db.ryujinDao())
                val syncRepo = FirestoreSyncRepository(appContext)
                RyujinStudioViewModel(
                    novelRepo = novelRepo,
                    syncRepo = syncRepo,
                    prefsRepo = prefsRepo,
                    currentUserId = user.uid,
                    userEmail = user.email.orEmpty(),
                    userDisplayName = user.displayName.orEmpty(),
                    isEmailVerified = user.isEmailVerified
                )
            }
        }
    )

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isSeeding by viewModel.isSeeding.collectAsStateWithLifecycle()
    val activeNovel by viewModel.activeNovel.collectAsStateWithLifecycle()
    val localNovels by viewModel.localNovels.collectAsStateWithLifecycle()
    val selectedArc by viewModel.selectedArc.collectAsStateWithLifecycle()
    val chapters by viewModel.chaptersInSelectedArc.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchArcFilter by viewModel.searchArcFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val activeReaderChapter by viewModel.activeReaderChapter.collectAsStateWithLifecycle()
    val activeReaderSegments by viewModel.activeReaderSegments.collectAsStateWithLifecycle()
    val isLoadingSegments by viewModel.isLoadingChapterSegments.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val characterCountries by viewModel.characterCountries.collectAsStateWithLifecycle()
    val totalRyujinWords by viewModel.totalRyujinWords.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allUsersForCreator by viewModel.allUsersForCreator.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedChatGroupId by viewModel.selectedChatGroupId.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val statusPosts by viewModel.statusPosts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val driveSyncLogs by viewModel.driveSyncLogs.collectAsStateWithLifecycle()
    val appDistribution by viewModel.appDistribution.collectAsStateWithLifecycle()
    val statusToast by viewModel.statusToast.collectAsStateWithLifecycle()
    val aiInsightState by viewModel.aiInsightState.collectAsStateWithLifecycle()
    val aiChatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val aiChatState by viewModel.aiChatState.collectAsStateWithLifecycle()
    val aiChapterDraft by viewModel.aiChapterDraft.collectAsStateWithLifecycle()
    val isGeneratingChapterDraft by viewModel.isGeneratingChapterDraft.collectAsStateWithLifecycle()
    val aiDraftError by viewModel.aiDraftError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDistributionDialog by remember { mutableStateOf(false) }
    var showAiCompanionDialog by remember { mutableStateOf(false) }
    var seenNotificationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var initialNotificationsCaptured by remember { mutableStateOf(false) }

    LaunchedEffect(statusToast) {
        val msg = statusToast
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    LaunchedEffect(notifications) {
        val ids = notifications.mapNotNull { it.id.takeIf(String::isNotBlank) }.toSet()
        if (!initialNotificationsCaptured) {
            seenNotificationIds = ids
            initialNotificationsCaptured = true
        } else {
            val newlyArrived = notifications.filter { it.id.isNotBlank() && it.id !in seenNotificationIds }
            newlyArrived.firstOrNull { it.category == "chapter_update" }?.let { notif ->
                StudioNotificationHelper.showLocalNotification(
                    context = context,
                    id = notif.id.hashCode(),
                    title = notif.title,
                    message = notif.message
                )
            }
            seenNotificationIds = seenNotificationIds + ids
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isInReader = currentDestination?.hasRoute<RyujinRoute.Reader>() == true

    val navItems = remember(viewModel.isExclusiveCreator) {
        listOf(
            TopLevelNavItem(RyujinRoute.Home, "Beranda", Icons.Default.Home, "nav_home"),
            TopLevelNavItem(RyujinRoute.Chapters, "Bab", Icons.Default.AutoStories, "nav_chapters"),
            TopLevelNavItem(RyujinRoute.Characters, "Karakter", Icons.Default.Groups, "nav_characters"),
            TopLevelNavItem(RyujinRoute.Community, "Komunitas", Icons.Default.Forum, "nav_community"),
            TopLevelNavItem(
                RyujinRoute.Studio,
                if (viewModel.isExclusiveCreator) "Studio" else "Profil",
                Icons.Default.AdminPanelSettings,
                "nav_studio"
            )
        )
    }

    fun navigateTopLevel(route: RyujinRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openChapter(novelId: String, chapterNumber: Int) {
        viewModel.openChapterForReading(novelId, chapterNumber)
        if (!isInReader) {
            navController.navigate(RyujinRoute.Reader(novelId, chapterNumber))
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (!isInReader) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "RYUJIN STUDIO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = if (viewModel.isExclusiveCreator) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (viewModel.isExclusiveCreator) "KREATOR" else "PEMBACA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (viewModel.isExclusiveCreator) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showAiCompanionDialog = true },
                                modifier = Modifier.testTag("topbar_ai_companion_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Asisten AI Ryujin",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = { showDistributionDialog = true },
                                modifier = Modifier.testTag("topbar_download_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Unduh & Bagikan Aplikasi",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleDarkMode(!settings.isDarkMode) },
                                modifier = Modifier.testTag("topbar_theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (settings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Ubah mode tema"
                                )
                            }
                            IconButton(
                                onClick = { showNotificationsDialog = true },
                                modifier = Modifier.testTag("topbar_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (notifications.isNotEmpty()) {
                                            Badge {
                                                Text(notifications.size.coerceAtMost(99).toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifikasi Real-Time"
                                    )
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen && !isInReader) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen && !isInReader) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (isSeeding && localNovels.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.size(12.dp))
                            Text("Menyiapkan 900 Bab & 400 Karakter Ryujin...")
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = RyujinRoute.Home
                        ) {
                            composable<RyujinRoute.Home> {
                                HomeScreen(
                                    activeNovel = activeNovel,
                                    allNovels = localNovels,
                                    totalRyujinWords = totalRyujinWords,
                                    lastChapterRead = currentUserProfile?.lastChapterRead ?: settings.lastChapterNumber,
                                    isExclusiveCreator = viewModel.isExclusiveCreator,
                                    onSelectNovel = { viewModel.selectNovel(it) },
                                    onOpenReader = { novelId, chNum -> openChapter(novelId, chNum) },
                                    onOpenChaptersTab = { arc ->
                                        viewModel.selectArc(arc)
                                        navigateTopLevel(RyujinRoute.Chapters)
                                    },
                                    onRequestPreview = { novelId, chNum ->
                                        viewModel.getChapterPreview(novelId, chNum)
                                    },
                                    onCreateNovel = { title, author, genre, blurb, preface, count ->
                                        viewModel.createNewNovel(title, author, genre, blurb, preface, count)
                                    },
                                    onOpenDistribution = { showDistributionDialog = true }
                                )
                            }

                            composable<RyujinRoute.Chapters> {
                                ChaptersScreen(
                                    activeNovel = activeNovel,
                                    selectedArc = selectedArc,
                                    chapters = chapters,
                                    searchQuery = searchQuery,
                                    searchArcFilter = searchArcFilter,
                                    searchResults = searchResults,
                                    isSearching = isSearching,
                                    isExclusiveCreator = viewModel.isExclusiveCreator,
                                    aiChapterDraft = aiChapterDraft,
                                    isGeneratingChapterDraft = isGeneratingChapterDraft,
                                    aiDraftError = aiDraftError,
                                    onGenerateChapterDraftWithAi = { arc, hint ->
                                        viewModel.generateChapterOutlineWithAi(arc, hint)
                                    },
                                    onConsumeAiChapterDraft = { viewModel.consumeAiChapterDraft() },
                                    onSelectArc = { viewModel.selectArc(it) },
                                    onSearchChange = { q, arc -> viewModel.updateSearch(q, arc) },
                                    onOpenReader = { novelId, chNum -> openChapter(novelId, chNum) },
                                    onRequestPreview = { novelId, chNum ->
                                        viewModel.getChapterPreview(novelId, chNum)
                                    },
                                    onAddChapter = { arc, title, summary, script ->
                                        viewModel.addChapterToActiveNovel(arc, title, summary, script, context)
                                    }
                                )
                            }

                            composable<RyujinRoute.Characters> {
                                CharactersScreen(
                                    characters = characters,
                                    countries = characterCountries,
                                    aiInsightState = aiInsightState,
                                    onAnalyzeCharacterWithAi = { viewModel.analyzeCharacterWithAi(it) },
                                    onResetAiInsight = { viewModel.resetAiInsight() }
                                )
                            }

                            composable<RyujinRoute.Community> {
                                CommunityScreen(
                                    chatMessages = chatMessages,
                                    groups = groups,
                                    selectedChatGroupId = selectedChatGroupId,
                                    comments = comments,
                                    statusPosts = statusPosts,
                                    onSelectChatGroup = { viewModel.selectChatGroup(it) },
                                    onSendChatMessage = { viewModel.sendChatMessage(it) },
                                    onCreateGroup = { name, desc, tag -> viewModel.createGroup(name, desc, tag) },
                                    onJoinGroup = { viewModel.joinGroup(it) },
                                    onPostComment = { chNum, body -> viewModel.postComment(chNum, body) },
                                    onLikeComment = { viewModel.likeComment(it) },
                                    onPostStatus = { mood, content -> viewModel.postStatus(mood, content) },
                                    onLikeStatus = { viewModel.likeStatus(it) }
                                )
                            }

                            composable<RyujinRoute.Studio> {
                                StudioProfileScreen(
                                    userProfile = currentUserProfile,
                                    userDisplayName = user.displayName.orEmpty(),
                                    userEmail = user.email.orEmpty(),
                                    isExclusiveCreator = viewModel.isExclusiveCreator,
                                    settings = settings,
                                    allUsers = allUsersForCreator,
                                    driveSyncLogs = driveSyncLogs,
                                    totalRyujinWords = totalRyujinWords,
                                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                    onChangeFontSize = { viewModel.setReaderFontSize(it) },
                                    onToggleAutoSync = { viewModel.setAutoSyncEnabled(it) },
                                    onConnectDriveFolder = { uri, name -> viewModel.connectDriveFolder(uri, name) },
                                    onTriggerManualSync = { viewModel.triggerManualDriveSync(context) },
                                    onExportPdf = { uri -> viewModel.exportPdfToUri(context, uri) },
                                    onExportExcel = { uri -> viewModel.exportExcelToUri(context, uri) },
                                    onBroadcastNotification = { t, m ->
                                        viewModel.broadcastChapterUpdateNotification(context, t, m)
                                    },
                                    onOpenDistribution = { showDistributionDialog = true },
                                    onSignOut = {
                                        signOutUser(credentialManager, scope)
                                    }
                                )
                            }

                            composable<RyujinRoute.Reader> {
                                val chapter = activeReaderChapter
                                if (chapter == null) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    ReaderScreen(
                                        chapter = chapter,
                                        segments = activeReaderSegments,
                                        isLoadingSegments = isLoadingSegments,
                                        totalChaptersInNovel = activeNovel?.totalChapters ?: 900,
                                        fontSizeSp = settings.readerFontSizeSp,
                                        isDarkMode = settings.isDarkMode,
                                        isExclusiveCreator = viewModel.isExclusiveCreator,
                                        chapterComments = comments,
                                        aiInsightState = aiInsightState,
                                        onAnalyzeChapterWithAi = { ch, excerpt ->
                                            viewModel.analyzeChapterWithAi(ch, excerpt)
                                        },
                                        onResetAiInsight = { viewModel.resetAiInsight() },
                                        onBack = {
                                            viewModel.closeReader()
                                            navController.popBackStack()
                                        },
                                        onNavigateChapter = { nextCh ->
                                            viewModel.openChapterForReading(chapter.novelId, nextCh)
                                        },
                                        onChangeFontSize = { viewModel.setReaderFontSize(it) },
                                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                        onSaveEditedSegment = { seg, newTitle, newBody ->
                                            viewModel.saveEditedSegment(context, seg, newTitle, newBody)
                                        },
                                        onDownloadChapter = { ch, uri ->
                                            viewModel.downloadChapterToUri(context, ch, uri)
                                        },
                                        onPostComment = { chNum, body ->
                                            viewModel.postComment(chNum, body)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notifikasi Real-Time (${notifications.size})") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Text("Belum ada notifikasi aktivitas terbaru.")
                    } else {
                        notifications.forEach { n ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = n.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(n.message, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "Oleh: ${n.actorName} • ${n.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showDistributionDialog) {
        AppDistributionDialog(
            config = appDistribution,
            isExclusiveCreator = viewModel.isExclusiveCreator,
            onSaveConfig = { previewUrl, apkUrl, version, notes ->
                viewModel.saveAppDistributionConfig(previewUrl, apkUrl, version, notes)
            },
            onDownloadBundleToUri = { uri ->
                viewModel.downloadCatalogSummaryToUri(context, uri)
            },
            onDismiss = { showDistributionDialog = false }
        )
    }

    if (showAiCompanionDialog) {
        RyujinAiCompanionDialog(
            messages = aiChatMessages,
            chatState = aiChatState,
            isExclusiveCreator = viewModel.isExclusiveCreator,
            onSendMessage = { viewModel.sendAiChatMessage(it) },
            onClearChat = { viewModel.clearAiChat() },
            onDismiss = { showAiCompanionDialog = false }
        )
    }
}
