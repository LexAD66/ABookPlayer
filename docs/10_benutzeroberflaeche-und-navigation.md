# 10. Benutzeroberfläche und Navigation

| Screen | Kernfunktionen |
| --- | --- |
| Bibliothek | Coverkarten, Titel, Autor, Fortschritt, Restzeit, Suche, Sortierung, Import und erneuter Scan. |
| Hörbuchdetails | Beschreibung, Metadaten, Kapitel, Lesezeichen, Wiedergabe starten, Löschen oder neu verknüpfen. |
| Player | Großes Cover, Titel/Kapitel, Seekbar, Zeiten, ±10 s, Play/Pause, Kapitelwechsel, Geschwindigkeit, Sleep Timer. |
| Kapitel | Kapitelreihenfolge, Dauer, aktuelles Kapitel und direkter Sprung. |
| Lesezeichen | Position, Kapitel, Zeitpunkt, optionale Notiz, Löschen und Anspringen. |
| Einstellungen | Sprungweiten, Standardgeschwindigkeit, Theme, Import-/Cacheoptionen. |

## 10.1 Visuelle Richtung

- Bibliothek: warmes Papier-/Buchgefühl mit ruhigen Karten.

- Player: dunkle, cinematic „Stage“-Darstellung mit Fokus auf Cover und Steuerung.

- Material 3 als technisches Designsystem, Prototypen nur als visuelle Referenz.

- Dark Mode bevorzugt, Light Mode möglich.

## 10.2 Barrierefreiheit

- Mindestens 48 dp große Touch-Ziele.

- Aussagekräftige contentDescription für Icons.

- Dynamische Schriftgrößen ohne abgeschnittene Inhalte.

- Ausreichender Kontrast und keine ausschließliche Farbcodierung.

- TalkBack-Reihenfolge und semantische Überschriften prüfen.

## 10.3 Navigationsrouten

```text
library
book/{audiobookId}
player/{audiobookId}
chapters/{audiobookId}
bookmarks/{audiobookId}
settings
```

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](09_audio-wiedergabe.md)
 · [Nächstes Kapitel →](11_funktionsumfang-und-status.md)
