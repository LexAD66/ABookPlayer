# 4. Architektur

Die App verwendet eine pragmatische Schichtentrennung. Abhängigkeiten zeigen nach innen: UI kennt ViewModels und Domain-Modelle, die Domain kennt keine Android-Details, und die Data-/Storage-Schicht kapselt Room, SAF und `.abook`-Verarbeitung.

| Schicht (Paket) | Verantwortung | Typische Bestandteile |
| --- | --- | --- |
| `ui/` | Darstellung, UI-Zustand, Benutzerereignisse | Compose Screens, Komponenten, `*UiState`, ViewModels, `AppRoot` (Navigation) |
| `domain/` | Geschäftsregeln und stabile Modelle | `domain/model/`, `domain/usecase/` (ohne Android-Abhängigkeiten) |
| `data/` | Persistenz | Room `entity/`, `dao/`, `db/AbookDatabase`, `repository/AudiobookRepository` |
| `storage/` | Datei- und Ordnerverarbeitung | `AbookStorage`, `FolderScanner`, `LibraryLocationManager`, Cover-Scraper, `storage/sync/` (WebDAV) |
| `player/` | Wiedergabe unabhängig von Screens | `player/controller/` (`PlaybackController`, `SleepTimerController`, `LoudnessController`), `player/service/AbookPlaybackService` |
| `util/` | Reine Hilfslogik | `DuplicateDetector`, `AudiobookMetadataText`, `PlayableMedia`, `ChapterDurations`, `CoverHelper`, `ShakeDetector` |
| `widget/` | Homescreen-Widgets | `AbookWidgetProvider`, `AbookBannerWidgetProvider` |

**Zusammenbau der Anwendung:** keine DI-Bibliothek. `MainActivity.onCreate` erzeugt `AbookDatabase.getInstance()`, `AudiobookRepository` und `PlaybackController.getInstance()`; `AppRoot` konstruiert die ViewModels mit `remember { … }`.

## 4.1 Datenfluss

```text
Compose UI
    -> ViewModel / UiState
        -> UseCase oder Repository / Storage
            -> Room / SAF / .abook / Media3 / WebDAV
```

```text
Player- und Datenereignisse
    -> Repository oder PlaybackController
        -> StateFlow / SharedFlow
            -> ViewModel
                -> Compose UI
```

## 4.2 Single Source of Truth

Room ist die verbindliche Quelle für Bibliothek, Kapitel, Fortschritt, Lesezeichen, Figuren und Statistik. Dateisystem und Archive liefern Importdaten; sie halten keinen dauerhaften UI-Zustand parallel zur Datenbank. Der Player besitzt nur den flüchtigen Wiedergabezustand und schreibt relevante Änderungen (Position, Speed) zurück in Room.

## 4.3 Fehlerbehandlung

| Fehler | UI-Reaktion | Technische Reaktion |
| --- | --- | --- |
| Keine abspielbare Datei (Pfad fehlt oder ist ein Verzeichnis) | Toast „Keine abspielbaren Audiodateien für … gefunden – bitte neu importieren." | `PlayableMedia.isPlayableFile` filtert Verzeichnisse aus; kein `play()`, `PlaybackController.playbackError` emittiert. |
| Berechtigung fehlt | Erklärung und Ordner erneut auswählen | Persistierte SAF-URI-Berechtigung erneuern. |
| Scan / Cleanup schlägt fehl | Snackbar mit Ursache | `LibraryViewModel` fängt `Exception` (nicht `CancellationException`), Re-Entrancy-Guard über `isScanning`/`isCleaning`. |
| Ungültiges `.abook` | Import abbrechen und Ursache anzeigen | Keine Teilimporte; ZIP-Slip-/Größenprüfung; temporäre Dateien bereinigen. |
| Verwaister DB-Eintrag (Datei/Ordner leer) | Wird beim „Aufräumen" entfernt | `cleanupDuplicatesAndOrphans`: Eintrag verwaist, wenn Pfad fehlt **oder** ein Verzeichnis ohne Audiodateien ist. |
| Datenbankmigration | Start ohne Datenverlust | Explizite Migrationen 3→11; `fallbackToDestructiveMigration()` nur als letzte Absicherung. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](03_technologiestack-und-entwicklungsumgebung.md)
 · [Nächstes Kapitel →](05_empfohlene-projekt-und-paketstruktur.md)
