package de.f_soft_studio.abookplayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CoverHelperTest {

    @Test
    fun `resolveCoverModel with null or empty returns null when no fallback`() {
        assertNull(CoverHelper.resolveCoverModel(null, null))
        assertNull(CoverHelper.resolveCoverModel("", ""))
        assertNull(CoverHelper.resolveCoverModel("   ", null))
    }

    @Test
    fun `resolveCoverModel with existing file path returns file`() {
        val tempFile = File.createTempFile("test_cover", ".jpg")
        try {
            val model = CoverHelper.resolveCoverModel(tempFile.absolutePath, null)
            assertNotNull(model)
            assertTrue(model is File)
            assertEquals(tempFile.absolutePath, (model as File).absolutePath)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `resolveCoverModel with file URI prefix returns file or uri`() {
        val tempFile = File.createTempFile("test_cover_uri", ".jpg")
        try {
            val fileUri = "file://${tempFile.absolutePath}"
            val model = CoverHelper.resolveCoverModel(fileUri, null)
            assertNotNull(model)
            assertTrue(model is File)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `resolveCoverModel with folder fallback returns cover file if present`() {
        val tempDir = java.nio.file.Files.createTempDirectory("test_book_dir").toFile()
        val coverFile = File(tempDir, "cover.jpg")
        coverFile.writeText("fake image content")
        try {
            val model = CoverHelper.resolveCoverModel(null, tempDir.absolutePath)
            assertNotNull(model)
            assertTrue(model is File)
            assertEquals(coverFile.absolutePath, (model as File).absolutePath)
        } finally {
            coverFile.delete()
            tempDir.delete()
        }
    }
}
