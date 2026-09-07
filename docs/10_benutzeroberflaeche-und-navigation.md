# 10. Benutzeroberfläche und Navigation

Alle Screens sind Jetpack Compose; die Navigation läuft über einen `NavHost` in `ui/AppRoot.kt` mit String-Routen.

| Screen (Route) | Kernfunktionen |
| --- | --- |
| Bibliothek (`library`) | Coverkarten, Titel, Autor, Fortschritt, Restzeit; Echtzeit-Suche (Titel/Autor/Sprecher); Status-Filter (Alle/Favoriten/Angefangen); Sortierung (Zuletzt gehört, Titel, Autor, Neu importiert `addedAt`, Restlaufzeit); Serien-Ansicht (Stapel/Regal/Ordner); Import, erneuter Scan, Aufräumen, Duplikate. |
| Hörbuchdetails (`details`) | Beschreibung, Metadaten-Editor, Cover (lokal/online), Kapitel, Lesezeichen, Figuren, Export, Wiedergabe starten, Löschen. |
| Player (`player`) | Großes Cover, Titel/Kapitel, Seekbar, Zeiten, `-10s`/`+10s`/`+30s`, Play/Pause, Kapitelwechsel, Geschwindigkeit, Lautheit/EQ, Sleep-Timer-Dialog, Auto-Modus öffnen. |
| Kapitel (`chapters`) | Kapitelliste mit Zeitstempeln, aktuelles Kapitel, direkter Sprung, Kapitel-Editor. |
| Lesezeichen (`bookmarks`) | Position, Zeitpunkt, optionale Notiz, Löschen und Anspringen. |
| Figuren (`characters/{audiobookId}`) | Charakterliste mit Rolle, Beschreibung, Beziehung, Hauptfigur-Markierung. |
| eBook-Info (`info`) | Zusätzliche Buch-/Metadaten-Ansicht. |
| Statistik (`statistics`) | Lokale Hörzeit, 7-Tage-Balkendiagramm, Streak. |
| Auto-Modus (`car_mode`) | Vereinfachte, große Steuerung für die Nutzung im Auto. |
| Einstellungen (`settings`) | Sprungweite, Standardgeschwindigkeit, Theme, Bibliotheksordner / `.abooklib`, Cache, gemerkte Scan-Ordner, WebDAV-Sync, Auto-Online-Cover. |

## 10.1 Visuelle Richtung

- Bibliothek: warmes Papier-/Buchgefühl mit ruhigen Karten.
- Player: dunkle, cinematic „Stage"-Darstellung mit Fokus auf Cover und Steuerung.
- Material 3 als technisches Designsystem; Theme-Modus in den Einstellungen (Standard: dunkel).
- JSX-Prototypen in `ABook_Player_Antigravity_Plan/` sind nur visuelle Referenz.

## 10.2 Barrierefreiheit

- Mindestens 48 dp große Touch-Ziele.
- Aussagekräftige `contentDescription` für Icons.
- Dynamische Schriftgrößen ohne abgeschnittene Inhalte.
- Ausreichender Kontrast, keine ausschließliche Farbcodierung.
- TalkBack-Reihenfolge und semantische Überschriften prüfen.

## 10.3 Hinweise zur laufenden Umstrukturierung

Die Trennung von „Bibliothek" (Hörfluss) und „Bibliothek verwalten" (Scan/Cleanup/Duplikate) sowie Top-Bar-Entschlackung und Filter-/Add-Bottom-Sheets sind in [`../ABookPlayer/redesign.md`](../ABookPlayer/redesign.md) spezifiziert (Phase 20, UI-Teil noch offen).

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](09_audio-wiedergabe.md)
 · [Nächstes Kapitel →](11_funktionsumfang-und-status.md)
