package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiChatEntry
import com.example.ai.AiGenerationState
import com.example.ai.GeneratedChapterOutline
import com.example.ai.RyujinAiService
import com.example.data.local.LocalChapterEntity
import com.example.data.local.LocalChapterSegmentEntity
import com.example.data.local.LocalCharacterEntity
import com.example.data.local.LocalNovelEntity
import com.example.data.local.ReaderSettings
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppDistributionConfig
import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.CommentItem
import com.example.data.model.CommunityGroup
import com.example.data.model.DriveSyncLog
import com.example.data.model.StatusPost
import com.example.data.model.UserProfile
import com.example.data.repository.AdvancedSearchResults
import com.example.data.repository.FirestoreSyncRepository
import com.example.data.repository.NovelRepository
import com.example.data.seed.RyujinNovelMeta
import com.example.export.ReportExporter
import com.example.export.StudioNotificationHelper
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RyujinStudioViewModel(
    private val novelRepo: NovelRepository,
    private val syncRepo: FirestoreSyncRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val currentUserId: String,
    private val userEmail: String,
    private val userDisplayName: String,
    private val isEmailVerified: Boolean,
    private val aiService: RyujinAiService = RyujinAiService()
) : ViewModel() {

    val isExclusiveCreator: Boolean = syncRepo.isVerifiedCreatorEmail(userEmail, isEmailVerified)

    val settings: StateFlow<ReaderSettings> = prefsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderSettings())

    private val _selectedNovelId = MutableStateFlow(RyujinNovelMeta.RYUJIN_NOVEL_ID)
    val selectedNovelId: StateFlow<String> = _selectedNovelId.asStateFlow()

    private val _selectedArc = MutableStateFlow(1)
    val selectedArc: StateFlow<Int> = _selectedArc.asStateFlow()

    private val _selectedChatGroupId = MutableStateFlow("global_lounge")
    val selectedChatGroupId: StateFlow<String> = _selectedChatGroupId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchArcFilter = MutableStateFlow(0)
    val searchArcFilter: StateFlow<Int> = _searchArcFilter.asStateFlow()

    private val _searchResults = MutableStateFlow(AdvancedSearchResults())
    val searchResults: StateFlow<AdvancedSearchResults> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private val _isSeeding = MutableStateFlow(true)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    // Gemini AI States
    private val _aiInsightState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiInsightState: StateFlow<AiGenerationState> = _aiInsightState.asStateFlow()

    private val _aiChatMessages = MutableStateFlow<List<AiChatEntry>>(emptyList())
    val aiChatMessages: StateFlow<List<AiChatEntry>> = _aiChatMessages.asStateFlow()

    private val _aiChatState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiChatState: StateFlow<AiGenerationState> = _aiChatState.asStateFlow()

    private val _aiChapterDraft = MutableStateFlow<GeneratedChapterOutline?>(null)
    val aiChapterDraft: StateFlow<GeneratedChapterOutline?> = _aiChapterDraft.asStateFlow()

    private val _isGeneratingChapterDraft = MutableStateFlow(false)
    val isGeneratingChapterDraft: StateFlow<Boolean> = _isGeneratingChapterDraft.asStateFlow()

    private val _aiDraftError = MutableStateFlow<String?>(null)
    val aiDraftError: StateFlow<String?> = _aiDraftError.asStateFlow()

    private var insightJob: Job? = null
    private var chatJob: Job? = null

    // Reader active state
    private val _activeReaderChapter = MutableStateFlow<LocalChapterEntity?>(null)
    val activeReaderChapter: StateFlow<LocalChapterEntity?> = _activeReaderChapter.asStateFlow()

    private val _isLoadingChapterSegments = MutableStateFlow(false)
    val isLoadingChapterSegments: StateFlow<Boolean> = _isLoadingChapterSegments.asStateFlow()

    val activeReaderSegments: StateFlow<List<LocalChapterSegmentEntity>> =
        _activeReaderChapter.flatMapLatest { chapter ->
            if (chapter == null) flowOf(emptyList())
            else novelRepo.observeSegmentsForChapter(chapter.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Novels & Chapters
    val localNovels: StateFlow<List<LocalNovelEntity>> = novelRepo.observeAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNovel: StateFlow<LocalNovelEntity?> = _selectedNovelId.flatMapLatest { id ->
        novelRepo.observeNovelById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chaptersInSelectedArc: StateFlow<List<LocalChapterEntity>> =
        combine(_selectedNovelId, _selectedArc) { id, arc -> id to arc }
            .flatMapLatest { (id, arc) ->
                if (id == RyujinNovelMeta.RYUJIN_NOVEL_ID) {
                    novelRepo.observeChaptersByArc(id, arc)
                } else {
                    novelRepo.observeAllChapters(id)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRyujinChapters: StateFlow<List<LocalChapterEntity>> =
        novelRepo.observeAllChapters(RyujinNovelMeta.RYUJIN_NOVEL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val characters: StateFlow<List<LocalCharacterEntity>> =
        novelRepo.observeCharacters(RyujinNovelMeta.RYUJIN_NOVEL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val characterCountries: StateFlow<List<String>> =
        novelRepo.observeCharacterCountries(RyujinNovelMeta.RYUJIN_NOVEL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRyujinWords: StateFlow<Long> =
        novelRepo.observeTotalWordCount(RyujinNovelMeta.RYUJIN_NOVEL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 47250000L)

    // Firestore Realtime Streams
    private fun <T> Flow<T>.safeFirestoreFlow(fallback: T, tag: String): Flow<T> =
        catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w("RyujinVM", "Firestore stream error ($tag): ${(fsErr ?: error).message}")
            emit(fallback)
        }

    val currentUserProfile: StateFlow<UserProfile?> =
        syncRepo.observeCurrentUserProfile(currentUserId)
            .safeFirestoreFlow(null, "profile")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsersForCreator: StateFlow<List<UserProfile>> =
        if (isExclusiveCreator) {
            syncRepo.observeAllUsersForCreator()
                .safeFirestoreFlow(emptyList(), "users_creator")
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } else {
            MutableStateFlow(emptyList())
        }

    val comments: StateFlow<List<CommentItem>> = _selectedNovelId.flatMapLatest { id ->
        syncRepo.observeComments(id).safeFirestoreFlow(emptyList(), "comments")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<CommunityGroup>> =
        syncRepo.observeGroups()
            .safeFirestoreFlow(emptyList(), "groups")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = _selectedChatGroupId.flatMapLatest { gid ->
        syncRepo.observeChatMessages(gid).safeFirestoreFlow(emptyList(), "chat")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statusPosts: StateFlow<List<StatusPost>> =
        syncRepo.observeStatusPosts()
            .safeFirestoreFlow(emptyList(), "status")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> =
        syncRepo.observeNotifications(currentUserId, isExclusiveCreator)
            .safeFirestoreFlow(emptyList(), "notifications")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driveSyncLogs: StateFlow<List<DriveSyncLog>> =
        if (isExclusiveCreator) {
            syncRepo.observeDriveSyncLogs()
                .safeFirestoreFlow(emptyList(), "drive_logs")
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } else {
            MutableStateFlow(emptyList())
        }

    val appDistribution: StateFlow<AppDistributionConfig> =
        syncRepo.observeAppDistribution()
            .safeFirestoreFlow(AppDistributionConfig(), "app_distribution")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppDistributionConfig())

    init {
        viewModelScope.launch {
            novelRepo.ensureRyujinSeeded()
            _isSeeding.value = false
        }
        viewModelScope.launch {
            syncRepo.ensureUserProfileExists(
                uid = currentUserId,
                displayName = userDisplayName,
                email = userEmail,
                isCreator = isExclusiveCreator
            )
            if (isExclusiveCreator) {
                syncRepo.syncNovelToCloud(
                    novelId = RyujinNovelMeta.RYUJIN_NOVEL_ID,
                    title = "Ryujin",
                    authorName = RyujinNovelMeta.AUTHOR_NAME,
                    genre = "Dark Action Thriller",
                    blurb = RyujinNovelMeta.BLURB,
                    preface = RyujinNovelMeta.PREFACE,
                    totalChapters = 900,
                    totalArcs = 3,
                    wordsPerChapter = 52500,
                    coverTag = "RYUJIN_900"
                )
            }
        }
    }

    fun clearToast() {
        _statusToast.value = null
    }

    fun selectNovel(novelId: String) {
        _selectedNovelId.value = novelId
    }

    fun selectArc(arcNumber: Int) {
        _selectedArc.value = arcNumber.coerceIn(1, 3)
    }

    fun selectChatGroup(groupId: String) {
        _selectedChatGroupId.value = groupId
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkMode(enabled) }
    }

    fun setReaderFontSize(sp: Int) {
        viewModelScope.launch { prefsRepo.setReaderFontSize(sp) }
    }

    fun updateSearch(query: String, arcFilter: Int = _searchArcFilter.value) {
        _searchQuery.value = query
        _searchArcFilter.value = arcFilter
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = AdvancedSearchResults()
                return@launch
            }
            _isSearching.value = true
            _searchResults.value = novelRepo.searchAdvanced(_selectedNovelId.value, query, arcFilter)
            _isSearching.value = false
        }
    }

    suspend fun getChapterPreview(novelId: String, chapterNumber: Int): String {
        return novelRepo.getPreviewExcerpt(novelId, chapterNumber)
    }

    fun openChapterForReading(novelId: String, chapterNumber: Int) {
        viewModelScope.launch {
            _isLoadingChapterSegments.value = true
            val chapter = novelRepo.ensureChapterSegmentsMaterialized(novelId, chapterNumber)
            _activeReaderChapter.value = chapter
            _isLoadingChapterSegments.value = false
            if (chapter != null) {
                prefsRepo.saveReadingProgress(novelId, chapterNumber)
                syncRepo.updateUserReadingProgress(
                    uid = currentUserId,
                    chapterNumber = chapterNumber,
                    wordsIncrement = 1200L
                )
            }
        }
    }

    fun closeReader() {
        _activeReaderChapter.value = null
    }

    // --- Creator-Exclusive Actions ---
    fun saveEditedSegment(
        context: Context,
        segment: LocalChapterSegmentEntity,
        newTitle: String,
        newContent: String
    ) {
        if (!isExclusiveCreator) {
            _statusToast.value = "Akses ditolak: Hanya kreator eksklusif yang dapat mengedit naskah."
            return
        }
        viewModelScope.launch {
            val updatedChapter = novelRepo.updateSegmentContent(
                chapterId = segment.chapterId,
                segment = segment,
                newTitle = newTitle,
                newContent = newContent
            )
            if (updatedChapter != null) {
                _activeReaderChapter.value = updatedChapter
                syncRepo.syncChapterEditToCloud(
                    novelId = updatedChapter.novelId,
                    chapterNumber = updatedChapter.chapterNumber,
                    arcNumber = updatedChapter.arcNumber,
                    title = updatedChapter.title,
                    summary = updatedChapter.summary,
                    wordCount = updatedChapter.wordCount,
                    contentPreview = newContent.take(2000)
                )
                if (settings.value.autoSyncEnabled) {
                    autoSyncChapterToDriveIfLinked(context, updatedChapter)
                }
                _statusToast.value = "Naskah Bab ${updatedChapter.chapterNumber} disimpan & disinkronkan!"
            }
        }
    }

    fun createNewNovel(
        title: String,
        authorName: String,
        genre: String,
        blurb: String,
        preface: String,
        initialChapters: Int
    ) {
        if (!isExclusiveCreator) {
            _statusToast.value = "Fitur Tambah Novel khusus untuk akun kreator developer."
            return
        }
        if (title.isBlank() || blurb.isBlank()) {
            _statusToast.value = "Judul dan Blurb wajib diisi."
            return
        }
        viewModelScope.launch {
            val novel = novelRepo.createNovelWithOpeningChapters(
                title = title,
                authorName = authorName.ifBlank { RyujinNovelMeta.AUTHOR_NAME },
                genre = genre.ifBlank { "Dark Action" },
                blurb = blurb,
                preface = preface.ifBlank { "Kata pengantar penulis untuk $title." },
                initialChaptersCount = initialChapters
            )
            syncRepo.syncNovelToCloud(
                novelId = novel.id,
                title = novel.title,
                authorName = novel.authorName,
                genre = novel.genre,
                blurb = novel.blurb,
                preface = novel.preface,
                totalChapters = novel.totalChapters,
                totalArcs = novel.totalArcs,
                wordsPerChapter = novel.wordsPerChapter,
                coverTag = novel.coverTag
            )
            syncRepo.publishChapterNotification(
                actorName = userDisplayName.ifBlank { "Creator" },
                title = "Novel Baru: ${novel.title}",
                message = "Karya baru bergenre ${novel.genre} (${novel.totalChapters} bab) telah diterbitkan!"
            )
            _selectedNovelId.value = novel.id
            _statusToast.value = "Novel '${novel.title}' berhasil ditambahkan & dipublikasikan!"
        }
    }

    fun addChapterToActiveNovel(
        arcNumber: Int,
        title: String,
        summary: String,
        openingScript: String,
        context: Context
    ) {
        if (!isExclusiveCreator) return
        if (title.isBlank()) {
            _statusToast.value = "Judul bab tidak boleh kosong."
            return
        }
        viewModelScope.launch {
            val ch = novelRepo.addChapterToNovel(
                novelId = _selectedNovelId.value,
                arcNumber = arcNumber,
                title = title,
                summary = summary.ifBlank { "Bab baru penuh dialog karya kreator." },
                customOpeningContent = openingScript
            )
            if (ch != null) {
                syncRepo.syncChapterEditToCloud(
                    novelId = ch.novelId,
                    chapterNumber = ch.chapterNumber,
                    arcNumber = ch.arcNumber,
                    title = ch.title,
                    summary = ch.summary,
                    wordCount = ch.wordCount,
                    contentPreview = openingScript.ifBlank { ch.summary }
                )
                syncRepo.publishChapterNotification(
                    actorName = userDisplayName.ifBlank { "Creator" },
                    title = "Update Bab ${ch.chapterNumber}: ${ch.title}",
                    message = "Bab baru telah dirilis dengan naskah lengkap ${ch.wordCount} kata!"
                )
                StudioNotificationHelper.showLocalNotification(
                    context = context,
                    id = ch.chapterNumber + 1000,
                    title = "Update Bab ${ch.chapterNumber}: ${ch.title}",
                    message = "Bab terbaru siap dibaca di Ryujin Studio."
                )
                _statusToast.value = "Bab ${ch.chapterNumber} berhasil ditambahkan & notifikasi terkirim!"
            }
        }
    }

    fun broadcastChapterUpdateNotification(context: Context, title: String, message: String) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            val res = syncRepo.publishChapterNotification(
                actorName = userDisplayName.ifBlank { "Developer" },
                title = title,
                message = message
            )
            if (res.isSuccess) {
                StudioNotificationHelper.showLocalNotification(
                    context = context,
                    id = (System.currentTimeMillis() % 10000).toInt(),
                    title = title,
                    message = message
                )
                _statusToast.value = "Notifikasi real-time disiarkan ke semua pembaca!"
            }
        }
    }

    // --- Google Drive Connection & Exports ---
    fun connectDriveFolder(uri: Uri, folderName: String) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            prefsRepo.saveConnectedDrive(uri.toString(), folderName)
            syncRepo.updateCreatorDriveStatus(
                uid = currentUserId,
                driveLinked = true,
                driveFolderName = folderName,
                syncEnabled = settings.value.autoSyncEnabled
            )
            syncRepo.logDriveSync(
                fileName = "Ryujin_Workspace_Manifest.json",
                syncType = "manual_drive",
                destination = "Drive / $folderName",
                status = "synced",
                wordCount = totalRyujinWords.value
            )
            _statusToast.value = "Google Drive '$folderName' terhubung untuk Auto-Sync!"
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            prefsRepo.setAutoSync(enabled)
            syncRepo.updateCreatorDriveStatus(
                uid = currentUserId,
                driveLinked = settings.value.connectedDriveUri.isNotBlank(),
                driveFolderName = settings.value.connectedDriveName,
                syncEnabled = enabled
            )
        }
    }

    fun triggerManualDriveSync(context: Context) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            val driveUri = settings.value.connectedDriveUri
            val folderName = settings.value.connectedDriveName
            if (driveUri.isBlank()) {
                _statusToast.value = "Pilih folder Google Drive terlebih dahulu."
                return@launch
            }
            val pdfBytes = ReportExporter.buildPdfReportBytes(
                novels = localNovels.value,
                chapters = allRyujinChapters.value,
                characters = characters.value,
                users = allUsersForCreator.value,
                syncLogs = driveSyncLogs.value
            )
            val stamp = System.currentTimeMillis() / 1000
            val res = ReportExporter.saveFileToConnectedDriveTree(
                context = context,
                treeUriString = driveUri,
                mimeType = "application/pdf",
                fileName = "Ryujin_AutoSync_Report_$stamp.pdf",
                bytes = pdfBytes
            )
            prefsRepo.markSyncedNow()
            syncRepo.logDriveSync(
                fileName = "Ryujin_AutoSync_Report_$stamp.pdf",
                syncType = "auto_sync",
                destination = "Drive / $folderName",
                status = if (res.isSuccess) "synced" else "failed",
                wordCount = totalRyujinWords.value
            )
            _statusToast.value = if (res.isSuccess) {
                "Sinkronisasi otomatis ke Drive '$folderName' berhasil!"
            } else {
                "Log Cloud tersimpan; izin folder lokal perlu dipilih ulang."
            }
        }
    }

    private suspend fun autoSyncChapterToDriveIfLinked(context: Context, chapter: LocalChapterEntity) {
        val driveUri = settings.value.connectedDriveUri
        val segs = novelRepo.getChapterSegmentsBlocking(chapter.novelId, chapter.chapterNumber)
        val novel = localNovels.value.firstOrNull { it.id == chapter.novelId } ?: RyujinNovelMeta.createRyujinNovelEntity()
        val bytes = ReportExporter.buildChapterManuscriptText(novel, chapter, segs)
        val fileName = "${novel.title}_Bab_${chapter.chapterNumber}.txt"
        val dest = if (driveUri.isNotBlank()) {
            ReportExporter.saveFileToConnectedDriveTree(context, driveUri, "text/plain", fileName, bytes)
            "Drive / ${settings.value.connectedDriveName}"
        } else {
            "Cloud Sync (Menunggu Tautan Folder Drive)"
        }
        prefsRepo.markSyncedNow()
        syncRepo.logDriveSync(
            fileName = fileName,
            syncType = "auto_sync",
            destination = dest,
            status = "synced",
            wordCount = chapter.wordCount.toLong()
        )
    }

    fun exportPdfToUri(context: Context, uri: Uri) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            val bytes = ReportExporter.buildPdfReportBytes(
                novels = localNovels.value,
                chapters = allRyujinChapters.value,
                characters = characters.value,
                users = allUsersForCreator.value,
                syncLogs = driveSyncLogs.value
            )
            val res = ReportExporter.writeBytesToUri(context, uri, bytes)
            if (res.isSuccess) {
                syncRepo.logDriveSync(
                    fileName = "Laporan_Ryujin_Studio.pdf",
                    syncType = "pdf_export",
                    destination = "Dokumen / Drive Ekspor",
                    status = "synced",
                    wordCount = totalRyujinWords.value
                )
                _statusToast.value = "Laporan PDF berhasil diekspor!"
            } else {
                _statusToast.value = "Gagal mengekspor PDF: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun exportExcelToUri(context: Context, uri: Uri) {
        if (!isExclusiveCreator) return
        viewModelScope.launch {
            val bytes = ReportExporter.buildExcelXlsxBytes(
                novels = localNovels.value,
                chapters = allRyujinChapters.value,
                characters = characters.value,
                users = allUsersForCreator.value
            )
            val res = ReportExporter.writeBytesToUri(context, uri, bytes)
            if (res.isSuccess) {
                syncRepo.logDriveSync(
                    fileName = "Laporan_Ryujin_Studio.xlsx",
                    syncType = "excel_export",
                    destination = "Dokumen / Drive Ekspor",
                    status = "synced",
                    wordCount = totalRyujinWords.value
                )
                _statusToast.value = "Laporan Excel (.xlsx) berhasil diekspor!"
            } else {
                _statusToast.value = "Gagal mengekspor Excel: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun downloadChapterToUri(context: Context, chapter: LocalChapterEntity, uri: Uri) {
        if (!isExclusiveCreator) {
            _statusToast.value = "Fitur unduh naskah eksklusif hanya untuk Kreator."
            return
        }
        viewModelScope.launch {
            val segs = novelRepo.getChapterSegmentsBlocking(chapter.novelId, chapter.chapterNumber)
            val novel = localNovels.value.firstOrNull { it.id == chapter.novelId } ?: RyujinNovelMeta.createRyujinNovelEntity()
            val bytes = ReportExporter.buildChapterManuscriptText(novel, chapter, segs)
            val res = ReportExporter.writeBytesToUri(context, uri, bytes)
            if (res.isSuccess) {
                syncRepo.logDriveSync(
                    fileName = "${novel.title}_Bab_${chapter.chapterNumber}.txt",
                    syncType = "backup",
                    destination = "Unduhan Kreator",
                    status = "synced",
                    wordCount = chapter.wordCount.toLong()
                )
                _statusToast.value = "Naskah Bab ${chapter.chapterNumber} (${chapter.wordCount} kata) berhasil diunduh!"
            } else {
                _statusToast.value = "Gagal mengunduh bab."
            }
        }
    }

    // --- Community Actions ---
    fun postComment(chapterNumber: Int, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            val name = currentUserProfile.value?.displayName?.ifBlank { userDisplayName } ?: userDisplayName.ifBlank { "Pembaca" }
            val res = syncRepo.postComment(name, _selectedNovelId.value, chapterNumber, body)
            if (res.isSuccess) _statusToast.value = "Komentar terkirim."
        }
    }

    fun likeComment(comment: CommentItem) {
        viewModelScope.launch { syncRepo.likeComment(comment) }
    }

    fun createGroup(name: String, description: String, genreTag: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val res = syncRepo.createGroup(name, description, genreTag)
            if (res.isSuccess) {
                _selectedChatGroupId.value = res.getOrThrow()
                _statusToast.value = "Grup '$name' dibuat!"
            }
        }
    }

    fun joinGroup(group: CommunityGroup) {
        viewModelScope.launch {
            val res = syncRepo.joinGroup(group)
            if (res.isSuccess) {
                _selectedChatGroupId.value = group.id
                _statusToast.value = "Bergabung ke grup ${group.name}!"
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val name = currentUserProfile.value?.displayName?.ifBlank { userDisplayName } ?: userDisplayName.ifBlank { "Pembaca" }
            syncRepo.sendChatMessage(
                authorName = name,
                isCreator = isExclusiveCreator,
                groupId = _selectedChatGroupId.value,
                text = text
            )
        }
    }

    fun postStatus(mood: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val name = currentUserProfile.value?.displayName?.ifBlank { userDisplayName } ?: userDisplayName.ifBlank { "Pembaca" }
            val res = syncRepo.postStatus(name, isExclusiveCreator, mood, content)
            if (res.isSuccess) _statusToast.value = "Status dibagikan!"
        }
    }

    fun likeStatus(post: StatusPost) {
        viewModelScope.launch { syncRepo.likeStatus(post) }
    }

    fun saveAppDistributionConfig(
        sharedPreviewUrl: String,
        apkDownloadUrl: String,
        versionLabel: String,
        releaseNotes: String
    ) {
        if (!isExclusiveCreator) {
            _statusToast.value = "Hanya Developer/Kreator eksklusif yang dapat mengubah link distribusi."
            return
        }
        viewModelScope.launch {
            val res = syncRepo.saveAppDistribution(
                sharedPreviewUrl = sharedPreviewUrl,
                apkDownloadUrl = apkDownloadUrl,
                versionLabel = versionLabel,
                releaseNotes = releaseNotes
            )
            if (res.isSuccess) {
                syncRepo.publishChapterNotification(
                    actorName = userDisplayName.ifBlank { "Developer" },
                    title = "Link Unduh Aplikasi Diperbarui ($versionLabel)",
                    message = "Pembaca sekarang dapat mengunduh & membagikan versi terbaru Ryujin Studio."
                )
                _statusToast.value = "Link unduh & distribusi aplikasi berhasil diperbarui!"
            } else {
                _statusToast.value = "Gagal menyimpan link distribusi."
            }
        }
    }

    fun downloadCatalogSummaryToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val sb = StringBuilder()
            sb.append("RYUJIN STUDIO — PAKET KATALOG & PANDUAN AKSES\n")
            sb.append("==============================================\n")
            sb.append("Developer/Kreator Eksklusif: ${RyujinNovelMeta.CREATOR_EMAIL}\n")
            sb.append("Link Akses Preview Pembaca: ${appDistribution.value.sharedPreviewUrl}\n")
            if (appDistribution.value.apkDownloadUrl.isNotBlank()) {
                sb.append("Link Unduh APK Resmi: ${appDistribution.value.apkDownloadUrl}\n")
            }
            sb.append("\nDaftar Novel (${localNovels.value.size}):\n")
            localNovels.value.forEach { n ->
                sb.append("- ${n.title} oleh ${n.authorName} (${n.totalChapters} Bab, ${n.totalArcs} Arc)\n")
                sb.append("  Blurb: ${n.blurb}\n\n")
            }
            sb.append("Total Bab Ryujin Terindeks: ${allRyujinChapters.value.size}\n")
            sb.append("Total Karakter Terdokumentasi: ${characters.value.size}\n")
            val res = ReportExporter.writeBytesToUri(context, uri, sb.toString().toByteArray(Charsets.UTF_8))
            _statusToast.value = if (res.isSuccess) {
                "Paket katalog & tautan Ryujin Studio berhasil diunduh!"
            } else {
                "Gagal mengunduh berkas."
            }
        }
    }

    // --- Gemini AI Features (Firebase AI Logic) ---
    fun resetAiInsight() {
        insightJob?.cancel()
        _aiInsightState.value = AiGenerationState.Idle
    }

    fun analyzeChapterWithAi(chapter: LocalChapterEntity, openingExcerpt: String) {
        insightJob?.cancel()
        insightJob = viewModelScope.launch {
            _aiInsightState.value = AiGenerationState.Loading
            var lastText = ""
            try {
                val prompt = buildString {
                    append("Analisis Bab ${chapter.chapterNumber}: '${chapter.title}' ")
                    append("(Arc ${chapter.arcNumber}: ${chapter.arcTitle}).\n")
                    append("Tokoh terlibat: ${chapter.featuredCharacters}\n")
                    append("Ringkasan bab: ${chapter.summary}\n")
                    if (openingExcerpt.isNotBlank()) {
                        append("Cuplikan naskah dialog:\n${openingExcerpt.take(2400)}\n\n")
                    }
                    append(
                        "Berikan: 1) Ringkasan tajam peristiwa utama, 2) Dinamika hubungan & motivasi karakter, " +
                            "dan 3) Pertanyaan misteri/foreshadowing untuk bab selanjutnya."
                    )
                }
                aiService.streamPrompt(prompt).collect { accumulated ->
                    lastText = accumulated
                    _aiInsightState.value = AiGenerationState.Streaming(accumulated)
                }
                _aiInsightState.value = AiGenerationState.Success(lastText)
            } catch (e: TimeoutCancellationException) {
                Log.e("RyujinVM", "Chapter AI analysis timed out", e)
                _aiInsightState.value = AiGenerationState.Error(
                    "Permintaan AI melebihi batas waktu 60 detik. Silakan coba lagi."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RyujinVM", "Chapter AI analysis failed", e)
                _aiInsightState.value = AiGenerationState.Error(
                    e.localizedMessage ?: "Gagal menganalisis bab dengan Gemini AI."
                )
            }
        }
    }

    fun analyzeCharacterWithAi(character: LocalCharacterEntity) {
        insightJob?.cancel()
        insightJob = viewModelScope.launch {
            _aiInsightState.value = AiGenerationState.Loading
            var lastText = ""
            try {
                val prompt = buildString {
                    append("Buat profil psikologis & analisis peran mendalam untuk karakter Ryujin:\n")
                    append("Nama: ${character.name} (${character.alias})\n")
                    append("Negara: ${character.country} • Faksi: ${character.faction}\n")
                    append("Peran: ${character.role} • Kemunculan: ${character.arcPresence}\n")
                    append("Sifat: ${character.traits}\n")
                    append("Alur cerita: ${character.storyline}\n")
                    append("Profil dasar: ${character.deepProfile}\n\n")
                    append(
                        "Uraikan kekuatan taktisnya, dilema emosional, potensi aliansi/pengkhianatan lintas negara, " +
                            "dan contoh 2 baris dialog khas karakter ini."
                    )
                }
                aiService.streamPrompt(prompt).collect { accumulated ->
                    lastText = accumulated
                    _aiInsightState.value = AiGenerationState.Streaming(accumulated)
                }
                _aiInsightState.value = AiGenerationState.Success(lastText)
            } catch (e: TimeoutCancellationException) {
                Log.e("RyujinVM", "Character AI analysis timed out", e)
                _aiInsightState.value = AiGenerationState.Error(
                    "Analisis karakter melebihi batas waktu 60 detik."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RyujinVM", "Character AI analysis failed", e)
                _aiInsightState.value = AiGenerationState.Error(
                    e.localizedMessage ?: "Gagal menganalisis karakter dengan Gemini AI."
                )
            }
        }
    }

    fun sendAiChatMessage(userMessage: String) {
        val trimmed = userMessage.trim()
        if (trimmed.isEmpty()) return
        chatJob?.cancel()
        val historyBefore = _aiChatMessages.value
        val userEntry = AiChatEntry(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = trimmed
        )
        _aiChatMessages.value = historyBefore + userEntry

        chatJob = viewModelScope.launch {
            _aiChatState.value = AiGenerationState.Loading
            var replyText = ""
            try {
                val novel = activeNovel.value
                val novelContext = if (novel != null) {
                    "${novel.title} oleh ${novel.authorName} (${novel.totalChapters} bab, ${novel.totalArcs} arc, genre ${novel.genre}). Blurb: ${novel.blurb}"
                } else {
                    RyujinNovelMeta.BLURB
                }
                aiService.streamChatReply(
                    historyEntries = historyBefore,
                    userMessage = trimmed,
                    activeNovelContext = novelContext
                ).collect { accumulated ->
                    replyText = accumulated
                    _aiChatState.value = AiGenerationState.Streaming(accumulated)
                }
                _aiChatMessages.value = _aiChatMessages.value + AiChatEntry(
                    id = "m_${System.currentTimeMillis()}",
                    isUser = false,
                    text = replyText
                )
                _aiChatState.value = AiGenerationState.Idle
            } catch (e: TimeoutCancellationException) {
                Log.e("RyujinVM", "AI chat timed out", e)
                _aiChatState.value = AiGenerationState.Error(
                    "Respons Gemini AI melebihi batas waktu 60 detik. Coba kirim ulang."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RyujinVM", "AI chat failed", e)
                _aiChatState.value = AiGenerationState.Error(
                    e.localizedMessage ?: "Gagal menghubungi Gemini AI."
                )
            }
        }
    }

    fun clearAiChat() {
        chatJob?.cancel()
        _aiChatMessages.value = emptyList()
        _aiChatState.value = AiGenerationState.Idle
    }

    fun generateChapterOutlineWithAi(arcNumber: Int, topicHint: String) {
        if (!isExclusiveCreator) {
            _aiDraftError.value = "Fitur generator bab khusus Kreator eksklusif."
            return
        }
        viewModelScope.launch {
            _isGeneratingChapterDraft.value = true
            _aiDraftError.value = null
            try {
                val novelTitle = activeNovel.value?.title ?: "Ryujin"
                val draft = aiService.generateStructuredChapterOutline(
                    novelTitle = novelTitle,
                    arcNumber = arcNumber,
                    topicHint = topicHint
                )
                _aiChapterDraft.value = draft
            } catch (e: TimeoutCancellationException) {
                Log.e("RyujinVM", "Structured draft generation timed out", e)
                _aiDraftError.value = "Pembuatan draf bab AI melebihi batas waktu 60 detik."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RyujinVM", "Structured draft generation failed", e)
                _aiDraftError.value = e.localizedMessage ?: "Gagal membuat draf bab otomatis."
            } finally {
                _isGeneratingChapterDraft.value = false
            }
        }
    }

    fun consumeAiChapterDraft() {
        _aiChapterDraft.value = null
        _aiDraftError.value = null
    }
}
