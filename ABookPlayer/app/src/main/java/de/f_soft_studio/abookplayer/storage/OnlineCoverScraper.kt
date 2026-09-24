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
    val narrator: String? = null,
    val series: String? = null,
    val seriesOrder: Int? = null,
    val coverPath: String? = null,
    val coverUrl: String? = null,
    val description: String? = null,
    val providerName: String = "",
    val publisher: String? = null,
    val releaseDate: String? = null
)


/**
 * Multiprovider Online-Cover-Scraper für Hörbücher & Bücher.
 * Bereinigt Pfad- & Kapitelnamen (CD1, Disc 1, 01 - etc.) und sucht auf Audible (DE), iTunes, Google Books und OpenLibrary.
 */
class OnlineCoverScraper(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Bereinigt HTML-Tags aus Beschreibungen (z. B. aus Audible-Zusammenfassungen).
     */
    fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .trim()
    }

    /**
     * Bereinigt Buch- und Ordnernamen von typischen Präfixen/Suffixen wie CD1, Disc 1, Teil 1, 01 -, .mp3 etc.
     */
    fun cleanQueryText(text: String): String {
        var clean = text.trim()
        clean = clean.replace(Regex("""(?i)\.(mp3|m4b|m4a|zip|abook|flac|wav|ogg|aac|opus)$"""), "")
        clean = clean.replace(Regex("""(?i)\s*\((ungekürzt|gekürzt|unabridged|abridged|hörbuch|audiobook|cd\s*\d+|mp3)\)"""), "")
        clean = clean.replace(Regex("""(?i)[._\s]+(ungekürzt|gekürzt|unabridged|abridged|hörbuch|audiobook)\b.*"""), "")
        clean = clean.replace(Regex("""(?i)\b(cd|disc|disk|teil|part)\s*[-_]?\s*\d+.*"""), "")
        clean = clean.replace(Regex("""^\d+[\s\-_.]+"""), "")
        // Punkte zwischen Buchstaben/Wörtern durch Leerzeichen ersetzen (z. B. "Gav.Thorpe.-.Warhammer" -> "Gav Thorpe - Warhammer")
        // Behalte Punkte zwischen Ziffern bei (z. B. "40.000" oder "1.5")
        clean = clean.replace(Regex("""(?<=\D)\.|\.(?=\D)"""), " ")
        clean = clean.replace(Regex("""[_]+"""), " ")
        clean = clean.replace(Regex("""\s+"""), " ").trim()
        return clean.ifBlank { text.trim() }
    }

    suspend fun searchCoverAndMetadata(rawTitle: String, rawAuthor: String = ""): OnlineMetadataResult? = withContext(Dispatchers.IO) {
        val cleanTitle = cleanQueryText(rawTitle)
        val cleanAuthor = cleanQueryText(rawAuthor)

        Log.d("OnlineCoverScraper", "Starte Online-Suche für: '$cleanTitle' (Autor: '$cleanAuthor')")

        // 1. Versuche Audible (DE) API (liefert Sprecher, Serie, Band und quadratische 1:1 HD-Cover)
        val audibleResult = searchAudible(cleanTitle, cleanAuthor)
        if (audibleResult != null && (!audibleResult.coverPath.isNullOrBlank() || !audibleResult.description.isNullOrBlank() || !audibleResult.narrator.isNullOrBlank())) {
            return@withContext audibleResult
        }

        // 2. Versuche iTunes Search API (Audiobooks DE)
        val itunesResult = searchITunes(cleanTitle, cleanAuthor)
        if (itunesResult != null && !itunesResult.coverPath.isNullOrBlank()) {
            return@withContext itunesResult
        }

        // 3. Versuche Google Books API
        val googleResult = searchGoogleBooks(cleanTitle, cleanAuthor)
        if (googleResult != null && !googleResult.coverPath.isNullOrBlank()) {
            return@withContext googleResult
        }

        // 4. Versuche OpenLibrary API
        val openLibraryResult = searchOpenLibrary(cleanTitle, cleanAuthor)
        if (openLibraryResult != null && !openLibraryResult.coverPath.isNullOrBlank()) {
            return@withContext openLibraryResult
        }

        // Fallback: Wenn Metadaten vorhanden waren (selbst ohne Cover)
        audibleResult ?: itunesResult ?: googleResult ?: openLibraryResult
    }

    /**
     * Sucht im deutschen Audible-Katalog (ohne Login/Key erreichbare Katalog-API).
     * Liefert Titel, Autor, Sprecher, Serie, Band-Nummer, Klappentext und quadratisches HD-Cover.
     */
    fun searchAudible(title: String, author: String): OnlineMetadataResult? {
        val cleanTitle = cleanQueryText(title)
        val cleanAuthor = cleanQueryText(author)

        // Segmentierung bei Bindestrichen prüfen (z. B. "Autor - Reihe - Titel")
        val segments = cleanTitle.split(Regex("""\s+[-–—]\s+""")).map { it.trim() }.filter { it.isNotBlank() }
        val authorCandidate = cleanAuthor.ifBlank { if (segments.size >= 2) segments.first() else "" }
        val titleCandidate = if (segments.size >= 2) segments.last() else cleanTitle

        // 1. Priorität: Autor + eigentlicher Buchtitel via Keywords (beste Audible-Trefferrate)
        if (authorCandidate.isNotBlank() && titleCandidate.isNotBlank()) {
            val res = queryAudibleEndpoint(keywords = "$authorCandidate $titleCandidate")
            if (res != null) return res
        }

        // 2. Priorität: Nur der eigentliche Buchtitel via Keywords
        if (titleCandidate.isNotBlank() && titleCandidate != cleanTitle) {
            val res = queryAudibleEndpoint(keywords = titleCandidate)
            if (res != null) return res
        }

        // 3. Priorität: Gesamter bereinigter Query-Text
        val fullQuery = if (cleanAuthor.isNotBlank()) "$cleanAuthor $cleanTitle" else cleanTitle
        val fullRes = queryAudibleEndpoint(keywords = fullQuery)
        if (fullRes != null) return fullRes

        // 4. Priorität: Direkter title & author Parameter
        if (cleanAuthor.isNotBlank()) {
            val strictRes = queryAudibleEndpoint(title = cleanTitle, author = cleanAuthor)
            if (strictRes != null) return strictRes
        }

        return null
    }

    private fun queryAudibleEndpoint(
        title: String? = null,
        author: String? = null,
        keywords: String? = null
    ): OnlineMetadataResult? {
        val queryKeywords = keywords ?: listOfNotNull(author, title).joinToString(" ")
        val results = queryAudibleProducts(queryKeywords, limit = 1)
        val first = results.firstOrNull() ?: return null

        var coverPath: String? = null
        if (!first.coverUrl.isNullOrBlank()) {
            val safeName = "audible_${(first.title ?: "cover").hashCode()}_${System.currentTimeMillis()}.jpg"
            coverPath = downloadImage(first.coverUrl, safeName)
        }
        return first.copy(coverPath = coverPath)
    }

    /**
     * Sucht mehrere Treffer bei Audible (DE) anhand eines Suchbegriffs.
     */
    fun queryAudibleProducts(keywords: String, limit: Int = 8): List<OnlineMetadataResult> {
        return try {
            val queryParams = mutableListOf<String>()
            queryParams.add("num_results=$limit")
            queryParams.add("products_sort_by=Relevance")
            queryParams.add("response_groups=product_desc,contributors,series,media,product_attrs")
            queryParams.add("keywords=${URLEncoder.encode(keywords, "UTF-8")}")

            val url = "https://api.audible.de/1.0/catalog/products?" + queryParams.joinToString("&")
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android; Mobile)")
            }

            if (conn.responseCode != 200) return emptyList()
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            parseAudibleProductsJson(response)
        } catch (e: Exception) {
            logWarn("OnlineCoverScraper", "Audible API Multi-Suche Fehler: ${e.message}")
            emptyList()
        }
    }

    private fun logWarn(tag: String, msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * Parst alle Produkte aus einer Audible-JSON-Antwort in eine Liste von OnlineMetadataResult.
     */
    fun parseAudibleProductsJson(jsonString: String): List<OnlineMetadataResult> {
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            val products = root["products"]?.jsonArray ?: return emptyList()
            val list = mutableListOf<OnlineMetadataResult>()

            for (elem in products) {
                val product = elem.jsonObject
                val resTitle = product["title"]?.jsonPrimitive?.content ?: continue

                // Autoren
                val authorsArray = product["authors"]?.jsonArray
                val resAuthor = authorsArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
                    ?.joinToString(", ")

                // Sprecher (Narrator)
                val narratorsArray = product["narrators"]?.jsonArray
                val resNarrator = narratorsArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
                    ?.joinToString(", ")

                // Serien & Band-Nummer
                val seriesArray = product["series"]?.jsonArray
                val firstSeries = seriesArray?.firstOrNull()?.jsonObject
                val resSeries = firstSeries?.get("title")?.jsonPrimitive?.content
                val resSeriesOrder = firstSeries?.get("sequence")?.jsonPrimitive?.content?.toIntOrNull()

                // Klappentext / Beschreibung
                val rawSummary = product["merchandising_summary"]?.jsonPrimitive?.content
                val resDescription = if (!rawSummary.isNullOrBlank()) cleanHtml(rawSummary) else null

                // Cover Bild
                val productImages = product["product_images"]?.jsonObject
                var coverUrl = productImages?.get("500")?.jsonPrimitive?.content
                if (coverUrl != null && coverUrl.contains("_SL500_")) {
                    coverUrl = coverUrl.replace("_SL500_", "_SL1000_")
                }

                val publisher = product["publisher_name"]?.jsonPrimitive?.content
                val releaseDate = product["release_date"]?.jsonPrimitive?.content

                list.add(
                    OnlineMetadataResult(
                        title = resTitle,
                        author = resAuthor?.ifBlank { null },
                        narrator = resNarrator,
                        series = resSeries,
                        seriesOrder = resSeriesOrder,
                        coverUrl = coverUrl,
                        description = resDescription,
                        providerName = "Audible (DE)",
                        publisher = publisher,
                        releaseDate = releaseDate
                    )
                )
            }
            list
        } catch (e: Throwable) {
            logWarn("OnlineCoverScraper", "Fehler beim Parsen der Audible-Produkte: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parst die JSON-Antwort von Audible (als eigene Methode auch für Unit-Tests isolierbar).
     */
    fun parseAudibleJson(
        jsonString: String,
        fallbackTitle: String,
        fallbackAuthor: String,
        downloadCover: Boolean = true
    ): OnlineMetadataResult? {
        val list = parseAudibleProductsJson(jsonString)
        val first = list.firstOrNull() ?: return null
        var coverPath: String? = null
        if (downloadCover && !first.coverUrl.isNullOrBlank()) {
            val safeName = "audible_${(first.title ?: fallbackTitle).hashCode()}_${System.currentTimeMillis()}.jpg"
            coverPath = downloadImage(first.coverUrl, safeName)
        }
        return first.copy(
            title = first.title ?: fallbackTitle,
            author = first.author ?: fallbackAuthor.ifBlank { null },
            coverPath = coverPath
        )
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
        val query = if (author.isNotBlank()) "$title $author" else title
        val results = queryITunesProducts(query, limit = 1)
        val first = results.firstOrNull() ?: return null

        var coverPath: String? = null
        if (!first.coverUrl.isNullOrBlank()) {
            coverPath = downloadImage(first.coverUrl, "itunes_${(first.title ?: title).hashCode()}.jpg")
        }
        return first.copy(coverPath = coverPath)
    }

    /**
     * Sucht mehrere Treffer bei iTunes Audiobooks anhand eines Suchbegriffs.
     */
    fun queryITunesProducts(query: String, limit: Int = 5): List<OnlineMetadataResult> {
        return try {
            val url = "https://itunes.apple.com/search?media=audiobook&entity=audiobook&country=de&term=${URLEncoder.encode(query, "UTF-8")}&limit=$limit"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "ABookPlayer/1.0 (Android)")
            }

            if (conn.responseCode != 200) return emptyList()
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            parseITunesProductsJson(response)
        } catch (e: Exception) {
            logWarn("OnlineCoverScraper", "iTunes API Fehler: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parst die JSON-Antwort von iTunes in eine Liste von OnlineMetadataResult.
     */
    fun parseITunesProductsJson(jsonString: String): List<OnlineMetadataResult> {
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            val resultsArray = root["results"]?.jsonArray ?: return emptyList()
            val list = mutableListOf<OnlineMetadataResult>()

            for (elem in resultsArray) {
                val item = elem.jsonObject
                val resTitle = item["collectionName"]?.jsonPrimitive?.content ?: item["trackName"]?.jsonPrimitive?.content ?: continue
                val resAuthor = item["artistName"]?.jsonPrimitive?.content
                val rawCoverUrl = item["artworkUrl100"]?.jsonPrimitive?.content ?: item["artworkUrl60"]?.jsonPrimitive?.content
                val description = item["description"]?.jsonPrimitive?.content
                    ?: item["longDescription"]?.jsonPrimitive?.content
                    ?: item["shortDescription"]?.jsonPrimitive?.content

                val highResCoverUrl = rawCoverUrl?.replace("100x100bb", "600x600bb")?.replace("60x60bb", "600x600bb")

                list.add(
                    OnlineMetadataResult(
                        title = resTitle,
                        author = resAuthor,
                        coverUrl = highResCoverUrl,
                        description = description,
                        providerName = "iTunes"
                    )
                )
            }
            list
        } catch (e: Throwable) {
            logWarn("OnlineCoverScraper", "iTunes JSON Parsing Fehler: ${e.message}")
            emptyList()
        }
    }

    /**
     * Sucht online nach passenden Kandidaten über mehrere Provider (Audible DE, iTunes).
     * Gibt eine Liste mit Treffern zurück, inklusive Cover-URL für die UI-Vorschau.
     */
    suspend fun searchCandidates(
        rawTitle: String,
        rawAuthor: String = "",
        limit: Int = 10
    ): List<OnlineMetadataResult> = withContext(Dispatchers.IO) {
        val cleanTitle = cleanQueryText(rawTitle)
        val cleanAuthor = cleanQueryText(rawAuthor)

        val query = if (cleanAuthor.isNotBlank() && !cleanTitle.contains(cleanAuthor, ignoreCase = true)) {
            "$cleanAuthor $cleanTitle"
        } else {
            cleanTitle
        }
        searchCandidatesForQuery(query, limit)
    }

    /**
     * Sucht anhand eines Freitext-Suchbegriffs bei Audible und iTunes.
     */
    suspend fun searchCandidatesForQuery(
        queryText: String,
        limit: Int = 10
    ): List<OnlineMetadataResult> = withContext(Dispatchers.IO) {
        val clean = cleanQueryText(queryText)
        if (clean.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineMetadataResult>()

        // 1. Audible DE durchsuchen
        val audibleResults = queryAudibleProducts(clean, limit = limit)
        results.addAll(audibleResults)

        // Falls Audible wenige Ergebnisse liefert und Trennstriche vorhanden sind
        val segments = clean.split(Regex("""\s+[-–—]\s+""")).map { it.trim() }.filter { it.isNotBlank() }
        if (results.size < 3 && segments.size >= 2) {
            val segmentQuery = "${segments.first()} ${segments.last()}"
            if (segmentQuery != clean) {
                val extraAudible = queryAudibleProducts(segmentQuery, limit = limit)
                for (item in extraAudible) {
                    if (results.none { it.title.equals(item.title, ignoreCase = true) }) {
                        results.add(item)
                    }
                }
            }
        }

        // 2. iTunes DE durchsuchen
        val itunesResults = queryITunesProducts(clean, limit = 5)
        for (item in itunesResults) {
            if (results.none { it.title.equals(item.title, ignoreCase = true) }) {
                results.add(item)
            }
        }

        results.take(limit)
    }

    /**
     * Lädt das Cover eines ausgewählten Kandidaten herunter und speichert es lokal ab.
     */
    suspend fun downloadSelectedCover(coverUrl: String, title: String): String? = withContext(Dispatchers.IO) {
        if (coverUrl.isBlank()) return@withContext null
        val safeTitle = title.take(20).replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "online_${safeTitle}_${System.currentTimeMillis()}.jpg"
        downloadImage(coverUrl, fileName)
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

    internal fun downloadImage(imageUrl: String, fileName: String): String? {
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

