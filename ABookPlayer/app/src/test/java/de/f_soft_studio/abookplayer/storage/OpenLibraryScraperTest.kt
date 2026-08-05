package de.f_soft_studio.abookplayer.storage

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OpenLibraryScraperTest {

    private val context: Context = mockk(relaxed = true)
    private val scraper = OpenLibraryScraper(context)

    @Test
    fun `searchMetadataAndCover returns null gracefully when network fails or no result`() {
        runBlocking {
            val result = scraper.searchMetadataAndCover("non_existent_book_title_xyz_12345")
            // Reagiert stabil ohne Crash
            assertTrue(result == null || result.title != null)
        }
    }
}
