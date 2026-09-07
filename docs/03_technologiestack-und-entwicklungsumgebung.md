# 3. Technologiestack und Entwicklungsumgebung

| Bereich | Ist-Zustand |
| --- | --- |
| Sprache | Kotlin 2.0.20 |
| UI | Jetpack Compose (BOM 2025.01.00) + Material 3, `material-icons-extended` |
| Architektur | MVVM mit praktischer Clean-Architecture-Trennung |
| Zustand | ViewModel, StateFlow / SharedFlow, Coroutines 1.9.0 |
| Navigation | Navigation Compose 2.8.x (String-Routen in `ui/AppRoot.kt`) |
| Audio | AndroidX Media3 1.4.1: ExoPlayer, `media3-session`, `MediaLibraryService` |
| Persistenz | Room 2.6.1 (+ `room-ktx`, KSP-Compiler), Schema-Version 11 |
| Einstellungen | `androidx.datastore:datastore-preferences` + klassische `SharedPreferences` |
| Dependency Injection | **Manuell** – `getInstance`-Singletons (`AbookDatabase`, `PlaybackController`) in `MainActivity`, ViewModels via `remember { … }` in `AppRoot`. **Kein Hilt/Dagger.** |
| Dateizugriff | Storage Access Framework mit persistierbaren URI-Berechtigungen, `androidx.documentfile` |
| Serialisierung | `kotlinx-serialization-json` 1.7.3 (Kotlin-Serialization-Plugin) |
| Bildladen | Coil (`coil-compose` 2.7.0) |
| Build | AGP 8.5.2, KSP 2.0.20-1.0.25, compileSdk/targetSdk 35, minSdk 24, `versionName` 2.0.0 / `versionCode` 300 |
| Release-Build | `isMinifyEnabled = false`; `proguard-rules.pro` vorhanden, R8 aktuell nicht aktiv |
| Tests | JUnit 4, MockK 1.13.13, `kotlinx-coroutines-test`, **Robolectric 4.14.1**, `room-testing`. Alle Tests laufen auf der JVM; **kein `androidTest`-/Instrumentation-Verzeichnis** |
| JDK / JVM-Target | 21 |

## 3.1 Grundregeln für Änderungen

- Kleine, kompilierbare Schritte.
- Keine Architekturwechsel ohne ausdrücklichen Auftrag (insbesondere kein Wechsel auf Hilt „nebenbei").
- Keine unnötigen Libraries.
- Keine Dateien löschen, bevor eine begründete Lösch- oder Archivierungsliste vorliegt.
- UI-Texte auf Deutsch aus String-Ressourcen; deutsche Kotlin-Kommentare nur dort, wo sie echten Kontext liefern.
- Zuerst analysieren, anschließend gezielt ändern und die Prüfungen aus [`../CLAUDE.md`](../CLAUDE.md) ausführen.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](02_projektueberblick.md)
 · [Nächstes Kapitel →](04_architektur.md)
