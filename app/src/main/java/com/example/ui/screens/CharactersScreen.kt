package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ai.AiGenerationState
import com.example.data.local.LocalCharacterEntity
import com.example.ui.theme.LoraFontFamily

@Composable
fun CharactersScreen(
    characters: List<LocalCharacterEntity>,
    countries: List<String>,
    aiInsightState: AiGenerationState = AiGenerationState.Idle,
    onAnalyzeCharacterWithAi: (LocalCharacterEntity) -> Unit = {},
    onResetAiInsight: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("Semua Negara") }
    var selectedCharacter by remember { mutableStateOf<LocalCharacterEntity?>(null) }
    var aiCharacterTarget by remember { mutableStateOf<LocalCharacterEntity?>(null) }

    val filteredCharacters by remember(characters, query, selectedCountry) {
        derivedStateOf {
            val q = query.trim().lowercase()
            characters.filter { ch ->
                val countryMatch = selectedCountry == "Semua Negara" || ch.country.equals(selectedCountry, ignoreCase = true)
                val queryMatch = q.isEmpty() ||
                    ch.name.lowercase().contains(q) ||
                    ch.alias.lowercase().contains(q) ||
                    ch.traits.lowercase().contains(q) ||
                    ch.country.lowercase().contains(q) ||
                    ch.faction.lowercase().contains(q) ||
                    ch.role.lowercase().contains(q)
                countryMatch && queryMatch
            }
        }
    }

    val featuredHeroes = remember(characters) {
        characters.filter { it.portraitKey in setOf("kenjiro", "aoi", "sakura") }.take(3)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ensiklopedia 400 Karakter Ryujin",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Nama • Sifat • Negara • Alur Cerita • Profil Mendalam",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari nama tokoh, sifat, negara, atau faksi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari karakter") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian karakter")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_search_input")
            )

            // Country Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCountry == "Semua Negara",
                        onClick = { selectedCountry = "Semua Negara" },
                        label = { Text("Semua (${characters.size})") }
                    )
                }
                items(countries, key = { it }) { country ->
                    FilterChip(
                        selected = selectedCountry == country,
                        onClick = { selectedCountry = country },
                        label = { Text(country) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (query.isBlank() && selectedCountry == "Semua Negara" && featuredHeroes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Tokoh Utama & Ilustrasi Karakter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(featuredHeroes, key = { "hero_${it.id}" }) { hero ->
                                FeaturedPortraitCard(
                                    character = hero,
                                    onClick = { selectedCharacter = hero }
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = "Seluruh Daftar Tokoh (${filteredCharacters.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Menampilkan ${filteredCharacters.size} dari ${characters.size} karakter",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                items(filteredCharacters, key = { it.id }) { ch ->
                    CharacterListCard(
                        character = ch,
                        onClick = { selectedCharacter = ch }
                    )
                }
            }
        }
    }

    if (selectedCharacter != null) {
        val current = selectedCharacter!!
        CharacterDetailDialog(
            character = current,
            onAnalyzeWithAi = {
                aiCharacterTarget = current
                onAnalyzeCharacterWithAi(current)
            },
            onDismiss = { selectedCharacter = null }
        )
    }

    if (aiCharacterTarget != null) {
        val target = aiCharacterTarget!!
        AiInsightDialog(
            title = "Profil AI: ${target.name}",
            state = aiInsightState,
            onRetry = { onAnalyzeCharacterWithAi(target) },
            onDismiss = {
                aiCharacterTarget = null
                onResetAiInsight()
            }
        )
    }
}

@Composable
private fun FeaturedPortraitCard(
    character: LocalCharacterEntity,
    onClick: () -> Unit
) {
    val portraitRes = portraitDrawableForKey(character.portraitKey)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(220.dp)
            .height(250.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (portraitRes != null) {
                Image(
                    painter = painterResource(id = portraitRes),
                    contentDescription = "Potret ${character.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xD90B0D12), Color(0xFA0B0D12))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = character.country,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = character.alias,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = character.traits,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDDE1EC),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CharacterListCard(
    character: LocalCharacterEntity,
    onClick: () -> Unit
) {
    val portraitRes = portraitDrawableForKey(character.portraitKey)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("character_card_${character.characterNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (portraitRes != null) {
                Image(
                    painter = painterResource(id = portraitRes),
                    contentDescription = "Avatar ${character.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                )
            } else {
                val initials = character.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${character.characterNumber} ${character.name}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = character.country,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${character.alias} • ${character.faction}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Sifat: ${character.traits}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = character.storyline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CharacterDetailDialog(
    character: LocalCharacterEntity,
    onAnalyzeWithAi: () -> Unit,
    onDismiss: () -> Unit
) {
    val portraitRes = portraitDrawableForKey(character.portraitKey)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("#${character.characterNumber} ${character.name}")
                Text(
                    text = character.alias,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (portraitRes != null) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = "Potret lengkap ${character.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(character.country, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(character.faction, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                ProfileBlock("Peran & Kemunculan", "${character.role} • ${character.arcPresence}")
                ProfileBlock("Sifat & Karakteristik", character.traits)
                HorizontalDivider()
                ProfileBlock("Alur Cerita (Storyline)", character.storyline, useSerif = true)
                ProfileBlock("Profil Mendalam", character.deepProfile, useSerif = true)
                FilledTonalButton(
                    onClick = onAnalyzeWithAi,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_ai_insight_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisis Mendalam Gemini AI")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun ProfileBlock(
    heading: String,
    body: String,
    useSerif: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = body,
            style = if (useSerif) MaterialTheme.typography.bodyMedium.copy(fontFamily = LoraFontFamily)
            else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun portraitDrawableForKey(key: String): Int? = when (key.lowercase()) {
    "kenjiro" -> R.drawable.img_char_kenjiro
    "aoi" -> R.drawable.img_char_aoi
    "sakura" -> R.drawable.img_char_sakura
    else -> null
}
