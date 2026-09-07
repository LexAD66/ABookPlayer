# 15. Master-Prompt für KI-Weiterentwicklung

Der folgende Prompt ist für Verbesserungen und kontrollierte Weiterentwicklung vorgesehen. Ergänzend gelten [`../CLAUDE.md`](../CLAUDE.md) und [`../ABook_Player_Antigravity_Plan/AGENTS.md`](../ABook_Player_Antigravity_Plan/AGENTS.md).

```text
Du arbeitest am Projekt „ABook Player“, einer offline-first Android-App für lokale Hörbücher.
```

```text
Entwicklungsumgebung:
- JDK / JVM-Target 21, Windows 11, Gradle-Root: ABookPlayer/ (gradlew.bat)
- AGP 8.5.2, Kotlin 2.0.20, KSP, compileSdk/targetSdk 35, minSdk 24
- Kotlin, Jetpack Compose (BOM 2025.01.00), Material 3
- Media3 1.4.1: ExoPlayer + MediaLibraryService
- Room 2.6.1 (Schema v11), Coroutines, StateFlow/SharedFlow
- Dependency Injection: MANUELL (getInstance-Singletons + remember), KEIN Hilt
- Paketname: de.f_soft_studio.abookplayer
```

```text
Verbindliche Regeln:
1. Analysiere zuerst die vorhandene Projektstruktur und den aktuellen Code.
2. Ändere nur Dateien, die für die konkrete Aufgabe erforderlich sind.
3. Erzeuge kleine, kompilierbare Schritte; keine komplette Neugenerierung.
4. Behalte MVVM, Repository-Schicht und Room als Single Source of Truth bei.
5. Verwende ausschließlich deutsche UI-Texte aus string resources.
6. Kommentiere neuen oder komplexen Kotlin-Code ausführlich auf Deutsch.
7. Füge keine Library hinzu, wenn die vorhandenen Mittel genügen.
8. Lösche keine Datei ohne vorherige Liste mit Begründung und Abhängigkeitsprüfung.
9. Berücksichtige Scoped Storage, persistierbare SAF-Berechtigungen und stabile IDs.
10. Für .abook gilt manifest.json oder manifest.xml als kanonisch; Legacy-metadata.json wird im Parser weiterhin erkannt.
11. Playeränderungen müssen Hintergrundwiedergabe, MediaSession, Audio-Focus und Fortschrittsspeicherung erhalten.
12. Erstelle oder aktualisiere passende Unit-Tests.
```

```text
Arbeitsablauf:
A. Nenne kurz den Ist-Zustand und das konkrete Problem.
B. Erstelle einen kleinen Änderungsplan.
C. Zeige alle betroffenen Dateien.
D. Implementiere die Änderung vollständig.
E. Erkläre wichtige Codeentscheidungen auf Deutsch.
F. Nenne die auszuführenden Prüfungen:
   - .\gradlew.bat test
   - .\gradlew.bat assembleDebug
   - bei Release-relevanten Änderungen zusätzlich .\gradlew.bat assembleRelease
G. Aktualisiere Dokumentation oder TODO, falls sich Verhalten oder Architektur ändert.
```

```text
Aktuelle Aufgabe:
[HIER DIE KONKRETE AUFGABE EINFÜGEN]
```

```text
Erwartete Ausgabe:
- Keine Platzhalterlösung.
- Keine erfundenen Dateien oder APIs.
- Vollständige, kompilierbare Änderungen.
- Deutsche Erklärungen, deutsche UI-Texte und ausführliche deutsche Kommentare.
```

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](14_migrations-und-konsolidierungsleitfaden.md)
 · [Nächstes Kapitel →](16_quellenzuordnung.md)
