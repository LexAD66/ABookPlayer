package de.f_soft_studio.abookplayer.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import de.f_soft_studio.abookplayer.data.local.db.AbookDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit-Tests für alle Room-Datenbankmigrationen (Schema v3 bis v11).
 * Prüft schrittweise Spaltenhinzufügungen, Tabellenerstellungen, Daten-Updates und das Gesamtszenario.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseMigrationTest {

    private fun createInMemoryDb(version: Int, onCreate: (SupportSQLiteDatabase) -> Unit): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    onCreate(db)
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    private fun createV3Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `audiobooks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `author` TEXT NOT NULL,
                `filePath` TEXT NOT NULL,
                `coverUri` TEXT,
                `description` TEXT,
                `duration` INTEGER NOT NULL,
                `currentPosition` INTEGER NOT NULL,
                `lastPlayed` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `chapters` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `audiobookId` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `startTime` INTEGER NOT NULL,
                `audioPath` TEXT,
                FOREIGN KEY(`audiobookId`) REFERENCES `audiobooks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chapters_audiobookId` ON `chapters` (`audiobookId`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bookmarks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `audiobookId` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                `note` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`audiobookId`) REFERENCES `audiobooks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_audiobookId` ON `bookmarks` (`audiobookId`)")
    }

    @Test
    fun testMigration_3_to_4_addsNarrator() {
        val db = createInMemoryDb(3) { createV3Schema(it) }
        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed) VALUES ('Titel', 'Autor', '/path', 100, 0, 1000)")

        AbookDatabase.MIGRATION_3_4.migrate(db)

        val cursor = db.query("SELECT * FROM audiobooks")
        assertTrue(cursor.moveToFirst())
        val narratorIndex = cursor.getColumnIndex("narrator")
        assertTrue("Spalte 'narrator' muss existieren", narratorIndex >= 0)
        cursor.close()
    }

    @Test
    fun testMigration_4_to_5_createsListeningSessions() {
        val db = createInMemoryDb(4) { createV3Schema(it) }

        AbookDatabase.MIGRATION_4_5.migrate(db)

        db.execSQL("INSERT INTO listening_sessions (audiobookId, date, durationSeconds, timestamp) VALUES (1, '2026-09-09', 120, 10000)")
        val cursor = db.query("SELECT * FROM listening_sessions")
        assertTrue(cursor.moveToFirst())
        assertEquals("2026-09-09", cursor.getString(cursor.getColumnIndexOrThrow("date")))
        assertEquals(120, cursor.getInt(cursor.getColumnIndexOrThrow("durationSeconds")))
        cursor.close()
    }

    @Test
    fun testMigration_5_to_6_addsSeriesColumns() {
        val db = createInMemoryDb(5) { createV3Schema(it) }

        AbookDatabase.MIGRATION_5_6.migrate(db)

        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed, series, seriesOrder) VALUES ('Band 1', 'Autor', '/path', 100, 0, 1000, 'Harry Potter', 1)")
        val cursor = db.query("SELECT series, seriesOrder FROM audiobooks")
        assertTrue(cursor.moveToFirst())
        assertEquals("Harry Potter", cursor.getString(0))
        assertEquals(1, cursor.getInt(1))
        cursor.close()
    }

    @Test
    fun testMigration_6_to_7_createsCharactersTable() {
        val db = createInMemoryDb(6) { createV3Schema(it) }

        AbookDatabase.MIGRATION_6_7.migrate(db)

        db.execSQL("INSERT INTO characters (audiobookId, name, role, description, relationship, isPrimary) VALUES (1, 'Sherlock', 'Detektiv', 'Genie', 'Partner von Watson', 1)")
        val cursor = db.query("SELECT name, role, isPrimary FROM characters")
        assertTrue(cursor.moveToFirst())
        assertEquals("Sherlock", cursor.getString(0))
        assertEquals("Detektiv", cursor.getString(1))
        assertEquals(1, cursor.getInt(2))
        cursor.close()
    }

    @Test
    fun testMigration_7_to_8_addsSpeedAndEqualizer() {
        val db = createInMemoryDb(7) { createV3Schema(it) }

        AbookDatabase.MIGRATION_7_8.migrate(db)

        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed, customSpeed, equalizerPreset) VALUES ('Titel', 'Autor', '/path', 100, 0, 1000, 1.25, 'VocalBoost')")
        val cursor = db.query("SELECT customSpeed, equalizerPreset FROM audiobooks")
        assertTrue(cursor.moveToFirst())
        assertEquals(1.25f, cursor.getFloat(0), 0.01f)
        assertEquals("VocalBoost", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun testMigration_8_to_9_addsFavoriteAndParentSeries() {
        val db = createInMemoryDb(8) { createV3Schema(it) }

        AbookDatabase.MIGRATION_8_9.migrate(db)

        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed, isFavorite, parentSeries) VALUES ('Titel', 'Autor', '/path', 100, 0, 1000, 1, 'Mittelerde')")
        val cursor = db.query("SELECT isFavorite, parentSeries FROM audiobooks")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals("Mittelerde", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun testMigration_9_to_10_addsAddedAtAndPopulates() {
        val db = createInMemoryDb(9) { createV3Schema(it) }
        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed) VALUES ('Buch 1', 'Autor', '/path1', 100, 0, 1234567890)")
        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed) VALUES ('Buch 2', 'Autor', '/path2', 100, 0, 0)")

        AbookDatabase.MIGRATION_9_10.migrate(db)

        val cursor = db.query("SELECT title, addedAt FROM audiobooks ORDER BY title ASC")
        assertTrue(cursor.moveToFirst())
        assertEquals("Buch 1", cursor.getString(0))
        assertEquals(1234567890L, cursor.getLong(1))

        assertTrue(cursor.moveToNext())
        assertEquals("Buch 2", cursor.getString(0))
        assertTrue("addedAt muss für lastPlayed=0 initialisiert worden sein", cursor.getLong(1) > 0L)
        cursor.close()
    }

    @Test
    fun testMigration_10_to_11_cleansChapterExtensions() {
        val db = createInMemoryDb(10) { createV3Schema(it) }
        db.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, '01 - Prolog.mp3', 0, NULL)")
        db.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, '02 - Das Erwachen.m4b', 1000, NULL)")
        db.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, '03 - Epilog.flac', 2000, NULL)")
        db.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, 'Normaler Titel ohne Endung', 3000, NULL)")

        AbookDatabase.MIGRATION_10_11.migrate(db)

        val cursor = db.query("SELECT title FROM chapters ORDER BY id ASC")
        assertTrue(cursor.moveToFirst())
        assertEquals("01 - Prolog", cursor.getString(0))
        assertTrue(cursor.moveToNext())
        assertEquals("02 - Das Erwachen", cursor.getString(0))
        assertTrue(cursor.moveToNext())
        assertEquals("03 - Epilog", cursor.getString(0))
        assertTrue(cursor.moveToNext())
        assertEquals("Normaler Titel ohne Endung", cursor.getString(0))
        cursor.close()
    }

    @Test
    fun testFullMigration_3_to_11_endToEnd() {
        val db = createInMemoryDb(3) { createV3Schema(it) }
        db.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed) VALUES ('Herr der Ringe', 'Tolkien', '/storage/hdr.abook', 500000, 10000, 9999999)")
        db.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, 'Kapitel 1.mp3', 0, NULL)")

        // Alle Migrationen nacheinander ausführen
        AbookDatabase.MIGRATION_3_4.migrate(db)
        AbookDatabase.MIGRATION_4_5.migrate(db)
        AbookDatabase.MIGRATION_5_6.migrate(db)
        AbookDatabase.MIGRATION_6_7.migrate(db)
        AbookDatabase.MIGRATION_7_8.migrate(db)
        AbookDatabase.MIGRATION_8_9.migrate(db)
        AbookDatabase.MIGRATION_9_10.migrate(db)
        AbookDatabase.MIGRATION_10_11.migrate(db)

        // Verifiziere Audiobook-Datensatz
        val bookCursor = db.query("SELECT title, author, addedAt, isFavorite, narrator, series, parentSeries FROM audiobooks")
        assertTrue(bookCursor.moveToFirst())
        assertEquals("Herr der Ringe", bookCursor.getString(0))
        assertEquals("Tolkien", bookCursor.getString(1))
        assertEquals(9999999L, bookCursor.getLong(2)) // addedAt aus lastPlayed
        assertEquals(0, bookCursor.getInt(3)) // isFavorite default 0
        bookCursor.close()

        // Verifiziere Kapitel
        val chapterCursor = db.query("SELECT title FROM chapters")
        assertTrue(chapterCursor.moveToFirst())
        assertEquals("Kapitel 1", chapterCursor.getString(0))
        chapterCursor.close()
    }

    @Test
    fun testRoomDatabaseBuilderCanOpenMigratedV3Database() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "room_migration_test.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    createV3Schema(db)
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val initialDb = helper.writableDatabase
        initialDb.execSQL("INSERT INTO audiobooks (title, author, filePath, duration, currentPosition, lastPlayed) VALUES ('Test Buch', 'Test Autor', '/path/test.abook', 1000, 0, 500)")
        initialDb.execSQL("INSERT INTO chapters (audiobookId, title, startTime, audioPath) VALUES (1, '01 - Prolog.mp3', 0, NULL)")
        initialDb.close()

        val roomDb = Room.databaseBuilder(
            context,
            AbookDatabase::class.java,
            dbName
        ).addMigrations(
            AbookDatabase.MIGRATION_3_4,
            AbookDatabase.MIGRATION_4_5,
            AbookDatabase.MIGRATION_5_6,
            AbookDatabase.MIGRATION_6_7,
            AbookDatabase.MIGRATION_7_8,
            AbookDatabase.MIGRATION_8_9,
            AbookDatabase.MIGRATION_9_10,
            AbookDatabase.MIGRATION_10_11
        ).build()

        runBlocking {
            val book = roomDb.audiobookDao().getAudiobookById(1)
            assertNotNull("Das migrierte Hörbuch muss auffindbar sein", book)
            assertEquals("Test Buch", book?.title)
            assertEquals(500L, book?.addedAt)

            val chapters = roomDb.chapterDao().getChaptersForAudiobook(1)
            assertEquals(1, chapters.size)
            assertEquals("01 - Prolog", chapters[0].title)
        }

        roomDb.close()
        context.deleteDatabase(dbName)
    }
}
