package com.example

import com.example.data.seed.RyujinCharacterSeeder
import com.example.data.seed.RyujinScriptEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RyujinContentTest {

    @Test
    fun ryujinChapters_has900ChaptersAcross3Arcs() {
        val chapters = RyujinScriptEngine.buildAll900Chapters("ryujin")
        assertEquals(900, chapters.size)
        assertEquals(300, chapters.count { it.arcNumber == 1 })
        assertEquals(300, chapters.count { it.arcNumber == 2 })
        assertEquals(300, chapters.count { it.arcNumber == 3 })
    }

    @Test
    fun ryujinCharacters_has400DetailedProfiles() {
        val characters = RyujinCharacterSeeder.buildAll400Characters("ryujin")
        assertEquals(400, characters.size)
        assertTrue(characters.all {
            it.name.isNotBlank() &&
                it.traits.isNotBlank() &&
                it.country.isNotBlank() &&
                it.storyline.isNotBlank() &&
                it.deepProfile.isNotBlank()
        })
    }

    @Test
    fun ryujinChapterScript_exceeds50000WordsFullDialogue() {
        val ch1 = RyujinScriptEngine.buildAll900Chapters("ryujin").first()
        val segments = RyujinScriptEngine.generateFullDialogueSegments(ch1)
        assertEquals(25, segments.size)
        val totalWords = segments.sumOf { it.wordCount }
        assertTrue("Expected >= 50,000 words per chapter, got $totalWords", totalWords >= 50_000)
    }
}
