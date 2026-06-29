package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.KohistaniTvDatabase
import com.example.data.model.Comment
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import com.example.data.repository.KohistaniTvRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KohistaniTvViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KohistaniTvDatabase.getDatabase(application)
    private val repository = KohistaniTvRepository(db.videoDao())

    // UI state configurations
    private val _currentTab = MutableStateFlow("feed") // "feed", "dashboard", "live", "notifications", "profile"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("سب")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _activeVideoIndex = MutableStateFlow(0)
    val activeVideoIndex: StateFlow<Int> = _activeVideoIndex.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Defaults to Dark Mode per request!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentUsername = MutableStateFlow("shahid_tv")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    // Loaded videos flow based on selected category
    val videos: StateFlow<List<VideoItem>> = _selectedCategory
        .flatMapLatest { category ->
            repository.getVideosByCategory(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val liveVideos: StateFlow<List<VideoItem>> = repository.liveVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current logged-in user profile
    val userProfile: StateFlow<UserProfile?> = _currentUsername
        .flatMapLatest { username ->
            repository.getProfile(username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Current selected creator profile (for exploring creators on feed)
    private val _selectedCreatorUsername = MutableStateFlow<String?>(null)
    val selectedCreatorUsername: StateFlow<String?> = _selectedCreatorUsername.asStateFlow()

    val selectedCreatorProfile: StateFlow<UserProfile?> = _selectedCreatorUsername
        .flatMapLatest { username ->
            if (username == null) {
                MutableStateFlow<UserProfile?>(null)
            } else {
                repository.getProfile(username)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Comments for active video
    private val _activeVideoIdForComments = MutableStateFlow<Int?>(null)
    val activeVideoIdForComments: StateFlow<Int?> = _activeVideoIdForComments.asStateFlow()

    val activeComments: StateFlow<List<Comment>> = _activeVideoIdForComments
        .flatMapLatest { videoId ->
            if (videoId == null) {
                MutableStateFlow(emptyList())
            } else {
                repository.getComments(videoId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live Streaming Chat Rolling simulation
    private val _liveChatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val liveChatMessages: StateFlow<List<Pair<String, String>>> = _liveChatMessages.asStateFlow()

    // Hearts floating animation emitter for live streaming
    private val _floatingHeartsCount = MutableStateFlow(0)
    val floatingHeartsCount: StateFlow<Int> = _floatingHeartsCount.asStateFlow()

    // Notification states
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database
            repository.prepopulateDatabaseIfEmpty()

            // Set up initial profile for the host user if it doesn't exist
            val profile = UserProfile(
                username = "shahid_tv",
                displayName = "شاہد خان کوہستانی",
                bio = "کوہستانی ٹی وی کے آفیشل ٹرانسمیٹر۔ کوہستان کی خوبصورتی اور روایات کو دنیا بھر میں پھیلانا میرا مقصد ہے۔",
                followersCount = 1420,
                followingCount = 120,
                likesCount = 5400,
                isLoggedIn = true,
                loginType = "Google"
            )
            repository.saveProfile(profile)

            // Setup mock notifications
            _notifications.value = listOf(
                NotificationItem(1, "شیر علی کوہستانی نے آپ کو فالو کرنا شروع کیا۔", "فالوور", System.currentTimeMillis() - 1200000),
                NotificationItem(2, "رانیال اسٹوڈیو نے آپ کی ویڈیو 'خوبصورت وادی' شیئر کی۔", "شئیر", System.currentTimeMillis() - 3600000),
                NotificationItem(3, "زاہد خان نے کمنٹ کیا: 'ماشاءاللہ بہت پیارے مناظر ہیں۔'", "کمنٹ", System.currentTimeMillis() - 7200000),
                NotificationItem(4, "آپ کی لائیو سٹریم پر 100 سے زیادہ نئے ناظرین شامل ہو گئے۔", "لائیو", System.currentTimeMillis() - 86400000)
            )

            // Start rolling live chat simulator
            simulateLiveChat()
        }
    }

    private fun simulateLiveChat() {
        val chatUsernames = listOf(
            "سجاد کوہستانی", "کریم اللہ", "نواز علی", "حماد خان", "گل زمان", "عثمان داسی", "یاسر خان", "بشیر احمد"
        )
        val chatPhrases = listOf(
            "ماشاءاللہ بہت زبردست پرفارمنس! ❤️",
            "سوات کوہستان سے خوش آئند پیغام!",
            "زندہ باد بھائیو! شاندار کام کر رہے ہو۔ 👍",
            "کیا یہ لائیو نشریات روزانہ ہوتی ہیں؟",
            "کوہستانی لوگ بہت مہمان نواز ہیں 🇵🇰",
            "دل خوش ہو گیا رباب کی آواز سن کر!",
            "سلام پیش کرتا ہوں پوری ٹیم کو۔",
            "کندھیا وادی سے سلام!"
        )

        viewModelScope.launch {
            while (true) {
                delay(2500)
                if (_currentTab.value == "live") {
                    val user = chatUsernames.random()
                    val msg = chatPhrases.random()
                    val currentList = _liveChatMessages.value.takeLast(15).toMutableList()
                    currentList.add(Pair(user, msg))
                    _liveChatMessages.value = currentList
                }
            }
        }
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        _activeVideoIndex.value = 0 // Reset sliding video index
    }

    fun setActiveVideoIndex(index: Int) {
        _activeVideoIndex.value = index
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectCreator(username: String?) {
        _selectedCreatorUsername.value = username
    }

    fun openCommentsFor(videoId: Int) {
        _activeVideoIdForComments.value = videoId
    }

    fun closeComments() {
        _activeVideoIdForComments.value = null
    }

    // Interaction functions
    fun likeVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.likeVideo(video)
        }
    }

    fun followCreator(video: VideoItem) {
        viewModelScope.launch {
            repository.followCreator(video)
            // Push simulated notification
            if (!video.isFollowed) {
                addNotification("آپ نے ${video.authorName} کو فالو کر لیا۔", "فالوور")
            }
        }
    }

    fun shareVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.shareVideo(video)
        }
    }

    fun addComment(videoId: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = userProfile.value
            val authorName = user?.displayName ?: "مہمان صارف"
            val comment = Comment(
                videoId = videoId,
                username = authorName,
                commentText = text
            )
            repository.insertComment(comment)
        }
    }

    // User authentication simulation
    fun registerOrUpdateProfile(id: String, displayName: String, bio: String, loginType: String) {
        viewModelScope.launch {
            val customProfile = UserProfile(
                username = id,
                displayName = displayName,
                bio = bio,
                followersCount = 0,
                followingCount = 10,
                likesCount = 0,
                isLoggedIn = true,
                loginType = loginType
            )
            repository.saveProfile(customProfile)
            _currentUsername.value = id
            addNotification("آپ کا اکاؤنٹ بخوبی رجسٹرڈ ہو چکا ہے ($loginType)۔ کوہستانی ٹی وی میں خوش آمدید!", "سسٹم")
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentUser = userProfile.value ?: return@launch
            val loggedOutProfile = currentUser.copy(isLoggedIn = false, loginType = "")
            repository.saveProfile(loggedOutProfile)
            addNotification("آپ کامیابی کے ساتھ لاگ آؤٹ ہو چکے ہیں۔", "سسٹم")
        }
    }

    fun emitLiveHeart() {
        _floatingHeartsCount.value = _floatingHeartsCount.value + 1
    }

    private fun addNotification(message: String, type: String) {
        val current = _notifications.value.toMutableList()
        current.add(0, NotificationItem(
            id = (current.maxByOrNull { it.id }?.id ?: 0) + 1,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        ))
        _notifications.value = current
    }
}

data class NotificationItem(
    val id: Int,
    val message: String,
    val type: String, // "فالوور", "کمنٹ", "شئیر", "سسٹم", "لائیو"
    val timestamp: Long
)

class KohistaniTvViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KohistaniTvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KohistaniTvViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
