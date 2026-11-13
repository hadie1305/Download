package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RyujinFirestoreRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_readerRole_succeedsAndReadsOwnProfile() = runBlocking {
        val uid = signInTestUser("reader_alice_${UUID.randomUUID()}@test.com")
        val repo = FirestoreSyncRepository(firestore)

        val result = withTimeout(5000L) {
            repo.ensureUserProfileExists(
                uid = uid,
                displayName = "Alice Reader",
                email = "alice@test.com",
                isCreator = false
            )
        }
        assertTrue(result.isSuccess)
        assertEquals("reader", result.getOrThrow().role)
    }

    @Test
    fun communityOperations_authenticatedReader_succeeds() = runBlocking {
        signInTestUser("reader_comm_${UUID.randomUUID()}@test.com")
        val repo = FirestoreSyncRepository(firestore)

        val commentRes = withTimeout(5000L) {
            repo.postComment(
                authorName = "Alice",
                novelId = "ryujin",
                chapterNumber = 1,
                body = "Bab 1 penuh dialog yang sangat mendalam!"
            )
        }
        assertTrue(commentRes.isSuccess)

        val chatRes = withTimeout(5000L) {
            repo.sendChatMessage(
                authorName = "Alice",
                isCreator = false,
                groupId = "global_lounge",
                text = "Halo semua penggemar Ryujin!"
            )
        }
        assertTrue(chatRes.isSuccess)

        val statusRes = withTimeout(5000L) {
            repo.postStatus(
                authorName = "Alice",
                isCreator = false,
                mood = "Terpukau",
                content = "Sedang membaca Arc 1 Bab 30!"
            )
        }
        assertTrue(statusRes.isSuccess)

        val comments = withTimeout(3000L) {
            repo.observeComments("ryujin").first { it.isNotEmpty() }
        }
        assertTrue(comments.isNotEmpty())
    }

    @Test
    fun exclusiveCreatorCollections_nonCreatorReader_denied() = runBlocking {
        signInTestUser("reader_bob_${UUID.randomUUID()}@test.com")
        val repo = FirestoreSyncRepository(firestore)

        var listUsersException: Exception? = null
        try {
            withTimeout(3000L) {
                repo.observeAllUsersForCreator().first()
            }
        } catch (e: Exception) {
            listUsersException = e
        }
        val firestoreErr1 = listUsersException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreErr1?.code)

        val novelResult = withTimeout(5000L) {
            repo.syncNovelToCloud(
                novelId = "unauthorized_${UUID.randomUUID().toString().take(6)}",
                title = "Unauthorized Novel",
                authorName = "Bob",
                genre = "Action",
                blurb = "Test",
                preface = "Test",
                totalChapters = 1,
                totalArcs = 1,
                wordsPerChapter = 500,
                coverTag = "TEST"
            )
        }
        assertTrue(novelResult.isFailure)
        val firestoreErr2 = novelResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreErr2?.code)
    }

    @Test
    fun observeComments_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = FirestoreSyncRepository(firestore)

        var repoException: Exception? = null
        try {
            withTimeout(3000L) {
                repo.observeComments("ryujin").first()
            }
        } catch (e: Exception) {
            repoException = e
        }
        assertTrue(repoException is IllegalStateException)

        var directFirestoreException: Exception? = null
        try {
            withTimeout(3000L) {
                firestore.collection("comments").get().await()
            }
        } catch (e: Exception) {
            directFirestoreException = e
        }
        val fsErr = directFirestoreException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }
}
