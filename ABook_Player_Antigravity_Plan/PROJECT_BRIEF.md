# ABook Player – Projekt-Brief

## Ziel
Eine Android-App zum lokalen Abspielen von Hörbüchern. Dieses Repository enthält aktuell vor allem Design-Prototypen, Screenshots und UI-Referenzen. Es dient als konsolidierte Basis für ein späteres Antigravity/Android-Studio-Projekt.

## Zielplattform
- Android Studio Otter 2025.2.1
- Kotlin
- Jetpack Compose
- Material 3
- Room
- Media3 ExoPlayer
- Hilt
- Coroutines / StateFlow
- MVVM

## MVP-Funktionen
- Lokale Hörbuch-Bibliothek
- Import von MP3/M4B-Dateien
- Player mit Play, Pause, ±10 Sekunden
- Fortschritt pro Hörbuch speichern
- Kapitel anzeigen
- Bookmarks speichern
- Sleep Timer

## UI/UX-Richtung
Dieses ZIP enthält zwei Designrichtungen:

1. **Library**
   - Warm
   - Papierartig
   - Literarisch
   - Gut für Bibliothek, Kapitel, Bookmarks

2. **Stage**
   - Dunkel
   - Cinematic
   - Cover-Art im Fokus
   - Gut für Player, Sleep Timer, Statistiken

## Screens im Prototyp
- Library Screen
- Player Screen
- Chapters Screen
- Characters Screen
- Bookmarks Screen
- Sleep Timer Screen
- Listening Stats Screen

## Verbindliche App-Architektur
```text
app/
├── data/
│   ├── local/
│   │   ├── entity/
│   │   ├── dao/
│   │   └── db/
│   └── repository/
├── domain/
│   ├── model/
│   └── usecase/
├── player/
│   ├── service/
│   └── controller/
└── ui/
    ├── library/
    ├── player/
    ├── chapters/
    ├── bookmarks/
    ├── sleep/
    └── theme/
```

## Datenstruktur
```text
Audiobook
├── id
├── title
├── author
├── filePath
├── coverUri
├── duration
├── currentPosition
└── lastPlayed

Chapter
├── id
├── audiobookId
├── title
└── startTime

Bookmark
├── id
├── audiobookId
├── position
├── note
└── createdAt
```

## Ergebnisziel
Aus den vorhandenen Design-Prototypen soll eine echte Android-App entstehen. Alte Varianten werden nicht vollständig übernommen, sondern nur als UI- und UX-Referenz verwendet.
