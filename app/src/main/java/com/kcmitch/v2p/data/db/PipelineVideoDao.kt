package com.kcmitch.v2p.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for pipeline queue video items.
 */
@Dao
interface PipelineVideoDao {
    @Query("SELECT * FROM pipeline_videos ORDER BY orderIndex ASC, addedTimestamp ASC")
    fun getAllPipelineVideosFlow(): Flow<List<PipelineVideoEntity>>

    @Query("SELECT * FROM pipeline_videos ORDER BY orderIndex ASC, addedTimestamp ASC")
    suspend fun getAllPipelineVideos(): List<PipelineVideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<PipelineVideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: PipelineVideoEntity)

    @Query("DELETE FROM pipeline_videos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pipeline_videos WHERE uriString IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)

    @Query("DELETE FROM pipeline_videos")
    suspend fun clearAll()
}
