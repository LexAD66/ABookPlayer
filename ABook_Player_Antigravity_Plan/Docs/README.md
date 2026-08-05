# 📚 ABook Player

Ein moderner, offline-fähiger Hörbuch-Player für Android – gebaut mit Jetpack Compose, Material 3, Media3/ExoPlayer und Clean Architecture.

---

## 🎯 Projektziel

ABook Player ermöglicht das Abspielen lokaler Hörbuch-Dateien, den Import von `.abook`-Archiven, die persistente Speicherung von Wiedergabepositionen sowie die komfortable Verwaltung von Bibliotheken und Ordnern – optimiert für lange Hörsitzungen.

---

## 🛠️ Tech Stack

| Bereich                | Technologie                              |
|------------------------|------------------------------------------|
| Sprache                | Kotlin                                   |
| UI                     | Jetpack Compose + Material 3             |
| Architektur            | MVVM + Clean Architecture                |
| Dependency Injection   | Hilt                                     |
| Datenbank              | Room                                     |
| Medienwiedergabe       | Android Media3 / ExoPlayer               |
| Hintergrundwiedergabe  | MediaSessionService + ForegroundService  |
| Build System           | Gradle Kotlin DSL                        |
| Min SDK                | 26 (Android 8.0)                         |
| Target SDK             | 35 (Android 15)                          |

---

## 🎵 Unterstützte Formate

- **MP3** – Standard-Audioformat
- **M4B** – Hörbuch-Format mit Kapitelmarken
- **M4A** – AAC-Audio im MPEG-4-Container
- **AAC** – Advanced Audio Coding
- **FLAC** – (zukünftig, verlustfrei)
- **.abook** – Eigenes Hörbuch-Archivformat (ZIP-basiert)

---

## ✨ Hauptfunktionen

### 📖 Bibliothek
- Ordner nach Hörbuch-Dateien durchsuchen
- Einzelne Dateien oder ganze Ordner importieren
- `.abook`-Archive importieren und verwalten
- Hörbücher gruppieren und anzeigen
- Fortschritt, Cover, Autor, Titel anzeigen

### ▶️ Wiedergabe
- Abspielen / Pausieren / Fortsetzen
- 10 s zurück und 30 s vor als primäre Hörbuch-Sprünge
- 1 min vor/zurück als erweiterte Steuerung
- Kapitelnavigation (M4B + .abook)
- Wiedergabegeschwindigkeit (0,5x – 3,0x)
- Schlaf-Timer
- Stille überspringen (optional)
- Letzte Position automatisch merken

### 🔖 Lesezeichen
- Lesezeichen erstellen und benennen
- Notizen hinzufügen
- Direkt zur gespeicherten Position springen

### 🔔 Hintergrundwiedergabe
- Wiedergabe im Hintergrund fortsetzen
- Sperrbild-Steuerung
- Benachrichtigungs-Controls
- Bluetooth / Headset-Tasten

### ⚙️ Einstellungen
- Standard-Wiedergabegeschwindigkeit
- Stille überspringen
- Theme (Hell / Dunkel / System)
- Bibliotheksverwaltung
- Importverhalten für `.abook`

---

## 🎨 Visuelles Konzept

Vollständige Beschreibung: siehe [VisualConcept.md](VisualConcept.md)

Die App verwendet drei klar getrennte visuelle Rollen:

| Rolle | Screens | Ziel |
|---|---|---|
| Warmes Bibliotheks-System | Home, Bibliothek, Hörbuchdetails, Import, Lesezeichen | Ruhige Scanbarkeit, Buch-/Papiergefühl, gute Lesbarkeit |
| Dunkles Stage-System | Wiedergabe, Kapitel-Auswahl im Player, Schlaf-Timer, Hörstatistik | Fokus auf Cover, Fortschritt und große Steuerung |
| Neutrales Material-3-System | Einstellungen, systemnahe Dialoge | Funktionale Konfiguration ohne visuelle Überladung |

Alle sichtbaren UI-Texte sind deutsch. Der sichtbare Produktname ist **ABook Player**. Alte Smart-Audiobook-Player-Screens und JSX-Prototypen sind nur Referenzmaterial, keine finale Android-Implementierung.

---

## 📁 Projektstruktur (Übersicht)

```
app/
├── data/
│   ├── abook/
│   │   ├── archive/          # ZIP-Verarbeitung und Validierung
│   │   ├── parser/           # Manifest-Parser (XML + JSON)
│   │   └── importer/         # Import-Workflow
│   ├── database/
│   │   ├── entities/         # Room-Entitäten
│   │   └── dao/              # Data Access Objects
│   ├── repository/           # Repository-Implementierungen
│   ├── scanner/              # Ordner- und Datei-Scanner
│   ├── player/               # ExoPlayer-Verwaltung
│   └── storage/              # Datei- und Dokumentzugriff
├── domain/
│   ├── model/                # Domain-Modelle
│   ├── repository/           # Repository-Schnittstellen
│   └── usecase/              # Anwendungsfälle
├── presentation/
│   ├── library/              # Bibliotheks-Screen
│   ├── player/               # Player-Screen
│   ├── bookmarks/            # Lesezeichen-Screen
│   ├── imports/              # Import-Screen
│   ├── settings/             # Einstellungs-Screen
│   └── home/                 # Home-Screen
└── service/                  # MediaSessionService
```

Vollständige Struktur: siehe [FolderStructure.md](FolderStructure.md)

---

## 🚀 Einrichtung

### Voraussetzungen
- Android Studio Meerkat (2024.3.1) oder neuer
- JDK 17+
- Android SDK 35
- Kotlin 2.0+

### Projekt klonen und öffnen

```bash
git clone https://github.com/yourorg/audiobookplayer.git
cd audiobookplayer
```

Projekt in Android Studio öffnen → Gradle sync abwarten → auf Gerät oder Emulator starten.

### Berechtigungen

Das App benötigt folgende Berechtigungen:

```xml
<!-- Medienzugriff (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- Speicherzugriff (bis Android 12) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Vordergrund-Service für Wiedergabe -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- Benachrichtigungen (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Wakelock für Hintergrundwiedergabe -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 🏗️ Architektur

Vollständige Beschreibung: siehe [Architecture.md](Architecture.md)

Das Projekt folgt **Clean Architecture** mit drei Hauptschichten:

```
Presentation Layer  ←→  Domain Layer  ←→  Data Layer
(ViewModels, UI)        (UseCases)        (Repositories, DB, Player)
```

- **Presentation**: Jetpack Compose Screens, ViewModels, UI State
- **Domain**: Pure Kotlin, keine Android-Abhängigkeiten, Use Cases
- **Data**: Room, ExoPlayer, .abook-Importer, Datei-Scanner

---

## 📦 .abook Format

Das `.abook`-Format ist ein ZIP-basiertes Hörbuch-Containerformat.

Vollständige Dokumentation: siehe [ABOOK_FORMAT.md](ABOOK_FORMAT.md)

Kurzübersicht:
- ZIP-Archiv mit Audio-Dateien, Manifest und Cover
- Manifest als `manifest.xml` oder `manifest.json`
- Definiert Metadaten und geordnete Trackliste
- Wird validiert, importiert und wie ein normales Hörbuch abgespielt

---

## 🧪 Tests

Testbeispiele befinden sich in `app/src/test/` und `app/src/androidTest/`.

Abgedeckte Bereiche:
- Manifest-Parser (XML + JSON)
- Archiv-Validierung und ZIP-Slip-Sicherheit
- Import-Workflow
- Repository-Tests
- ViewModel-Tests
- Fehlerszenarien (fehlendes Manifest, fehlende Dateien, ungültige Archive)

---

## 📄 Dokumentation

| Datei                  | Inhalt                                         |
|------------------------|------------------------------------------------|
| [README.md](README.md)             | Projektübersicht (diese Datei)     |
| [Architecture.md](Architecture.md) | Architektur und Schichten           |
| [FolderStructure.md](FolderStructure.md) | Vollständige Ordnerstruktur   |
| [FeatureList.md](FeatureList.md)   | Alle Features mit Details           |
| [VisualConcept.md](VisualConcept.md) | Verbindliches UI-/UX-Zielbild |
| [ABOOK_FORMAT.md](ABOOK_FORMAT.md) | .abook Formatspezifikation          |
| [ImportFlow.md](ImportFlow.md)     | Import-Workflow Schritt für Schritt |

---

## 📝 Lizenz

Dieses Projekt steht unter der MIT-Lizenz. Siehe `LICENSE` für Details.

---

## 🤝 Beitragen

Pull Requests sind willkommen. Bitte erst ein Issue öffnen, um größere Änderungen zu besprechen.

---

*Entwickelt mit ❤️ für Hörbuch-Liebhaber.*
