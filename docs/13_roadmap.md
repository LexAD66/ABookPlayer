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

## Offen

| Priorität | Thema | Inhalt |
| --- | --- | --- |
| P0 | Library-UI-Umbau (Phase 20, Teil B) | Top-Bar entschlacken, Filter- und Add-Bottom-Sheet, eigener `library_management`-Screen, Zerlegung von `LibraryScreen.kt`. Siehe [`../ABookPlayer/redesign.md`](../ABookPlayer/redesign.md). |
| P1 | Serienansicht als Modus | Nur bei „Sortierung = Serien" als eigener Navigationsmodus behandeln. |
| P1 | UX-Feinschliff | Empty States, Fortschrittsanzeige für Scan/Cleanup (`isScanning`/`isCleaning` an die UI binden), Tablet-Layouts. |
| P2 | Release-Härtung | R8/Minify für den Release-Build aktivieren und ProGuard-Regeln verifizieren; Migrationstests ergänzen. |
| P2 | Instrumentation-Tests | SAF-Import, MediaSessionService und Prozessneustart als `androidTest`. |
| Später | Wear OS | Nicht geplant. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](12_teststrategie-und-qualitaetssicherung.md)
 · [Nächstes Kapitel →](14_migrations-und-konsolidierungsleitfaden.md)
