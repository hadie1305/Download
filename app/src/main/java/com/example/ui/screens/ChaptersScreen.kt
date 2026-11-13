package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai.GeneratedChapterOutline
import com.example.data.local.LocalChapterEntity
import com.example.data.local.LocalNovelEntity
import com.example.data.repository.AdvancedSearchResults
import com.example.data.seed.RyujinNovelMeta
import com.example.ui.theme.LoraFontFamily
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ChaptersScreen(
    activeNovel: LocalNovelEntity?,
    selectedArc: Int,
    chapters: List<LocalChapterEntity>,
    searchQuery: String,
    searchArcFilter: Int,
    searchResults: AdvancedSearchResults,
    isSearching: Boolean,
    isExclusiveCreator: Boolean,
    aiChapterDraft: GeneratedChapterOutline? = null,
    isGeneratingChapterDraft: Boolean = false,
    aiDraftError: String? = null,
    onGenerateChapterDraftWithAi: (Int, String) -> Unit = { _, _ -> },
    onConsumeAiChapterDraft: () -> Unit = {},
    onSelectArc: (Int) -> Unit,
    onSearchChange: (String, Int) -> Unit,
    onOpenReader: (String, Int) -> Unit,
    onRequestPreview: suspend (String, Int) -> String,
    onAddChapter: (arc: Int, title: String, summary: String, script: String) -> Unit
) {
    val novel = activeNovel ?: RyujinNovelMeta.createRyujinNovelEntity()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var jumpInput by remember { mutableStateOf("") }
    var previewState by remember { mutableStateOf<Pair<LocalChapterEntity, String>?>(null) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    val numFmt = remember { NumberFormat.getIntegerInstance(Locale("id", "ID")) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top bar: Title + Jump to Chapter + Creator Add Chapter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${novel.title} — Daftar Bab",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (novel.isRyujinDefault) "900 Bab • 3 Arc • ≥50.000 Kata/Bab" else "${novel.totalChapters} Bab",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (isExclusiveCreator) {
                    Button(
                        onClick = { showAddChapterDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_chapter_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bab Baru")
                    }
                }
            }

            // Advanced FTS4 Search Bar + Jump to Bab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { onSearchChange(it, searchArcFilter) },
                    placeholder = { Text("Cari judul, dialog, tokoh, negara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("", searchArcFilter) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("advanced_search_input")
                )

                OutlinedTextField(
                    value = jumpInput,
                    onValueChange = { jumpInput = it.filter { c -> c.isDigit() }.take(4) },
                    placeholder = { Text("Bab #") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(88.dp)
                )

                Button(
                    onClick = {
                        val target = jumpInput.toIntOrNull()
                        if (target != null && target >= 1) {
                            val clamped = target.coerceAtMost(novel.totalChapters.coerceAtLeast(1))
                            onOpenReader(novel.id, clamped)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Buka")
                }
            }

            // Arc Filter / Switcher Chips
            if (novel.isRyujinDefault) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (searchQuery.isNotBlank()) {
                        item {
                            FilterChip(
                                selected = searchArcFilter == 0,
                                onClick = { onSearchChange(searchQuery, 0) },
                                label = { Text("Semua Arc") }
                            )
                        }
                    }
                    items(listOf(1, 2, 3)) { arcNum ->
                        val label = when (arcNum) {
                            1 -> "Arc 1: Ryujin No Tanjou (1–300)"
                            2 -> "Arc 2: The Mask of Angels (301–600)"
                            else -> "Arc 3: The Last Generation (601–900)"
                        }
                        val isSelected = if (searchQuery.isNotBlank()) searchArcFilter == arcNum else selectedArc == arcNum
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    onSearchChange(searchQuery, arcNum)
                                } else {
                                    onSelectArc(arcNum)
                                    scope.launch { listState.scrollToItem(0) }
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (searchQuery.isNotBlank()) {
                // Advanced Search Results View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = "Hasil Pencarian Canggih: ${searchResults.chapters.size} Bab • ${searchResults.characters.size} Tokoh • ${searchResults.segments.size} Adegan Dialog",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (searchResults.segments.isNotEmpty()) {
                        item {
                            Text("Kecocokan Naskah Dialog:", style = MaterialTheme.typography.titleMedium)
                        }
                        items(searchResults.segments, key = { "seg_${it.id}" }) { seg ->
                            Card(
                                onClick = { onOpenReader(seg.novelId, seg.chapterNumber) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Bab ${seg.chapterNumber} • ${seg.sceneTitle}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = seg.content.take(180).replace("\n", " ") + "...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (searchResults.characters.isNotEmpty()) {
                        item {
                            Text("Kecocokan Tokoh (400 Karakter):", style = MaterialTheme.typography.titleMedium)
                        }
                        items(searchResults.characters.take(15), key = { "ch_res_${it.id}" }) { ch ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${ch.name} (${ch.alias})", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${ch.country} • ${ch.traits}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = ch.storyline,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    items(searchResults.chapters, key = { "res_${it.id}" }) { ch ->
                        ChapterRowCard(
                            chapter = ch,
                            numFmt = numFmt,
                            onPreview = {
                                scope.launch {
                                    val txt = onRequestPreview(ch.novelId, ch.chapterNumber)
                                    previewState = ch to txt
                                }
                            },
                            onRead = { onOpenReader(ch.novelId, ch.chapterNumber) }
                        )
                    }
                }
            } else {
                // Full Arc Chapter List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(chapters, key = { it.id }) { ch ->
                        ChapterRowCard(
                            chapter = ch,
                            numFmt = numFmt,
                            onPreview = {
                                scope.launch {
                                    val txt = onRequestPreview(ch.novelId, ch.chapterNumber)
                                    previewState = ch to txt
                                }
                            },
                            onRead = { onOpenReader(ch.novelId, ch.chapterNumber) }
                        )
                    }
                }
            }
        }
    }

    if (previewState != null) {
        val (ch, excerpt) = previewState!!
        AlertDialog(
            onDismissRequest = { previewState = null },
            title = { Text("Pratinjau Bab ${ch.chapterNumber}: ${ch.title}") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = excerpt,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LoraFontFamily)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    previewState = null
                    onOpenReader(ch.novelId, ch.chapterNumber)
                }) {
                    Text("Baca Penuh (${numFmt.format(ch.wordCount)} kata)")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewState = null }) { Text("Tutup") }
            }
        )
    }

    if (showAddChapterDialog) {
        AddChapterDialog(
            defaultArc = selectedArc,
            aiChapterDraft = aiChapterDraft,
            isGeneratingDraft = isGeneratingChapterDraft,
            aiDraftError = aiDraftError,
            onGenerateDraft = onGenerateChapterDraftWithAi,
            onConsumeDraft = onConsumeAiChapterDraft,
            onDismiss = {
                onConsumeAiChapterDraft()
                showAddChapterDialog = false
            },
            onSubmit = { arc, title, summary, script ->
                onConsumeAiChapterDraft()
                onAddChapter(arc, title, summary, script)
                showAddChapterDialog = false
            }
        )
    }
}

@Composable
private fun ChapterRowCard(
    chapter: LocalChapterEntity,
    numFmt: NumberFormat,
    onPreview: () -> Unit,
    onRead: () -> Unit
) {
    Card(
        onClick = onRead,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Bab ${chapter.chapterNumber} • Arc ${chapter.arcNumber} (#${chapter.arcChapterNumber})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${numFmt.format(chapter.wordCount)} kata",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tokoh: ${chapter.featuredCharacters}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pratinjau", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRead,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Baca", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AddChapterDialog(
    defaultArc: Int,
    aiChapterDraft: GeneratedChapterOutline?,
    isGeneratingDraft: Boolean,
    aiDraftError: String?,
    onGenerateDraft: (arc: Int, topicHint: String) -> Unit,
    onConsumeDraft: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (arc: Int, title: String, summary: String, script: String) -> Unit
) {
    var arcText by remember { mutableStateOf(defaultArc.toString()) }
    var aiTopicHint by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var script by remember { mutableStateOf("") }

    LaunchedEffect(aiChapterDraft) {
        if (aiChapterDraft != null) {
            arcText = aiChapterDraft.arcNumber.toString()
            title = aiChapterDraft.title
            summary = aiChapterDraft.summary
            script = aiChapterDraft.openingDialogue
            onConsumeDraft()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Bab Baru & Siarkan Update") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = aiTopicHint,
                    onValueChange = { aiTopicHint = it },
                    label = { Text("Topik/Ide Bab untuk Gemini AI (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FilledTonalButton(
                    onClick = {
                        val arc = arcText.toIntOrNull()?.coerceIn(1, 3) ?: defaultArc
                        onGenerateDraft(arc, aiTopicHint)
                    },
                    enabled = !isGeneratingDraft,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_chapter_ai_button")
                ) {
                    if (isGeneratingDraft) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyusun Draf Bab dengan Gemini AI...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buat Draf Bab Otomatis (Gemini AI)")
                    }
                }
                if (!aiDraftError.isNullOrBlank()) {
                    Text(
                        text = aiDraftError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedTextField(
                    value = arcText,
                    onValueChange = { arcText = it.filter { c -> c.isDigit() }.take(1) },
                    label = { Text("Arc (1 - 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Bab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Ringkasan Alur") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = script,
                    onValueChange = { script = it },
                    label = { Text("Naskah Dialog Pembuka (Opsional — otomatis dilengkapi 52.500+ kata)") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val arc = arcText.toIntOrNull()?.coerceIn(1, 3) ?: 1
                    onSubmit(arc, title, summary, script)
                },
                enabled = title.isNotBlank() && !isGeneratingDraft
            ) {
                Text("Simpan & Siarkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
