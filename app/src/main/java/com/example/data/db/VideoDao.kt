package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoItem
import com.example.data.model.Comment
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    // Videos Queries
    @Query("SELECT * FROM videos ORDER BY id ASC")
    fun getAllVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY id ASC")
    fun getVideosByCategory(category: String): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE isLive = 1 ORDER BY id DESC")
    fun getLiveVideos(): Flow<List<VideoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoItem>)

    @Update
    suspend fun updateVideo(video: VideoItem)

    // Comments Queries
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: Int): Flow<List<Comment>>

    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId")
    suspend fun getCommentsCount(videoId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    // Profiles Queries
    @Query("SELECT * FROM user_profiles WHERE username = :username LIMIT 1")
    fun getProfile(username: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}
