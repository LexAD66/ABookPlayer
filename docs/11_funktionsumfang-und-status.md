# 11. Funktionsumfang und Status

| Bereich | Release 1.1.0 Status | Bemerkung |
| --- | --- | --- |
| Projektkonsolidierung | 🟢 Abgeschlossen | Trennung von Designprototypen (`ABook_Player_Antigravity_Plan`) und nativem Android-Projekt (`ABookPlayer`). |
| Android-Grundlage | 🟢 Abgeschlossen | AGP 8.5.2, SDK 35, Jetpack Compose, Material 3, Hilt/Media3, 100% grüne Test-Suite. |
| Room / Repositories / UseCases | 🟢 Abgeschlossen | Room DB v6 (Audiobooks, Chapters, Bookmarks, ListeningSessions), Migrationen MIGRATION_4_5 & MIGRATION_5_6, Repositories und Clean UseCases. |
| Bibliothek & Usability | 🟢 Abgeschlossen | Warmes Paper-Look Theme, Echtzeit-Suche (Titel/Autor/Sprecher), Sortierung, Favoriten-Filter, Multi-Select Stapelverarbeitung & Ordner-Scan. |
| Player & Audio Engine | 🟢 Abgeschlossen | Media3 ExoPlayer, MediaLibraryService (Background Playback & Android Auto), OLED True Black Theme, Speed 0.75x–2.0x, ±10s Skip, Smart Rewind. |
| Kapitel & Lesezeichen | 🟢 Abgeschlossen | Kapitelnavigation mit Zeitstempeln, Lesezeichen an aktueller Position mit Notizfunktion. |
| Sleep Timer & Shake to Extend | 🟢 Abgeschlossen | Abschalttimer (15m, 30m, 45m, 60m, am Kapitelende) mit Bewegungssensor (+15 Min. bei Schütteln). |
| Storage, SAF & Export | 🟢 Abgeschlossen | Atomare `.abook`-Entpackung, SAF import/export, Zip-Bomb Protection, FLAC-Unterstützung & FolderScanner mit CD1/CD2 Zusammenführung. |
| Serien & Metadaten | 🟢 Abgeschlossen | Buchreihen-Verwaltung (`series` & `seriesOrder`), Metadaten-Editor Dialog & OpenLibrary Cover Scraper. |
| Hörstatistik | 🟢 Abgeschlossen | 100% lokales, datenschutzfreundliches Tracking mit 7-Tage-Canvas-Balkendiagramm und Streak-Auswertung. |
| System-Integration | 🟢 Abgeschlossen | Android Homescreen Widget, Skip Silence (Stille überspringen), i18n (DE/EN). |
| Release-Build & QS | 🟢 Abgeschlossen | Debug & Release APKs verifiziert (`BUILD SUCCESSFUL`), R8/ProGuard Regeln eingerichtet, Unit- & Integrationstestabdeckung 100% grün. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](10_benutzeroberflaeche-und-navigation.md)
 · [Nächstes Kapitel →](12_teststrategie-und-qualitaetssicherung.md)
