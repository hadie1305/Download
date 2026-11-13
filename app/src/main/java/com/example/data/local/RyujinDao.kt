package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RyujinDao {
    // --- Novels ---
    @Query("SELECT * FROM novels ORDER BY isRyujinDefault DESC, createdAtMillis DESC")
    fun observeAllNovels(): Flow<List<LocalNovelEntity>>

    @Query("SELECT * FROM novels WHERE id = :novelId LIMIT 1")
    fun observeNovelById(novelId: String): Flow<LocalNovelEntity?>

    @Query("SELECT * FROM novels WHERE id = :novelId LIMIT 1")
    suspend fun getNovelById(novelId: String): LocalNovelEntity?

    @Query("SELECT COUNT(*) FROM novels")
    suspend fun getNovelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: LocalNovelEntity)

    @Update
    suspend fun updateNovel(novel: LocalNovelEntity)

    // --- Chapters ---
    @Query("SELECT * FROM chapters WHERE novelId = :novelId AND arcNumber = :arcNumber ORDER BY chapterNumber ASC")
    fun observeChaptersByArc(novelId: String, arcNumber: Int): Flow<List<LocalChapterEntity>>

    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY chapterNumber ASC")
    fun observeAllChapters(novelId: String): Flow<List<LocalChapterEntity>>

    @Query("SELECT * FROM chapters WHERE novelId = :novelId AND chapterNumber = :chapterNumber LIMIT 1")
    suspend fun getChapterByNumber(novelId: String, chapterNumber: Int): LocalChapterEntity?

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun observeChapterById(chapterId: String): Flow<LocalChapterEntity?>

    @Query("SELECT COUNT(*) FROM chapters WHERE novelId = :novelId")
    suspend fun getChapterCount(novelId: String): Int

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE novelId = :novelId")
    fun observeTotalWordCount(novelId: String): Flow<Long>

    @Query("SELECT MAX(chapterNumber) FROM chapters WHERE novelId = :novelId")
    suspend fun getMaxChapterNumber(novelId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<LocalChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: LocalChapterEntity)

    @Update
    suspend fun updateChapter(chapter: LocalChapterEntity)

    // --- Chapter Segments (25 scenes per chapter for lightweight 50,000+ word streaming) ---
    @Query("SELECT * FROM chapter_segments WHERE chapterId = :chapterId ORDER BY segmentIndex ASC")
    fun observeSegmentsForChapter(chapterId: String): Flow<List<LocalChapterSegmentEntity>>

    @Query("SELECT * FROM chapter_segments WHERE chapterId = :chapterId ORDER BY segmentIndex ASC")
    suspend fun getSegmentsForChapter(chapterId: String): List<LocalChapterSegmentEntity>

    @Query("SELECT COUNT(*) FROM chapter_segments WHERE chapterId = :chapterId")
    suspend fun getSegmentCountForChapter(chapterId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<LocalChapterSegmentEntity>)

    @Update
    suspend fun updateSegment(segment: LocalChapterSegmentEntity)

    // --- Advanced FTS4 + Filtered Search ---
    @Query(
        """
        SELECT chapters.* FROM chapters
        JOIN chapters_fts ON chapters.rowid = chapters_fts.rowid
        WHERE chapters_fts MATCH :ftsQuery
        AND (:arcFilter = 0 OR chapters.arcNumber = :arcFilter)
        ORDER BY chapters.chapterNumber ASC
        LIMIT 120
        """
    )
    suspend fun searchChaptersFts(ftsQuery: String, arcFilter: Int): List<LocalChapterEntity>

    @Query(
        """
        SELECT * FROM chapters
        WHERE novelId = :novelId
        AND (:arcFilter = 0 OR arcNumber = :arcFilter)
        AND (title LIKE '%' || :plainQuery || '%' OR summary LIKE '%' || :plainQuery || '%' OR featuredCharacters LIKE '%' || :plainQuery || '%')
        ORDER BY chapterNumber ASC
        LIMIT 120
        """
    )
    suspend fun searchChaptersLike(novelId: String, plainQuery: String, arcFilter: Int): List<LocalChapterEntity>

    @Query(
        """
        SELECT chapter_segments.* FROM chapter_segments
        JOIN chapter_segments_fts ON chapter_segments.rowid = chapter_segments_fts.rowid
        WHERE chapter_segments_fts MATCH :ftsQuery
        ORDER BY chapter_segments.chapterNumber ASC, chapter_segments.segmentIndex ASC
        LIMIT 80
        """
    )
    suspend fun searchSegmentsFts(ftsQuery: String): List<LocalChapterSegmentEntity>

    // --- 400 Characters ---
    @Query("SELECT * FROM characters WHERE novelId = :novelId ORDER BY characterNumber ASC")
    fun observeCharacters(novelId: String): Flow<List<LocalCharacterEntity>>

    @Query("SELECT COUNT(*) FROM characters WHERE novelId = :novelId")
    suspend fun getCharacterCount(novelId: String): Int

    @Query("SELECT DISTINCT country FROM characters WHERE novelId = :novelId ORDER BY country ASC")
    fun observeCharacterCountries(novelId: String): Flow<List<String>>

    @Query(
        """
        SELECT characters.* FROM characters
        JOIN characters_fts ON characters.rowid = characters_fts.rowid
        WHERE characters_fts MATCH :ftsQuery
        ORDER BY characters.characterNumber ASC
        LIMIT 150
        """
    )
    suspend fun searchCharactersFts(ftsQuery: String): List<LocalCharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<LocalCharacterEntity>)
}
