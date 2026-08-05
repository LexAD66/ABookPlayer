# 🏗️ Architecture.md

## Architekturübersicht – ABook Player

ABook Player folgt dem Prinzip der **Clean Architecture** kombiniert mit dem **MVVM-Pattern** für die Präsentationsschicht. Ziel ist eine klare Trennung der Verantwortlichkeiten, hohe Testbarkeit und langfristige Wartbarkeit.

Das verbindliche visuelle Zielbild steht in [VisualConcept.md](VisualConcept.md). Architekturentscheidungen zur Presentation-Schicht müssen die dort definierten visuellen Rollen, deutschen UI-Texte und Navigationsregeln respektieren.

---

## Schichtenmodell

```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│   Compose Screens · ViewModels · UI State · UI Events   │
└────────────────────────┬────────────────────────────────┘
                         │ ruft auf
┌────────────────────────▼────────────────────────────────┐
│                    DOMAIN LAYER                          │
│     Use Cases · Domain Models · Repository Interfaces   │
│          Parser Contracts · Validation Logic            │
└────────────────────────┬────────────────────────────────┘
                         │ implementiert
┌────────────────────────▼────────────────────────────────┐
│                     DATA LAYER                           │
│  Room DB · ExoPlayer · .abook Importer · File Scanner  │
│    Repository Impl · Metadata Extractor · Storage      │
└─────────────────────────────────────────────────────────┘
```

**Abhängigkeitsregel:** Jede Schicht darf nur die nächst tiefere Schicht kennen. Die Domain-Schicht ist vollständig unabhängig von Android-Klassen.

---

## 1. Presentation Layer

### Verantwortlichkeiten
- Anzeige der UI mit Jetpack Compose
- Verwaltung von UI-Zustand über `StateFlow` / `UiState`-Klassen
- Weiterleitung von Benutzerinteraktionen an ViewModels
- Reaktion auf UI-Events

### Hauptkomponenten

| Komponente | Beschreibung |
|---|---|
| `HomeScreen` | Zuletzt gehört, Weitermachen, Schnellzugriff |
| `LibraryScreen` | Hörbuch-Liste/Grid mit Filtern |
| `PlayerScreen` | Haupt-Player mit Controls |
| `BookmarksScreen` | Lesezeichenverwaltung |
| `ImportScreen` | Datei- und .abook-Import |
| `SettingsScreen` | App-Einstellungen |

### Visuelle Rollen der Presentation Layer

| Rolle | Screens | Theme-Verantwortung |
|---|---|---|
| Warmes Bibliotheks-System | Home, Bibliothek, Hörbuchdetails, Import, Lesezeichen | Lesbare Listen, Coverkarten, Metadaten, Notizen |
| Dunkles Stage-System | Player, Kapitel-Auswahl im Player, Schlaf-Timer, Hörstatistik | Cover-Fokus, Fortschritt, große Steuerung, Nacht-Nutzung |
| Neutrales Material-3-System | Einstellungen, systemnahe Dialoge | Funktionale Konfiguration |

### ViewModels

| ViewModel | Zuständig für |
|---|---|
| `HomeViewModel` | Letzte Hörbücher, Fortschritt |
| `LibraryViewModel` | Bibliotheksliste, Filter, Sortierung |
| `PlaybackViewModel` | Wiedergabestatus, Controls, Timer |
| `BookmarkViewModel` | Lesezeichen CRUD |
| `ImportViewModel` | Import-Workflow, Fortschritt, Fehler |
| `SettingsViewModel` | Einstellungen lesen/schreiben |

### UI State Pattern

```kotlin
// Beispiel UI-State-Klasse
data class LibraryUiState(
    val audiobooks: List<AudiobookUi> = emptyList(),
    val isLoading: Boolean = false,
    val filter: LibraryFilter = LibraryFilter.ALL,
    val error: String? = null
)

sealed class LibraryUiEvent {
    data class OpenBook(val id: Long) : LibraryUiEvent()
    data object RefreshLibrary : LibraryUiEvent()
    data class DeleteBook(val id: Long) : LibraryUiEvent()
}
```

### Navigation

Navigation erfolgt über **Jetpack Navigation Compose** mit einem zentralen `NavHost` und typisierten Routen:

```
NavGraph:
  library/        → LibraryScreen
  player/{bookId} → PlayerScreen
  settings/       → SettingsScreen
  book/{bookId}   → AudiobookDetailScreen
  chapters/{bookId} → ChapterScreen oder Player-Bottom-Sheet
  bookmarks/{bookId} → BookmarksScreen
  import/         → ImportScreen
```

Wenn eine permanente App-Shell verwendet wird, enthält die Bottom Navigation nur die Hauptziele **Bibliothek**, **Wiedergabe** und **Einstellungen**. Hörbuchdetails, Kapitel, Lesezeichen und Import sind Kontextziele und werden nicht als gleichwertige Haupttabs dargestellt.

---

## 2. Domain Layer

Die Domain-Schicht enthält **keine Android-Abhängigkeiten** und kann vollständig mit JUnit getestet werden.

### Domain Models

| Modell | Felder (Auswahl) |
|---|---|
| `Audiobook` | id, title, author, narrator, coverPath, chapters, source, progress |
| `Chapter` / `Track` | id, title, filePath, durationMs, position |
| `Bookmark` | id, bookId, positionMs, chapterIndex, note, createdAt |
| `PlaybackState` | bookId, positionMs, chapterIndex, speed, timestamp |
| `ImportResult` | success, audiobook?, error? |
| `AbookManifest` | version, id, title, author, tracks, coverPath |

### Repository Interfaces (Domain)

```kotlin
interface AudiobookRepository {
    fun getAllAudiobooks(): Flow<List<Audiobook>>
    suspend fun getAudiobookById(id: Long): Audiobook?
    suspend fun saveAudiobook(audiobook: Audiobook): Long
    suspend fun deleteAudiobook(id: Long)
    suspend fun updateProgress(id: Long, positionMs: Long, chapterIndex: Int)
}

interface PlaybackRepository {
    suspend fun getPlaybackState(bookId: Long): PlaybackState?
    suspend fun savePlaybackState(state: PlaybackState)
    suspend fun getLastPlayedBook(): Audiobook?
}

interface BookmarkRepository {
    fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>>
    suspend fun addBookmark(bookmark: Bookmark): Long
    suspend fun deleteBookmark(id: Long)
    suspend fun updateBookmark(bookmark: Bookmark)
}
```

### Use Cases

Jeder Use Case kapselt **einen** Anwendungsfall und wird in ViewModels injiziert:

| Use Case | Beschreibung |
|---|---|
| `GetLibraryUseCase` | Alle Hörbücher laden |
| `ImportAbookUseCase` | .abook-Archiv importieren |
| `ScanFolderUseCase` | Ordner nach Hörbüchern durchsuchen |
| `GetPlaybackStateUseCase` | Letzten Wiedergabestand laden |
| `SavePlaybackStateUseCase` | Wiedergabestand speichern |
| `AddBookmarkUseCase` | Lesezeichen hinzufügen |
| `GetBookmarksUseCase` | Lesezeichen eines Buchs laden |
| `ValidateAbookUseCase` | .abook-Archiv validieren |

---

## 3. Data Layer

### Untermodule

#### 3a. Room-Datenbank

Datenbankname: `audiobookplayer.db`

**Entitäten:**

| Entität | Tabelle | Beschreibung |
|---|---|---|
| `AudiobookEntity` | `audiobooks` | Hörbuch-Metadaten |
| `TrackEntity` | `tracks` | Einzelne Tracks/Kapitel |
| `BookmarkEntity` | `bookmarks` | Gespeicherte Positionen |
| `PlaybackStateEntity` | `playback_states` | Letzter Wiedergabestand |
| `ImportSourceEntity` | `import_sources` | Herkunft des Imports |

**DAOs:**
- `AudiobookDao`
- `TrackDao`
- `BookmarkDao`
- `PlaybackStateDao`

#### 3b. .abook Subsystem

```
data/abook/
├── archive/
│   ├── AbookArchiveReader.kt      # ZIP öffnen, Dateien lesen
│   └── AbookArchiveValidator.kt   # Struktur und Sicherheit prüfen
├── parser/
│   ├── AbookManifestParser.kt     # Interface
│   ├── AbookXmlManifestParser.kt  # XML-Implementierung
│   └── AbookJsonManifestParser.kt # JSON-Implementierung
└── importer/
    └── AbookImporter.kt           # Vollständiger Import-Workflow
```

#### 3c. Medien-Scanner

`FolderScanner` durchsucht vom Nutzer gewählte Ordner (über SAF) nach unterstützten Audio-Dateien und gruppiert sie zu Hörbüchern.

`MetadataExtractor` liest ID3-Tags / M4B-Metadaten über `MediaMetadataRetriever`.

#### 3d. Player

`AudioPlayerManager` kapselt den ExoPlayer und kommuniziert über `MediaSession` mit dem `PlaybackService`.

```
AudioPlayerManager
    └── ExoPlayer (Media3)
          └── MediaSession
                └── PlaybackService (ForegroundService)
```

---

## 4. Service Layer

### PlaybackService (MediaSessionService)

- Erbt von `MediaSessionService` (Media3)
- Läuft als Vordergrund-Service
- Verwaltet `MediaSession` und `Player`
- Stellt Benachrichtigung mit Steuerungselementen bereit
- Reagiert auf Headset-Tasten und Bluetooth-Ereignisse

```kotlin
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
}
```

---

## 5. Dependency Injection (Hilt)

Hilt-Module organisieren die Bereitstellung von Abhängigkeiten:

| Modul | Bereitgestellte Objekte |
|---|---|
| `DatabaseModule` | `AppDatabase`, alle DAOs |
| `RepositoryModule` | Repository-Implementierungen |
| `PlayerModule` | `ExoPlayer`, `AudioPlayerManager` |
| `AbookModule` | `AbookArchiveReader`, `AbookImporter` |
| `ScannerModule` | `FolderScanner`, `MetadataExtractor` |

---

## 6. Datenflussprinzip

```
Benutzer-Aktion
    → Compose UI (Event)
        → ViewModel (verarbeitet Event)
            → Use Case (Business-Logik)
                → Repository Interface
                    → Repository Impl (Data Layer)
                        → Room DB / ExoPlayer / Dateisystem
                            → Flow<Result>
                                → Repository Interface
                            → Use Case
                        → Domain Model
                    → ViewModel (aktualisiert UiState)
                → StateFlow<UiState>
            → Compose UI (rekomponiert)
```

---

## 7. Fehlerbehandlung

- Alle Fehler werden als `Result<T>` oder `sealed class` modelliert
- Netzwerk- und Datei-Fehler werden in der Data-Schicht abgefangen
- ViewModels übersetzen Fehler in nutzerfreundliche deutsche Fehlermeldungen
- `.abook`-Importfehler werden detailliert protokolliert und angezeigt

```kotlin
sealed class ImportError {
    object KeinManifestGefunden : ImportError()
    object UngueltigesArchivformat : ImportError()
    data class AudiodateiNichtGefunden(val pfad: String) : ImportError()
    object CoverNichtGefunden : ImportError()
    data class AllgemeinerFehler(val meldung: String) : ImportError()
}
```

---

## 8. Testbarkeit

| Schicht | Testtyp | Framework |
|---|---|---|
| Domain (Use Cases, Models) | Unit Tests | JUnit 5, Kotlin Coroutines Test |
| Data (Repository, Parser) | Unit Tests | JUnit 5, MockK, Robolectric |
| ViewModel | Unit Tests | JUnit 5, Turbine (Flow) |
| UI (Screens) | UI Tests | Compose Testing, Espresso |
| Integration | Integrationstests | AndroidTest |

---

*Weitere Details zur Ordnerstruktur: [FolderStructure.md](FolderStructure.md)*
*Weitere Details zum .abook-Format: [ABOOK_FORMAT.md](ABOOK_FORMAT.md)*
