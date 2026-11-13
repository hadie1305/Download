package com.example.data.local

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "novels")
data class LocalNovelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authorName: String,
    val genre: String,
    val blurb: String,
    val preface: String,
    val totalChapters: Int,
    val totalArcs: Int,
    val wordsPerChapter: Int,
    val isRyujinDefault: Boolean,
    val isPublished: Boolean,
    val coverTag: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["novelId", "chapterNumber"]),
        Index(value = ["novelId", "arcNumber", "arcChapterNumber"])
    ]
)
data class LocalChapterEntity(
    @PrimaryKey val id: String,
    val novelId: String,
    val chapterNumber: Int,
    val arcNumber: Int,
    val arcChapterNumber: Int,
    val arcTitle: String,
    val title: String,
    val summary: String,
    val wordCount: Int,
    val dialogueLineCount: Int,
    val featuredCharacters: String,
    val isEdited: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Fts4(contentEntity = LocalChapterEntity::class)
@Entity(tableName = "chapters_fts")
data class LocalChapterFtsEntity(
    val title: String,
    val summary: String,
    val arcTitle: String,
    val featuredCharacters: String
)

@Entity(
    tableName = "chapter_segments",
    indices = [Index(value = ["chapterId", "segmentIndex"])]
)
data class LocalChapterSegmentEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val novelId: String,
    val chapterNumber: Int,
    val segmentIndex: Int,
    val sceneTitle: String,
    val content: String,
    val wordCount: Int
)

@Fts4(contentEntity = LocalChapterSegmentEntity::class)
@Entity(tableName = "chapter_segments_fts")
data class LocalSegmentFtsEntity(
    val sceneTitle: String,
    val content: String
)

@Entity(
    tableName = "characters",
    indices = [Index(value = ["novelId", "characterNumber"])]
)
data class LocalCharacterEntity(
    @PrimaryKey val id: String,
    val novelId: String,
    val characterNumber: Int,
    val name: String,
    val alias: String,
    val country: String,
    val faction: String,
    val role: String,
    val traits: String,
    val arcPresence: String,
    val storyline: String,
    val deepProfile: String,
    val portraitKey: String
)

@Fts4(contentEntity = LocalCharacterEntity::class)
@Entity(tableName = "characters_fts")
data class LocalCharacterFtsEntity(
    val name: String,
    val alias: String,
    val country: String,
    val faction: String,
    val traits: String,
    val storyline: String,
    val deepProfile: String
)
