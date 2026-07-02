package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "videos", indices = [Index(value = ["path"], unique = true)])
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val size: Long,
    val duration: Long,
    val resolution: String,
    val path: String,
    val folderName: String,
    val lastPlayedPosition: Long = 0,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val isAudio: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isSystem: Boolean = false
)

@Entity(tableName = "playlist_video_cross_ref", primaryKeys = ["playlistId", "videoId"])
data class PlaylistVideoCrossRef(
    val playlistId: Int,
    val videoId: Int
)

@Entity(tableName = "vault_settings")
data class VaultSetting(
    @PrimaryKey val id: Int = 1,
    val pin: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = ""
)

@Dao
interface MediaDao {
    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isHidden = 0 ORDER BY dateAdded DESC")
    fun getVisibleVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isHidden = 1 ORDER BY dateAdded DESC")
    fun getHiddenVideos(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET lastPlayedPosition = :position WHERE id = :id")
    suspend fun updateLastPlayedPosition(id: Int, position: Long)

    @Query("UPDATE videos SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE videos SET isHidden = :isHidden WHERE id = :id")
    suspend fun updateHidden(id: Int, isHidden: Boolean)

    @Query("UPDATE videos SET title = :newTitle WHERE id = :id")
    suspend fun renameVideo(id: Int, newTitle: String)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: Int)

    // Playlists
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistVideoCrossRef(ref: PlaylistVideoCrossRef)

    @Query("DELETE FROM playlist_video_cross_ref WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Int, videoId: Int)

    @Query("""
        SELECT v.* FROM videos v 
        INNER JOIN playlist_video_cross_ref ref ON v.id = ref.videoId 
        WHERE ref.playlistId = :playlistId AND v.isHidden = 0
    """)
    fun getVideosInPlaylist(playlistId: Int): Flow<List<VideoEntity>>

    // Vault
    @Query("SELECT * FROM vault_settings WHERE id = 1 LIMIT 1")
    suspend fun getVaultSetting(): VaultSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVaultSetting(setting: VaultSetting)
}

@Database(
    entities = [VideoEntity::class, PlaylistEntity::class, PlaylistVideoCrossRef::class, VaultSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "infinity_video_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
