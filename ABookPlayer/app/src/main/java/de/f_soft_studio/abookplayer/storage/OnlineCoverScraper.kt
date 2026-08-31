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

data class OnlineMetadataResult(
    val title: String? = null,
    val author: String? = null,
    val coverPath: String? = null,
    val description: String? = null,
    val providerName: String = ""
)

/**
 * Multiprovider Online-Cover-Scraper für Hörbücher & Bücher.
 * Bereinigt Pfad- & Kapitelnamen (CD1, Disc 1, 01 - etc.) und sucht auf Google Books, iTunes und OpenLibrary.
 */
class OnlineCoverScraper(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Bereinigt Buch- und Ordnernamen von typischen Präfixen/Suffixen wie CD1, Disc 1, Teil 1, 01 -, .mp3 etc.
     */
    fun cleanQueryText(text: String): String {
        var clean = text.trim()
        clean = clean.replace(Regex("""(?i)\.(mp3|m4b|m4a|zip|abook|flac|wav|ogg|aac|opus)$"""), "")
        clean = clean.replace(Regex("""(?i)\b(cd|disc|disk|teil|part)\s*[-_]?\s*\d+.*"""), "")
        clean = clean.replace(Regex("""^\d+[\s\-_.]+"""), "")
        clean = clean.replace(Regex("""[_]+"""), " ")
        clean = clean.replace(Regex("""\s+"""), " ").trim()
        return clean.ifBlank { text.trim() }
    }

    suspend fun searchCoverAndMetadata(rawTitle: String, rawAuthor: String = ""): OnlineMetadataResult? = withContext(Dispatchers.IO) {
        val cleanTitle = cleanQueryText(rawTitle)
        val cleanAuthor = cleanQueryText(rawAuthor)

        Log.d("OnlineCoverScraper", "Starte Online-Suche für: '$cleanTitle' (Autor: '$cleanAuthor')")

        // 1. Versuche Google Books API
        val googleResult = searchGoogleBooks(cleanTitle, cleanAuthor)
        if (googleResult != null && !googleResult.coverPath.isNullOrBlank()) {
            return@withContext googleResult
        }

        // 2. Versuche iTunes Search API
        val itunesResult = searchITunes(cleanTitle, cleanAuthor)
        if (itunesResult != null && !itunesResult.coverPath.isNullOrBlank()) {
            return@withContext itunesResult
        }

        // 3. Versuche OpenLibrary API
        val openLibraryResult = searchOpenLibrary(cleanTitle, cleanAuthor)
        if (openLibraryResult != null && !openLibraryResult.coverPath.isNullOrBlank()) {
            return@withContext openLibraryResult
        }

        // Fallback: Wenn Metadaten von Google Books oder iTunes vorhanden waren (selbst ohne Cover)
        googleResult ?: itunesResult ?: openLibraryResult
    }

    private fun searchGoogleBooks(title: String, author: String): OnlineMetadataResult? {
        return try {
            val query = if (author.isNotBlank()) "intitle:$title inauthor:$author" else title
            val url = "https://www.googleapis.com/books/v1/volumes?q=${URLEncoder.encode(query, "UTF-8")}&maxResults=1"

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android)")
            }

            if (conn.responseCode != 200) return null
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val root = json.parseToJsonElement(response).jsonObject
            val items = root["items"]?.jsonArray ?: return null
            if (items.isEmpty()) return null

            val volumeInfo = items[0].jsonObject["volumeInfo"]?.jsonObject ?: return null
            val resTitle = volumeInfo["title"]?.jsonPrimitive?.content
            val authors = volumeInfo["authors"]?.jsonArray
            val resAuthor = authors?.firstOrNull()?.jsonPrimitive?.content ?: author
            val description = volumeInfo["description"]?.jsonPrimitive?.content

            val imageLinks = volumeInfo["imageLinks"]?.jsonObject
            var coverUrl = imageLinks?.get("thumbnail")?.jsonPrimitive?.content
                ?: imageLinks?.get("smallThumbnail")?.jsonPrimitive?.content

            if (coverUrl != null && coverUrl.startsWith("http://")) {
                coverUrl = coverUrl.replace("http://", "https://")
            }

            var coverPath: String? = null
            if (coverUrl != null) {
                coverPath = downloadImage(coverUrl, "google_books_${resTitle?.hashCode() ?: 0}.jpg")
            }

            OnlineMetadataResult(
                title = resTitle ?: title,
                author = resAuthor,
                coverPath = coverPath,
                description = description,
                providerName = "Google Books"
            )
        } catch (e: Exception) {
            Log.w("OnlineCoverScraper", "Google Books API Fehler: ${e.message}")
            null
        }
    }

    private fun searchITunes(title: String, author: String): OnlineMetadataResult? {
        return try {
            val query = if (author.isNotBlank()) "$title $author" else title
            val url = "https://itunes.apple.com/search?media=audiobook&term=${URLEncoder.encode(query, "UTF-8")}&limit=1"

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android)")
            }

            if (conn.responseCode != 200) return null
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val root = json.parseToJsonElement(response).jsonObject
            val results = root["results"]?.jsonArray ?: return null
            if (results.isEmpty()) return null

            val item = results[0].jsonObject
            val resTitle = item["collectionName"]?.jsonPrimitive?.content ?: item["trackName"]?.jsonPrimitive?.content
            val resAuthor = item["artistName"]?.jsonPrimitive?.content ?: author
            val rawCoverUrl = item["artworkUrl100"]?.jsonPrimitive?.content ?: item["artworkUrl60"]?.jsonPrimitive?.content
            val description = item["description"]?.jsonPrimitive?.content

            val highResCoverUrl = rawCoverUrl?.replace("100x100bb", "600x600bb")?.replace("60x60bb", "600x600bb")

            var coverPath: String? = null
            if (highResCoverUrl != null) {
                coverPath = downloadImage(highResCoverUrl, "itunes_${resTitle?.hashCode() ?: 0}.jpg")
            }

            OnlineMetadataResult(
                title = resTitle ?: title,
                author = resAuthor,
                coverPath = coverPath,
                description = description,
                providerName = "iTunes"
            )
        } catch (e: Exception) {
            Log.w("OnlineCoverScraper", "iTunes API Fehler: ${e.message}")
            null
        }
    }

    private fun searchOpenLibrary(title: String, author: String): OnlineMetadataResult? {
        return try {
            val query = if (author.isNotBlank()) "$title $author" else title
            val url = "https://openlibrary.org/search.json?q=${URLEncoder.encode(query, "UTF-8")}&limit=1"

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android)")
            }

            if (conn.responseCode != 200) return null
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val root = json.parseToJsonElement(response).jsonObject
            val docs = root["docs"]?.jsonArray ?: return null
            if (docs.isEmpty()) return null

            val doc = docs[0].jsonObject
            val resTitle = doc["title"]?.jsonPrimitive?.content
            val authors = doc["author_name"]?.jsonArray
            val resAuthor = authors?.firstOrNull()?.jsonPrimitive?.content ?: author
            val coverI = doc["cover_i"]?.jsonPrimitive?.content

            var coverPath: String? = null
            if (coverI != null && coverI != "null") {
                val coverUrl = "https://covers.openlibrary.org/b/id/$coverI-L.jpg"
                coverPath = downloadImage(coverUrl, "openlibrary_$coverI.jpg")
            }

            OnlineMetadataResult(
                title = resTitle ?: title,
                author = resAuthor,
                coverPath = coverPath,
                description = null,
                providerName = "OpenLibrary"
            )
        } catch (e: Exception) {
            Log.w("OnlineCoverScraper", "OpenLibrary API Fehler: ${e.message}")
            null
        }
    }

    private fun downloadImage(imageUrl: String, fileName: String): String? {
        return try {
            val coversDir = LibraryLocationManager.getCoversDir(context)
            val targetFile = File(coversDir, fileName)


            val conn = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "ABookPlayer/1.0")
            }

            if (conn.responseCode == 200) {
                conn.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            } else null
        } catch (e: Exception) {
            Log.e("OnlineCoverScraper", "Fehler beim Bild-Download ($imageUrl): ${e.message}")
            null
        }
    }
}
