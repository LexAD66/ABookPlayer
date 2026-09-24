# ABook Player — Architektur- und Systemdokumentation

Diese Dokumentation beschreibt die vollständige Architektur, Datenflüsse, Modulgrenzen und Kernprinzipien des **ABook Player** (Version 2.0.0).

---

## 1. Leitphilosophie & Grundprinzipien

- **100% Free & Open Source (FOSS):** Keine proprietären Bibliotheken, keine Google Play Services (GMS), keine Werbe- oder Tracking-Frameworks. F-Droid-kompatibel.
- **Offline-First & Datenschutz:** Alle Hörbücher, Metadaten, Lesezeichen und Hörstatistiken werden ausschließlich lokal auf dem Endgerät in einer Room-SQLite-Datenbank verwaltet.
- **Clean Architecture & Pragmatismus:** Strikte Trennung von Geschäftslogik (`domain`), Datenzugriff (`data`), Dateisystem/SAF (`storage`), Wiedergabe (`player`) und Benutzeroberfläche (`ui`).
- **Verbindliche UI-Sprache:** Die Benutzeroberfläche ist nativ in deutscher Sprache verfasst.

---

## 2. Schichtenarchitektur (Layer Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer (Compose)                    │
│   LibraryScreen, PlayerScreen, DetailsScreen, CarModeScreen │
│   ViewModels (StateFlow / UDF), Themes (Paper & True Black) │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
               ▼                               ▼
┌──────────────────────────────┐ ┌─────────────────────────────┐
│         Player Layer         │ │        Domain Layer         │
│  PlaybackController          │ │  Modelle: Audiobook,        │
│  AbookPlaybackService        │ │           Chapter, Bookmark │
│  ExoPlayer (Media3), Widgets │ │  UseCases: SaveProgress,    │
│  SleepTimer & Loudness       │ │            Import/Export    │
└──────────────┬───────────────┘ └─────────────┬───────────────┘
               │                               │
               ▼                               ▼
┌──────────────────────────────┐ ┌─────────────────────────────┐
│        Storage Layer         │ │         Data Layer          │
│  AbookStorage (ZIP/.abook)   │ │  AudiobookRepository        │
│  FolderScanner (CD1/CD2)     │ │  AbookDatabase (Room v11)   │
│  OnlineCoverScraper (Audible)│ │  DAOs: Audiobook, Chapter,  │
│  LibraryLocationManager(SAF) │ │        Bookmark, Character  │
└──────────────────────────────┴─┴─────────────────────────────┘
```

### 2.1 Domain Layer (`de.f_soft_studio.abookplayer.domain`)
- **Verantwortung:** Enthält stabile Geschäftsmodelle und Anwendungsfälle (UseCases).
- **Regel:** Keine Abhängigkeiten zu Android-Framework-Klassen (`Context`, `Intent`, `ExoPlayer`, `Room`).
- **Wichtige Klassen:**
  - `Audiobook`: Repräsentation eines Hörbuchs mit Metadaten, Position, Favoritenstatus und Laufzeiten.
  - `Chapter`: Kapitelabschnitte mit Titel, Startzeit und partiellem Dateipfad.
  - `Bookmark`: Zeitstempel, Notizen und Kapitelposition.
  - `BookCharacter`: Personenregister mit Rollen und Beziehungen.
  - `SaveProgressUseCase`: Atomares Speichern des Abspielzeitpunkts mit Seek-Schutz für Statistiken.

### 2.2 Data Layer (`de.f_soft_studio.abookplayer.data`)
- **Verantwortung:** Persistenz in Room (SQLite).
- **Entities:** `AudiobookEntity`, `ChapterEntity`, `BookmarkEntity`, `CharacterEntity`, `ListeningSessionEntity`.
- **Datenbank:** `AbookDatabase` (aktuelles Schema: **Version 11** mit sauberem Migrationspfad ab Version 3).
- **Kritische Architektur-Regel (SQLite CASCADE DELETE Trap):**
  Zwischen `AudiobookEntity` und `ChapterEntity` besteht eine Fremdschlüsselbeziehung mit `onDelete = ForeignKey.CASCADE`. Wenn ein bestehendes Buch (`id > 0`) per `@Insert(onConflict = OnConflictStrategy.REPLACE)` gespeichert wird, löscht SQLite intern die Zeile vor dem Neueinfügen und löscht kaskadierend alle zugehörigen Kapitel!
  *Lösung:* Bestehende Datensätze werden im Repository ausnahmslos über `AudiobookDao.updateAudiobook` (`@Update`) aktualisiert.

### 2.3 Storage Layer (`de.f_soft_studio.abookplayer.storage`)
- **Verantwortung:** Dateisysteminteraktion, Storage Access Framework (SAF), Container-Handling und Online-Recherche.
- **`AbookStorage`:**
  - Importiert `.abook`-Archive und `.zip`-Dateien.
  - Schutz vor Sicherheitsrisiken wie **Zip-Slip** (Validierung von Zielpfaden) und **Zip-Bombs** (Größenbegrenzung).
  - Parst `manifest.json` (Format v1) sowie Legacy-Dateien (`metadata.json`).
- **`FolderScanner`:**
  - Rekursiver Ordnerscanner für Sammlungen.
  - Erkennt Multi-Disc-Strukturen (`CD1`, `CD2`, `Disc 1`, `Part 2`) und fasst diese automatisch zu einem zusammenhängenden Hörbuch zusammen.
- **`OnlineCoverScraper`:**
  - Multi-Provider-Kaskade: **Audible (DE)** ➔ **iTunes (DE)** ➔ **Google Books** ➔ **OpenLibrary**.
  - Extrahiert Sprecher, Serien, Bandnummern, Beschreibungen und quadratische HD-Cover (bis 1000x1000).
- **`LibraryLocationManager`:**
  - Verwaltet dauerhaft freigegebene SAF-Verzeichnisse (`takePersistableUriPermission`).

### 2.4 Player Layer (`de.f_soft_studio.abookplayer.player`)
- **Verantwortung:** Audiowiedergabe, Audio-Fokus, Benachrichtigungen, Hintergrundbetrieb und Sperrbildschirm-Steuerung.
- **`AbookPlaybackService`:**
  - AndroidX Media3 `MediaLibraryService` / Foreground Service.
  - Steuert MediaSession, Android Auto und Lockscreen-Notification.
- **`PlaybackController`:**
  - Verwaltet den `ExoPlayer`, Playlists, Speed (0.75x–2.0x), Skip Silence und Smart Rewind.
  - **Atomarer Hörbuch-Wechsel:** Beim Auswählen eines anderen Titels wird die aktuelle Wiedergabe sofort gestoppt, der Fortschritt gesichert und erst danach der neue Titel geladen.
  - **Dynamische Kapitelrekonstruktion:** Wenn für ein Ordner-Hörbuch keine Kapitel in der Datenbank existieren, scannt der Controller das Verzeichnis automatisch on-the-fly (`FolderScanner`) und baut die Medienliste ohne Fehler auf.
- **`SleepTimerController`:**
  - Flexibler Timer (Minuten oder Kapitelende) mit "Shake to Extend" (+15 Min. per Beschleunigungssensor).

### 2.5 UI Layer (`de.f_soft_studio.abookplayer.ui`)
- **Technologie:** Jetpack Compose mit Material Design 3.
- **Designs:**
  - **Paper-Look:** Warme Bibliotheksansicht für entspanntes Stöbern.
  - **OLED True Black:** Reines Schwarz (`#000000`) im Player zur maximalen Akkuschonung auf AMOLED-Displays.
- **Spezial-Screens:**
  - `CarModeScreen`: Großtasten-Ansicht mit hohem Kontrast für die sichere Bedienung während der Autofahrt.
  - `MetadataSearchDialog`: Interaktive Suche nach Online-Metadaten mit Trefferauswahl und Cover-Vorschau.

---

## 3. Datenflüsse

### 3.1 Wiedergabe-Start und Umschalten
```
[User tippt Hörbuch an]
       │
       ▼
AppRoot / LibraryViewModel.selectAudiobook()
       │
       ▼
PlaybackController.loadAudiobook(audiobook)
       │
       ├─► 1. Altes Buch? -> Fortschritt per SaveProgressUseCase sichern
       ├─► 2. player.stop() & player.clearMediaItems()
       ├─► 3. Kapitel aus Repository laden (oder on-the-fly via FolderScanner reparieren)
       ├─► 4. MediaItems bauen (file:// oder content://) & player.setMediaItems()
       ├─► 5. player.seekTo(savedPosition) & player.prepare() & player.play()
       │
       ▼
AbookPlaybackService aktualisiert Notification & Android Auto
```

---

## 4. Build-, Release- und Qualitätssicherung

- **Build-System:** Gradle mit Kotlin DSL (`build.gradle.kts`).
- **Zielplattform:** `minSdk = 24`, `targetSdk = 35`, `compileSdk = 35`, Java 21 / Kotlin 2.0.20.
- **R8 / ProGuard:** Release-Builds nutzen aggressive Code-Minifizierung und Resource-Shrinking. Entsprechende Keep-Rules für Room, Media3, Serialization und Coil sind in `proguard-rules.pro` definiert.
- **Test-Suite:** JVM Unit-Tests mit MockK und Robolectric für DAOs, Storage, Scraper und ViewModels (`.\gradlew.bat test`).
- **F-Droid Release:** Vollständige Fastlane-Metadaten in `fastlane/metadata/android/` und Build-Rezept in `metadata/de.f_soft_studio.abookplayer.yml`.
