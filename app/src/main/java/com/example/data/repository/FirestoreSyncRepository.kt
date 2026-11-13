package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AppDistributionConfig
import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.CommentItem
import com.example.data.model.CommunityGroup
import com.example.data.model.DriveSyncLog
import com.example.data.model.NovelMeta
import com.example.data.model.OperationType
import com.example.data.model.StatusPost
import com.example.data.model.UserProfile
import com.example.data.model.handleFirestoreError
import com.example.data.seed.RyujinNovelMeta
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreSyncRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isVerifiedCreatorEmail(email: String?, isVerified: Boolean): Boolean {
        return isVerified && email.equals(RyujinNovelMeta.CREATOR_EMAIL, ignoreCase = true)
    }

    private fun <T> Flow<T>.handleSnapshotErrors(operationType: OperationType, path: String, uid: String): Flow<T> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                handleFirestoreError(exceptionToLog, operationType, path)
            }
            throw error
        }

    suspend fun ensureUserProfileExists(
        uid: String,
        displayName: String,
        email: String,
        isCreator: Boolean
    ): Result<UserProfile> {
        val docRef = db.collection("users").document(uid)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (existing != null) {
                    return Result.success(existing)
                }
            }
            val safeName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Pembaca Ryujin" } }.take(75)
            val safeEmail = email.ifBlank { "user@ryujin.studio" }.take(150)
            val role = if (isCreator) "creator" else "reader"
            val payload = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "email" to safeEmail,
                "role" to role,
                "lastChapterRead" to 1,
                "wordsRead" to 0L,
                "syncEnabled" to true,
                "driveLinked" to false,
                "driveFolderName" to "",
                "statusMessage" to if (isCreator) "Developer & Author Utama Ryujin" else "Membaca Ryujin",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            val created = docRef.get().await().toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: UserProfile(id = uid, userId = uid, displayName = safeName, email = safeEmail, role = role)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserReadingProgress(
        uid: String,
        chapterNumber: Int,
        wordsIncrement: Long
    ) {
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            val currentWords = snap.getLong("wordsRead") ?: 0L
            docRef.update(
                mapOf(
                    "lastChapterRead" to chapterNumber.coerceAtLeast(1),
                    "wordsRead" to (currentWords + wordsIncrement).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    suspend fun updateCreatorDriveStatus(
        uid: String,
        driveLinked: Boolean,
        driveFolderName: String,
        syncEnabled: Boolean
    ) {
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "driveLinked" to driveLinked,
                    "driveFolderName" to driveFolderName.take(150),
                    "syncEnabled" to syncEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    fun observeCurrentUserProfile(uid: String): Flow<UserProfile?> = flow {
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleSnapshotErrors(OperationType.GET, path, uid)
        )
    }

    // Exclusive Creator-only: list all users in user database
    fun observeAllUsersForCreator(): Flow<List<UserProfile>> = flow {
        val uid = requireUserId()
        val path = "users"
        emitAll(
            db.collection(path)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeNovels(isCreator: Boolean): Flow<List<NovelMeta>> = flow {
        val uid = requireUserId()
        val path = "novels"
        val query: Query = if (isCreator) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("isPublished", true)
        }
        emitAll(
            query.snapshots()
                .map { it.toObjects(NovelMeta::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun syncNovelToCloud(
        novelId: String,
        title: String,
        authorName: String,
        genre: String,
        blurb: String,
        preface: String,
        totalChapters: Int,
        totalArcs: Int,
        wordsPerChapter: Int,
        coverTag: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("novels").document(novelId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to title.take(155),
                        "authorName" to authorName.take(95),
                        "genre" to genre.take(55),
                        "blurb" to blurb.take(3900),
                        "preface" to preface.take(5900),
                        "totalChapters" to totalChapters,
                        "totalArcs" to totalArcs,
                        "wordsPerChapter" to wordsPerChapter,
                        "isPublished" to true,
                        "coverTag" to coverTag.take(55),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "authorUid" to uid,
                        "title" to title.take(155),
                        "authorName" to authorName.take(95),
                        "genre" to genre.take(55),
                        "blurb" to blurb.take(3900),
                        "preface" to preface.take(5900),
                        "totalChapters" to totalChapters,
                        "totalArcs" to totalArcs,
                        "wordsPerChapter" to wordsPerChapter,
                        "isPublished" to true,
                        "coverTag" to coverTag.take(55),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun syncChapterEditToCloud(
        novelId: String,
        chapterNumber: Int,
        arcNumber: Int,
        title: String,
        summary: String,
        wordCount: Int,
        contentPreview: String
    ): Result<Unit> {
        val uid = requireUserId()
        val chapterId = "ch_${chapterNumber.toString().padStart(4, '0')}"
        val docRef = db.collection("novels").document(novelId).collection("chapters").document(chapterId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "chapterNumber" to chapterNumber,
                        "arcNumber" to arcNumber.coerceIn(1, 3),
                        "title" to title.take(195),
                        "summary" to summary.take(950),
                        "wordCount" to wordCount,
                        "contentPreview" to contentPreview.take(24000),
                        "editedByUid" to uid,
                        "isPublished" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "novelId" to novelId,
                        "chapterNumber" to chapterNumber,
                        "arcNumber" to arcNumber.coerceIn(1, 3),
                        "title" to title.take(195),
                        "summary" to summary.take(950),
                        "wordCount" to wordCount,
                        "contentPreview" to contentPreview.take(24000),
                        "editedByUid" to uid,
                        "isPublished" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Comments ---
    fun observeComments(novelId: String): Flow<List<CommentItem>> = flow {
        val uid = requireUserId()
        val path = "comments"
        emitAll(
            db.collection(path)
                .whereEqualTo("novelId", novelId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CommentItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun postComment(
        authorName: String,
        novelId: String,
        chapterNumber: Int,
        body: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("comments").document("cmt_" + UUID.randomUUID().toString().replace("-", "").take(12))
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "authorName" to authorName.take(75),
                    "novelId" to novelId,
                    "chapterNumber" to chapterNumber.coerceAtLeast(0),
                    "body" to body.trim().take(950),
                    "likesCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            emitActivityNotification(
                actorName = authorName,
                title = "Komentar Baru Bab $chapterNumber",
                message = "$authorName mengomentari Bab $chapterNumber: \"${body.trim().take(120)}\"",
                category = "community",
                targetScope = "creator"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun likeComment(comment: CommentItem): Result<Unit> {
        requireUserId()
        if (comment.id.isBlank()) return Result.failure(IllegalArgumentException("Blank ID"))
        val docRef = db.collection("comments").document(comment.id)
        return try {
            docRef.update(
                mapOf(
                    "likesCount" to (comment.likesCount + 1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Groups ---
    fun observeGroups(): Flow<List<CommunityGroup>> = flow {
        val uid = requireUserId()
        val path = "groups"
        emitAll(
            db.collection(path)
                .whereEqualTo("isActive", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CommunityGroup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.memberCount }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun createGroup(name: String, description: String, genreTag: String): Result<String> {
        val uid = requireUserId()
        val id = "grp_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val docRef = db.collection("groups").document(id)
        return try {
            docRef.set(
                mapOf(
                    "creatorUid" to uid,
                    "name" to name.trim().take(75),
                    "description" to description.trim().take(280),
                    "genreTag" to genreTag.trim().ifEmpty { "Ryujin Lore" }.take(38),
                    "memberCount" to 1,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinGroup(group: CommunityGroup): Result<Unit> {
        requireUserId()
        val docRef = db.collection("groups").document(group.id)
        return try {
            docRef.update(
                mapOf(
                    "memberCount" to (group.memberCount + 1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Chat Messages ---
    fun observeChatMessages(groupId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        val path = "chat_messages"
        emitAll(
            db.collection(path)
                .whereEqualTo("groupId", groupId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun sendChatMessage(
        authorName: String,
        isCreator: Boolean,
        groupId: String,
        text: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("chat_messages").document("msg_" + UUID.randomUUID().toString().replace("-", "").take(12))
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "authorName" to authorName.take(75),
                    "authorRole" to if (isCreator) "creator" else "reader",
                    "groupId" to groupId.take(60),
                    "text" to text.trim().take(950),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Status Posts ---
    fun observeStatusPosts(): Flow<List<StatusPost>> = flow {
        val uid = requireUserId()
        val path = "status_posts"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(StatusPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun postStatus(
        authorName: String,
        isCreator: Boolean,
        mood: String,
        content: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("status_posts").document("st_" + UUID.randomUUID().toString().replace("-", "").take(12))
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "authorName" to authorName.take(75),
                    "authorRole" to if (isCreator) "creator" else "reader",
                    "mood" to mood.trim().ifEmpty { "Terpukau" }.take(38),
                    "content" to content.trim().take(480),
                    "likesCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun likeStatus(post: StatusPost): Result<Unit> {
        requireUserId()
        val docRef = db.collection("status_posts").document(post.id)
        return try {
            docRef.update(
                mapOf(
                    "likesCount" to (post.likesCount + 1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Real-time Notifications ---
    fun observeNotifications(uid: String, isCreator: Boolean): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        val query: Query = if (isCreator) {
            db.collection(path).orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
        } else {
            db.collection(path).whereEqualTo("targetScope", "all")
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun publishChapterNotification(
        actorName: String,
        title: String,
        message: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("notifications").document("notif_" + UUID.randomUUID().toString().replace("-", "").take(12))
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "actorName" to actorName.take(75),
                    "title" to title.take(115),
                    "message" to message.take(390),
                    "category" to "chapter_update",
                    "targetScope" to "all",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun emitActivityNotification(
        actorName: String,
        title: String,
        message: String,
        category: String = "activity",
        targetScope: String = "creator"
    ) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("notifications").document("act_" + UUID.randomUUID().toString().replace("-", "").take(12))
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "actorName" to actorName.take(75),
                    "title" to title.take(115),
                    "message" to message.take(390),
                    "category" to category,
                    "targetScope" to targetScope,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Activity notification skipped: ${e.message}")
        }
    }

    // --- Creator Drive & Auto-Sync Logs ---
    fun observeDriveSyncLogs(): Flow<List<DriveSyncLog>> = flow {
        val uid = requireUserId()
        val path = "drive_sync_logs"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(40)
                .snapshots()
                .map { it.toObjects(DriveSyncLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun logDriveSync(
        fileName: String,
        syncType: String,
        destination: String,
        status: String,
        wordCount: Long
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("drive_sync_logs").document("drv_" + UUID.randomUUID().toString().replace("-", "").take(12))
        return try {
            docRef.set(
                mapOf(
                    "creatorUid" to uid,
                    "fileName" to fileName.take(155),
                    "syncType" to syncType,
                    "destination" to destination.take(195),
                    "status" to status,
                    "wordCount" to wordCount,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- App Distribution & Download Links ---
    fun observeAppDistribution(): Flow<AppDistributionConfig> = flow {
        val uid = requireUserId()
        val path = "app_distribution/release"
        emitAll(
            db.collection("app_distribution").document("release")
                .snapshots()
                .map { snap ->
                    snap.toObject(AppDistributionConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppDistributionConfig()
                }
                .handleSnapshotErrors(OperationType.GET, path, uid)
        )
    }

    suspend fun saveAppDistribution(
        sharedPreviewUrl: String,
        apkDownloadUrl: String,
        versionLabel: String,
        releaseNotes: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("app_distribution").document("release")
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "updatedByUid" to uid,
                        "sharedPreviewUrl" to sharedPreviewUrl.trim().take(490),
                        "apkDownloadUrl" to apkDownloadUrl.trim().take(490),
                        "versionLabel" to versionLabel.trim().ifEmpty { "v1.0.0" }.take(55),
                        "releaseNotes" to releaseNotes.trim().take(950),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "updatedByUid" to uid,
                        "sharedPreviewUrl" to sharedPreviewUrl.trim().take(490),
                        "apkDownloadUrl" to apkDownloadUrl.trim().take(490),
                        "versionLabel" to versionLabel.trim().ifEmpty { "v1.0.0" }.take(55),
                        "releaseNotes" to releaseNotes.trim().take(950),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
