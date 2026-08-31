package de.f_soft_studio.abookplayer.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class LibraryLocationManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAbookLibStructureCreation() {
        val customLibDir = tempFolder.newFolder("Audiobooks")
        LibraryLocationManager.setLibraryDir(context, customLibDir)

        val libDir = LibraryLocationManager.getLibraryDir(context)
        assertEquals(customLibDir.absolutePath, libDir.absolutePath)

        val abookLib = LibraryLocationManager.getAbookLibDir(context)
        assertTrue(abookLib.exists())
        assertEquals(".abooklib", abookLib.name)

        val coversDir = LibraryLocationManager.getCoversDir(context)
        assertTrue(coversDir.exists())
        assertEquals(File(abookLib, "covers").absolutePath, coversDir.absolutePath)

        val metaDir = LibraryLocationManager.getMetadataDir(context)
        assertTrue(metaDir.exists())
        assertEquals(File(abookLib, "metadata").absolutePath, metaDir.absolutePath)

        val dbFile = LibraryLocationManager.getDatabaseFile(context)
        assertEquals(File(abookLib, "abook_database.db").absolutePath, dbFile.absolutePath)

        val nomediaFile = File(abookLib, ".nomedia")
        assertTrue(nomediaFile.exists())
    }

    @Test
    fun testTempDirIsInsideCacheDir() {
        val tempDir = LibraryLocationManager.getTempDir(context)
        assertTrue(tempDir.exists())
        assertTrue(tempDir.absolutePath.startsWith(context.cacheDir.absolutePath))

        val dummyFile = File(tempDir, "test.tmp").apply { writeText("hello") }
        assertTrue(dummyFile.exists())

        LibraryLocationManager.clearTempDir(context)
        assertFalse(dummyFile.exists())
    }


    @Test
    fun testFolderScannerIgnoresAbookLib() {
        val abookLibDir = File(tempFolder.root, ".abooklib")
        assertTrue(FolderScanner.isIgnoredDirectory(abookLibDir))

        val normalDir = File(tempFolder.root, "Perry Rhodan")
        assertFalse(FolderScanner.isIgnoredDirectory(normalDir))
    }
}
