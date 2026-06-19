package com.example.data.repository

import com.example.data.db.VideoDao
import com.example.data.model.VideoItem
import com.example.data.model.Comment
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class KohistaniTvRepository(private val videoDao: VideoDao) {

    val allVideos: Flow<List<VideoItem>> = videoDao.getAllVideos()
    val liveVideos: Flow<List<VideoItem>> = videoDao.getLiveVideos()

    fun getVideosByCategory(category: String): Flow<List<VideoItem>> {
        return if (category == "سب" || category == "All") {
            videoDao.getAllVideos()
        } else {
            videoDao.getVideosByCategory(category)
        }
    }

    fun getComments(videoId: Int): Flow<List<Comment>> = videoDao.getCommentsForVideo(videoId)

    fun getProfile(username: String): Flow<UserProfile?> = videoDao.getProfile(username)

    suspend fun insertComment(comment: Comment) {
        videoDao.insertComment(comment)
        // Refresh comment count in video entity
        val count = videoDao.getCommentsCount(comment.videoId)
        videoDao.getAllVideos().first().find { it.id == comment.videoId }?.let { video ->
            video.commentsCount = count
            videoDao.updateVideo(video)
        }
    }

    suspend fun likeVideo(video: VideoItem) {
        val updated = video.copy(
            isLiked = !video.isLiked,
            likesCount = if (video.isLiked) video.likesCount - 1 else video.likesCount + 1
        )
        videoDao.updateVideo(updated)
    }

    suspend fun followCreator(video: VideoItem) {
        val updated = video.copy(isFollowed = !video.isFollowed)
        videoDao.updateVideo(updated)

        // Also update creator profile likes/followers if they exist
        val creatorName = video.username
        videoDao.getProfile(creatorName).firstOrNull()?.let { profile ->
            val updatedProfile = profile.copy(
                followersCount = if (updated.isFollowed) profile.followersCount + 1 else profile.followersCount - 1
            )
            videoDao.insertProfile(updatedProfile)
        }
    }

    suspend fun shareVideo(video: VideoItem) {
        val updated = video.copy(sharesCount = video.sharesCount + 1)
        videoDao.updateVideo(updated)
    }

    suspend fun saveProfile(profile: UserProfile) {
        videoDao.insertProfile(profile)
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val currentVideos = videoDao.getAllVideos().first()
        if (currentVideos.isEmpty()) {
            val initialVideos = listOf(
                // Live streams representation
                VideoItem(
                    username = "ranial_studio",
                    authorName = "رانیال اسٹوڈیو",
                    videoTitle = "کوہستانی روایتی رباب محفل",
                    description = "کوہستان داس کی خوبصورت وادی سے براہِ راست روایتی رباب اور الغوزہ کی لائیو پرفارمنس دیکھیں۔ کمنٹ کریں اور اپنی فرمائش بتائیں۔",
                    category = "لوک موسیقی",
                    soundName = "براہِ راست - رانیال اسٹوڈیو رباب دھن",
                    likesCount = 890,
                    commentsCount = 3,
                    sharesCount = 124,
                    isLiked = false,
                    isFollowed = false,
                    isLive = true,
                    viewerCount = 1420,
                    videoColorHex = "#FF0D1B2A"
                ),
                VideoItem(
                    username = "kandia_news",
                    authorName = "کندھیا لائیو ٹی وی",
                    videoTitle = "سوات کوہستان کلچر شو براہ راست",
                    description = "سوات کوہستان کے سالانہ ثقافتی میلے سے براہِ راست احوال دیکھیں۔ پورے پاکستان سے آئے مہمانوں کا شاندار استقبال۔",
                    category = "ثقافت",
                    soundName = "براہِ راست نشریات - کوہستانی ٹی وی",
                    likesCount = 1245,
                    commentsCount = 2,
                    sharesCount = 422,
                    isLiked = false,
                    isFollowed = false,
                    isLive = true,
                    viewerCount = 3450,
                    videoColorHex = "#FF1F1A24"
                ),
                // Videos
                VideoItem(
                    username = "sher_ali_official",
                    authorName = "شیر علی کوہستانی",
                    videoTitle = "خوبصورت کوہستانی غزل اور جھومر رقص",
                    description = "رانیال کلوز پشاور اسٹوڈیو میں تیار کردہ خوبصورت لوک گیت۔ تمام کوہستانی بھائیوں کے لیے زبردست تحفہ۔ لائک اور شئیر لازمی کریں!",
                    category = "لوک موسیقی",
                    soundName = "شیر علی کوہستانی - سوزِ دل",
                    likesCount = 3400,
                    commentsCount = 2,
                    sharesCount = 680,
                    isLiked = false,
                    isFollowed = false,
                    isLive = false,
                    videoColorHex = "#FF3A0CA3"
                ),
                VideoItem(
                    username = "kohistani_vlog",
                    authorName = "زاہد خان کوہستانی",
                    videoTitle = "کندھیا کے دلفریب باغات اور چشمے",
                    description = "آج کے ولاگ میں ہم آپ کو دکھائیں گے وادی کندھیا کے برف پوش پہاڑ اور نیلگوں پانی کے چشمے۔ جنت نظیر نظارے۔",
                    category = "منظر خوبصورت",
                    soundName = "صداۓ آبشار - اوریجنل آڈیو کوہستان",
                    likesCount = 9820,
                    commentsCount = 4,
                    sharesCount = 1290,
                    isLiked = false,
                    isFollowed = false,
                    isLive = false,
                    videoColorHex = "#FF1A5F7A"
                ),
                VideoItem(
                    username = "kohistan_comedy",
                    authorName = "کوہستانی ڈرامہ وائنز",
                    videoTitle = "جب مہمان بن بلائے آ جاۓ - کامیڈی ویڈیو",
                    description = "انتہائی مزاحیہ کوہستانی ڈرامہ۔ اپنے چہرے پر مسکراہٹ لائیں اور دوستوں کے ساتھ واٹس ایپ اور ٹاک ٹاک پر شیئر کریں۔",
                    category = "ڈرامہ و مزاح",
                    soundName = "ہنسی مذاق - اوریجنل ہنسی آڈیو",
                    likesCount = 5610,
                    commentsCount = 3,
                    sharesCount = 2300,
                    isLiked = false,
                    isFollowed = false,
                    isLive = false,
                    videoColorHex = "#FF5F0F40"
                ),
                VideoItem(
                    username = "abasin_sports_club",
                    authorName = "اباسین اسپورٹس",
                    videoTitle = "سنگِ ماربل اٹھانے کا شاندار روایتی مقابلہ",
                    description = "کوہستان میں کھیلا جانے والا روایتی بڑا پتھر اٹھانے کا دنگل۔ نوجوانوں نے طاقت کا شاندار مظاہرہ پیش کیا۔ شائقین کی خوب حوصلہ افزائی۔",
                    category = "کھیل و روایات",
                    soundName = "میلے کی ڈھول تھاپ دھن",
                    likesCount = 2300,
                    commentsCount = 2,
                    sharesCount = 540,
                    isLiked = false,
                    isFollowed = false,
                    isLive = false,
                    videoColorHex = "#FF0F4C5C"
                ),
                VideoItem(
                    username = "qari_zahir",
                    authorName = "قاری ظاہر کوہستانی",
                    videoTitle = "کوہستان کی مسحور کن نعت شریف",
                    description = "محفلِ نعت داس۔ خوبصورت آواز اور بلند عقیدت۔ دل کو گرما دینے والا لحن۔ کمنٹ میں ماشاءاللہ لکھیں۔",
                    category = "ثقافت",
                    soundName = "قاری ظاہر کوہستانی - نعت پاک",
                    likesCount = 15300,
                    commentsCount = 5,
                    sharesCount = 7800,
                    isLiked = false,
                    isFollowed = false,
                    isLive = false,
                    videoColorHex = "#FF31572C"
                )
            )
            videoDao.insertVideos(initialVideos)

            // Seed initial comments
            val sampleComments = listOf(
                Comment(videoId = 1, username = "shahid_kohistani", commentText = "ماشاءاللہ بہت ہی زبردست آواز بھائی۔"),
                Comment(videoId = 1, username = "valley_explorer", commentText = "کتنی خوبصورت دھن ہے، روح تروتازہ ہو گئی۔"),
                Comment(videoId = 2, username = "khalid_swati", commentText = "سوات کوہستان زندہ باد۔ شاندار کلچر ہے ہمارا۔"),
                Comment(videoId = 3, username = "pukhtoon_traveler", commentText = "شیر علی بھائی آپ تو کمال کرتے ہو ہر بار!"),
                Comment(videoId = 4, username = "gul_khan", commentText = "کندھیا دنیا کا سب سے پیارا علاقہ ہے۔ میرا آبائی وطن۔"),
                Comment(videoId = 5, username = "dilbar_kohistani", commentText = "ہاہاہا بہت ہنسی آئی میاں صاحب کی اداکاری دیکھ کر۔")
            )
            for (comment in sampleComments) {
                videoDao.insertComment(comment)
            }

            // Seed profiles for creators
            val sampleProfiles = listOf(
                UserProfile(username = "ranial_studio", displayName = "رانیال اسٹوڈیو", bio = "ثقافتی اور ہائی فائی کوہستانی محافل اور گیتوں کے آفیشل یوٹیوب اور کوہستانی ٹی وی پارٹنر۔", followersCount = 25000, followingCount = 14, likesCount = 14000),
                UserProfile(username = "sher_ali_official", displayName = "شیر علی کوہستانی", bio = "فنکارِ کوہستان۔ روایتی اور پاپ گیت۔ آپ سب کی محبت کا بے حد شکریہ۔", followersCount = 89000, followingCount = 45, likesCount = 76000),
                UserProfile(username = "kohistani_vlog", displayName = "زاہد خان کوہستانی", bio = "آئیں میرے ساتھ اباسین کوہستان کی خوبصورتی کو ایکسپلور کریں۔ روزانہ نیا ولاگ۔", followersCount = 12000, followingCount = 90, likesCount = 33000),
                UserProfile(username = "kohistan_comedy", displayName = "کوہستانی ڈرامہ وائنز", bio = "پورے گاؤں کا ہنس مکھ گروپ۔ ہماری ویڈیوز کا مقصد صرف تفریح پھیلانا ہے۔", followersCount = 54000, followingCount = 8, likesCount = 98000),
                UserProfile(username = "qari_zahir", displayName = "قاری ظاہر کوہستانی", bio = "کوہستانی مذہبی نعتوں اور خطبات کا آفیشل چینل۔ محبتِ رسول ہی سب کچھ ہے۔", followersCount = 150000, followingCount = 3, likesCount = 432000)
            )
            for (profile in sampleProfiles) {
                videoDao.insertProfile(profile)
            }
        }
    }
}
