# ABook Player Codex-Regeln

## Projektkontext

Dieses Repository enthaelt eine native Android-App fuer lokale Hoerbuecher im `.abook`-Format. Die aktive App-Codebasis liegt in `ABookPlayer/`. Die Ordner `docs/`, `Docs/` und `ABook_Player_Antigravity_Plan/` enthalten Projektgeschichte, Spezifikation und Designreferenzen.

## Verbindliche Arbeitsweise

- Erst analysieren, dann aendern.
- Kleine, kompilierbare Schritte bevorzugen.
- Keine Architekturwechsel ohne ausdruecklichen Auftrag.
- Keine unnoetigen Libraries einfuehren.
- Keine Dateien loeschen, verschieben oder grob umstrukturieren, ohne vorher eine begruendete Liste vorzulegen.
- Uncommitted Aenderungen anderer Sitzungen nicht zuruecksetzen.
- Deutsche UI-Texte verwenden.
- Kotlin-Kommentare auf Deutsch schreiben, aber nur dort, wo sie echten Kontext liefern.
- Bestehende Projektmuster bevorzugen: MVVM, pragmatische Clean Architecture, Compose, Room, Media3, StateFlow/Coroutines.

## Modulgrenzen

- `ui/`: Compose Screens, Dialoge, ViewModels, Navigation und Theme.
- `domain/`: stabile Modelle und UseCases ohne Android-Details.
- `data/`: Room-Entities, DAOs, Datenbank und Repository.
- `storage/`: `.abook`, SAF, Scanner, Cover/Metadaten und Sync-Transport.
- `player/`: Media3/ExoPlayer, MediaSession, Sleep Timer, Loudness und Playback-Service.
- `widget/`: Android Homescreen-Widgets.

## Tests und Verifikation & Automatisches Phone-Deployment

- Fuer Logikaenderungen passende Unit-Tests ergaenzen oder anpassen.
- Standardpruefung ist `.\gradlew.bat test`.
- Bei UI-/Ressourcen-/Manifest-/Buildaenderungen zusaetzlich `.\gradlew.bat assembleDebug`.
- Vor Release-nahen Aenderungen `.\gradlew.bat assembleDebug assembleRelease`.
- **Automatische Phone-Aktualisierung (Pflichtregel)**: Nach JEDER erfolgreichen Code- oder App-Änderung und Verifikation MUSS die App automatisch per `.\gradlew.bat installDebug` auf dem angeschlossenen Smartphone aktualisiert und gestartet werden (`adb shell am start -n de.f_soft_studio.abookplayer/.MainActivity`). Falls kein Gerät verbunden ist, wird dies im Statusbericht gemeldet.

## Wichtige Referenzdokumente für KI & Agenten

- **Architektur-Spezifikation:** [`ARCHITECTURE.md`](ARCHITECTURE.md)
- **Leitfaden für KI-Entwickler & Fallstricke:** [`docs/AI_DEVELOPMENT_GUIDE.md`](docs/AI_DEVELOPMENT_GUIDE.md)
- **F-Droid Build-Rezept:** [`metadata/de.f_soft_studio.abookplayer.yml`](metadata/de.f_soft_studio.abookplayer.yml)
- **Fastlane-Metadaten:** [`fastlane/metadata/android/`](fastlane/metadata/android/)

## Kritische Architektur-Regeln (Gotchas)

- **Room SQLite CASCADE DELETE Falle**: Niemals `@Insert(REPLACE)` auf `AudiobookEntity` ausführen, wenn ein Hörbuch bereits existiert (`id > 0`). Immer `@Update` (`updateAudiobook`) verwenden, da SQLite sonst kaskadierend alle Kapitel (`chapters`) löscht!
- **Media3 Verzeichnis-Falle**: Bei Ordner-Hörbüchern zeigt `Audiobook.filePath` auf einen Ordner. ExoPlayer benötigt Datei-URIs aus den Kapiteln. Fehlen Kapitel, on-the-fly via `FolderScanner` rekonstruieren.
- **FOSS & F-Droid Compliance**: Ausschließlich freie Open-Source-Bibliotheken (Apache 2.0 / MIT) verwenden. Keine GMS-, Firebase- oder Tracking-Abhängigkeiten einführen.

## Subagenten

Subagenten nur fuer klar getrennte Aufgaben nutzen. Bei paralleler Implementierung muessen Schreibbereiche disjunkt bleiben. Read-only Exploration, Doku-Abgleich und Test-/Loganalyse eignen sich besonders gut fuer Subagenten.

