# 2. Projektüberblick

ABook Player ist eine offline-first Android-App zum Importieren, Organisieren und Abspielen lokaler Hörbücher. Im Mittelpunkt stehen zuverlässige Hintergrundwiedergabe, dauerhaft gespeicherter Fortschritt, einfache Bedienung und ein portables ZIP-basiertes `.abook`-Containerformat.

## 2.1 Ziele

- Lokale Hörbücher über Storage Access Framework, Ordner-Scan oder `.abook`-Datei importieren.
- Bibliothek mit Cover, Titel, Autor, Fortschritt, Restzeit, Suche, Status-Filter, Serien- und Sortier-Ansicht bereitstellen.
- Stabile Wiedergabe mit Media3, MediaSession, Benachrichtigung und Screen-Off-Unterstützung.
- Fortschritt, Kapitel, Lesezeichen, Geschwindigkeit, Lautheit/Equalizer und Sleep Timer verwalten.
- Saubere, wartbare Kotlin-Codebasis mit MVVM, Repository-Schicht und Room als Single Source of Truth.
- Deutsche UI-Texte, große Touch-Ziele und barrierearme Bedienung.

## 2.2 Kernmerkmale (Ist-Zustand)

- **Import:** SAF-Datei, SAF-Ordner (rekursiv, CD1/CD2-Zusammenführung), `.abook`/`.zip`-Container, Scan eines Standardordners. Importierte Ordner werden in einen app-verwalteten Bibliotheksordner kopiert (`imported_*`).
- **Player:** Media3-`MediaLibraryService` als Foreground Service, Hintergrundwiedergabe, MediaSession-/Notification-Steuerung, Geschwindigkeit 0,75×–2,0×, ±10 s / +30 s, Kapitelsprung, Smart Rewind, Skip Silence, Lautheits-Boost/Equalizer-Presets.
- **Komfort:** Sleep Timer inkl. „Kapitelende", Shake-to-Extend, Smart Sleep Bookmark, Homescreen-Widgets, Hörstatistik (lokal), Figuren-/Charakterverwaltung, Kapitel-Editor, eBook-Info.
- **Sync:** optionaler WebDAV-Abgleich des Hörfortschritts zwischen Geräten (kein Pflicht-Server).
- **Android Auto:** eigener Auto-Modus, MediaLibraryService-Browsing, app-lokale `-10s`/`+10s`-Custom-Actions.

## 2.3 Nicht-Ziele

- Kein Login und keine Benutzerkonten.
- Kein DRM- oder Streaming-Katalog.
- Keine Pflicht-Abhängigkeit von einem externen Server (WebDAV-Sync ist optional und rein für den Fortschritt).
- Kein Wear OS.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](01_dokumentstatus-und-konsolidierungsregeln.md)
 · [Nächstes Kapitel →](03_technologiestack-und-entwicklungsumgebung.md)
