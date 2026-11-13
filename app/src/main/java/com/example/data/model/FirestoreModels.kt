package com.example.data.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }
    val currentUser = auth?.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "reader",
    val lastChapterRead: Int = 1,
    val wordsRead: Long = 0L,
    val syncEnabled: Boolean = true,
    val driveLinked: Boolean = false,
    val driveFolderName: String = "",
    val statusMessage: String = "Sedang menjelajahi kisah Ryujin",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class NovelMeta(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val title: String = "",
    val authorName: String = "",
    val genre: String = "",
    val blurb: String = "",
    val preface: String = "",
    val totalChapters: Int = 1,
    val totalArcs: Int = 1,
    val wordsPerChapter: Int = 52500,
    val isPublished: Boolean = true,
    val coverTag: String = "CUSTOM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChapterSync(
    @DocumentId val id: String = "",
    val novelId: String = "",
    val chapterNumber: Int = 1,
    val arcNumber: Int = 1,
    val title: String = "",
    val summary: String = "",
    val wordCount: Int = 0,
    val contentPreview: String = "",
    val editedByUid: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommentItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val novelId: String = "ryujin",
    val chapterNumber: Int = 1,
    val body: String = "",
    val likesCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityGroup(
    @DocumentId val id: String = "",
    val creatorUid: String = "",
    val name: String = "",
    val description: String = "",
    val genreTag: String = "Ryujin Lore",
    val memberCount: Int = 1,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorRole: String = "reader",
    val groupId: String = "global_lounge",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StatusPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorRole: String = "reader",
    val mood: String = "Terpukau",
    val content: String = "",
    val likesCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorName: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "activity",
    val targetScope: String = "creator",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DriveSyncLog(
    @DocumentId val id: String = "",
    val creatorUid: String = "",
    val fileName: String = "",
    val syncType: String = "auto_sync",
    val destination: String = "Google Drive",
    val status: String = "synced",
    val wordCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppDistributionConfig(
    @DocumentId val id: String = "",
    val updatedByUid: String = "",
    val sharedPreviewUrl: String = "https://ais-pre-bih67rbfpkrrba3vtfrnjq-22106480739.asia-east1.run.app",
    val apkDownloadUrl: String = "",
    val versionLabel: String = "v1.0.0 (900 Bab)",
    val releaseNotes: String = "Rilis resmi Ryujin Studio — 900 Bab, 3 Arc, 400 Karakter, ≥50.000 Kata/Bab.",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
