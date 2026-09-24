# ABook Player

Android-App zum Abspielen von **.abook**-Hörbüchern (ZIP mit Audio-Kapiteln, `metadata.json` und optionalem Cover) sowie entpackten Hörbuch-Ordnern.  
Erstellt für **Android Studio Narwhal 4 Feature Drop | 2025.1.4**.

---

## 📊 Projektstatus
- **Status:** 🎉 **Version 2.0.0 (Major Release) vollständig abgeschlossen!**
- **Build-Status:** 🟢 `BUILD SUCCESSFUL` (Debug & Release APKs, ProGuard/R8 verifiziert)
- **Test-Status:** 🟢 Alle Unit-, ViewModel-, Storage-, Service- & DAO-Tests bestanden (53 Tests).
- **F-Droid / FOSS Status:** 🟢 100% quelloffen, Fastlane-Metadaten & F-Droid-Rezept vorhanden.

---

## 📚 Projektdokumentation
Die ausführliche, konsolidierte Projektdokumentation (Kapitel 01–17) befindet sich im Ordner [**Docs/**](Docs/README.md). Die ganzheitliche Systemarchitektur ist in [`ARCHITECTURE.md`](../ARCHITECTURE.md) beschrieben.

---

## ⚙️ Eckdaten
- **Projektname:** ABook Player
- **Paketname:** `de.f_soft_studio.abookplayer`
- **Entwickler-Domain:** `f-soft-studio.de`
- **Version:** `2.0.0` (versionCode: `302`)
- **Sprache:** Kotlin 2.0.20
- **Build:** Gradle (AGP 8.5.2), compileSdk/targetSdk 35, minSdk 24, Java 21
- **Architektur:** Clean Architecture + MVVM (`domain` / `data` / `storage` / `player` / `ui`)
- **UI:** Jetpack Compose + Material 3 (Compose BOM 2025.01.00), Navigation-Compose
- **Player:** Media3 1.4.1 (ExoPlayer + MediaSession + MediaLibraryService)
- **Persistenz:** Room 2.6.1 (Audiobooks, Chapters, Bookmarks, ListeningSessions, Characters - DB v11), DataStore Preferences
- **Sonstiges:** Kotlinx Serialization (metadata.json), Coil (Cover-Rendering), Coroutines

---

## 📸 Screenshots & Benutzeroberfläche

| Bibliothek (Listenansicht) | Bibliothek (Rasteransicht) | OLED Audioplayer | Kapitelübersicht |
| :---: | :---: | :---: | :---: |
| <img src="../docs/screenshots/01_library_list.png" width="220" alt="Bibliothek Liste" /> | <img src="../docs/screenshots/02_library_grid.png" width="220" alt="Bibliothek Raster" /> | <img src="../docs/screenshots/03_player.png" width="220" alt="OLED Player" /> | <img src="../docs/screenshots/04_chapters.png" width="220" alt="Kapitelübersicht" /> |
| *Listenansicht mit Mini-Player* | *2-Spalten-Raster mit Cover-Art* | *Reinschwarz (#000000) Stage* | *Live-Wellenform am aktiven Track* |

| Sound & Equalizer | Wiedergabetempo | Player-Menü | Hörbuch-Details |
| :---: | :---: | :---: | :---: |
| <img src="../docs/screenshots/05_equalizer.png" width="220" alt="Sound & Equalizer" /> | <img src="../docs/screenshots/06_speed.png" width="220" alt="Wiedergabetempo" /> | <img src="../docs/screenshots/07_player_menu.png" width="220" alt="Player Optionen" /> | <img src="../docs/screenshots/08_details.png" width="220" alt="Hörbuch Details" /> |
| *Boost, Stille überspringen & Presets* | *0.75x–2.0x & Stufenlos* | *Sleep-Timer, Lesezeichen & Auto* | *Metadaten, Figuren & Export* |

| Online-Metadatensuche | Metadaten-Editor | Technische Spezifikationen |
| :---: | :---: | :---: |
| <img src="../docs/screenshots/09_metadata_search.png" width="220" alt="Online-Metadatensuche" /> | <img src="../docs/screenshots/10_metadata_edit.png" width="220" alt="Metadaten bearbeiten" /> | <img src="../docs/screenshots/11_technical_info.png" width="220" alt="Technische Spezifikationen" /> |
| *Audible & iTunes Live-Suche* | *Reihen-, Band- & Sprecher-Editor* | *.abook Container & Audio-Encoding* |

---

## 🌟 Haupt-Features (Release 2.0.0)
1. **.abook-Format Import & Kanonischer Export:** Abspielen von `.abook`-Dateien (ZIP mit Audio-Dateien + `metadata.json`), automatisches atomares Entpacken in den App-Cache mit Zip-Bomb & Zip-Slip Protection sowie SAF Export.
2. **Smarter Ordner-Scanner:** automatischer Scan entpackter Ordnerstrukturen & intelligente CD-Zusammenführung (`CD1`, `CD2`, `Disc 1` usw.) zu einem einzigen Hörbuch inklusive Duplikaterkennung.
3. **Formatunterstützung:** MP3, M4A, OGG, FLAC, AAC, WAV.
4. **Media3 Audio Engine & Android Auto:** MediaLibraryService für nahtlose Hintergrundwiedergabe, Automotive-Integration, variabler Speed (0.75x–2.0x) & Smart Rewind.
5. **OLED True Black Theme & Paper Look:** Warmes Bibliotheks-Design (Material 3 Paper-Look) & stromsparendes Reinschwarz-Design für den Player Stage Screen.
6. **Sleep Timer & Shake to Extend:** Abschalttimer (15m, 30m, 45m, 60m, am Kapitelende) & Schütteln des Geräts zur automatischen Verlängerung (+15 Min.).
7. **Lokale Hörstatistiken:** Tägliche/wöchentliche Auswertung & Streaks (100% datenschutzfreundlich, Canvas 7-Tage-Balkendiagramm).
8. **Serien-Verwaltung, Audible- & iTunes-Scraper:** Reihen-Verwaltung (`series` & `seriesOrder`), Metadaten-Editor Dialog & automatischer Metadaten- und Cover-Download via Audible & iTunes API.
9. **Bibliotheks-Usability & Multi-Select:** Stapelverarbeitung für Favoritisierung/Löschen, Sprecher-Suche, neue Sortierungen (*Restlaufzeit*, *Hinzugefügt am*).
10. **Komfort-Funktionen:** Android Homescreen Widget, Stille überspringen (Skip Silence), Equalizer / Lautstärke-Boost & Mehrsprachigkeit (DE/EN).

---

## 📁 Projektstruktur
```
ABookPlayer/
 ├─ app/src/main/java/de/f_soft_studio/abookplayer/
 │   ├─ domain/
 │   │   ├─ model/         (Audiobook, Chapter, Bookmark, ListeningStatistics, ExportState)
 │   │   └─ usecase/       (GetAudiobooks, GetAudiobookDetails, SaveProgress, AddBookmark, DeleteAudiobook, ExportAudiobook, RecordListeningTime, GetListeningStatistics)
 │   ├─ data/
 │   │   ├─ local/entity/   (AudiobookEntity, ChapterEntity, BookmarkEntity, ListeningSessionEntity)
 │   │   ├─ local/dao/      (AudiobookDao, ChapterDao, BookmarkDao, ListeningSessionDao)
 │   │   ├─ local/db/       (AbookDatabase)
 │   │   └─ repository/     (AudiobookRepository)
 │   ├─ storage/            (AbookStorage, FolderScanner, OpenLibraryScraper)
 │   ├─ player/
 │   │   ├─ controller/     (PlaybackController, SleepTimerController)
 │   │   └─ service/        (AbookPlaybackService - MediaLibraryService)
 │   ├─ ui/
 │   │   ├─ library/ details/ player/ chapters/ bookmarks/ statistics/ sleep/ theme/
 │   │   └─ AppRoot.kt      (NavHost & Dialoge)
 │   ├─ util/               (AbookModels, ChapterDurations, ShakeDetector)
 │   ├─ widget/             (AbookWidgetProvider)
 │   └─ MainActivity.kt
 ├─ app/src/test/java/de/f_soft_studio/abookplayer/  (Unit- & Integrationstests)
 ├─ run_app.ps1 / run_app.bat  (Build & Launch Skripte)
 └─ build.gradle.kts
```

---

## 🚀 Ausführung & Installation
1. Projekt in Android Studio öffnen (**File → Open → `build.gradle.kts`**).
2. Gradle Sync durchführen.
3. App auf Android-Gerät oder Emulator (minSdk 24 / SDK 35) ausführen.
4. Für automatisches Ausführen über PowerShell:
   ```powershell
   .\run_app.ps1
   ```
