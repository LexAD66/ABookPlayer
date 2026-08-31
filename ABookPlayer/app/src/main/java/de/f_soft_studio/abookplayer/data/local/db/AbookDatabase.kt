package de.f_soft_studio.abookplayer.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.f_soft_studio.abookplayer.data.local.dao.AudiobookDao
import de.f_soft_studio.abookplayer.data.local.dao.BookmarkDao
import de.f_soft_studio.abookplayer.data.local.dao.ChapterDao
import de.f_soft_studio.abookplayer.data.local.dao.CharacterDao
import de.f_soft_studio.abookplayer.data.local.dao.ListeningSessionDao
import de.f_soft_studio.abookplayer.data.local.entity.AudiobookEntity
import de.f_soft_studio.abookplayer.data.local.entity.BookmarkEntity
import de.f_soft_studio.abookplayer.data.local.entity.ChapterEntity
import de.f_soft_studio.abookplayer.data.local.entity.CharacterEntity
import de.f_soft_studio.abookplayer.data.local.entity.ListeningSessionEntity

/**
 * Zentrale Room-Datenbankinstanz der ABook Player App.
 */
@Database(
    entities = [
        AudiobookEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        ListeningSessionEntity::class,
        CharacterEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AbookDatabase : RoomDatabase() {

    abstract fun audiobookDao(): AudiobookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun listeningSessionDao(): ListeningSessionDao
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AbookDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN narrator TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `listening_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `audiobookId` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN series TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN seriesOrder INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `characters` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `audiobookId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `role` TEXT NOT NULL DEFAULT '',
                        `description` TEXT NOT NULL DEFAULT '',
                        `relationship` TEXT NOT NULL DEFAULT '',
                        `isPrimary` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`audiobookId`) REFERENCES `audiobooks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_characters_audiobookId` ON `characters` (`audiobookId`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN customSpeed REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN equalizerPreset TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN parentSeries TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audiobooks ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE audiobooks SET addedAt = " +
                        "CASE WHEN lastPlayed > 0 THEN lastPlayed ELSE strftime('%s','now')*1000 END"
                )
            }
        }

        fun getInstance(context: Context): AbookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbookDatabase::class.java,
                    "abook_database.db"
                )
                    .addMigrations(
                        MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}




