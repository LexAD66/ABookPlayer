package de.f_soft_studio.abookplayer.storage

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ScrapedMetadataResult(
    val title: String? = null,
    val author: String? = null,
    val coverPath: String? = null,
    val description: String? = null,
    val firstPublishYear: Int? = null
)

/**
 * Leichtgewichtiger Scraper für OpenLibrary API (ohne schwere Drittanbieter-Bibliotheken).
 * Sucht nach Buchcovern und Metadaten und speichert sie lokal ab.
 */
class OpenLibraryScraper(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchMetadataAndCover(title: String, author: String = ""): ScrapedMetadataResult? = withContext(Dispatchers.IO) {
        return@withContext try {
            val query = if (author.isNotBlank()) "$title $author" else title
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val apiUrl = "https://openlibrary.org/search.json?q=$encodedQuery&limit=1"

            val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android Audiobook App)")
            }

            if (connection.responseCode != 200) {
                return@withContext null
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonRoot = json.parseToJsonElement(responseText).jsonObject
            val docs = jsonRoot["docs"]?.jsonArray ?: return@withContext null

            if (docs.isEmpty()) return@withContext null

            val doc = docs[0].jsonObject
            val resTitle = doc["title"]?.jsonPrimitive?.content
            val authorArray = doc["author_name"]?.jsonArray
            val resAuthor = authorArray?.firstOrNull()?.jsonPrimitive?.content ?: author
            val coverI = doc["cover_i"]?.jsonPrimitive?.content
            val firstYear = doc["first_publish_year"]?.jsonPrimitive?.content?.toIntOrNull()

            var localCoverPath: String? = null
            if (coverI != null && coverI != "null") {
                val imageUrl = "https://covers.openlibrary.org/b/id/$coverI-L.jpg"
                localCoverPath = downloadCoverImage(imageUrl, "openlibrary_$coverI.jpg")
            }

            ScrapedMetadataResult(
                title = resTitle,
                author = resAuthor,
                coverPath = localCoverPath,
                description = if (firstYear != null) "Erstmals veröffentlicht: $firstYear" else null,
                firstPublishYear = firstYear
            )
        } catch (e: Exception) {
            println("OpenLibraryScraper: Fehler bei der Online-Suche: ${e.message}")
            null
        }
    }

    private fun downloadCoverImage(imageUrl: String, targetFileName: String): String? {
        return try {
            val coversDir = LibraryLocationManager.getCoversDir(context)
            val targetFile = File(coversDir, targetFileName)


            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "ABookPlayer/1.0")
            }

            if (connection.responseCode == 200) {
                connection.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            println("OpenLibraryScraper: Fehler beim Herunterladen des Covers: ${e.message}")
            null
        }
    }
}
