package com.kcmitch.v2p.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PipelineVideoDao_Impl(
  __db: RoomDatabase,
) : PipelineVideoDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPipelineVideoEntity: EntityInsertAdapter<PipelineVideoEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPipelineVideoEntity = object : EntityInsertAdapter<PipelineVideoEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `pipeline_videos` (`id`,`uriString`,`name`,`durationMs`,`sizeBytes`,`fps`,`orderIndex`,`addedTimestamp`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PipelineVideoEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.uriString)
        statement.bindText(3, entity.name)
        statement.bindLong(4, entity.durationMs)
        statement.bindLong(5, entity.sizeBytes)
        statement.bindDouble(6, entity.fps)
        statement.bindLong(7, entity.orderIndex.toLong())
        statement.bindLong(8, entity.addedTimestamp)
      }
    }
  }

  public override suspend fun insertAll(videos: List<PipelineVideoEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPipelineVideoEntity.insert(_connection, videos)
  }

  public override suspend fun insert(video: PipelineVideoEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfPipelineVideoEntity.insert(_connection, video)
  }

  public override fun getAllPipelineVideosFlow(): Flow<List<PipelineVideoEntity>> {
    val _sql: String = "SELECT * FROM pipeline_videos ORDER BY orderIndex ASC, addedTimestamp ASC"
    return createFlow(__db, false, arrayOf("pipeline_videos")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUriString: Int = getColumnIndexOrThrow(_stmt, "uriString")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfFps: Int = getColumnIndexOrThrow(_stmt, "fps")
        val _columnIndexOfOrderIndex: Int = getColumnIndexOrThrow(_stmt, "orderIndex")
        val _columnIndexOfAddedTimestamp: Int = getColumnIndexOrThrow(_stmt, "addedTimestamp")
        val _result: MutableList<PipelineVideoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PipelineVideoEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUriString: String
          _tmpUriString = _stmt.getText(_columnIndexOfUriString)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpFps: Double
          _tmpFps = _stmt.getDouble(_columnIndexOfFps)
          val _tmpOrderIndex: Int
          _tmpOrderIndex = _stmt.getLong(_columnIndexOfOrderIndex).toInt()
          val _tmpAddedTimestamp: Long
          _tmpAddedTimestamp = _stmt.getLong(_columnIndexOfAddedTimestamp)
          _item =
              PipelineVideoEntity(_tmpId,_tmpUriString,_tmpName,_tmpDurationMs,_tmpSizeBytes,_tmpFps,_tmpOrderIndex,_tmpAddedTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPipelineVideos(): List<PipelineVideoEntity> {
    val _sql: String = "SELECT * FROM pipeline_videos ORDER BY orderIndex ASC, addedTimestamp ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUriString: Int = getColumnIndexOrThrow(_stmt, "uriString")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfFps: Int = getColumnIndexOrThrow(_stmt, "fps")
        val _columnIndexOfOrderIndex: Int = getColumnIndexOrThrow(_stmt, "orderIndex")
        val _columnIndexOfAddedTimestamp: Int = getColumnIndexOrThrow(_stmt, "addedTimestamp")
        val _result: MutableList<PipelineVideoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PipelineVideoEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUriString: String
          _tmpUriString = _stmt.getText(_columnIndexOfUriString)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpFps: Double
          _tmpFps = _stmt.getDouble(_columnIndexOfFps)
          val _tmpOrderIndex: Int
          _tmpOrderIndex = _stmt.getLong(_columnIndexOfOrderIndex).toInt()
          val _tmpAddedTimestamp: Long
          _tmpAddedTimestamp = _stmt.getLong(_columnIndexOfAddedTimestamp)
          _item =
              PipelineVideoEntity(_tmpId,_tmpUriString,_tmpName,_tmpDurationMs,_tmpSizeBytes,_tmpFps,_tmpOrderIndex,_tmpAddedTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM pipeline_videos WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByUris(uris: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM pipeline_videos WHERE uriString IN (")
    val _inputSize: Int = uris.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in uris) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM pipeline_videos"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
