# 11. Funktionsumfang und Status

Stand: App-Version **2.0.0**, Phasen 0–23 vollständig umgesetzt (inkl. Library-UI-Redesign, R8-Härtung, Migrationstests, Instrumentation-Tests & Tablet/Foldable-UX). Details siehe [`../PROJECT_HISTORY.md`](../PROJECT_HISTORY.md).

| Bereich | Status | Bemerkung |
| --- | --- | --- |
| Projektkonsolidierung | 🟢 | Designprototypen (`ABook_Player_Antigravity_Plan/`) vom nativen Projekt (`ABookPlayer/`) getrennt. |
| Android-Grundlage | 🟢 | AGP 8.5.2, Kotlin 2.0.20, compileSdk/targetSdk 35, minSdk 24, JVM 21, Compose BOM 2025.01.00, Media3 1.4.1. **DI manuell, kein Hilt.** |
| Room / Repository / UseCases | 🟢 | `AbookDatabase` **Schema v11** (5 Entities), Migrationen 3→11, `AudiobookRepository`, 10 UseCases. |
| Bibliothek & Usability | 🟢 | Schlanke Top-Bar, kompakte Active-Filter-Bar, `FilterBottomSheet`, `AddAudiobookBottomSheet`, modularisierte Karten (`AudiobookItemCard`, `AudiobookGridCard`, `MiniPlayerBar`), Multi-Select. |
| Player & Audio-Engine | 🟢 | Media3 ExoPlayer, `MediaLibraryService`, Hintergrundwiedergabe, Speed 0,75×–2,0×, `-10s`/`+10s`/`+30s`, Smart Rewind, Skip Silence, Verzeichnispfade werden abgefangen (`PlayableMedia`), sichtbarer Wiedergabefehler statt Stille. |
| Android Auto | 🟢 | Auto-Modus-Screen, MediaLibraryService-Browsing, app-lokale `-10s`/`+10s`-Icons. |
| Kapitel & Lesezeichen | 🟢 | Kapitelnavigation mit Zeitstempeln, Kapitel-Editor, Lesezeichen mit Notiz, Smart Sleep Bookmark. |
| Sleep Timer & Komfort | 🟢 | Timer 15/30/45/60 min + Kapitelende, Shake-to-Extend, optionales Ausblenden. |
| Storage, SAF & Export | 🟢 | `.abook`/ZIP-Import mit Zip-Bomb-Schutz, SAF Datei-/Ordner-Import, Kopie in app-verwalteten Bibliotheksordner (`imported_*`, `.abooklib`), FolderScanner mit CD1/CD2, Export. |
| Bibliothekswartung | 🟢 | Eigener Screen `LibraryManagementScreen` für Scan, Bereinigung, Duplikat-Behandlung (`DuplicateMatch`) und 0-Minuten-Reparatur. |
| Serien & Metadaten | 🟢 | `series`/`parentSeries`/`seriesOrder`, Metadaten-Editor, Online-Cover (`OnlineCoverScraper`, `OpenLibraryScraper`). |
| Figuren / Charaktere | 🟢 | `CharacterEntity`, Figurenverwaltung pro Hörbuch. |
| Hörstatistik | 🟢 | Lokales Tracking (`ListeningSessionEntity`), 7-Tage-Balkendiagramm, Streak. |
| Fortschritts-Sync | 🟢 | Optionaler WebDAV-Abgleich (`storage/sync/`). |
| System-Integration | 🟢 | Homescreen-Widgets (`AbookWidgetProvider`, `AbookBannerWidgetProvider`), Skip Silence, DE/EN. |
| Release-Build & QS | 🟢 | Release-Build mit R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) und `proguard-rules.pro` verifiziert (`assembleRelease`); JVM- & Migrationstests grün (Robolectric); `androidTest/` mit Compose UI-Tests (`assembleDebugAndroidTest`). |
| Library-UI-Umbau (Phase 20) | 🟢 | Top-Bar entschlackt, Filter-/Add-Bottom-Sheet, `library_management`-Screen, Zerlegung `LibraryScreen.kt` abgeschlossen. |
| UI-Animationen & Transitions | 🟢 | Flüssige NavHost-Übergänge (Slide-Up/Down für Player Stage, horizontale Slides für Detail/Settings/Management). |
| Favoriten persistent | 🟢 | `isFavorite` als Room-Spalte (Migration 8→9). |
| Tablet- & Foldable-UX (Phase 23) | 🟢 | Adaptives 2-Spalten-Layout (Split View) für Details & Statistiken; responsives Grid (`GridCells.Adaptive`) mit Series-Span-Unterstützung. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](10_benutzeroberflaeche-und-navigation.md)
 · [Nächstes Kapitel →](12_teststrategie-und-qualitaetssicherung.md)
