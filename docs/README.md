# ABook Player – Projektdokumentation

Diese Dokumentation wurde aus der konsolidierten Word-Gesamtdokumentation in mehrere Markdown-Dateien aufgeteilt.

## Verwendung

- `README.md` dient als Einstieg und Inhaltsverzeichnis.
- Die Dateien `01_...md` bis `17_...md` enthalten jeweils ein Hauptkapitel.
- Relative Links funktionieren in GitHub, GitLab, VS Code und Android Studio.
- Für KI-Aufgaben sollten nur die jeweils benötigten Kapitel zusammen mit der konkreten Aufgabe geladen werden.

## Dokumentstatus

ABOOK PLAYER

Konsolidierte Projektdokumentation

Zusammenführung der Entwicklungsdokumentation und des Antigravity-Plans

| Dokument | Angabe |
| --- | --- |
| Projekt | Offline-first Android-Hörbuchplayer für lokale Dateien und .abook-Container |
| Zielplattform | Android, Kotlin, Jetpack Compose, Material 3, Media3, Room, Hilt |
| Paketname | de.f_soft_studio.abookplayer |
| Zielsprache der Oberfläche | Deutsch |
| Dokumentstand | 1. August 2026 |
| Quellen | ABook_Player_Antigravity_Plan.zip und docs(1).zip |

Zweck: Eine einzige verbindliche Dokumentation ohne doppelte oder widersprüchliche Teilstände.

## Inhaltsverzeichnis

- [1. Dokumentstatus und Konsolidierungsregeln](01_dokumentstatus-und-konsolidierungsregeln.md)
- [2. Projektüberblick](02_projektueberblick.md)
- [3. Technologiestack und Entwicklungsumgebung](03_technologiestack-und-entwicklungsumgebung.md)
- [4. Architektur](04_architektur.md)
- [5. Empfohlene Projekt- und Paketstruktur](05_empfohlene-projekt-und-paketstruktur.md)
- [6. Datenmodell](06_datenmodell.md)
- [7. Import und Storage](07_import-und-storage.md)
- [8. Spezifikation des .abook-Formats](08_spezifikation-des-abook-formats.md)
- [9. Audio-Wiedergabe](09_audio-wiedergabe.md)
- [10. Benutzeroberfläche und Navigation](10_benutzeroberflaeche-und-navigation.md)
- [11. Funktionsumfang und Status](11_funktionsumfang-und-status.md)
- [12. Teststrategie und Qualitätssicherung](12_teststrategie-und-qualitaetssicherung.md)
- [13. Roadmap](13_roadmap.md)
- [14. Migrations- und Konsolidierungsleitfaden](14_migrations-und-konsolidierungsleitfaden.md)
- [15. Master-Prompt für Gemini Flash 2.5](15_master-prompt-fuer-gemini-flash-25.md)
- [16. Quellenzuordnung](16_quellenzuordnung.md)
- [17. Schlussfolgerung](17_schlussfolgerung.md)

## Empfohlene Kapitel für typische Aufgaben

| Aufgabe | Relevante Dateien |
| --- | --- |
| Architektur oder Refactoring | `04_architektur.md`, `05_empfohlene-projekt-und-paketstruktur.md`, `06_datenmodell.md` |
| Import, SAF oder Dateiscan | `07_import-und-storage.md`, `08_spezifikation-des-abook-formats.md` |
| Player und Media3 | `09_audio-wiedergabe.md`, `12_teststrategie-und-qualitaetssicherung.md` |
| Compose-Oberfläche | `10_benutzeroberflaeche-und-navigation.md` |
| Projektplanung | `11_funktionsumfang-und-status.md`, `13_roadmap.md`, `14_migrations-und-konsolidierungsleitfaden.md` |
| Gemini-Auftrag | `15_master-prompt-fuer-gemini-flash-25.md` plus fachlich relevante Kapitel |
