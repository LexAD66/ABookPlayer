# Projektentscheidungen

## App-Typ
- Offline-first Android-App für lokale Hörbücher.

## Technologie
- Kotlin als Hauptsprache.
- Jetpack Compose für UI.
- Material 3 für Designsystem.
- Room für lokale Datenbank.
- Media3 ExoPlayer für Wiedergabe.
- Hilt für Dependency Injection.
- Coroutines und StateFlow für asynchrone Datenströme.

## Architektur
- MVVM.
- Repository als Datenzugriffsschicht.
- Room als Single Source of Truth.
- Player-Logik getrennt von UI.

## Daten
- Ein Hörbuch kann mehrere Kapitel haben.
- Ein Hörbuch kann mehrere Bookmarks haben.
- Fortschritt wird pro Hörbuch gespeichert.
- Letzte Wiedergabezeit wird für Sortierung genutzt.

## Design
- Deutsche UI-Texte.
- Dark Mode wird bevorzugt.
- Große Touch-Ziele für Player-Steuerung.
- 10-Sekunden-Skip statt Song-orientierter Steuerung.
- Vorhandene Design-Prototypen sind Referenz, nicht finaler Code.

## Nicht-Ziele im MVP
- Kein Cloud-Sync.
- Kein Login.
- Kein Streaming-Katalog.
- Kein Android Auto im ersten Schritt.
- Kein Wear OS im ersten Schritt.
