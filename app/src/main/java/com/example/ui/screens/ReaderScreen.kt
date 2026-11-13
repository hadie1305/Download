package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiGenerationState
import com.example.data.local.LocalChapterEntity
import com.example.data.local.LocalChapterSegmentEntity
import com.example.data.model.CommentItem
import com.example.ui.theme.LoraFontFamily
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapter: LocalChapterEntity,
    segments: List<LocalChapterSegmentEntity>,
    isLoadingSegments: Boolean,
    totalChaptersInNovel: Int,
    fontSizeSp: Int,
    isDarkMode: Boolean,
    isExclusiveCreator: Boolean,
    chapterComments: List<CommentItem>,
    aiInsightState: AiGenerationState = AiGenerationState.Idle,
    onAnalyzeChapterWithAi: (LocalChapterEntity, String) -> Unit = { _, _ -> },
    onResetAiInsight: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateChapter: (Int) -> Unit,
    onChangeFontSize: (Int) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSaveEditedSegment: (LocalChapterSegmentEntity, String, String) -> Unit,
    onDownloadChapter: (LocalChapterEntity, android.net.Uri) -> Unit,
    onPostComment: (Int, String) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var editingSegment by remember { mutableStateOf<LocalChapterSegmentEntity?>(null) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showAiAnalysisDialog by remember { mutableStateOf(false) }
    val numFmt = remember { NumberFormat.getIntegerInstance(Locale("id", "ID")) }

    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            onDownloadChapter(chapter, uri)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Bab ${chapter.chapterNumber}: ${chapter.title}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${chapter.arcTitle} • ${numFmt.format(chapter.wordCount)} kata",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val excerpt = segments.firstOrNull()?.content.orEmpty().take(2400)
                            onAnalyzeChapterWithAi(chapter, excerpt)
                            showAiAnalysisDialog = true
                        },
                        modifier = Modifier.testTag("reader_ai_analyze_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ringkas & Analisis Bab AI",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = { onChangeFontSize(fontSizeSp - 1) }) {
                        Icon(Icons.Default.TextDecrease, contentDescription = "Perkecil huruf")
                    }
                    IconButton(onClick = { onChangeFontSize(fontSizeSp + 1) }) {
                        Icon(Icons.Default.TextIncrease, contentDescription = "Perbesar huruf")
                    }
                    IconButton(onClick = { onToggleDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Ubah mode tema"
                        )
                    }
                    if (isExclusiveCreator) {
                        IconButton(
                            onClick = { downloadLauncher.launch("Ryujin_Bab_${chapter.chapterNumber}.txt") },
                            modifier = Modifier.testTag("download_chapter_button")
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Unduh naskah bab",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigateChapter(chapter.chapterNumber - 1) },
                        enabled = chapter.chapterNumber > 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bab ${chapter.chapterNumber - 1}")
                    }
                    TextButton(onClick = { showCommentsDialog = true }) {
                        Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Komentar (${chapterComments.count { it.chapterNumber == chapter.chapterNumber }})")
                    }
                    Button(
                        onClick = { onNavigateChapter(chapter.chapterNumber + 1) },
                        enabled = chapter.chapterNumber < totalChaptersInNovel
                    ) {
                        Text("Bab ${chapter.chapterNumber + 1}")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoadingSegments && segments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat 52.500+ kata naskah dialog penuh...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 780.dp)
                ) {
                    // Quick Scene Jump Bar (Adegan 1..25)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(segments, key = { _, s -> s.id }) { idx, _ ->
                            FilterChip(
                                selected = listState.firstVisibleItemIndex == idx,
                                onClick = { scope.launch { listState.animateScrollToItem(idx) } },
                                label = { Text("Adegan ${idx + 1}") }
                            )
                        }
                    }

                    // Paged Streaming Dialogue Content
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        itemsIndexed(segments, key = { _, seg -> seg.id }) { _, seg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = seg.sceneTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "${numFmt.format(seg.wordCount)} kata • Full Dialog",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isExclusiveCreator) {
                                            IconButton(
                                                onClick = { editingSegment = seg },
                                                modifier = Modifier.testTag("edit_segment_${seg.segmentIndex}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Adegan",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    DialogueScriptText(
                                        rawText = seg.content,
                                        fontSizeSp = fontSizeSp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingSegment != null) {
        val seg = editingSegment!!
        var editTitle by remember(seg.id) { mutableStateOf(seg.sceneTitle) }
        var editBody by remember(seg.id) { mutableStateOf(seg.content) }
        AlertDialog(
            onDismissRequest = { editingSegment = null },
            title = { Text("Edit Naskah (Eksklusif Kreator)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Judul Adegan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBody,
                        onValueChange = { editBody = it },
                        label = { Text("Naskah Dialog") },
                        minLines = 8,
                        maxLines = 14,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveEditedSegment(seg, editTitle, editBody)
                    editingSegment = null
                }) {
                    Text("Simpan & Sinkronkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSegment = null }) { Text("Batal") }
            }
        )
    }

    if (showCommentsDialog) {
        var commentBody by remember { mutableStateOf("") }
        val filtered = chapterComments.filter { it.chapterNumber == chapter.chapterNumber }
        AlertDialog(
            onDismissRequest = { showCommentsDialog = false },
            title = { Text("Komentar Bab ${chapter.chapterNumber}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = commentBody,
                        onValueChange = { commentBody = it },
                        label = { Text("Tulis komentar untuk bab ini...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onPostComment(chapter.chapterNumber, commentBody)
                            commentBody = ""
                        },
                        enabled = commentBody.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim Komentar")
                    }
                    filtered.forEach { c ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(c.authorName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                Text(c.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    if (filtered.isEmpty()) {
                        Text("Belum ada komentar pada bab ini.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommentsDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showAiAnalysisDialog) {
        AiInsightDialog(
            title = "Analisis AI: Bab ${chapter.chapterNumber}",
            state = aiInsightState,
            onRetry = {
                val excerpt = segments.firstOrNull()?.content.orEmpty().take(2400)
                onAnalyzeChapterWithAi(chapter, excerpt)
            },
            onDismiss = {
                showAiAnalysisDialog = false
                onResetAiInsight()
            }
        )
    }
}

@Composable
private fun DialogueScriptText(
    rawText: String,
    fontSizeSp: Int
) {
    val speakerColor = MaterialTheme.colorScheme.secondary
    val stageColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.onSurface

    val annotated = remember(rawText, speakerColor, stageColor, bodyColor) {
        buildAnnotatedString {
            rawText.lines().forEachIndexed { idx, line ->
                if (idx > 0) append("\n")
                when {
                    line.startsWith("[") -> {
                        withStyle(SpanStyle(color = stageColor, fontWeight = FontWeight.SemiBold)) {
                            append(line)
                        }
                    }
                    line.contains(": \"") -> {
                        val colonIdx = line.indexOf(": \"")
                        withStyle(SpanStyle(color = speakerColor, fontWeight = FontWeight.Bold)) {
                            append(line.substring(0, colonIdx + 1))
                        }
                        withStyle(SpanStyle(color = bodyColor)) {
                            append(line.substring(colonIdx + 1))
                        }
                    }
                    else -> {
                        withStyle(SpanStyle(color = bodyColor)) {
                            append(line)
                        }
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = LoraFontFamily,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp + 9).sp
        )
    )
}
