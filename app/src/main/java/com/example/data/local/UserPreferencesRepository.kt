package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ryujin_preferences")

data class ReaderSettings(
    val isDarkMode: Boolean = true,
    val readerFontSizeSp: Int = 16,
    val lastNovelId: String = "ryujin",
    val lastChapterNumber: Int = 1,
    val autoSyncEnabled: Boolean = true,
    val connectedDriveUri: String = "",
    val connectedDriveName: String = "Belum terhubung",
    val lastSyncTimestamp: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val READER_FONT_SIZE = intPreferencesKey("reader_font_size")
        val LAST_NOVEL_ID = stringPreferencesKey("last_novel_id")
        val LAST_CHAPTER_NUMBER = intPreferencesKey("last_chapter_number")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val CONNECTED_DRIVE_URI = stringPreferencesKey("connected_drive_uri")
        val CONNECTED_DRIVE_NAME = stringPreferencesKey("connected_drive_name")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    val settingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings(
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            readerFontSizeSp = prefs[Keys.READER_FONT_SIZE] ?: 16,
            lastNovelId = prefs[Keys.LAST_NOVEL_ID] ?: "ryujin",
            lastChapterNumber = prefs[Keys.LAST_CHAPTER_NUMBER] ?: 1,
            autoSyncEnabled = prefs[Keys.AUTO_SYNC_ENABLED] ?: true,
            connectedDriveUri = prefs[Keys.CONNECTED_DRIVE_URI] ?: "",
            connectedDriveName = prefs[Keys.CONNECTED_DRIVE_NAME] ?: "Belum terhubung",
            lastSyncTimestamp = prefs[Keys.LAST_SYNC_TIMESTAMP] ?: 0L
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setReaderFontSize(sp: Int) {
        context.dataStore.edit { it[Keys.READER_FONT_SIZE] = sp.coerceIn(13, 24) }
    }

    suspend fun saveReadingProgress(novelId: String, chapterNumber: Int) {
        context.dataStore.edit {
            it[Keys.LAST_NOVEL_ID] = novelId
            it[Keys.LAST_CHAPTER_NUMBER] = chapterNumber
        }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SYNC_ENABLED] = enabled }
    }

    suspend fun saveConnectedDrive(uri: String, folderName: String) {
        context.dataStore.edit {
            it[Keys.CONNECTED_DRIVE_URI] = uri
            it[Keys.CONNECTED_DRIVE_NAME] = folderName
            it[Keys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun markSyncedNow() {
        context.dataStore.edit { it[Keys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis() }
    }
}
