package com.example.data.repository

import com.example.data.local.LocalChapterEntity
import com.example.data.local.LocalChapterSegmentEntity
import com.example.data.local.LocalCharacterEntity
import com.example.data.local.LocalNovelEntity
import com.example.data.local.RyujinDao
import com.example.data.seed.RyujinCharacterSeeder
import com.example.data.seed.RyujinNovelMeta
import com.example.data.seed.RyujinScriptEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

data class AdvancedSearchResults(
    val chapters: List<LocalChapterEntity> = emptyList(),
    val characters: List<LocalCharacterEntity> = emptyList(),
    val segments: List<LocalChapterSegmentEntity> = emptyList()
)

class NovelRepository(private val dao: RyujinDao) {

    private val seedMutex = Mutex()
    private val segmentMutex = Mutex()

    fun observeAllNovels(): Flow<List<LocalNovelEntity>> = dao.observeAllNovels()

    fun observeNovelById(novelId: String): Flow<LocalNovelEntity?> = dao.observeNovelById(novelId)

    fun observeChaptersByArc(novelId: String, arcNumber: Int): Flow<List<LocalChapterEntity>> =
        dao.observeChaptersByArc(novelId, arcNumber)

    fun observeAllChapters(novelId: String): Flow<List<LocalChapterEntity>> =
        dao.observeAllChapters(novelId)

    fun observeChapterById(chapterId: String): Flow<LocalChapterEntity?> =
        dao.observeChapterById(chapterId)

    fun observeTotalWordCount(novelId: String): Flow<Long> =
        dao.observeTotalWordCount(novelId)

    fun observeSegmentsForChapter(chapterId: String): Flow<List<LocalChapterSegmentEntity>> =
        dao.observeSegmentsForChapter(chapterId)

    fun observeCharacters(novelId: String): Flow<List<LocalCharacterEntity>> =
        dao.observeCharacters(novelId)

    fun observeCharacterCountries(novelId: String): Flow<List<String>> =
        dao.observeCharacterCountries(novelId)

    suspend fun ensureRyujinSeeded() = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            if (dao.getNovelById(RyujinNovelMeta.RYUJIN_NOVEL_ID) == null) {
                dao.insertNovel(RyujinNovelMeta.createRyujinNovelEntity())
            }
            if (dao.getChapterCount(RyujinNovelMeta.RYUJIN_NOVEL_ID) < 900) {
                val chapters = RyujinScriptEngine.buildAll900Chapters(RyujinNovelMeta.RYUJIN_NOVEL_ID)
                chapters.chunked(150).forEach { batch ->
                    dao.insertChapters(batch)
                }
            }
            if (dao.getCharacterCount(RyujinNovelMeta.RYUJIN_NOVEL_ID) < 400) {
                val characters = RyujinCharacterSeeder.buildAll400Characters(RyujinNovelMeta.RYUJIN_NOVEL_ID)
                characters.chunked(100).forEach { batch ->
                    dao.insertCharacters(batch)
                }
            }
        }
    }

    suspend fun ensureChapterSegmentsMaterialized(
        novelId: String,
        chapterNumber: Int
    ): LocalChapterEntity? = withContext(Dispatchers.IO) {
        val chapter = dao.getChapterByNumber(novelId, chapterNumber) ?: return@withContext null
        segmentMutex.withLock {
            if (dao.getSegmentCountForChapter(chapter.id) == 0) {
                val generated = RyujinScriptEngine.generateFullDialogueSegments(chapter)
                dao.insertSegments(generated)
                val totalWords = generated.sumOf { it.wordCount }
                if (totalWords != chapter.wordCount) {
                    dao.updateChapter(
                        chapter.copy(
                            wordCount = totalWords,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
        dao.getChapterByNumber(novelId, chapterNumber)
    }

    suspend fun getChapterSegmentsBlocking(novelId: String, chapterNumber: Int): List<LocalChapterSegmentEntity> {
        val chapter = ensureChapterSegmentsMaterialized(novelId, chapterNumber) ?: return emptyList()
        return withContext(Dispatchers.IO) {
            dao.getSegmentsForChapter(chapter.id)
        }
    }

    suspend fun getPreviewExcerpt(novelId: String, chapterNumber: Int): String = withContext(Dispatchers.IO) {
        val chapter = dao.getChapterByNumber(novelId, chapterNumber) ?: return@withContext ""
        val existingSegments = dao.getSegmentsForChapter(chapter.id)
        if (existingSegments.isNotEmpty()) {
            existingSegments.first().content.lineSequence().take(26).joinToString("\n")
        } else {
            RyujinScriptEngine.generatePreviewExcerpt(chapter)
        }
    }

    suspend fun searchAdvanced(
        novelId: String,
        rawQuery: String,
        arcFilter: Int
    ): AdvancedSearchResults = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return@withContext AdvancedSearchResults()

        val ftsTokens = trimmed
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .joinToString(" ") { "$it*" }

        val chapters = try {
            if (ftsTokens.isNotEmpty()) {
                dao.searchChaptersFts(ftsTokens, arcFilter).filter { it.novelId == novelId }
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }.ifEmpty {
            dao.searchChaptersLike(novelId, trimmed, arcFilter)
        }

        val characters = try {
            if (ftsTokens.isNotEmpty()) {
                dao.searchCharactersFts(ftsTokens).filter { it.novelId == novelId }
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val segments = try {
            if (ftsTokens.isNotEmpty()) {
                dao.searchSegmentsFts(ftsTokens).filter { it.novelId == novelId }
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        AdvancedSearchResults(
            chapters = chapters,
            characters = characters,
            segments = segments
        )
    }

    suspend fun updateSegmentContent(
        chapterId: String,
        segment: LocalChapterSegmentEntity,
        newTitle: String,
        newContent: String
    ): LocalChapterEntity? = withContext(Dispatchers.IO) {
        val wordCount = RyujinScriptEngine.countWords(newContent)
        dao.updateSegment(
            segment.copy(
                sceneTitle = newTitle.trim().ifEmpty { segment.sceneTitle },
                content = newContent,
                wordCount = wordCount
            )
        )
        val allSegs = dao.getSegmentsForChapter(chapterId)
        val totalChapterWords = allSegs.sumOf { it.wordCount }
        val chapter = dao.getChapterByNumber(segment.novelId, segment.chapterNumber)
        if (chapter != null) {
            val updated = chapter.copy(
                wordCount = totalChapterWords,
                isEdited = true,
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.updateChapter(updated)
            return@withContext updated
        }
        null
    }

    suspend fun createNovelWithOpeningChapters(
        title: String,
        authorName: String,
        genre: String,
        blurb: String,
        preface: String,
        initialChaptersCount: Int
    ): LocalNovelEntity = withContext(Dispatchers.IO) {
        val safeCount = initialChaptersCount.coerceIn(1, 30)
        val novelId = "novel_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val now = System.currentTimeMillis()
        val novel = LocalNovelEntity(
            id = novelId,
            title = title.trim(),
            authorName = authorName.trim(),
            genre = genre.trim(),
            blurb = blurb.trim(),
            preface = preface.trim(),
            totalChapters = safeCount,
            totalArcs = 1,
            wordsPerChapter = 52500,
            isRyujinDefault = false,
            isPublished = true,
            coverTag = title.trim().take(12).uppercase(),
            createdAtMillis = now
        )
        dao.insertNovel(novel)

        val chapters = (1..safeCount).map { chNum ->
            LocalChapterEntity(
                id = "${novelId}_ch_${chNum.toString().padStart(4, '0')}",
                novelId = novelId,
                chapterNumber = chNum,
                arcNumber = 1,
                arcChapterNumber = chNum,
                arcTitle = "ARC 1: PEMBUKAAN KISAH",
                title = "Bab $chNum — Langkah Awal di $title",
                summary = "Bab pembuka penuh dialog untuk novel '$title' karya $authorName.",
                wordCount = 52500,
                dialogueLineCount = 1650,
                featuredCharacters = "$authorName (Narator Utama), Tokoh Sentral",
                updatedAtMillis = now
            )
        }
        dao.insertChapters(chapters)
        novel
    }

    suspend fun addChapterToNovel(
        novelId: String,
        arcNumber: Int,
        title: String,
        summary: String,
        customOpeningContent: String
    ): LocalChapterEntity? = withContext(Dispatchers.IO) {
        val novel = dao.getNovelById(novelId) ?: return@withContext null
        val nextNumber = (dao.getMaxChapterNumber(novelId) ?: 0) + 1
        val now = System.currentTimeMillis()
        val chapter = LocalChapterEntity(
            id = "${novelId}_ch_${nextNumber.toString().padStart(4, '0')}",
            novelId = novelId,
            chapterNumber = nextNumber,
            arcNumber = arcNumber.coerceIn(1, 3),
            arcChapterNumber = nextNumber,
            arcTitle = "ARC ${arcNumber.coerceIn(1, 3)}",
            title = title.trim(),
            summary = summary.trim(),
            wordCount = if (customOpeningContent.isNotBlank()) RyujinScriptEngine.countWords(customOpeningContent) else 52500,
            dialogueLineCount = 1650,
            featuredCharacters = "Kenjiro Tatsumoto (K), Sakura Miya",
            isEdited = true,
            updatedAtMillis = now
        )
        dao.insertChapter(chapter)
        dao.updateNovel(novel.copy(totalChapters = nextNumber))

        if (customOpeningContent.isNotBlank()) {
            val generated = RyujinScriptEngine.generateFullDialogueSegments(chapter).toMutableList()
            generated[0] = generated[0].copy(
                sceneTitle = "Adegan 1/25: Naskah Kreator",
                content = customOpeningContent,
                wordCount = RyujinScriptEngine.countWords(customOpeningContent)
            )
            dao.insertSegments(generated)
            val totalWords = generated.sumOf { it.wordCount }
            val updatedChapter = chapter.copy(wordCount = totalWords)
            dao.updateChapter(updatedChapter)
            return@withContext updatedChapter
        }
        chapter
    }
}
