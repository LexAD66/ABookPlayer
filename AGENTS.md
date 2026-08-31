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

## Tests und Verifikation

- Fuer Logikaenderungen passende Unit-Tests ergaenzen oder anpassen.
- Standardpruefung ist `.\gradlew.bat test`.
- Bei UI-/Ressourcen-/Manifest-/Buildaenderungen zusaetzlich `.\gradlew.bat assembleDebug`.
- Vor Release-nahen Aenderungen `.\gradlew.bat assembleDebug assembleRelease`.
- Nach erfolgreicher App-Aenderung und Verifikation gilt die vorhandene Option-B-Regel: `.\gradlew.bat installDebug` versuchen. Falls kein Geraet verbunden ist, Ergebnis klar melden.

## Subagenten

Subagenten nur fuer klar getrennte Aufgaben nutzen. Bei paralleler Implementierung muessen Schreibbereiche disjunkt bleiben. Read-only Exploration, Doku-Abgleich und Test-/Loganalyse eignen sich besonders gut fuer Subagenten.

