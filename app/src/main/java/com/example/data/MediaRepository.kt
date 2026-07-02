package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context, private val mediaDao: MediaDao) {

    val allVideos: Flow<List<VideoEntity>> = mediaDao.getAllVideos()
    val visibleVideos: Flow<List<VideoEntity>> = mediaDao.getVisibleVideos()
    val hiddenVideos: Flow<List<VideoEntity>> = mediaDao.getHiddenVideos()
    val allPlaylists: Flow<List<PlaylistEntity>> = mediaDao.getAllPlaylists()

    suspend fun insertVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        mediaDao.insertVideo(video)
    }

    suspend fun updateLastPlayed(id: Int, position: Long) = withContext(Dispatchers.IO) {
        mediaDao.updateLastPlayedPosition(id, position)
    }

    suspend fun toggleFavorite(id: Int, isFav: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.updateFavorite(id, isFav)
    }

    suspend fun toggleHidden(id: Int, isHidden: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.updateHidden(id, isHidden)
    }

    suspend fun renameVideo(id: Int, newTitle: String) = withContext(Dispatchers.IO) {
        mediaDao.renameVideo(id, newTitle)
    }

    suspend fun deleteVideo(id: Int) = withContext(Dispatchers.IO) {
        mediaDao.deleteVideo(id)
    }

    // Playlists
    suspend fun createPlaylist(name: String, isSystem: Boolean = false): Long = withContext(Dispatchers.IO) {
        mediaDao.insertPlaylist(PlaylistEntity(name = name, isSystem = isSystem))
    }

    suspend fun deletePlaylist(id: Int) = withContext(Dispatchers.IO) {
        mediaDao.deletePlaylist(id)
    }

    suspend fun addVideoToPlaylist(playlistId: Int, videoId: Int) = withContext(Dispatchers.IO) {
        mediaDao.insertPlaylistVideoCrossRef(PlaylistVideoCrossRef(playlistId, videoId))
    }

    suspend fun removeVideoFromPlaylist(playlistId: Int, videoId: Int) = withContext(Dispatchers.IO) {
        mediaDao.removeVideoFromPlaylist(playlistId, videoId)
    }

    fun getVideosInPlaylist(playlistId: Int): Flow<List<VideoEntity>> {
        return mediaDao.getVideosInPlaylist(playlistId)
    }

    // Vault PIN settings
    suspend fun getVaultSetting(): VaultSetting? = withContext(Dispatchers.IO) {
        mediaDao.getVaultSetting()
    }

    suspend fun saveVaultSetting(pin: String, securityQuestion: String, securityAnswer: String) = withContext(Dispatchers.IO) {
        mediaDao.saveVaultSetting(VaultSetting(pin = pin, securityQuestion = securityQuestion, securityAnswer = securityAnswer))
    }

    // Perform Auto Storage Scan
    suspend fun scanStorage() = withContext(Dispatchers.IO) {
        val scannedList = mutableListOf<VideoEntity>()
        
        try {
            // Scan videos from MediaStore
            queryMediaStoreVideos(scannedList)
            // Scan audio from MediaStore
            queryMediaStoreAudio(scannedList)
        } catch (e: Exception) {
            Log.e("MediaRepository", "Error scanning media store: ${e.message}")
        }

        if (scannedList.isNotEmpty()) {
            mediaDao.insertVideos(scannedList)
        }

        // Always check if we should pre-seed demo videos (e.g., if there are no videos or we want to ensure robust playback testing)
        // Let's check how many videos we currently have in database
        val existingCount = getVisibleVideosCount()
        if (existingCount < 2) {
            seedDemoVideos()
        }
    }

    private suspend fun getVisibleVideosCount(): Int = withContext(Dispatchers.IO) {
        return@withContext mediaDao.getVisibleVideos().first().size
    }

    private fun queryMediaStoreVideos(list: MutableList<VideoEntity>) {
        val contentResolver: ContentResolver = context.contentResolver
        val videoUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.RESOLUTION,
            MediaStore.Video.Media.DATE_ADDED
        )

        val cursor = contentResolver.query(videoUri, projection, null, null, null)
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Video_$id"
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn) ?: ""
                val resolution = cursor.getString(resolutionColumn) ?: "1080p"
                val date = cursor.getLong(dateColumn) * 1000L

                // Derive folder name
                val file = File(path)
                val folderName = file.parentFile?.name ?: "Internal Storage"

                list.add(
                    VideoEntity(
                        title = name,
                        size = size,
                        duration = duration,
                        resolution = resolution,
                        path = path,
                        folderName = folderName,
                        dateAdded = date,
                        isAudio = false
                    )
                )
            }
            cursor.close()
        }
    }

    private fun queryMediaStoreAudio(list: MutableList<VideoEntity>) {
        val contentResolver: ContentResolver = context.contentResolver
        val audioUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val cursor = contentResolver.query(audioUri, projection, null, null, null)
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Audio_$id"
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn) ?: ""
                val date = cursor.getLong(dateColumn) * 1000L

                val file = File(path)
                val folderName = file.parentFile?.name ?: "Music"

                list.add(
                    VideoEntity(
                        title = name,
                        size = size,
                        duration = duration,
                        resolution = "MP3",
                        path = path,
                        folderName = folderName,
                        dateAdded = date,
                        isAudio = true
                    )
                )
            }
            cursor.close()
        }
    }

    // Seeds high-quality public media streaming assets for test playability in Emulator
    private suspend fun seedDemoVideos() {
        val demoItems = listOf(
            VideoEntity(
                title = "Big Buck Bunny (Animation)",
                size = 127592420,
                duration = 596000,
                resolution = "1920x1080",
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                folderName = "Downloads",
                isAudio = false
            ),
            VideoEntity(
                title = "Sintel (Movie)",
                size = 243405780,
                duration = 888000,
                resolution = "1920x1080",
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                folderName = "Camera",
                isAudio = false
            ),
            VideoEntity(
                title = "Tears of Steel (Sci-Fi)",
                size = 184592420,
                duration = 734000,
                resolution = "1920x1080",
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                folderName = "WhatsApp Media",
                isAudio = false
            ),
            VideoEntity(
                title = "Elephants Dream (Visuals)",
                size = 109242400,
                duration = 653000,
                resolution = "1920x1080",
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                folderName = "Telegram Media",
                isAudio = false
            ),
            VideoEntity(
                title = "Synth Vibe (Track 01)",
                size = 6291456,
                duration = 372000,
                resolution = "320kbps",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                folderName = "Downloads",
                isAudio = true
            ),
            VideoEntity(
                title = "Chill Beats (Track 02)",
                size = 7340032,
                duration = 423000,
                resolution = "320kbps",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                folderName = "Music",
                isAudio = true
            )
        )

        mediaDao.insertVideos(demoItems)

        // Preseed standard system playlists
        val systemPlaylists = listOf("Movie Playlist", "Cartoon Playlist", "Study Playlist", "Favorite Playlist", "Watch Later")
        systemPlaylists.forEach { name ->
            mediaDao.insertPlaylist(PlaylistEntity(name = name, isSystem = true))
        }
    }
}
