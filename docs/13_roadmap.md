# 13. Roadmap

## Erledigt

| Thema | Ergebnis |
| --- | --- |
| Import-Härtung | Größenlimits, ZIP-Bomb-Schutz, ZIP-Slip-Prüfung, Kopie in app-verwalteten Bibliotheksordner. |
| Player-Robustheit | Audio-Focus, Service-Neustart, Fortschritt, Kapitelgrenzen; Verzeichnispfade werden abgefangen; sichtbarer Wiedergabefehler statt Stille. |
| Statistik | Lokale Hörzeit pro Tag/Woche, 7-Tage-Diagramm, Streak. |
| Export | Hörbuchordner → `.abook` (`ExportAudiobookUseCase`). |
| Android Auto | Auto-Modus, MediaLibraryService-Browsing, app-lokale Skip-Icons. |
| Fortschritts-Sync | Optionaler WebDAV-Abgleich. |
| Bibliothekswartung | Duplikat-/Orphan-Cleanup inkl. leerer `imported_*`-Ordner; keine „(Edition)"-Klon-Schleife mehr. |
| Library-UI-Redesign (Phase 20) | Top-Bar entschlackt, Filter- & Add-Bottom-Sheet, `library_management`-Screen, Zerlegung `LibraryScreen.kt`. |
| Release-Härtung & R8/ProGuard | R8/Minify & Resource-Shrinking aktiv (`isMinifyEnabled = true`, `isShrinkResources = true`), `proguard-rules.pro` für Room, Media3, Coil, Kotlinx Serialization, Widgets und Domain-Modelle verifiziert. |
| Room-Migrationstests | Automatisierte Migrationstests für alle Schema-Stufen v3→v11 (`DatabaseMigrationTest.kt`). |
| UI-Animationen & Transitions | Flüssige NavHost-Übergänge (Slide-Up/Down für Player Stage, horizontale Slides für Details/Settings/Management). |
| Instrumentation-Tests (`androidTest/`) | AndroidX Compose UI-Tests aufgesetzt (`AudiobookCardTest.kt`), TestRunner und Manifest konfiguriert. |
| Tablet- & Foldable-UX (Phase 23) | Adaptives 2-Spalten-Layout (Split View) für Details, Statistiken & Library; responsive adaptive Grid-Spalten (`GridCells.Adaptive`) mit Series-Span-Unterstützung. |

## Offen

| Priorität | Thema | Inhalt |
| --- | --- | --- |
| Später | Wear OS | Nicht geplant. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](12_teststrategie-und-qualitaetssicherung.md)
 · [Nächstes Kapitel →](14_migrations-und-konsolidierungsleitfaden.md)
