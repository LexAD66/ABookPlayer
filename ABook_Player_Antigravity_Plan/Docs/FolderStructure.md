# 📁 FolderStructure.md

## Vollständige Projektstruktur – ABook Player

```
ABookPlayer/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   │
│   │   │   └── kotlin/com/example/audiobookplayer/
│   │   │       │
│   │   │       ├── AudiobookPlayerApp.kt          # Application-Klasse (Hilt)
│   │   │       ├── MainActivity.kt                # Einstiegspunkt, NavHost
│   │   │       │
│   │   │       ├── data/
│   │   │       │   │
│   │   │       │   ├── abook/
│   │   │       │   │   ├── archive/
│   │   │       │   │   │   ├── AbookArchiveReader.kt      # ZIP öffnen, Dateien extrahieren
│   │   │       │   │   │   └── AbookArchiveValidator.kt   # Struktur validieren, ZIP-Slip-Schutz
│   │   │       │   │   │
│   │   │       │   │   ├── parser/
│   │   │       │   │   │   ├── AbookManifestParser.kt         # Interface
│   │   │       │   │   │   ├── AbookXmlManifestParser.kt      # XML-Implementierung
│   │   │       │   │   │   └── AbookJsonManifestParser.kt     # JSON-Implementierung
│   │   │       │   │   │
│   │   │       │   │   └── importer/
│   │   │       │   │       └── AbookImporter.kt           # Vollständiger Import-Workflow
│   │   │       │   │
│   │   │       │   ├── database/
│   │   │       │   │   ├── AppDatabase.kt                 # Room-Datenbank-Definition
│   │   │       │   │   │
│   │   │       │   │   ├── entities/
│   │   │       │   │   │   ├── AudiobookEntity.kt         # Hörbuch-Tabelle
│   │   │       │   │   │   ├── TrackEntity.kt             # Track/Kapitel-Tabelle
│   │   │       │   │   │   ├── BookmarkEntity.kt          # Lesezeichen-Tabelle
│   │   │       │   │   │   ├── PlaybackStateEntity.kt     # Wiedergabestand-Tabelle
│   │   │       │   │   │   └── ImportSourceEntity.kt      # Import-Herkunft-Tabelle
│   │   │       │   │   │
│   │   │       │   │   └── dao/
│   │   │       │   │       ├── AudiobookDao.kt            # CRUD für Hörbücher
│   │   │       │   │       ├── TrackDao.kt                # CRUD für Tracks
│   │   │       │   │       ├── BookmarkDao.kt             # CRUD für Lesezeichen
│   │   │       │   │       └── PlaybackStateDao.kt        # CRUD für Wiedergabestand
│   │   │       │   │
│   │   │       │   ├── repository/
│   │   │       │   │   ├── AudiobookRepositoryImpl.kt     # Implementierung AudiobookRepository
│   │   │       │   │   ├── PlaybackRepositoryImpl.kt      # Implementierung PlaybackRepository
│   │   │       │   │   └── BookmarkRepositoryImpl.kt      # Implementierung BookmarkRepository
│   │   │       │   │
│   │   │       │   ├── scanner/
│   │   │       │   │   ├── FolderScanner.kt               # Ordner nach Hörbüchern durchsuchen
│   │   │       │   │   └── MetadataExtractor.kt           # ID3/M4B-Metadaten auslesen
│   │   │       │   │
│   │   │       │   ├── player/
│   │   │       │   │   └── AudioPlayerManager.kt          # ExoPlayer-Verwaltung
│   │   │       │   │
│   │   │       │   └── storage/
│   │   │       │       ├── DocumentAccessHelper.kt        # SAF (Storage Access Framework)
│   │   │       │       └── AppStorageManager.kt           # App-interner Speicher
│   │   │       │
│   │   │       ├── domain/
│   │   │       │   │
│   │   │       │   ├── model/
│   │   │       │   │   ├── Audiobook.kt                   # Domain-Modell Hörbuch
│   │   │       │   │   ├── Track.kt                       # Domain-Modell Track/Kapitel
│   │   │       │   │   ├── Bookmark.kt                    # Domain-Modell Lesezeichen
│   │   │       │   │   ├── PlaybackState.kt               # Domain-Modell Wiedergabestand
│   │   │       │   │   ├── ImportResult.kt                # Ergebnis eines Imports
│   │   │       │   │   ├── AbookManifest.kt               # Geparster Manifest-Inhalt
│   │   │       │   │   ├── AbookMetadata.kt               # Metadaten aus dem Manifest
│   │   │       │   │   ├── AbookTrack.kt                  # Track-Eintrag aus Manifest
│   │   │       │   │   └── ArchiveValidationResult.kt     # Ergebnis der Archiv-Validierung
│   │   │       │   │
│   │   │       │   ├── repository/
│   │   │       │   │   ├── AudiobookRepository.kt         # Interface
│   │   │       │   │   ├── PlaybackRepository.kt          # Interface
│   │   │       │   │   └── BookmarkRepository.kt          # Interface
│   │   │       │   │
│   │   │       │   └── usecase/
│   │   │       │       ├── GetLibraryUseCase.kt           # Alle Hörbücher laden
│   │   │       │       ├── GetAudiobookByIdUseCase.kt     # Einzelnes Hörbuch laden
│   │   │       │       ├── ImportAbookUseCase.kt          # .abook importieren
│   │   │       │       ├── ScanFolderUseCase.kt           # Ordner scannen
│   │   │       │       ├── ValidateAbookUseCase.kt        # Archiv validieren
│   │   │       │       ├── GetPlaybackStateUseCase.kt     # Letzten Stand laden
│   │   │       │       ├── SavePlaybackStateUseCase.kt    # Stand speichern
│   │   │       │       ├── GetLastPlayedBookUseCase.kt    # Zuletzt gehörtes Buch
│   │   │       │       ├── AddBookmarkUseCase.kt          # Lesezeichen hinzufügen
│   │   │       │       ├── GetBookmarksUseCase.kt         # Lesezeichen laden
│   │   │       │       ├── DeleteBookmarkUseCase.kt       # Lesezeichen löschen
│   │   │       │       └── UpdateBookmarkUseCase.kt       # Lesezeichen bearbeiten
│   │   │       │
│   │   │       ├── presentation/
│   │   │       │   │
│   │   │       │   ├── theme/
│   │   │       │   │   ├── Theme.kt                   # Material-3-Theme und visuelle Rollen
│   │   │       │   │   ├── Color.kt                   # Bibliotheks-, Stage- und neutrale Tokens
│   │   │       │   │   ├── Typography.kt              # Schriftgrößen und Lesbarkeit
│   │   │       │   │   └── Shape.kt                   # Formen für Karten, Sheets und Controls
│   │   │       │   │
│   │   │       │   ├── home/
│   │   │       │   │   ├── HomeScreen.kt                  # Home-Screen
│   │   │       │   │   ├── HomeViewModel.kt               # ViewModel Home
│   │   │       │   │   └── HomeUiState.kt                 # UI-Zustand Home
│   │   │       │   │
│   │   │       │   ├── library/
│   │   │       │   │   ├── LibraryScreen.kt               # Bibliotheks-Screen
│   │   │       │   │   ├── LibraryViewModel.kt            # ViewModel Bibliothek
│   │   │       │   │   ├── LibraryUiState.kt              # UI-Zustand Bibliothek
│   │   │       │   │   └── components/
│   │   │       │   │       ├── AudiobookListItem.kt       # Listen-Eintrag
│   │   │       │   │       ├── AudiobookGridItem.kt       # Grid-Eintrag
│   │   │       │   │       └── LibraryFilterBar.kt        # Filter-Tabs (Alle/Neu/etc.)
│   │   │       │   │
│   │   │       │   ├── player/
│   │   │       │   │   ├── PlayerScreen.kt                # Player-Screen
│   │   │       │   │   ├── PlayerViewModel.kt             # ViewModel Player
│   │   │       │   │   ├── PlayerUiState.kt               # UI-Zustand Player
│   │   │       │   │   └── components/
│   │   │       │   │       ├── CoverArtwork.kt            # Cover-Anzeige
│   │   │       │   │       ├── PlaybackControls.kt        # Buttons (Play/Pause/Skip)
│   │   │       │   │       ├── ProgressBar.kt             # Fortschrittsbalken
│   │   │       │   │       ├── ChapterSelector.kt         # Kapitel-Auswahl
│   │   │       │   │       ├── SpeedControl.kt            # Geschwindigkeits-Regler
│   │   │       │   │       └── SleepTimerDialog.kt        # Schlaf-Timer Dialog
│   │   │       │   │
│   │   │       │   ├── bookmarks/
│   │   │       │   │   ├── BookmarksScreen.kt             # Lesezeichen-Screen
│   │   │       │   │   ├── BookmarkViewModel.kt           # ViewModel Lesezeichen
│   │   │       │   │   ├── BookmarkUiState.kt             # UI-Zustand Lesezeichen
│   │   │       │   │   └── components/
│   │   │       │   │       ├── BookmarkListItem.kt        # Lesezeichen-Eintrag
│   │   │       │   │       └── AddBookmarkDialog.kt       # Dialog: Lesezeichen hinzufügen
│   │   │       │   │
│   │   │       │   ├── imports/
│   │   │       │   │   ├── ImportScreen.kt                # Import-Screen
│   │   │       │   │   ├── ImportViewModel.kt             # ViewModel Import
│   │   │       │   │   ├── ImportUiState.kt               # UI-Zustand Import
│   │   │       │   │   └── components/
│   │   │       │   │       ├── ImportProgressCard.kt      # Fortschrittsanzeige
│   │   │       │   │       └── ImportErrorCard.kt         # Fehleranzeige
│   │   │       │   │
│   │   │       │   ├── settings/
│   │   │       │   │   ├── SettingsScreen.kt              # Einstellungs-Screen
│   │   │       │   │   ├── SettingsViewModel.kt           # ViewModel Einstellungen
│   │   │       │   │   └── SettingsUiState.kt             # UI-Zustand Einstellungen
│   │   │       │   │
│   │   │       │   └── common/
│   │   │       │       ├── components/
│   │   │       │       │   ├── CoverImage.kt              # Wiederverwendbare Cover-Anzeige
│   │   │       │       │   ├── LoadingIndicator.kt        # Lade-Indikator
│   │   │       │       │   ├── ErrorMessage.kt            # Fehleranzeige-Komponente
│   │   │       │       │   └── ProgressIndicator.kt       # Fortschritts-Komponente
│   │   │       │       └── navigation/
│   │   │       │           ├── NavGraph.kt                # Navigations-Graph
│   │   │       │           └── Routes.kt                  # Routen-Definitionen
│   │   │       │
│   │   │       ├── service/
│   │   │       │   └── PlaybackService.kt                 # MediaSessionService (Hintergrundwiedergabe)
│   │   │       │
│   │   │       └── di/
│   │   │           ├── DatabaseModule.kt                  # Hilt: Room-Datenbank
│   │   │           ├── RepositoryModule.kt                # Hilt: Repositories
│   │   │           ├── PlayerModule.kt                    # Hilt: ExoPlayer
│   │   │           ├── AbookModule.kt                     # Hilt: .abook-Komponenten
│   │   │           └── ScannerModule.kt                   # Hilt: Scanner & Extraktor
│   │   │
│   │   ├── test/
│   │   │   └── kotlin/com/example/audiobookplayer/
│   │   │       ├── data/
│   │   │       │   ├── abook/
│   │   │       │   │   ├── AbookArchiveValidatorTest.kt   # Archiv-Validierungs-Tests
│   │   │       │   │   ├── AbookXmlManifestParserTest.kt  # XML-Parser-Tests
│   │   │       │   │   ├── AbookJsonManifestParserTest.kt # JSON-Parser-Tests
│   │   │       │   │   └── AbookImporterTest.kt           # Importer-Tests
│   │   │       │   └── repository/
│   │   │       │       ├── AudiobookRepositoryTest.kt     # Repository-Tests
│   │   │       │       └── PlaybackRepositoryTest.kt      # Wiedergabe-Repository-Tests
│   │   │       ├── domain/
│   │   │       │   └── usecase/
│   │   │       │       ├── ImportAbookUseCaseTest.kt      # Use-Case-Tests
│   │   │       │       └── ValidateAbookUseCaseTest.kt    # Validierungs-Tests
│   │   │       └── presentation/
│   │   │           ├── LibraryViewModelTest.kt            # ViewModel-Tests
│   │   │           └── PlaybackViewModelTest.kt           # Player-ViewModel-Tests
│   │   │
│   │   └── androidTest/
│   │       └── kotlin/com/example/audiobookplayer/
│   │           ├── LibraryScreenTest.kt                   # UI-Tests Bibliothek
│   │           └── PlayerScreenTest.kt                    # UI-Tests Player
│   │
│   ├── build.gradle.kts                                   # App-Modul Build
│   └── proguard-rules.pro
│
├── docs/
│   ├── README.md                                          # Projektübersicht
│   ├── Architecture.md                                    # Architektur-Dokumentation
│   ├── FolderStructure.md                                 # Diese Datei
│   ├── FeatureList.md                                     # Feature-Liste
│   ├── VisualConcept.md                                   # Verbindliches UI-/UX-Zielbild
│   ├── ABOOK_FORMAT.md                                    # .abook Formatspezifikation
│   └── ImportFlow.md                                      # Import-Workflow
│
├── build.gradle.kts                                       # Projekt-Build
├── settings.gradle.kts                                    # Projekt-Einstellungen
├── gradle.properties                                      # Gradle-Eigenschaften
└── gradle/
    └── libs.versions.toml                                 # Version Catalog
```

---

## Paketstruktur – Begründung

### `data/abook/`
Das `.abook`-Subsystem ist bewusst in drei Untermodule unterteilt:
- **`archive/`**: Technische ZIP-Operationen, sicherheitskritisch (ZIP-Slip-Schutz)
- **`parser/`**: Reine Daten-Transformation, leicht testbar, ohne Android-Abhängigkeiten
- **`importer/`**: Orchestriert den gesamten Workflow, nutzt archive + parser + repository

### `domain/model/`
Alle Domain-Modelle sind **data classes** ohne Android-Abhängigkeiten. Sie sind das einzige, was zwischen Domain und Presentation ausgetauscht wird.

### `presentation/common/`
Wiederverwendbare Compose-Komponenten und das Material-3-Theme werden hier zentral verwaltet und in allen Screens genutzt.

### `presentation/theme/`
Das Theme bildet die in [VisualConcept.md](VisualConcept.md) definierten Rollen ab:
- warmes Bibliotheks-System,
- dunkles Stage-System,
- neutrales Material-3-System.

Die Theme-Dateien dürfen keine weitere konkurrierende Farbwelt als finales Ziel einführen.

### `di/`
Alle Hilt-Module sind nach ihrem Verantwortungsbereich benannt. Ein Modul pro Themengebiet verhindert zu große, schwer wartbare DI-Konfigurationsdateien.

---

## Namenskonventionen

| Typ | Konvention | Beispiel |
|---|---|---|
| Kotlin-Dateien | PascalCase | `AudiobookEntity.kt` |
| Klassen | PascalCase | `AbookImporter` |
| Funktionen / Variablen | camelCase | `importAbook()` |
| Konstanten | SCREAMING_SNAKE_CASE | `MANIFEST_FILE_NAME` |
| Compose-Funktionen | PascalCase | `LibraryScreen()` |
| Room-Tabellen | snake_case | `audiobooks`, `playback_states` |
| Ressourcen-IDs | snake_case | `ic_play_arrow` |
