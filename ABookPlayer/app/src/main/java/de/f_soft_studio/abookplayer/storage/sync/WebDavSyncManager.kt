package de.f_soft_studio.abookplayer.storage.sync

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.UUID

/**
 * WebDavSyncManager: Verwaltet die WebDAV-Verbindung zu Nextcloud, ownCloud oder benutzerdefinierten WebDAV-Servern
 * zum Hochladen und Herunterladen der abook_sync_state.json.
 */
class WebDavSyncManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "").orEmpty()
        set(value) {
            val formatted = value.trim().let { if (it.isNotEmpty() && !it.endsWith("/")) "$it/" else it }
            prefs.edit().putString(KEY_SERVER_URL, formatted).apply()
        }

    var username: String
        get() = prefs.getString(KEY_USERNAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_USERNAME, value.trim()).apply()

    var passwordToken: String
        get() = prefs.getString(KEY_PASSWORD_TOKEN, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_PASSWORD_TOKEN, value).apply()

    var isAutoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    val deviceId: String
        get() {
            var id = prefs.getString(KEY_DEVICE_ID, null)
            if (id.isNullOrBlank()) {
                id = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            }
            return id
        }

    val isConfigured: Boolean
        get() = serverUrl.isNotBlank() && username.isNotBlank() && passwordToken.isNotBlank()

    /**
     * Prüft die WebDAV-Verbindung durch Ausführen eines PROPFIND/HEAD/GET Requests auf dem Zielordner.
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalArgumentException("WebDAV-Zugangsdaten sind unvollständig."))
        }

        try {
            val targetUrl = getSyncFileUrl()
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Authorization", getAuthHeader())
                setRequestProperty("User-Agent", "ABookPlayer-Android/1.2.0")
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode in 200..299 || responseCode == 404) {
                // 200 (Datei existiert) oder 404 (Datei existiert noch nicht, aber Server/Auth OK)
                Result.success(true)
            } else if (responseCode == 401 || responseCode == 403) {
                Result.failure(IllegalStateException("Zugriff verweigert (HTTP $responseCode). Bitte Benutzername/Passwort prüfen."))
            } else {
                Result.failure(IllegalStateException("Server antwortete mit HTTP $responseCode."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lädt den aktuellen Sync-Zustand vom WebDAV-Server herunter.
     */
    suspend fun downloadSyncState(): Result<SyncStateDto?> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalArgumentException("WebDAV ist nicht konfiguriert."))
        }

        try {
            val targetUrl = getSyncFileUrl()
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Authorization", getAuthHeader())
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ABookPlayer-Android/1.2.0")
            }

            val responseCode = connection.responseCode
            if (responseCode == 404) {
                connection.disconnect()
                return@withContext Result.success(null) // Noch kein Sync-Zustand vorhanden
            }

            if (responseCode !in 200..299) {
                connection.disconnect()
                return@withContext Result.failure(IllegalStateException("Download fehlgeschlagen (HTTP $responseCode)"))
            }

            val content = connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            }
            connection.disconnect()

            val state = json.decodeFromString<SyncStateDto>(content)
            Result.success(state)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lädt den neuen Sync-Zustand auf den WebDAV-Server hoch.
     */
    suspend fun uploadSyncState(state: SyncStateDto): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalArgumentException("WebDAV ist nicht konfiguriert."))
        }

        try {
            val targetUrl = getSyncFileUrl()
            val jsonText = json.encodeToString(SyncStateDto.serializer(), state)
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Authorization", getAuthHeader())
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("User-Agent", "ABookPlayer-Android/1.2.0")
            }

            connection.outputStream.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonText)
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode in 200..299) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Upload fehlgeschlagen (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSyncFileUrl(): String {
        val base = serverUrl.trimEnd('/')
        return "$base/$SYNC_FILENAME"
    }

    private fun getAuthHeader(): String {
        val credentials = "$username:$passwordToken"
        val encoded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
        } else {
            android.util.Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        }
        return "Basic $encoded"
    }

    companion object {
        private const val PREFS_NAME = "webdav_sync_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD_TOKEN = "password_token"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_DEVICE_ID = "device_id"
        private const val SYNC_FILENAME = "abook_sync_state.json"
    }
}
