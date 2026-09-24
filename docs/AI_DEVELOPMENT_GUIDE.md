# Leitfaden für KI-Entwickler & Subagenten (AI Development Guide)

Dieses Dokument dient als präziser Leitfaden für autonome KI-Coding-Assistenten (Antigravity, Claude, GPT, Cursor usw.), die an der **ABook Player**-Codebasis arbeiten. Es enthält die essenziellen Richtlinien, Systemzusammenhänge und bekannten Fallstricke, um fehlerfreie, wartbare und architekturtreue Beiträge zu gewährleisten.

---

## 1. Grundregeln & Invarianten

1. **Deutsche Sprache in UI und Kommentaren:**
   - Alle dem Benutzer sichtbaren Texte (Dialoge, Buttons, Fehlermeldungen, Beschriftungen) müssen auf Deutsch verfasst sein.
   - Code-Kommentare und KDoc werden auf Deutsch geschrieben.
2. **Kompilierbare & geprüfte Einzelschritte:**
   - Vor jeder Änderung die betroffenen Codestellen analysieren.
   - Keine unbedachten Großrefactorings oder Architekturwechsel.
   - Standard-Prüfung: `.\gradlew.bat test` im Ordner `ABookPlayer`.
   - Bei UI- oder Manifest-Änderungen: `.\gradlew.bat assembleDebug`.
3. **Automatische Smartphone-Aktualisierung (Pflichtregel):**
   - Nach JEDER erfolgreichen Code-Änderung und bestandener Testprüfung MUSS die App automatisch per `.\gradlew.bat installDebug` auf dem verbundenen Android-Gerät installiert und gestartet werden:
     ```powershell
     cd ABookPlayer
     .\gradlew.bat installDebug
     adb shell am start -n de.f_soft_studio.abookplayer/.MainActivity
     ```
4. **100% FOSS & F-Droid-Kompatibilität:**
   - Keine proprietären Google Play Services (GMS), keine Firebase-Pakete, keine Telemetrie- oder Tracking-SDKs.
   - Alle neuen Abhängigkeiten müssen Open-Source (Apache 2.0 / MIT) und mit F-Droid vereinbar sein.

---

## 2. Bekannte Fallstricke & Anti-Patterns (Gotchas)

### 2.1 Die Room SQLite CASCADE DELETE Falle
- **Problem:** `ChapterEntity` besitzt eine Fremdschlüssel-Kaskade auf `AudiobookEntity` (`onDelete = ForeignKey.CASCADE`).
- **Fehler:** Ein Aufruf von `AudiobookDao.insertAudiobook` mit `OnConflictStrategy.REPLACE` auf ein bereits bestehendes Hörbuch (`id > 0`) triggert SQLite-intern ein `DELETE` der existierenden Zeile. Dies löst sofort die Fremdschlüssel-Kaskade aus und **löscht unwiederbringlich alle Kapitel des Hörbuchs**!
- **Lösung:** Bestehende Hörbücher dürfen NIEMALS mit `insertAudiobook` überschrieben werden. Verwende immer:
  ```kotlin
  if (audiobook.id > 0L) {
      audiobookDao.updateAudiobook(entity)
  } else {
      audiobookDao.insertAudiobook(entity)
  }
  ```

### 2.2 Die Media3 Verzeichnis-Falle
- **Problem:** Bei entpackten Hörbuch-Ordnern zeigt `Audiobook.filePath` auf ein **Verzeichnis** (z. B. `/storage/emulated/0/ABook/MeinBuch`), nicht auf eine Audiodatei!
- **Fehler:** Übergibt man `audiobook.filePath` direkt an ExoPlayer, wirft ExoPlayer einen `EISDIR (Is a directory)` Fehler.
- **Lösung:** ExoPlayer benötigt für jedes Kapitel eine konkrete abspielbare Mediendatei (`chapter.audioPath` oder Einzeldatei via `PlayableMedia.isPlayableFile`). Falls in der Datenbank keine Kapitel vorliegen, muss der Ordner dynamisch über `FolderScanner(context).scanBookFolder(bookDir)` gescannt werden.

### 2.3 Atomarer Hörbuch-Wechsel im Player
- Vor dem Laden eines neuen Titels in `PlaybackController.loadAudiobook` muss:
  1. Der Fortschritt des alten Titels per `SaveProgressUseCase` gesichert werden.
  2. Die Wiedergabe sofort angehalten und die Playlist geleert werden (`player.stop()`, `player.clearMediaItems()`).
  Erst danach wird die neue Playlist vorbereitet und gestartet.

---

## 3. Schichtenarchitektur & Modulgrenzen

| Schicht | Paketpfad | Verantwortlichkeit & Regeln |
|---|---|---|
| **Domain** | `de.f_soft_studio.abookplayer.domain` | Reine Kotlin-Modelle & UseCases. **Absolut keine Android-Imports** (`Context`, `ExoPlayer`, `Room`). |
| **Data** | `de.f_soft_studio.abookplayer.data` | Room-DAOs, Entitäten, `AbookDatabase` (v11), Repository. Kapselt alle SQL-Operationen. |
| **Storage** | `de.f_soft_studio.abookplayer.storage` | `.abook` ZIP-Container, `FolderScanner` (CD1/CD2 Merging), `OnlineCoverScraper` (Audible/iTunes), SAF. |
| **Player** | `de.f_soft_studio.abookplayer.player` | Media3 `ExoPlayer`, `PlaybackController`, `AbookPlaybackService`, Sleep-Timer, Equalizer. |
| **UI** | `de.f_soft_studio.abookplayer.ui` | Jetpack Compose Screens, ViewModels (`StateFlow`), Navigation, Themes (Paper & True Black). |
| **Widget** | `de.f_soft_studio.abookplayer.widget` | Android Homescreen-Widgets. |

---

## 4. Standard-Vorgehensweisen für häufige Aufgaben

### 4.1 Neue UI-Funktion hinzufügen
1. Domain-Modell oder UseCase bei Bedarf erweitern.
2. State im entsprechenden ViewModel als `StateFlow` abbilden.
3. Compose-Funktion unter Beachtung von Material 3 und dem Farbschema (OLED True Black für Player, Paper für Library) erstellen.
4. Preview oder Robolectric/Unit-Test erstellen.
5. `.\gradlew.bat test` ausführen.
6. `.\gradlew.bat installDebug` ausführen und am Smartphone testen.

### 4.2 Room-Datenbank erweitern (Schema-Migration)
1. Entity anpassen oder neu anlegen.
2. Schema-Version in `AbookDatabase` inkrementieren (z. B. von 11 auf 12).
3. Eindeutiges `Migration(11, 12)`-Objekt mit exakten SQL-Statements definieren und in `getInstance()` registrieren.
4. Migrationstest in `app/src/test/` ergänzen.

---

## 5. Referenzdokumente

- **Systemarchitektur:** [`ARCHITECTURE.md`](../ARCHITECTURE.md)
- **Kapitelweise Gesamtdoku:** [`docs/README.md`](README.md)
- **F-Droid Rezept:** [`metadata/de.f_soft_studio.abookplayer.yml`](../metadata/de.f_soft_studio.abookplayer.yml)
- **Fastlane-Metadaten:** [`fastlane/metadata/android/`](../fastlane/metadata/android/)
