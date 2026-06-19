package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val authorName: String,
    val videoTitle: String,
    val description: String,
    val category: String,
    val soundName: String,
    var likesCount: Int,
    var commentsCount: Int,
    var sharesCount: Int,
    var isLiked: Boolean = false,
    var isFollowed: Boolean = false,
    val isLive: Boolean = false,
    val viewerCount: Int = 0,
    val videoColorHex: String = "#FF1E1E24"
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: Int,
    val username: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val username: String,
    val displayName: String,
    val bio: String,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    var likesCount: Int = 0,
    val isLoggedIn: Boolean = false,
    val loginType: String = "" // "Google", "Phone", ""
)
