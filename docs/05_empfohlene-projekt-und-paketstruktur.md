# 5. Projekt- und Paketstruktur

Ist-Zustand unter `app/src/main/java/de/f_soft_studio/abookplayer/` (Gradle-Root: `ABookPlayer/`).

```text
de/f_soft_studio/abookplayer/
├── MainActivity.kt              # Einstieg, manuelle Verdrahtung (keine Application-Klasse)
├── data/
│   ├── local/
│   │   ├── entity/             # AudiobookEntity, ChapterEntity, BookmarkEntity,
│   │   │                       #   ListeningSessionEntity, CharacterEntity (+ Mapper)
│   │   ├── dao/                # AudiobookDao, ChapterDao, BookmarkDao,
│   │   │                       #   ListeningSessionDao, CharacterDao
│   │   └── db/                 # AbookDatabase (Version 11, Migrationen 3→11)
│   └── repository/             # AudiobookRepository (keine separaten Interfaces)
├── domain/
│   ├── model/                 # Audiobook, Chapter, Bookmark, BookCharacter,
│   │                          #   ListeningStatistics, ExportState
│   └── usecase/               # GetAudiobooks, GetAudiobookDetails, ImportAudiobook,
│                              #   SaveProgress, AddBookmark, DeleteAudiobook,
│                              #   ExportAudiobook, GetListeningStatistics,
│                              #   RecordListeningTime, SyncProgress
├── storage/
│   ├── AbookStorage.kt        # .abook-/ZIP-Parsing, SAF-Import, Ordner-Scan-Orchestrierung, Export
│   ├── FolderScanner.kt       # rekursiver Audio-Scan, CD1/CD2, isAudioFile
│   ├── LibraryLocationManager.kt  # Bibliotheksordner + .abooklib (DB/Cover/Metadaten)
│   ├── OnlineCoverScraper.kt / OpenLibraryScraper.kt  # Cover-/Metadatensuche
│   └── sync/                  # WebDavSyncManager, SyncDataModels (Fortschritts-Sync)
├── player/
│   ├── controller/            # PlaybackController, SleepTimerController, LoudnessController
│   └── service/               # AbookPlaybackService (MediaLibraryService, Foreground)
├── util/                      # DuplicateDetector, AudiobookMetadataText, PlayableMedia,
│                              #   ChapterDurations, CoverHelper, ShakeDetector, AbookModels
├── ui/
│   ├── AppRoot.kt             # NavHost + String-Routen, ViewModel-Konstruktion
│   ├── library/  player/  details/  chapters/  bookmarks/  characters/
│   ├── info/  statistics/  settings/  sleep/  car/  common/
│   └── theme/
└── widget/                    # AbookWidgetProvider, AbookBannerWidgetProvider
```

## 5.1 Namenskonventionen

- Screens: `LibraryScreen`, `PlayerScreen`, `DetailsScreen`, `ChaptersScreen`, `CharactersScreen`, `StatisticsScreen`, `EbookInfoScreen`, `SettingsScreen`.
- ViewModels: `LibraryViewModel`, `PlayerViewModel`, `CharactersViewModel`, `StatisticsViewModel`, `SettingsViewModel`.
- Room-Entities: `AudiobookEntity`, `ChapterEntity`, `BookmarkEntity`, `ListeningSessionEntity`, `CharacterEntity`. Der Hörfortschritt liegt als Spalte `currentPosition` **in** `AudiobookEntity` (keine eigene Progress-Entity).
- UseCases als Tätigkeit benennen (`ImportAudiobookUseCase`, `SaveProgressUseCase`, …).
- `domain/` bleibt frei von Android-Imports; `AudiobookRepository` ist eine konkrete Klasse ohne separates Interface.
- Deutsche UI-Texte ausschließlich aus String-Ressourcen; keine fest codierten UI-Texte.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](04_architektur.md)
 · [Nächstes Kapitel →](06_datenmodell.md)
