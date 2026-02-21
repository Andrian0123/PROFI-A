package ru.profia.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.profia.app.data.local.dao.IntermediateEstimateActDao
import ru.profia.app.data.local.dao.OpeningDao
import ru.profia.app.data.local.dao.ProjectDao
import ru.profia.app.data.local.dao.RoomDao
import ru.profia.app.data.local.dao.RoomScanDao
import ru.profia.app.data.local.dao.RoomWorkItemDao
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.local.entity.OpeningEntity
import ru.profia.app.data.local.entity.ProjectEntity
import ru.profia.app.data.local.entity.RoomEntity
import ru.profia.app.data.local.entity.RoomScanEntity
import ru.profia.app.data.local.entity.RoomWorkItemEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS room_scans (
                id TEXT NOT NULL PRIMARY KEY,
                projectId TEXT NOT NULL,
                roomId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                thumbnailPath TEXT,
                meshPath TEXT,
                status TEXT NOT NULL DEFAULT 'draft',
                title TEXT,
                FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_room_scans_projectId ON room_scans(projectId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_room_scans_roomId ON room_scans(roomId)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS room_work_items (
                id TEXT NOT NULL PRIMARY KEY,
                roomId TEXT NOT NULL,
                category TEXT NOT NULL,
                name TEXT NOT NULL,
                unitAbbr TEXT NOT NULL,
                price REAL NOT NULL,
                quantity REAL NOT NULL,
                FOREIGN KEY(roomId) REFERENCES rooms(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_room_work_items_roomId ON room_work_items(roomId)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS intermediate_estimate_acts (
                id TEXT NOT NULL PRIMARY KEY,
                projectId TEXT NOT NULL,
                title TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_intermediate_estimate_acts_projectId ON intermediate_estimate_acts(projectId)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS intermediate_estimate_act_items (
                id TEXT NOT NULL PRIMARY KEY,
                actId TEXT NOT NULL,
                roomName TEXT NOT NULL,
                category TEXT NOT NULL,
                name TEXT NOT NULL,
                unitAbbr TEXT NOT NULL,
                price REAL NOT NULL,
                quantity REAL NOT NULL,
                sortOrder INTEGER NOT NULL,
                FOREIGN KEY(actId) REFERENCES intermediate_estimate_acts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_intermediate_estimate_act_items_actId ON intermediate_estimate_act_items(actId)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE intermediate_estimate_act_items ADD COLUMN workItemId TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE projects ADD COLUMN discountText TEXT")
        db.execSQL("ALTER TABLE projects ADD COLUMN taxPercentText TEXT")
    }
}

@Database(
    entities = [
        ProjectEntity::class,
        RoomEntity::class,
        OpeningEntity::class,
        RoomScanEntity::class,
        RoomWorkItemEntity::class,
        IntermediateEstimateActEntity::class,
        IntermediateEstimateActItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun roomDao(): RoomDao
    abstract fun openingDao(): OpeningDao
    abstract fun roomScanDao(): RoomScanDao
    abstract fun roomWorkItemDao(): RoomWorkItemDao
    abstract fun intermediateEstimateActDao(): IntermediateEstimateActDao
}
