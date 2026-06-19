package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.VideoItem
import com.example.data.model.Comment
import com.example.data.model.UserProfile

@Database(entities = [VideoItem::class, Comment::class, UserProfile::class], version = 1, exportSchema = false)
abstract class KohistaniTvDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: KohistaniTvDatabase? = null

        fun getDatabase(context: Context): KohistaniTvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KohistaniTvDatabase::class.java,
                    "kohistani_tv_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
