package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object PlaybackManager {
    private var mediaPlayer: MediaPlayer? = null
    private var scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var repository: MediaRepository? = null

    // State flows
    private val _currentMedia = MutableStateFlow<VideoEntity?>(null)
    val currentMedia: StateFlow<VideoEntity?> = _currentMedia

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked

    private val _isBackgroundMode = MutableStateFlow(false)
    val isBackgroundMode: StateFlow<Boolean> = _isBackgroundMode

    private val _isFloatingMode = MutableStateFlow(false)
    val isFloatingMode: StateFlow<Boolean> = _isFloatingMode

    fun init(repo: MediaRepository) {
        this.repository = repo
    }

    fun getMediaPlayer(context: Context): MediaPlayer {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration.toLong()
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = _duration.value
                    stopProgressTracker()
                    saveProgress()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("PlaybackManager", "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    stopProgressTracker()
                    true
                }
            }
        }
        return mediaPlayer!!
    }

    fun playMedia(context: Context, media: VideoEntity, startPosition: Long = -1L) {
        _currentMedia.value = media
        _playbackSpeed.value = 1.0f
        
        val player = getMediaPlayer(context)
        try {
            player.reset()
            val uri = if (media.path.startsWith("http")) {
                Uri.parse(media.path)
            } else {
                Uri.parse(media.path)
            }
            player.setDataSource(context, uri)
            player.prepareAsync()

            val resumePos = if (startPosition >= 0) startPosition else media.lastPlayedPosition
            player.setOnPreparedListener { mp ->
                _duration.value = mp.duration.toLong()
                if (resumePos > 0 && resumePos < mp.duration) {
                    mp.seekTo(resumePos.toInt())
                }
                mp.start()
                _isPlaying.value = true
                startProgressTracker()
                
                // Track play count
                scope.launch {
                    val updatedMedia = media.copy(playCount = media.playCount + 1)
                    repository?.insertVideo(updatedMedia)
                }
            }
        } catch (e: Exception) {
            Log.e("PlaybackManager", "Error playing media: ${e.message}")
        }
    }

    fun togglePlayPause(context: Context) {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
            saveProgress()
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun seekTo(position: Long) {
        val player = mediaPlayer ?: return
        player.seekTo(position.toInt())
        _currentPosition.value = position
        saveProgress()
    }

    fun setSpeed(speed: Float) {
        val player = mediaPlayer ?: return
        _playbackSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            } catch (e: Exception) {
                Log.e("PlaybackManager", "Failed to set playback speed: ${e.message}")
            }
        }
    }

    fun setScreenLocked(locked: Boolean) {
        _isScreenLocked.value = locked
    }

    fun setBackgroundMode(enabled: Boolean) {
        _isBackgroundMode.value = enabled
    }

    fun setFloatingMode(enabled: Boolean) {
        _isFloatingMode.value = enabled
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        saveProgress()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentMedia.value = null
    }

    private fun saveProgress() {
        val media = _currentMedia.value ?: return
        val pos = _currentPosition.value
        scope.launch {
            repository?.updateLastPlayed(media.id, pos)
        }
    }
}
