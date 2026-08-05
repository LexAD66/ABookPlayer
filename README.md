# ABook Player Project

[![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/CHANGELOG.md)
[![Status](https://img.shields.io/badge/status-Release%201.1.0%20Stable-brightgreen.svg)](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/PROJECT_HISTORY.md)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/build.gradle.kts)

Willkommen beim **ABook Player** Projekt-Repository!

Dieses Repository enthält die native Android-App zur Wiedergabe von Hörbüchern im **.abook**-Containerformat sowie die vollständige, konsolidierte Projektdokumentation.

---

## 🚀 Key Features (Release 1.1.0)

- 🎧 **Media3 ExoPlayer & Android Auto:** Nahtlose Hintergrund-Wiedergabe, Automotive-Integration, variabler Speed (0.75x–2.0x) & Smart Rewind.
- 📦 **.abook Container & SAF:** Import & Export von `.abook`-Dateien (ZIP mit `manifest.json`, Cover & Audio).
- 📁 **Smarter Ordner-Scanner:** Automatische CD1/CD2 Unterordner-Zusammenführung & Duplikaterkennung.
- 🌙 **OLED True Black Theme & Paper Look:** Warmes Bibliotheks-Design & stromsparendes Reinschwarz-Design für den Player.
- ⏰ **Sleep Timer & Shake to Extend:** Abschalttimer (inkl. am Kapitelende) & Schütteln zum Verlängern (+15 Min.).
- 📊 **Lokale Hörstatistiken:** Tägliche/wöchentliche Auswertung & Streaks (100% datenschutzfreundlich).
- 📚 **Serien-Verwaltung & OpenLibrary Scraper:** Buchreihen-Sortierung, Metadaten-Editor & Cover-Download.
- ⚡ **Homescreen Widget, Skip Silence & FLAC Support:** Widgets, Stille überspringen, FLAC-Codec-Support & Mehrsprachigkeit (DE/EN).

---

## 📚 Offizielle Projektdokumentation

Die aktuelle und verbindliche Projektdokumentation befindet sich im Ordner [**docs/**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/README.md):

- 📖 [**Dokumentations-Hauptseite (docs/README.md)**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/README.md) – Übersicht aller 17 Kapitel.
- 📐 [**04_architektur.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/04_architektur.md) – Clean Architecture & Schichtenmodell.
- 🗄️ [**06_datenmodell.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/06_datenmodell.md) – Room-Datenbank, Entitäten und Migrationen.
- 📦 [**08_spezifikation-des-abook-formats.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/08_spezifikation-des-abook-formats.md) – Spezifikation des `.abook`-Containerformats.
- 🎵 [**09_audio-wiedergabe.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/09_audio-wiedergabe.md) – Media3 ExoPlayer Engine & Foreground Service.
- 🧪 [**12_teststrategie-und-qualitaetssicherung.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/12_teststrategie-und-qualitaetssicherung.md) – Testkonzept & Qualitätssicherung.

---

## 📂 Repository-Struktur

- 📱 [**ABookPlayer/**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/README.md)  
  Das vollständige Kotlin Android-Projekt (Jetpack Compose, Media3 ExoPlayer, Room, Clean Architecture).
- 📚 [**docs/**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/docs/README.md)  
  Die konsolidierte Projektdokumentation (Kapitel 01 bis 17).
- 📋 [**PROJECT_HISTORY.md**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/PROJECT_HISTORY.md)  
  Vollständige Chronologie und Nachweis aller abgeschlossenen Phasen (Phase 0 bis 15).
- 📐 [**ABook_Player_Antigravity_Plan/**](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABook_Player_Antigravity_Plan/)  
  Spezifikationen, UI-Prototypen und Architektur-Richtlinien.

---

## 🛠️ Build & Installation

Zum Bauen der Android Application (Release 1.1.0):

```bash
cd ABookPlayer
# Tests ausführen
.\gradlew.bat test

# Debug APK erstellen
.\gradlew.bat assembleDebug

# Release APK erstellen
.\gradlew.bat assembleRelease
```

---

## 📊 Aktueller Status

- **Entwicklungsstand:** 🎉 **100% abgeschlossen (Release 1.1.0 / Phasen 0–15)**
- **Build-Ergebnis:** 🟢 `BUILD SUCCESSFUL` (Debug & Release APKs)
- **Test-Ergebnis:** 🟢 Alle Unit-, ViewModel-, Storage-, Service- & DAO-Tests bestanden

---

## 🌐 Git-Veröffentlichung & Remote Push

Dieses Repository ist mit Git versioniert. Um das Projekt auf GitHub oder ein anderes Git-Remote zu veröffentlichen:

```bash
git remote add origin <DEIN_GIT_REPOSITORY_URL>
git branch -M main
git push -u origin main --tags
```
