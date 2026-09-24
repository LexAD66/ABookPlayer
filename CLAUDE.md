# CLAUDE.md – ABook Player

Native Android-App für lokale Hörbücher im `.abook`-Format.

## Wo liegt was

| Pfad | Inhalt |
|------|--------|
| `ABookPlayer/` | Aktive App-Codebasis (Gradle-Root, `gradlew.bat` liegt hier) |
| `docs/` | Konsolidierte Projektdokumentation (Kapitel 01–17) |
| `ABook_Player_Antigravity_Plan/` | Spezifikation, Design-Referenzen (JSX-Prototypen), Regeln |
| `PROJECT_HISTORY.md` | Chronologisches Entwicklungsjournal (Phasen) |
| `ABookPlayer/redesign.md` | Aktives Arbeitsdokument: Library-Umstrukturierung |

## Architektur

Pragmatische Clean Architecture + MVVM. Kotlin, Jetpack Compose, Media3/ExoPlayer,
Room (v11), Manuelle Dependency Injection (Singletons / Factory-Methoden), Coroutines/StateFlow.

Details siehe:
- 📐 [**ARCHITECTURE.md**](ARCHITECTURE.md)
- 🤖 [**docs/AI_DEVELOPMENT_GUIDE.md**](docs/AI_DEVELOPMENT_GUIDE.md)

- `ui/` – Compose Screens, Dialoge, ViewModels, Navigation, Theme (Paper & OLED True Black)
- `domain/` – Reine Modelle & UseCases ohne Android-Abhängigkeiten
- `data/` – Room-Entities, DAOs, Datenbank, Repository
- `storage/` – `.abook`, SAF, Ordner-Scanner, Cover/Metadaten-Scraper (Audible/iTunes)
- `player/` – Media3, MediaSession, Sleep Timer, Loudness, Playback-Service
- `widget/` – Homescreen-Widgets

## Verbindliche Arbeitsweise

Siehe [`AGENTS.md`](AGENTS.md) – gilt vollständig. Kurzfassung:

- Erst analysieren, dann ändern. Kleine, kompilierbare Schritte.
- Kein Architekturwechsel, keine unnötigen Libraries ohne Auftrag.
- Keine Dateien löschen/verschieben ohne begründete Liste vorab.
- Uncommitted Änderungen anderer Sitzungen nicht zurücksetzen.
- Deutsche UI-Texte. Deutsche Kotlin-Kommentare nur dort, wo sie echten Kontext liefern.

## Verifikation (Definition of Done pro Feature)

Aus `ABookPlayer/` heraus:

```bash
./gradlew.bat test              # immer
./gradlew.bat assembleDebug     # bei UI-/Ressourcen-/Manifest-/Build-Änderungen
./gradlew.bat assembleRelease   # zusätzlich vor Release-nahen Änderungen
./gradlew.bat installDebug      # PFLICHT nach jeder Änderung: direkt aufs verbundene Smartphone installieren und starten; wenn kein Gerät: klar melden
```

Zusätzlich: neue Logik braucht Unit-Test (ViewModel / UseCase / Controller),
PROJECT_HISTORY.md-Eintrag ergänzen, Commit mit klarer Message.

## Branch-Modell (Solo)

- `master` = immer baubar + installierbar
- `feat/<thema>` pro Feature, kleiner Scope, nach Verifikation zurück nach `master`
- Sicherungs-Branch vor riskanten Umbauten: `backup/<zweck>-<datum>`
