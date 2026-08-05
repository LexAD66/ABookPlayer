# 3. Technologiestack und Entwicklungsumgebung

| Bereich | Festlegung |
| --- | --- |
| Sprache | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architektur | MVVM mit praktischer Clean-Architecture-Trennung |
| Zustand | ViewModel, StateFlow, Coroutines |
| Navigation | Navigation Compose |
| Audio | AndroidX Media3: ExoPlayer, MediaSession, MediaSessionService |
| Persistenz | Room |
| Dependency Injection | Hilt |
| Dateizugriff | Storage Access Framework mit persistierbaren URI-Berechtigungen |
| Bildladen | Coil oder gleichwertige Compose-Integration |
| Tests | JUnit, Coroutine-Test, Room-Tests; Instrumentation für SAF/Service |
| JDK | 21 |
| IDE-Ziel | Android Studio Otter \| 2025.2.1 |

## 3.1 Grundregeln für Änderungen

- Kleine, kompilierbare Schritte.

- Keine Architekturwechsel ohne ausdrücklichen Auftrag.

- Keine unnötigen Libraries.

- Keine Dateien löschen, bevor eine begründete Lösch- oder Archivierungsliste vorliegt.

- UI-Texte auf Deutsch; Kotlin-Code ausführlich auf Deutsch kommentieren.

- Zuerst analysieren, anschließend gezielt ändern und Tests ausführen.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](02_projektueberblick.md)
 · [Nächstes Kapitel →](04_architektur.md)
