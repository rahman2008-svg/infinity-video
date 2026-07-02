package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MediaRepository
import com.example.data.PlaylistEntity
import com.example.data.VideoEntity
import com.example.data.VaultSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    Splash,
    Welcome,
    Permission,
    Home,
    VideoPlayer
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = MediaRepository(application, database.mediaDao())

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Active media state for the player
    private val _activeMedia = MutableStateFlow<VideoEntity?>(null)
    val activeMedia: StateFlow<VideoEntity?> = _activeMedia.asStateFlow()

    // Scanning state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Sort order
    private val _sortOrder = MutableStateFlow("date") // date, name, size
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    // Core list of videos from database
    val visibleVideos: StateFlow<List<VideoEntity>> = repository.visibleVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenVideos: StateFlow<List<VideoEntity>> = repository.hiddenVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected folder for navigation
    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    // Selected playlist for navigation
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    // Private Vault state
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultSetting = MutableStateFlow<VaultSetting?>(null)
    val vaultSetting: StateFlow<VaultSetting?> = _vaultSetting.asStateFlow()

    // App Preferences
    private val _selectedLanguage = MutableStateFlow("en") // en, bn, es, hi
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow("off") // off, en, bn, es
    val selectedSubtitle: StateFlow<String> = _selectedSubtitle.asStateFlow()

    private val _isAutoScanEnabled = MutableStateFlow(true)
    val isAutoScanEnabled: StateFlow<Boolean> = _isAutoScanEnabled.asStateFlow()

    init {
        // Load vault settings on init
        viewModelScope.launch {
            _vaultSetting.value = repository.getVaultSetting()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun playVideo(video: VideoEntity) {
        _activeMedia.value = video
        navigateTo(Screen.VideoPlayer)
    }

    fun selectFolder(folderName: String?) {
        _selectedFolder.value = folderName
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun startStorageScan() {
        viewModelScope.launch {
            _isScanning.value = true
            repository.scanStorage()
            _isScanning.value = false
        }
    }

    // Favorites
    fun toggleFavorite(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, !video.isFavorite)
        }
    }

    // Playlists
    fun createCustomPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, isSystem = false)
        }
    }

    fun deletePlaylist(id: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addVideoToPlaylist(playlistId: Int, videoId: Int) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, videoId)
        }
    }

    fun removeVideoFromPlaylist(playlistId: Int, videoId: Int) {
        viewModelScope.launch {
            repository.removeVideoFromPlaylist(playlistId, videoId)
        }
    }

    fun getVideosInPlaylist(playlistId: Int) = repository.getVideosInPlaylist(playlistId)

    // Private Vault
    fun setupVaultPIN(pin: String, question: String, answer: String) {
        viewModelScope.launch {
            repository.saveVaultSetting(pin, question, answer)
            _vaultSetting.value = repository.getVaultSetting()
            _isVaultUnlocked.value = true
        }
    }

    fun unlockVault(pin: String): Boolean {
        val currentSetting = _vaultSetting.value
        return if (currentSetting != null && currentSetting.pin == pin) {
            _isVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun resetVaultPINWithAnswer(answer: String, newPin: String): Boolean {
        val currentSetting = _vaultSetting.value
        return if (currentSetting != null && currentSetting.securityAnswer.equals(answer, ignoreCase = true)) {
            setupVaultPIN(newPin, currentSetting.securityQuestion, currentSetting.securityAnswer)
            true
        } else {
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun hideVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleHidden(video.id, isHidden = true)
        }
    }

    fun unhideVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleHidden(video.id, isHidden = false)
        }
    }

    // Video Operations
    fun renameVideo(video: VideoEntity, newTitle: String) {
        viewModelScope.launch {
            repository.renameVideo(video.id, newTitle)
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video.id)
        }
    }

    // App preferences actions
    fun setLanguage(langCode: String) {
        _selectedLanguage.value = langCode
    }

    fun setSubtitle(sub: String) {
        _selectedSubtitle.value = sub
    }

    fun setAutoScan(enabled: Boolean) {
        _isAutoScanEnabled.value = enabled
    }

    fun clearCache() {
        // Cache clearing simulated log
    }
}
