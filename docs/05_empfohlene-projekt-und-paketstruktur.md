# 5. Empfohlene Projekt- und Paketstruktur

```text
app/src/main/java/de/f_soft_studio/abookplayer/
├── ABookApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/              # Room: Entity, DAO, Database, Migrationen
│   ├── repository/         # Repository-Implementierungen
│   ├── storage/            # SAF, Ordnerscan, URI-Verwaltung
│   └── abook/              # ZIP-Validierung, Manifest, Extraktion
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── playback/
│   ├── PlaybackService.kt
│   ├── PlayerController.kt
│   └── MediaItemFactory.kt
├── presentation/
│   ├── library/
│   ├── details/
│   ├── player/
│   ├── chapters/
│   ├── bookmarks/
│   ├── settings/
│   └── common/
├── navigation/
├── di/
└── ui/theme/
```

## 5.1 Namenskonventionen

- Screens: LibraryScreen, PlayerScreen, AudiobookDetailsScreen.

- ViewModels: LibraryViewModel, PlayerViewModel.

- Room: AudiobookEntity, ChapterEntity, BookmarkEntity, PlaybackProgressEntity.

- UseCases als Tätigkeit benennen, z. B. ImportAudiobookUseCase oder SaveProgressUseCase.

- Repository-Interfaces in domain, Implementierungen in data.

- Deutsche Texte ausschließlich aus string resources; keine fest codierten UI-Texte.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](04_architektur.md)
 · [Nächstes Kapitel →](06_datenmodell.md)
