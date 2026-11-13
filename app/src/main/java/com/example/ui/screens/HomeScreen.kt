package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LocalNovelEntity
import com.example.data.seed.RyujinNovelMeta
import com.example.ui.theme.LoraFontFamily
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    activeNovel: LocalNovelEntity?,
    allNovels: List<LocalNovelEntity>,
    totalRyujinWords: Long,
    lastChapterRead: Int,
    isExclusiveCreator: Boolean,
    onSelectNovel: (String) -> Unit,
    onOpenReader: (String, Int) -> Unit,
    onOpenChaptersTab: (Int) -> Unit,
    onRequestPreview: suspend (String, Int) -> String,
    onCreateNovel: (title: String, author: String, genre: String, blurb: String, preface: String, chapters: Int) -> Unit,
    onOpenDistribution: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showBlurb by remember { mutableStateOf(true) }
    var showPreface by remember { mutableStateOf(false) }
    var previewText by remember { mutableStateOf<String?>(null) }
    var showAddNovelDialog by remember { mutableStateOf(false) }

    val novel = activeNovel ?: RyujinNovelMeta.createRyujinNovelEntity()
    val formatter = remember { NumberFormat.getIntegerInstance(Locale("id", "ID")) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ryujin_hero),
                        contentDescription = "Sampul Utama ${novel.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x330B0D12), Color(0xCC0B0D12), Color(0xF50B0D12))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = novel.genre,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Karya Resmi",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = novel.title.uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Penulis: ${novel.authorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${novel.totalChapters} Bab • ${novel.totalArcs} Arc • ${formatter.format(if (novel.isRyujinDefault) totalRyujinWords else novel.totalChapters.toLong() * novel.wordsPerChapter)} Kata",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFDDE1EC)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onOpenReader(novel.id, lastChapterRead.coerceAtLeast(1)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("resume_reading_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Baca Bab ${lastChapterRead.coerceAtLeast(1)}")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        previewText = onRequestPreview(novel.id, 1)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.testTag("preview_novel_button")
                            ) {
                                Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau")
                            }
                            OutlinedButton(
                                onClick = onOpenDistribution,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("home_download_app_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Unduh & Bagikan Aplikasi", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Popular Genres Showcase
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Genre Populer Pilihan",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Eksplorasi Cepat",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(RyujinNovelMeta.popularGenres, key = { it.id }) { genre ->
                            Card(
                                onClick = {
                                    val targetArc = when (genre.id) {
                                        "g2" -> 2
                                        "g4" -> 3
                                        else -> 1
                                    }
                                    onOpenChaptersTab(targetArc)
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .width(210.dp)
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
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = genre.badge,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = genre.tagline,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Blurb Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBlurb = !showBlurb },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Blurb & Sinopsis Resmi", style = MaterialTheme.typography.titleMedium)
                            }
                            Icon(
                                imageVector = if (showBlurb) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Blurb"
                            )
                        }
                        AnimatedVisibility(visible = showBlurb) {
                            Text(
                                text = novel.blurb,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LoraFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            // Preface (Kata Pengantar)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPreface = !showPreface },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kata Pengantar Penulis", style = MaterialTheme.typography.titleMedium)
                            }
                            Icon(
                                imageVector = if (showPreface) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Kata Pengantar"
                            )
                        }
                        AnimatedVisibility(visible = showPreface) {
                            Text(
                                text = novel.preface,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LoraFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            // 3 Arcs Quick Explorer (when Ryujin is selected)
            if (novel.isRyujinDefault) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Struktur 3 Arc (900 Bab Penuh)",
                            style = MaterialTheme.typography.titleLarge
                        )
                        RyujinNovelMeta.arcs.forEach { arc ->
                            Card(
                                onClick = { onOpenChaptersTab(arc.arcNumber) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = arc.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        AssistChip(
                                            onClick = { onOpenChaptersTab(arc.arcNumber) },
                                            label = { Text(arc.rangeLabel) }
                                        )
                                    }
                                    Text(
                                        text = arc.subtitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = arc.synopsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Novel Library + Add Novel (Author feature)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rak Novel Studio (${allNovels.size})", style = MaterialTheme.typography.titleLarge)
                        if (isExclusiveCreator) {
                            FilledTonalButton(
                                onClick = { showAddNovelDialog = true },
                                modifier = Modifier.testTag("add_novel_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Novel")
                            }
                        }
                    }

                    allNovels.forEach { item ->
                        val isSelected = item.id == novel.id
                        Card(
                            onClick = { onSelectNovel(item.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${item.authorName} • ${item.genre}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${item.totalChapters} Bab • ${item.totalArcs} Arc",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                TextButton(onClick = { onOpenReader(item.id, 1) }) {
                                    Text("Buka")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Preview Modal
    if (previewText != null) {
        AlertDialog(
            onDismissRequest = { previewText = null },
            title = { Text("Pratinjau Naskah: ${novel.title} (Bab 1)") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = previewText!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LoraFontFamily)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    previewText = null
                    onOpenReader(novel.id, 1)
                }) {
                    Text("Baca Bab Penuh")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewText = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Creator Add Novel Dialog
    if (showAddNovelDialog) {
        AddNovelDialog(
            onDismiss = { showAddNovelDialog = false },
            onSubmit = { title, author, genre, blurb, preface, count ->
                onCreateNovel(title, author, genre, blurb, preface, count)
                showAddNovelDialog = false
            }
        )
    }
}

@Composable
private fun AddNovelDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, author: String, genre: String, blurb: String, preface: String, count: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf(RyujinNovelMeta.AUTHOR_NAME) }
    var genre by remember { mutableStateOf("Dark Action Thriller") }
    var blurb by remember { mutableStateOf("") }
    var preface by remember { mutableStateOf("") }
    var initialChapters by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Novel Baru (Author Studio)") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Novel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Nama Penulis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialChapters,
                    onValueChange = { initialChapters = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah Bab Awal (1–30)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = blurb,
                    onValueChange = { blurb = it },
                    label = { Text("Blurb / Deskripsi Cerita") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = preface,
                    onValueChange = { preface = it },
                    label = { Text("Kata Pengantar Penulis") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = initialChapters.toIntOrNull()?.coerceIn(1, 30) ?: 5
                    onSubmit(title, author, genre, blurb, preface, count)
                },
                enabled = title.isNotBlank() && blurb.isNotBlank()
            ) {
                Text("Terbitkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
