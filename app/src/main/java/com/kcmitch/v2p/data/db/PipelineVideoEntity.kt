package com.kcmitch.v2p.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a video in the pipeline queue.
 * Persists the user's selected video queue across app closes and process restarts.
 */
@Entity(tableName = "pipeline_videos")
data class PipelineVideoEntity(
    @PrimaryKey val id: String,
    val uriString: String,
    val name: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val fps: Double = 30.0,
    val orderIndex: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis()
)
