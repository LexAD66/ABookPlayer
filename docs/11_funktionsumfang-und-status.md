# 11. Funktionsumfang und Status

Stand: App-Version **2.0.0**, Phasen 0–19 umgesetzt, Phase 20 (Library-Umstrukturierung) mit abgeschlossenem Backend-Teil. Details siehe [`../PROJECT_HISTORY.md`](../PROJECT_HISTORY.md).

| Bereich | Status | Bemerkung |
| --- | --- | --- |
| Projektkonsolidierung | 🟢 | Designprototypen (`ABook_Player_Antigravity_Plan/`) vom nativen Projekt (`ABookPlayer/`) getrennt. |
| Android-Grundlage | 🟢 | AGP 8.5.2, Kotlin 2.0.20, compileSdk/targetSdk 35, minSdk 24, JVM 21, Compose BOM 2025.01.00, Media3 1.4.1. **DI manuell, kein Hilt.** |
| Room / Repository / UseCases | 🟢 | `AbookDatabase` **Schema v11** (5 Entities), Migrationen 3→11, `AudiobookRepository`, 10 UseCases. |
| Bibliothek & Usability | 🟢 | Echtzeit-Suche, Status-Filter, Sortierung inkl. `addedAt`, Serien-Ansicht, Multi-Select, Duplikat-Ansicht, expliziter `LibraryMaintenanceUiState`. Kein Auto-Scan mehr beim Start. |
| Player & Audio-Engine | 🟢 | Media3 ExoPlayer, `MediaLibraryService`, Hintergrundwiedergabe, Speed 0,75×–2,0×, `-10s`/`+10s`/`+30s`, Smart Rewind, Skip Silence, Verzeichnispfade werden abgefangen (`PlayableMedia`), sichtbarer Wiedergabefehler statt Stille. |
| Android Auto | 🟢 | Auto-Modus-Screen, MediaLibraryService-Browsing, app-lokale `-10s`/`+10s`-Icons. |
| Kapitel & Lesezeichen | 🟢 | Kapitelnavigation mit Zeitstempeln, Kapitel-Editor, Lesezeichen mit Notiz, Smart Sleep Bookmark. |
| Sleep Timer & Komfort | 🟢 | Timer 15/30/45/60 min + Kapitelende, Shake-to-Extend, optionales Ausblenden. |
| Storage, SAF & Export | 🟢 | `.abook`/ZIP-Import mit Zip-Bomb-Schutz, SAF Datei-/Ordner-Import, Kopie in app-verwalteten Bibliotheksordner (`imported_*`, `.abooklib`), FolderScanner mit CD1/CD2, Export. |
| Bibliothekswartung | 🟢 | Cleanup entfernt Duplikate und verwaiste Einträge – auch leere `imported_*`-Ordner; 0-Minuten-Reparatur; `DuplicateDetector` erzeugt keine „(Edition)"-Klone mehr bei unbekannter Dauer. |
| Serien & Metadaten | 🟢 | `series`/`parentSeries`/`seriesOrder`, Metadaten-Editor, Online-Cover (`OnlineCoverScraper`, `OpenLibraryScraper`). |
| Figuren / Charaktere | 🟢 | `CharacterEntity`, Figurenverwaltung pro Hörbuch. |
| Hörstatistik | 🟢 | Lokales Tracking (`ListeningSessionEntity`), 7-Tage-Balkendiagramm, Streak. |
| Fortschritts-Sync | 🟢 | Optionaler WebDAV-Abgleich (`storage/sync/`). |
| System-Integration | 🟢 | Homescreen-Widgets (`AbookWidgetProvider`, `AbookBannerWidgetProvider`), Skip Silence, DE/EN. |
| Release-Build & QS | 🟡 | Debug-Build verifiziert; JVM-Tests grün (Robolectric). `isMinifyEnabled = false` (R8 aus), `proguard-rules.pro` vorhanden. **Kein `androidTest`-/Instrumentation-Verzeichnis.** |
| Library-UI-Umbau (Phase 20, Teil B) | 🔴 offen | Top-Bar entschlacken, Filter-/Add-Bottom-Sheet, `library_management`-Screen, Zerlegung `LibraryScreen.kt`. Siehe `redesign.md`. |
| Favoriten persistent | 🟢 | `isFavorite` als Room-Spalte (Migration 8→9). |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](10_benutzeroberflaeche-und-navigation.md)
 · [Nächstes Kapitel →](12_teststrategie-und-qualitaetssicherung.md)
