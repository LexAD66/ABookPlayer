# 6. Datenmodell

| Entität | Wichtige Felder | Hinweise |
| --- | --- | --- |
| AudiobookEntity | id, title, author, narrator, description, coverPath/coverUri, sourceUri, rootTreeUri, totalDurationMs, createdAt, updatedAt, lastPlayedAt | ID stabil; Quelle und Verfügbarkeit getrennt behandeln. |
| ChapterEntity | id, audiobookId, title, uri/path, durationMs, chapterIndex | FK zu Hörbuch; feste Reihenfolge; stabile ID aus Quelle ableiten. |
| PlaybackProgressEntity | audiobookId, chapterId, positionMs, updatedAt | Ein Datensatz pro Hörbuch; häufige Schreibvorgänge drosseln. |
| BookmarkEntity | id, audiobookId, chapterId, positionMs, note, createdAt | Mehrere Einträge pro Hörbuch; Sprung direkt zur Position. |
| ListeningStatsEntity oder Event | audiobookId, listenedMs, sessionStart, sessionEnd | Optional; für Tages-/Wochenstatistik und Gesamtzeit. |

## 6.1 Stabile IDs und Migration

Track- oder Kapitel-IDs dürfen nicht allein aus Listenpositionen bestehen. Empfohlen ist ein deterministischer Schlüssel aus Hörbuch-ID plus normalisiertem relativem Pfad. Beim erneuten Scan desselben rootTreeUri werden vorhandene Datensätze aktualisiert statt dupliziert. Schemaänderungen erhalten explizite Room-Migrationen und Migrationstests.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](05_empfohlene-projekt-und-paketstruktur.md)
 · [Nächstes Kapitel →](07_import-und-storage.md)
