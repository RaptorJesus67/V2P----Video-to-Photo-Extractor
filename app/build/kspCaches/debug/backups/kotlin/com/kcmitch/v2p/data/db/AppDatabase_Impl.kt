package com.kcmitch.v2p.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _pipelineVideoDao: Lazy<PipelineVideoDao> = lazy {
    PipelineVideoDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "1c51f263441197f2df723bf14879a2de", "c352ec38d12a312449eeee4333c47d68") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `pipeline_videos` (`id` TEXT NOT NULL, `uriString` TEXT NOT NULL, `name` TEXT NOT NULL, `durationMs` INTEGER NOT NULL, `sizeBytes` INTEGER NOT NULL, `fps` REAL NOT NULL, `orderIndex` INTEGER NOT NULL, `addedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1c51f263441197f2df723bf14879a2de')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `pipeline_videos`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsPipelineVideos: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPipelineVideos.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("uriString", TableInfo.Column("uriString", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("fps", TableInfo.Column("fps", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("orderIndex", TableInfo.Column("orderIndex", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPipelineVideos.put("addedTimestamp", TableInfo.Column("addedTimestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPipelineVideos: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPipelineVideos: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPipelineVideos: TableInfo = TableInfo("pipeline_videos", _columnsPipelineVideos,
            _foreignKeysPipelineVideos, _indicesPipelineVideos)
        val _existingPipelineVideos: TableInfo = read(connection, "pipeline_videos")
        if (!_infoPipelineVideos.equals(_existingPipelineVideos)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |pipeline_videos(com.kcmitch.v2p.data.db.PipelineVideoEntity).
              | Expected:
              |""".trimMargin() + _infoPipelineVideos + """
              |
              | Found:
              |""".trimMargin() + _existingPipelineVideos)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "pipeline_videos")
  }

  public override fun clearAllTables() {
    super.performClear(false, "pipeline_videos")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(PipelineVideoDao::class, PipelineVideoDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun pipelineVideoDao(): PipelineVideoDao = _pipelineVideoDao.value
}
