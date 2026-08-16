package com.kcmitch.v2p.data.db

import android.content.Context
import com.kcmitch.v2p.pages.workspace.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository layer for pipeline queue persistence.
 */
class PipelineRepository(private val dao: PipelineVideoDao) {

    val allPipelineVideosFlow: Flow<List<PipelineVideoEntity>> = dao.getAllPipelineVideosFlow()

    suspend fun getCachedPipelineVideos(): List<PipelineVideoEntity> = withContext(Dispatchers.IO) {
        dao.getAllPipelineVideos()
    }

    suspend fun savePipelineVideos(videos: List<VideoItem>) = withContext(Dispatchers.IO) {
        dao.clearAll()
        val entities = videos.mapIndexed { index, video ->
            PipelineVideoEntity(
                id = video.id,
                uriString = video.uri.toString(),
                name = video.name,
                durationMs = video.durationMs,
                sizeBytes = video.sizeBytes,
                fps = video.fps,
                orderIndex = index,
                addedTimestamp = System.currentTimeMillis()
            )
        }
        dao.insertAll(entities)
    }

    suspend fun removeVideo(videoId: String) = withContext(Dispatchers.IO) {
        dao.deleteById(videoId)
    }

    suspend fun removeVideosByUris(uris: List<String>) = withContext(Dispatchers.IO) {
        dao.deleteByUris(uris)
    }

    suspend fun clearPipeline() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    companion object {
        @Volatile
        private var INSTANCE: PipelineRepository? = null

        fun getInstance(context: Context): PipelineRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = PipelineRepository(db.pipelineVideoDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
