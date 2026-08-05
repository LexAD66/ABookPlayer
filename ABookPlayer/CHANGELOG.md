# Changelog

Alle wichtigen Änderungen am ABook Player Projekt werden in dieser Datei dokumentiert.

## [1.1.0] — 2026-08-05 (Official Stable Release)

### 🚀 Highlights & Hauptfeatures
- **Vollständige Clean Architecture & Jetpack Compose UI:**
  - Modernes Material 3 Design im warmen "Paper-Look" für die Bibliothek und im OLED True Black Theme für den Player.
  - Reines Schwarz (`#000000`) für maximale AMOLED-Batterieersparnis.
- **Media3 ExoPlayer Audio Engine & Android Auto:**
  - `MediaLibraryService` Integration für nahtlose Hintergrundwiedergabe und Steuerung über Android Auto und Lockscreen/System-Notification.
  - Einstellbare Wiedergabegeschwindigkeit (0.75x – 2.0x), präzises Seeking (±10s) und intelligenter Rücksprung (Smart Rewind nach Pausen).
- **.abook Container & SAF Integration:**
  - Robuster Import und Kanonischer ZIP-Export von `.abook`-Dateien mit Zip-Bomb & Zip-Slip Security Protection.
  - FLAC, MP3, M4A & AAC Audioformat-Unterstützung.
- **Smarter Ordner-Scanner & CD-Zusammenführung:**
  - Automatischer Scan von entpackten Ordnerstrukturen inkl. intelligenter Zusammenführung von Unterordnern (`CD1`, `CD2`, `Disc 1` usw.) zu einem einzigen zusammenhängenden Hörbuch.
  - Duplikat-Erkennung zur Vermeidung redundanter Imports.
- **Serien- & Reihen-Verwaltung & Metadaten-Editor:**
  - Verwaltung von Buchreihen (`series` & `seriesOrder`), Sortierung und dedizierter Serien-FilterChip.
  - Metadaten-Dialog zum manuellen Bearbeiten von Titel, Autor, Sprecher, Serienname & Bandnummer.
  - Online Cover- & Metadaten-Scraper via OpenLibrary API.
- **Lokales Hörstatistik-Dashboard:**
  - 100% datenschutzfreundliches lokales Tracking der täglichen und wöchentlichen Hörzeiten mit Canvas-Balkendiagramm und Streak-Statistiken.
- **Erweiterter Sleep Timer & Shake-to-Extend:**
  - Abschalttimer (15m, 30m, 45m, 60m, am Kapitelende) mit Bewegungssensor-Unterstützung: Schütteln des Geräts verlängert den Timer automatisch um **+15 Minuten**.
- **Bibliotheks-Usability & Komfortfunktionen:**
  - Stapelverarbeitung (Multi-Select Batching) für Favoritisieren & Löschen.
  - Suche nach Sprechern, Neue Sortieroptionen (*Restlaufzeit*, *Hinzugefügt am*), Favoriten-Filter.
  - Stille überspringen (Skip Silence), Android Homescreen-Widget & Mehrsprachigkeit (Deutsch / Englisch).

---

## [0.0.25.10-alpha] — 2025-10-29
- Initiale Projektstruktur mit MVVM und Compose
- Media3 ExoPlayer + Foreground-Service
- Bibliotheksscan unter `/Download/ABook/` (wird automatisch erstellt)
- Room-Bookmarks (Kapitel, Position, Notiz)
- DataStore für Einstellungen
- Kapitel-Daueranalyse
- Grundlegende UI (Bibliothek, Player, Details) mit Navigation
