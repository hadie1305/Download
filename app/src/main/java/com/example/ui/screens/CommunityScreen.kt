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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.CommentItem
import com.example.data.model.CommunityGroup
import com.example.data.model.StatusPost
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    chatMessages: List<ChatMessage>,
    groups: List<CommunityGroup>,
    selectedChatGroupId: String,
    comments: List<CommentItem>,
    statusPosts: List<StatusPost>,
    onSelectChatGroup: (String) -> Unit,
    onSendChatMessage: (String) -> Unit,
    onCreateGroup: (name: String, description: String, genreTag: String) -> Unit,
    onJoinGroup: (CommunityGroup) -> Unit,
    onPostComment: (chapterNumber: Int, body: String) -> Unit,
    onLikeComment: (CommentItem) -> Unit,
    onPostStatus: (mood: String, content: String) -> Unit,
    onLikeStatus: (StatusPost) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Obrolan", "Komentar", "Group", "Status")
    val timeFmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    val icon = when (index) {
                        0 -> Icons.Default.Forum
                        1 -> Icons.Default.Comment
                        2 -> Icons.Default.Group
                        else -> Icons.Default.Mood
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) },
                        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ChatTabContent(
                    chatMessages = chatMessages,
                    groups = groups,
                    selectedChatGroupId = selectedChatGroupId,
                    timeFmt = timeFmt,
                    onSelectChatGroup = onSelectChatGroup,
                    onSendChatMessage = onSendChatMessage
                )

                1 -> CommentsTabContent(
                    comments = comments,
                    timeFmt = timeFmt,
                    onPostComment = onPostComment,
                    onLikeComment = onLikeComment
                )

                2 -> GroupsTabContent(
                    groups = groups,
                    selectedChatGroupId = selectedChatGroupId,
                    onCreateGroup = onCreateGroup,
                    onJoinGroup = onJoinGroup,
                    onOpenChat = { gid ->
                        onSelectChatGroup(gid)
                        selectedTab = 0
                    }
                )

                3 -> StatusTabContent(
                    statusPosts = statusPosts,
                    timeFmt = timeFmt,
                    onPostStatus = onPostStatus,
                    onLikeStatus = onLikeStatus
                )
            }
        }
    }
}

@Composable
private fun ChatTabContent(
    chatMessages: List<ChatMessage>,
    groups: List<CommunityGroup>,
    selectedChatGroupId: String,
    timeFmt: SimpleDateFormat,
    onSelectChatGroup: (String) -> Unit,
    onSendChatMessage: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedChatGroupId == "global_lounge",
                    onClick = { onSelectChatGroup("global_lounge") },
                    label = { Text("Lounge Utama") }
                )
            }
            items(groups, key = { it.id }) { grp ->
                FilterChip(
                    selected = selectedChatGroupId == grp.id,
                    onClick = { onSelectChatGroup(grp.id) },
                    label = { Text(grp.name) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Belum ada pesan di ruang obrolan ini. Mulai diskusi bersama pembaca lain & kreator!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            items(chatMessages, key = { it.id.ifBlank { it.hashCode().toString() } }) { msg ->
                val isCreatorMsg = msg.authorRole == "creator"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCreatorMsg) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = msg.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCreatorMsg) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (isCreatorMsg) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "KREATOR",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = msg.createdAt?.toDate()?.let { timeFmt.format(it) } ?: "Baru saja",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = msg.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Tulis pesan obrolan...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input")
            )
            Button(
                onClick = {
                    onSendChatMessage(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("send_chat_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim obrolan")
            }
        }
    }
}

@Composable
private fun CommentsTabContent(
    comments: List<CommentItem>,
    timeFmt: SimpleDateFormat,
    onPostComment: (Int, String) -> Unit,
    onLikeComment: (CommentItem) -> Unit
) {
    var chapterText by remember { mutableStateOf("1") }
    var bodyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Tulis Komentar Bab", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chapterText,
                            onValueChange = { chapterText = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Bab #") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(96.dp)
                        )
                        OutlinedTextField(
                            value = bodyText,
                            onValueChange = { bodyText = it },
                            label = { Text("Pendapat atau teori cerita...") },
                            minLines = 2,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input")
                        )
                    }
                    Button(
                        onClick = {
                            val chNum = chapterText.toIntOrNull()?.coerceIn(1, 900) ?: 1
                            onPostComment(chNum, bodyText)
                            bodyText = ""
                        },
                        enabled = bodyText.isNotBlank(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("post_comment_button")
                    ) {
                        Text("Kirim Komentar")
                    }
                }
            }
        }

        items(comments, key = { it.id.ifBlank { it.hashCode().toString() } }) { comment ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
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
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Bab ${comment.chapterNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = comment.body, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.createdAt?.toDate()?.let { timeFmt.format(it) } ?: "Baru saja",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { onLikeComment(comment) }) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Suka komentar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${comment.likesCount}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupsTabContent(
    groups: List<CommunityGroup>,
    selectedChatGroupId: String,
    onCreateGroup: (name: String, description: String, genreTag: String) -> Unit,
    onJoinGroup: (CommunityGroup) -> Unit,
    onOpenChat: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grup Diskusi & Klan Pembaca", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Buat atau gabung grup untuk obrolan teori & arc.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_group_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Group")
                }
            }
        }

        if (groups.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Belum ada grup komunitas. Klik 'Buat Group' untuk mendirikan grup diskusi pertama!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(groups, key = { it.id.ifBlank { it.name } }) { group ->
            val isActiveChat = selectedChatGroupId == group.id
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isActiveChat) 1.5.dp else 1.dp,
                        color = if (isActiveChat) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${group.genreTag} • ${group.memberCount} anggota",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (group.description.isNotBlank()) {
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { onJoinGroup(group) }) {
                            Text("Gabung")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onOpenChat(group.id) }) {
                            Text("Masuk Obrolan")
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var tag by remember { mutableStateOf("Ryujin Lore") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Buat Group Komunitas Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Group") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Topik / Genre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Deskripsi Singkat") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateGroup(name, desc, tag)
                        showCreateDialog = false
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Buat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun StatusTabContent(
    statusPosts: List<StatusPost>,
    timeFmt: SimpleDateFormat,
    onPostStatus: (mood: String, content: String) -> Unit,
    onLikeStatus: (StatusPost) -> Unit
) {
    val moods = listOf("Terpukau", "Penasaran", "Menulis", "Tegang", "Teori")
    var selectedMood by remember { mutableStateOf(moods.first()) }
    var statusText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Bagikan Status Membaca", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(moods, key = { it }) { mood ->
                            FilterChip(
                                selected = selectedMood == mood,
                                onClick = { selectedMood = mood },
                                label = { Text(mood) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        label = { Text("Apa kesanmu tentang bab atau tokoh hari ini?") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("status_input")
                    )
                    Button(
                        onClick = {
                            onPostStatus(selectedMood, statusText)
                            statusText = ""
                        },
                        enabled = statusText.isNotBlank(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("post_status_button")
                    ) {
                        Text("Bagikan Status")
                    }
                }
            }
        }

        items(statusPosts, key = { it.id.ifBlank { it.hashCode().toString() } }) { post ->
            val isCreatorPost = post.authorRole == "creator"
            Card(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCreatorPost) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "KREATOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = post.mood,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.createdAt?.toDate()?.let { timeFmt.format(it) } ?: "Baru saja",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { onLikeStatus(post) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Suka status",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.likesCount}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
