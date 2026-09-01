# ABook Player – Projekthistorie & Umsetzungsstand

Diese Datei dokumentiert alle **abgeschlossenen Fortschritte** sowie alle **geplanten Aufgaben** für die Entwicklung der ABook Player Android-App.

---

## 📊 Übersicht des aktuellen Projektstatus

- **Status:** Phasen 0–19 umgesetzt und versioniert. Nächster Schwerpunkt: **Phase 20 – Library-Umstrukturierung** (siehe [`ABookPlayer/redesign.md`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/redesign.md)).
- **Letzte Aktualisierung:** 31. August 2026
- **Git:** Arbeitsstand der Phasen 15–19 in thematischen Commits gesichert (Sync, Audio-Engine, Figuren, Library-Wartung, Kapitel-Editor, Core-Verdrahtung). Sicherungs-Branch `backup/pre-cleanup-2026-08-31`.
- **Build-Status:** ⏳ Verifikation nach dem Cleanup-Commit ausstehend (`gradlew test` / `assembleDebug`).
- **Zielarchitektur:** Clean Architecture (MVVM mit Jetpack Compose, Media3, Room, Hilt)

### 🔜 Phase 20: Library-Umstrukturierung (geplant)

- [ ] Informationsarchitektur gemäß `redesign.md` umsetzen: Trennung von **Bibliothek** (Hörfluss) und **Bibliothek verwalten** (Scan, Cleanup, Duplikate, Dauer-Reparatur, Speicherort-Migration).
- [ ] Top-Bar entschlacken: nur Suche, Ansicht (Liste/Raster), Mehr-Menü.
- [ ] Kompakte Active-Filter-Bar (max. 2–3 sichtbare Chips + `Filter`-Button) statt mehrerer Chip-Zeilen.
- [ ] Import/Scan/Cleanup aus einem Sammel-Dialog in ein `Hinzufügen`-Bottom-Sheet + eigenen Wartungsbereich aufteilen.
- [ ] Favoriten persistent machen (Room statt nur ViewModel-State).
- [x] `addedAt`-Spalte + Room-Migration 9→10; „Neu importiert" sortiert nach `addedAt`.
- [x] Automatischen Scan + Dauer-Reparatur aus `LibraryViewModel.init` entfernt.
- [x] `LibraryMaintenanceUiState` (isScanning / isCleaning / lastScanMessage / duplicates / cleanupResult) eingeführt.
- [ ] Serienansicht als bewusster Modus behandeln (nur bei `Sortierung = Serien`).
- [ ] Tests: `LibraryViewModelTest` erweitern; Build-Verifizierung.

> Backend-Teil (Spec A, `docs/superpowers/specs/2026-08-31-library-backend-design.md`)
> abgeschlossen. Offen: UI-Umbau (Spec B) – Top-Bar, Filter-Bottom-Sheet,
> Add-Bottom-Sheet, `library_management`-Screen, Zerlegung von `LibraryScreen.kt`.

---

## ✅ Abgeschlossene Aufgaben

### Phase 0: Projektkonsolidierung & Analyse
- [x] **Projektstruktur analysiert:** Trennung der React/JSX Design-Prototypen (`ABook_Player_Antigravity_Plan`) vom nativen Kotlin Android-Projekt (`ABookPlayer`).
- [x] **Redundante Dateien bereinigt:** Löschen der doppelten `AbookPlaybackService.kt` im Wurzelordner & Bereinigen leerer Alt-Ordner.
- [x] **Führende Services & Models festgelegt:** Service in `player/service/AbookPlaybackService.kt` & Modelle in `domain/model/`.

### Phase 1: Android-Grundlagen, MCP-Server & Agenten-Setup
- [x] **Subagenten-Einrichtung:** 5 spezialisierte Subagenten definiert (`android_clean_architect`, `room_database_expert`, `compose_ui_designer`, `media3_playback_engineer`, `android_qa_tester`).
- [x] **MCP-Server Konfiguration:** Integration von `filesystem`, `sqlite` und `github` in `mcp_config.json`.
- [x] **Automatisierte Test-Suite eingerichtet & verifiziert:** Unit-Tests für `metadata.json`-Parsing & Room DB.

### Phase 2: Datenbank & Domain-UseCases
- [x] **Entitäten & DAOs:** Entitäten (`AudiobookEntity`, `ChapterEntity`, `BookmarkEntity`) & DAOs.
- [x] **Vervollständigung aller Domain UseCases:** `GetAudiobooksUseCase`, `GetAudiobookDetailsUseCase`, `SaveProgressUseCase`, `AddBookmarkUseCase`, `DeleteAudiobookUseCase`.
- [x] **Testverifizierung:** [`RepositoryAndUseCaseTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/domain/RepositoryAndUseCaseTest.kt).

### Phase 3: Bibliothek (Library Screen UI im warmen Paper-Look)
- [x] **Material 3 Paper-Design:** Warmes Theme (`WarmBackground`, `WarmSurface`, `WarmCardBorder`).
- [x] **Echtzeit-Suche & Sortierung:** Suchfeld für Titel/Autor & Sortier-Chips (*Zuletzt gehört*, *Titel*, *Autor*).
- [x] **Ordner-Scan & Empty State:** Button zum Scannen des Download-Ordners `/ABook/`.
- [x] **Testabdeckung:** [`LibraryViewModelTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt).

### Phase 4: Player (Stage Screen UI & Media3 Engine)
- [x] **Dunkles Stage-Design:** Material 3 Dark Theme mit Amber Akzenten (`PrimaryAmber`, `DarkBackground`).
- [x] **Playback Controller:** Play/Pause (`Icons.Default.Pause`), Skip ±10s, Seekbar Slider und Geschwindigkeitswahl (0.75x - 2.0x).
- [x] **Kapitel-Navigation:** Kapitel-Vor/Zurück Buttons (`skipToNextChapter()`, `skipToPreviousChapter()`).
- [x] **Testabdeckung:** [`PlayerViewModelTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/PlayerViewModelTest.kt) (🟢 PASSED).

### Phase 5: Kapitel & Lesezeichen (Bookmarks)
- [x] **Kapitel-Navigation:**
  - Anzeige der Kapitelliste mit Zeitstempeln & direktem Sprung zu Kapiteln (`ChaptersScreen.kt`).
- [x] **Bookmark-Verwaltung:**
  - Setzen von Lesezeichen an aktueller Position mit optionaler Notiz & Löschfunktion (`BookmarksScreen.kt`).
  - Ansicht aller Lesezeichen eines Hörbuchs und Anspringen der Position.

### Phase 6: Sleep Timer & Komfortfunktionen
- [x] **Sleep Timer:**
  - Timer-Dialog (`SleepTimerDialog.kt`) für 15m, 30m, 45m, 60m & Deaktivierung.
  - Automatische Pausierung der Wiedergabe bei Ablauf via `SleepTimerController.kt`.
  - Testabdeckung: [`SleepTimerControllerTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/player/SleepTimerControllerTest.kt) (🟢 PASSED).

### Phase 7: End-to-End Testing & Release Build
- [x] **ProGuard/R8 Regeln:**
  - `proguard-rules.pro` mit Keep-Rules für Room, Media3 ExoPlayer und Kotlinx Serialization erweitert.
- [x] **Release & Debug Builds:**
  - Erfolgreiche Generierung der Debug & Release APKs (`.\gradlew.bat assembleDebug assembleRelease` -> 🟢 `BUILD SUCCESSFUL in 2m 22s`).

### Phase 8: Import-Härtung, Dokus-Integration & Erweiterte Sicherheit
- [x] **Dokumentations-Konsolidierung:** Integration des vollumfänglichen Dokumentationspakets in [`docs/`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/README.md) (Kapitel 01–17).
- [x] **Zip-Bomb & Zip-Slip Protection:** Härtung von `AbookStorage` (max 500 MB/Datei, max 2 GB Gesamtextraktion, max 100:1 Kompressionsverhältnis & Pfad-Traversierungsschutz).
- [x] **Atomare Extraktion & SAF Perms:** Schreiben über temporäre `.tmp`-Dateien und Absicherung persistenter URI-Rechte.
- [x] **Storage Test-Suite:** Erstellung von [`AbookStorageTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/storage/AbookStorageTest.kt) (🟢 PASSED).

### Phase 9: Hörstatistik & Auswertungs-Dashboard (Datenschutzfreundlich & Lokal)
- [x] **Room Datenmodell & DAO:** `ListeningSessionEntity`, `ListeningSessionDao` & DB-Migration auf Version 5 (`MIGRATION_4_5`).
- [x] **Domain-Logik & UseCases:** `ListeningStatistics`, `RecordListeningTimeUseCase` & `GetListeningStatisticsUseCase` für Tages/Wochen-Summen und Streak-Berechnungen.
- [x] **Automatisches Tracking:** Einbindung von `RecordListeningTimeUseCase` in `SaveProgressUseCase` zur genauen Messung geleisteter Hörzeit.
- [x] **UI & Navigation:** `StatisticsViewModel` & `StatisticsScreen` mit 7-Tage-Canvas-Balkendiagramm und Metric-Cards im Paper-Look.
- [x] **Testverifizierung:** Erstellung von [`ListeningStatisticsTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/domain/ListeningStatisticsTest.kt) (🟢 ALL TESTS PASSED) & APK Builds (`assembleDebug`, `assembleRelease` 🟢 `BUILD SUCCESSFUL`).

### Phase 10: `.abook` Export-Funktion (Roadmap P2)
- [x] **Kanonischer ZIP-Exporter:** Erstellung von `exportToAbook(...)` in `AbookStorage.kt` zur Verpackung von `manifest.json` v1, Cover (`cover.jpg`) und Audio-Tracks.
- [x] **Domain & State:** Erstellung von `ExportState.kt` und `ExportAudiobookUseCase.kt`.
- [x] **UI & SAF Picker:** Export-Button in `DetailsScreen.kt` mit Android SAF File-Picker (`CreateDocument`), Ladebalken und Statusanzeigen.
- [x] **Testabdeckung & Release Builds:** Erstellung von [`AbookStorageExportTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/storage/AbookStorageExportTest.kt) (🟢 ALL TESTS PASSED) & APK-Builds (`assembleDebug`, `assembleRelease` 🟢 `BUILD SUCCESSFUL in 1m 16s`).

### Phase 11: 5 Neue Features & System-Erweiterungen
- [x] **1. Schlaf-Timer (Kapitelende):** Erweiterung des `SleepTimerController.kt` und `SleepTimerDialog.kt` um die Option "Am Ende des Kapitels stoppen".
- [x] **2. Stille überspringen (Skip Silence):** Integration von `skipSilenceEnabled` in `PlaybackController.kt` und `PlayerViewModel.kt`.
- [x] **3. Android Homescreen-Widget:** Erstellung von `AbookWidgetProvider.kt`, `widget_abook_player.xml` und `abook_widget_info.xml` mit Play/Pause, Skip & Fortschrittsanzeige.
- [x] **4. Mehrsprachigkeit (i18n):** Konsolidierung aller String-Ressourcen in `values/strings.xml` (DE) und `values-en/strings.xml` (EN).
- [x] **5. FLAC-Audioformat-Unterstützung:** Erweiterung von `AbookStorage.kt` zur offiziellen Erkennung und Entpackung von `.flac`-Audiodateien.

### Phase 12: Android Auto, Smart Rewind & Erweiterte Bibliotheksverwaltung (CD1/CD2 Ordner-Zusammenführung)
- [x] **1. Android Auto Support:** Umstellung von `AbookPlaybackService` auf Media3 `MediaLibraryService`, Hinzufügen von `automotive_app_desc.xml` und Manifest-Einträgen für Automotive-Displays.
- [x] **2. Audio Engine & Smart Rewind:** Dynamisch anpassbares Spulintervall in `PlaybackController.kt` & automatischer Rücksprung nach Hörpausen.
- [x] **3. Smarter Ordner-Scanner & CD-Zusammenführung:** Erstellung von [`FolderScanner.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/storage/FolderScanner.kt) zur Erkennung entpackter Hörbuch-Ordner. Zusammenführung von Unterordnern wie `CD1`, `CD2`, `Disc 1` zu **einem einzigen zusammenhängenden Hörbuch**.
- [x] **4. Duplikat-Behandlung:** Automatische Erkennung und Ignorieren bereits vorhandener Hörbücher beim Ordner-Scan.
- [x] **5. Testverifizierung & Builds:** Erstellung von [`FolderScannerTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/storage/FolderScannerTest.kt) (🟢 ALL TESTS PASSED) & APK Builds (`assembleDebug`, `assembleRelease` 🟢 `BUILD SUCCESSFUL in 35s`).

### Phase 13: Serien-Verwaltung, OLED True Black Theme & Online Cover-Scraper
- [x] **1. Serien- & Reihen-Verwaltung:** DB-Migration v6 (`series`, `seriesOrder`), Einbindung des "Serien"-Filter-Chips in [`LibraryScreen.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryScreen.kt) & Sortierung nach Buchreihen.
- [x] **2. OLED True Black Theme:** Erstellung von [`AppTheme.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/theme/AppTheme.kt) mit reinem Schwarz (`#000000`) für maximale AMOLED-Batterieersparnis.
- [x] **3. Online Cover & Metadaten Scraper:** Erstellung von [`OpenLibraryScraper.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/storage/OpenLibraryScraper.kt) (OpenLibrary API) und Integration in `DetailsScreen.kt` zum automatischen Laden fehlender Cover und Beschreibungen.
- [x] **4. Testverifizierung & APK Deployment:** Erstellung von [`OpenLibraryScraperTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/storage/OpenLibraryScraperTest.kt) (🟢 ALL TESTS PASSED) & erfolgreiche Installation auf dem Smartphone (`installDebug` 🟢 `BUILD SUCCESSFUL in 16s`).

### Phase 14: Shake to Extend Sleep Timer & Metadaten-Editor
- [x] **1. Shake to Extend:** Erstellung von [`ShakeDetector.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/util/ShakeDetector.kt) und Integration in `SleepTimerController.kt` zur Verlängerung des Timers um **+15 Minuten** bei Schütteln des Geräts.
- [x] **2. Metadaten- & Serien-Editor Dialog:** Erstellung von [`EditAudiobookDialog.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/details/EditAudiobookDialog.kt) zum manuellen Anpassen von Titel, Autor, Sprecher, Serienname & Band-Nummer in `DetailsScreen.kt`.
- [x] **3. Testabdeckung & Deployment:** Erstellung von [`ShakeDetectorTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/util/ShakeDetectorTest.kt) (🟢 33 TESTS PASSED) & erneute Installation auf dem Smartphone (`installDebug` 🟢 `BUILD SUCCESSFUL in 15s`).

### Phase 15: Bibliotheks-Usability & Funktionalitäts-Erweiterung
- [x] **1. ⭐ Favoriten-FilterChip:** Neuer FilterChip "Favoriten" in `LibraryScreen.kt` und Einbindung von `StatusFilter.FAVORITEN` in `LibraryViewModel.kt`.
- [x] **2. 🎙️ Sprecher-Suche & Anzeige:** Erweiterung der Suchleiste um Sprechernamen und Anzeige von "Gelesen von..." in `AudiobookItemCard`.
- [x] **3. ⏱️ Neue Sortieroptionen:** Sortierung nach `RESTLAUFZEIT` (Kürzeste Restzeit zuerst) und `HINZUGEFUEGT_AM` (Neueste zuerst).
- [x] **4. 📦 Stapelverarbeitung (Multi-Select):** Stapelfavoritisierung via Stern-Button und Stapellöschen via Mülleimer-Button inkl. Material 3 `AlertDialog`.
- [x] **5. Testverifizierung:** Erweiterung von [`LibraryViewModelTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt) (🟢 `BUILD SUCCESSFUL in 12s`).

### Phase 16: Audio Enhancements, Equalizer Presets & Loudness Normalization (Release 1.2.0)
- [x] **1. Loudness & Equalizer Engine:** Erstellung von [`LoudnessController.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/player/controller/LoudnessController.kt) mit Anbindung an ExoPlayer AudioSessionId, Support für Presets (*Voice Boost*, *Bass Reduction*, *Night Mode*, *Custom 5-Band*) & anpassbarem Loudness Gain (+0 dB bis +15 dB).
- [x] **2. Equalizer & Boost UI Dialog:** Überarbeitung von [`EqualizerDialog.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/player/EqualizerDialog.kt) mit Slider für Verstärkung, Preset-Auswahl und "Stille überspringen" Umschaltung.
- [x] **3. State Management & ViewModel Integration:** Erweiterung von [`PlaybackController.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/player/controller/PlaybackController.kt) und [`PlayerViewModel.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/player/PlayerViewModel.kt).
- [x] **4. Testabdeckung & Build-Verifizierung:** Erstellung von [`LoudnessControllerTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/player/LoudnessControllerTest.kt) (🟢 `BUILD SUCCESSFUL in 59s`).

### Phase 17: WebDAV Cloud-Sync & Nextcloud Backup (Release 1.3.0)
- [x] **1. WebDAV Sync Engine & DTOs:** Erstellung von [`SyncDataModels.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/storage/sync/SyncDataModels.kt) und [`WebDavSyncManager.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/storage/sync/WebDavSyncManager.kt) (HTTP PUT/GET Sync für Nextcloud / ownCloud).
- [x] **2. Bidirektionale Konfliktbehandlung (UseCase):** Erstellung von [`SyncProgressUseCase.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/domain/usecase/SyncProgressUseCase.kt) ("Neuester Timestamp gewinnt" für Hörpositionen & Lesezeichen).
- [x] **3. Sync-Einstellungen & Dialog UI:** Erstellung von [`SyncSettingsDialog.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/settings/SyncSettingsDialog.kt) & Einbindung in [`SettingsScreen.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/settings/SettingsScreen.kt).
- [x] **4. Testabdeckung & Build-Verifizierung:** Erstellung von [`SyncProgressUseCaseTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/domain/SyncProgressUseCaseTest.kt) (🟢 `BUILD SUCCESSFUL in 22s`).

### Phase 19: Smart Audio Engine & Custom Kapitel-Editor (Release 2.0.0)
- [x] **1. Smart Sleep Bookmark:** Automatisches Erstellen von "Einschlaf-Start (Timer)" Lesezeichen beim Starten des Sleep-Timers.
- [x] **2. Interaktiver Kapitel-Editor UI:** Erstellung von [`EditChaptersDialog.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/chapters/EditChaptersDialog.kt) zum Hinzufügen, Umbenennen, Verschieben und Löschen von Kapiteln inkl. Zeitstempel-Parsing (`HH:MM:SS`).
- [x] **3. Navigation & App Integration:** Einbindung des Kapitel-Editors in [`ChaptersScreen.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/chapters/ChaptersScreen.kt) und [`AppRoot.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/AppRoot.kt).
- [x] **4. Testabdeckung & Build-Verifizierung:** Erstellung von [`SmartSleepBookmarkTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/player/SmartSleepBookmarkTest.kt) (🟢 `BUILD SUCCESSFUL in 23s`).

### Phase 20 (Fix): Android Auto – fehlende Bedienelemente & Dateiname statt Titel
- [x] **1. Custom-Action-Icons app-lokal:** Neue Vektor-Drawables [`ic_skip_back_10.xml`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/res/drawable/ic_skip_back_10.xml) / [`ic_skip_forward_10.xml`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/res/drawable/ic_skip_forward_10.xml). Die `CommandButton`s in [`PlaybackController.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/player/controller/PlaybackController.kt) und die Notification-Actions in [`AbookPlaybackService.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/player/service/AbookPlaybackService.kt) nutzen jetzt diese statt `android.R.drawable.*` – Android Auto kann die Icons auflösen und zeigt die -10s/+10s-Buttons wieder an.
- [x] **2. Metadaten-Textlogik ausgelagert:** Neue testbare Klasse [`AudiobookMetadataText.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/util/AudiobookMetadataText.kt) leitet Titel/Untertitel/Interpret ab, entfernt Audio-Datei-Endungen aus Kapiteltiteln und garantiert einen nicht-leeren Buchtitel (kein Roh-Dateiname "116 – Kapitel 116.mp3" mehr).
- [x] **3. Import-Bereinigung:** SAF-Ordner-Import in [`AbookStorage.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/storage/AbookStorage.kt) speichert Kapiteltitel ohne Datei-Endung.
- [x] **4. Room-Migration 10→11:** Bereinigt bestehende `chapters.title` um Audio-Endungen ([`AbookDatabase.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt)).
- [x] **5. Testabdeckung & Build-Verifizierung:** [`AudiobookMetadataTextTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/util/AudiobookMetadataTextTest.kt) (🟢 `./gradlew.bat test` + `assembleDebug` `BUILD SUCCESSFUL`). `installDebug` übersprungen – kein Gerät verbunden.

### Phase 20 (Fix): Player „reagiert nicht" bei fehlenden Audiodateien (EISDIR)
- **Diagnose:** Logcat zeigte `ExoPlaybackException: Source error … EISDIR (Is a directory)` für `/storage/emulated/0/Audiobooks/imported_1988008138`. Ursache: Kapiteldateien verschoben/gelöscht (Ordner-Umzug `ABook`→`Audiobooks` + wiederholte Re-Imports → ~51 Duplikat-Zeilen, 61/73 Büchern mit `duration=0`). Waren alle Kapiteldateien weg, fiel [`PlaybackController.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/player/controller/PlaybackController.kt) auf `File(audiobook.filePath)` zurück – und `File.exists()` ist auch für **Verzeichnisse** `true`, sodass der `imported_*`-Ordner selbst an ExoPlayer ging. Fehler wurde nirgends angezeigt → Player wirkte tot. Der Fallback existiert unverändert seit v1.1.0, keine Regression aus Phase 20.
- [x] **1. Verzeichnisse nie abspielen:** Neue reine, testbare Klasse [`PlayableMedia.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/util/PlayableMedia.kt) (`isPlayableFile` = `File(path).isFile`). Alle drei Media-Item-Zweige im `PlaybackController` (Multi-File, Single-File, Buchpfad-Fallback) prüfen jetzt `isFile` statt `exists()`.
- [x] **2. Sichtbarer Fehler statt Stille:** `PlaybackController.playbackError: SharedFlow<String>` – bei 0 abspielbaren Dateien wird kein `play()` mehr aufgerufen, sondern eine Meldung emittiert. [`AppRoot.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/AppRoot.kt) zeigt sie als Toast („Keine abspielbaren Audiodateien für … gefunden – bitte neu importieren.").
- [x] **3. Testabdeckung & Build:** [`PlayableMediaTest.kt`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/util/PlayableMediaTest.kt) (Verzeichnis/fehlend/leer → nicht abspielbar). 🟢 `./gradlew.bat testDebugUnitTest` + `installDebug` (`SM-S921B`) `BUILD SUCCESSFUL`. On-Device-Endtest (Buch antippen → Toast) noch vom Nutzer zu bestätigen.
- **Offen (nicht Teil dieses Fix):** Duplikat-Bereinigung der Bibliothek + Prüfung, warum `cleanupDuplicatesAndOrphans` die toten/doppelten Einträge nicht entfernt.

---

## 📌 Dokumentations-Links

- 📚 [docs/README.md](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/README.md) – Konsolidierte Projektdokumentation
- 📄 [PROJECT_BRIEF.md](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABook_Player_Antigravity_Plan/PROJECT_BRIEF.md) – Spezifikation & Architektur
- 📄 [AGENTS.md](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABook_Player_Antigravity_Plan/AGENTS.md) – Entwicklungs- & Qualitätsregeln
- 📄 [walkthrough.md](file:///C:/Users/olexa/.gemini/antigravity/brain/d0d0d344-15b2-4895-9fef-6b5d7ea4c4fd/walkthrough.md) – Detaillierter Änderungswalkthrough


