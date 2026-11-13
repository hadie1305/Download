package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalNovelEntity::class,
        LocalChapterEntity::class,
        LocalChapterFtsEntity::class,
        LocalChapterSegmentEntity::class,
        LocalSegmentFtsEntity::class,
        LocalCharacterEntity::class,
        LocalCharacterFtsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RyujinDatabase : RoomDatabase() {
    abstract fun ryujinDao(): RyujinDao

    companion object {
        @Volatile
        private var INSTANCE: RyujinDatabase? = null

        fun getInstance(context: Context): RyujinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RyujinDatabase::class.java,
                    "ryujin_studio_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
