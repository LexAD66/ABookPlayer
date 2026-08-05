# TODO – Konsolidierter Umsetzungsplan

## Phase 0 – Projekt konsolidieren
- [x] Projektinhalt analysieren.
- [x] Design-Prototypen von Android-Code trennen.
- [x] Doppelte oder alte Ansätze identifizieren.
- [x] Vor dem Löschen eine Löschliste mit Begründung erstellen.

## Phase 1 – Android-Grundlage & Testing
- [x] Android-Projektstruktur erstellen oder prüfen.
- [x] Gradle-Konfiguration prüfen (Dependencies, Test-Suites).
- [x] Package-Namen festlegen (`de.f_soft_studio.abookplayer`).
- [x] Material-3-Theme mit deutschen UI-Texten vorbereiten.
- [x] MCP-Server & 5 Entwickler-Subagenten einrichten.
- [x] Automatisierte Unit-Test-Suite eingerichtet & erfolgreich ausgeführt (`.\gradlew.bat test` PASSED).

## Phase 2 – Datenbank & UseCases
- [x] `AudiobookEntity` erstellen & verifizieren.
- [x] `ChapterEntity` erstellen & verifizieren.
- [x] `BookmarkEntity` erstellen & verifizieren.
- [x] DAO-Klassen erstellen & testen.
- [x] Room-Datenbank & Repository anbinden.
- [x] Domain UseCases erstellen (`GetAudiobookDetailsUseCase`, `DeleteAudiobookUseCase`, `AddBookmarkUseCase`).
- [x] Unit-Tests für Repository & UseCases erfolgreich ausgeführt.

## Phase 3 – Bibliothek (Library UI)
- [x] Library Screen in Compose (Warmes Papier-Design) umgesetzt.
- [x] Hörbuchkarten mit Cover, Titel, Autor, Fortschrittsleiste & Restzeit.
- [x] Echtzeit-Suche für Titel & Autor.
- [x] Sortierung nach Zuletzt gehört, Titel & Autor.
- [x] Button für Ordner-Scan (`/ABook/`) & SAF-Import.
- [x] Unit-Tests für `LibraryViewModelTest` erfolgreich ausgeführt.

## Phase 4 – Player (Stage UI & Engine)
- [x] Player Screen in Compose (Dunkles Cinematic Stage-Design) umgesetzt.
- [x] Korrektes Pause-Icon (`Icons.Default.Pause`) & große Bedienelemente.
- [x] Media3 ExoPlayer Steuerung mit Play/Pause & ±10s Sprüngen.
- [x] Kapitel-Vor/Zurück Sprünge (`skipToNextChapter()`, `skipToPreviousChapter()`).
- [x] Geschwindigkeitsauswahl (0.75x bis 2.0x).
- [x] Fortschritt automatisch via `SaveProgressUseCase` speichern.
- [x] Unit-Tests in `PlayerViewModelTest` erfolgreich ausgeführt.

## Phase 5 – Kapitel und Bookmarks
- [x] Kapitel-Liste erstellen (`ChaptersScreen.kt`).
- [x] Kapitel-Sprung implementieren.
- [x] Bookmark-Liste erstellen (`BookmarksScreen.kt`).
- [x] Bookmark an aktueller Position speichern.

## Phase 6 – Sleep Timer
- [x] Sleep-Timer-UI erstellen (`SleepTimerDialog.kt`).
- [x] Timer-Logik implementieren (`SleepTimerController.kt`).
- [x] Wiedergabe nach Ablauf pausieren.
- [x] Unit-Tests für `SleepTimerControllerTest` bestanden.

## Phase 7 – Tests & Release
- [x] Projekt bauen (`assembleDebug` & `assembleRelease` PASSED).
- [x] ProGuard / R8 Konfiguration in `proguard-rules.pro` ergänzt.
- [x] Unit-Tests & Build-Artefakte erfolgreich verifiziert.
