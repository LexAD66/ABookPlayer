# 6. Datenmodell

Room-Datenbank `abook_database.db`, `@Database`-Version **11**. Fünf Entities; Fremdschlüssel auf `audiobooks(id)` mit `ON DELETE CASCADE` (bei `chapters`, `bookmarks`, `characters`; `listening_sessions` ohne FK).

| Entität (Tabelle) | Felder | Hinweise |
| --- | --- | --- |
| `AudiobookEntity` (`audiobooks`) | `id`, `title`, `author`, `narrator?`, `filePath`, `coverUri?`, `description?`, `duration`, `currentPosition`, `lastPlayed`, `addedAt`, `parentSeries?`, `series?`, `seriesOrder?`, `isFavorite`, `customSpeed?`, `equalizerPreset?` | Hörfortschritt liegt direkt hier (`currentPosition`, `duration`). `filePath` = `.abook`-Datei, Einzeldatei **oder** Ordner. `addedAt` seit v10 für Sortierung „Neu importiert". |
| `ChapterEntity` (`chapters`) | `id`, `audiobookId`, `title`, `startTime`, `audioPath?` | `startTime` = globale Startposition in ms. `audioPath` je Kapitel bei Mehrdatei-Hörbüchern, sonst `null` (eine Quelle). Titel ohne Datei-Endung (Migration 10→11 bereinigt Altbestände). |
| `BookmarkEntity` (`bookmarks`) | `id`, `audiobookId`, `position`, `note`, `createdAt` | Mehrere pro Hörbuch; Sprung direkt zur `position`. |
| `ListeningSessionEntity` (`listening_sessions`) | `id`, `audiobookId`, `date` (`YYYY-MM-DD`), `durationSeconds`, `timestamp` | Grundlage für Tages-/Wochen-Statistik und Streak. |
| `CharacterEntity` (`characters`) | `id`, `audiobookId`, `name`, `role`, `description`, `relationship`, `isPrimary` | Figuren-/Charakterverwaltung pro Hörbuch. |

## 6.1 Migrationsleiter

| Migration | Änderung |
| --- | --- |
| 3→4 | `audiobooks.narrator` |
| 4→5 | Struktur-/Indexpflege |
| 5→6 | `audiobooks.series`, `audiobooks.seriesOrder` |
| 6→7 | Tabelle `characters` + Index |
| 7→8 | `audiobooks.customSpeed`, `audiobooks.equalizerPreset` |
| 8→9 | `audiobooks.isFavorite`, `audiobooks.parentSeries` |
| 9→10 | `audiobooks.addedAt` (+ Backfill aus `lastPlayed` bzw. „jetzt") |
| 10→11 | `chapters.title` von Audio-Datei-Endungen bereinigen + `TRIM` |

## 6.2 Duplikate und stabile Datensätze

Beim erneuten Scan wird ein bestehendes Hörbuch anhand von `filePath` bzw. Titel/Autor erkannt (`DuplicateDetector`). Eine **unbekannte Dauer** (`duration <= 0`, z. B. weil Dateien nicht lesbar sind) gilt nicht als Unterscheidungsmerkmal – so entsteht kein neuer „(Edition)"-Datensatz bei jedem Scan. Schemaänderungen erhalten stets explizite Room-Migrationen.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](05_empfohlene-projekt-und-paketstruktur.md)
 · [Nächstes Kapitel →](07_import-und-storage.md)
