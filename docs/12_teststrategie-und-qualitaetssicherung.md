# 12. Teststrategie und Qualitätssicherung

Alle automatisierten Tests laufen auf der **JVM** über `./gradlew.bat testDebugUnitTest`. Android-Abhängigkeiten (Room, Context) werden mit **Robolectric 4.14.1** ausgeführt. Ein `androidTest`-/Instrumentation-Verzeichnis existiert derzeit **nicht**.

## 12.1 Vorhandene Testklassen (`app/src/test/`)

| Bereich | Klassen |
| --- | --- |
| Repository / UseCases (Robolectric + In-Memory-Room) | `domain/RepositoryAndUseCaseTest`, `domain/CleanupOrphansTest`, `domain/ListeningStatisticsTest`, `domain/SyncProgressUseCaseTest` |
| Room DAOs | `data/RoomDaoTest` |
| Storage / Import | `storage/AbookStorageTest`, `storage/AbookStorageExportTest`, `storage/AbookManifestParserTest`, `storage/AbookMetadataParserTest`, `storage/FolderScannerTest`, `storage/LibraryLocationManagerTest`, `storage/OpenLibraryScraperTest` |
| Player | `player/SleepTimerControllerTest`, `player/LoudnessControllerTest`, `player/SmartSleepBookmarkTest` |
| ViewModels | `ui/LibraryViewModelTest`, `ui/PlayerViewModelTest` |
| Reine Util-Logik | `util/DuplicateDetectorTest`, `util/PlayableMediaTest`, `util/AudiobookMetadataTextTest`, `util/CoverHelperTest`, `util/ShakeDetectorTest` |

## 12.2 Manuelle Release-Tests

- Import aus internem Speicher, SD-Karte und Netzlaufwerk-Provider, soweit vom Android-Provider unterstützt.
- Screen-Off, App aus Übersicht entfernen, Headset-Tasten, Bluetooth, Android Auto.
- Telefonanruf-/Audio-Focus-Unterbrechung, Kopfhörer abziehen.
- Rotation, Dark/Light Mode, große Schrift, TalkBack.
- Doppelte Importe, fehlende Dateien / leere `imported_*`-Ordner, entzogene Berechtigung, beschädigtes Archiv.
- Datenbankupgrade von einer früheren Version (Migration bis v11).

## 12.3 Definition of Done (aus `../CLAUDE.md`)

Aus `ABookPlayer/` heraus:

- `./gradlew.bat test` – immer.
- `./gradlew.bat assembleDebug` – bei UI-/Ressourcen-/Manifest-/Build-Änderungen.
- `./gradlew.bat assembleRelease` – zusätzlich vor release-nahen Änderungen.
- `./gradlew.bat installDebug` – aufs verbundene Gerät; wenn keins da ist, klar melden.

Zusätzlich: neue Logik braucht einen Unit-Test (ViewModel / UseCase / Controller / reine Util-Klasse), `PROJECT_HISTORY.md`-Eintrag ergänzen, Commit mit klarer Message. Keine fest codierten fremdsprachigen UI-Texte, keine bekannten Datenverlustpfade, Fehlerzustände verständlich bedienbar.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](11_funktionsumfang-und-status.md)
 · [Nächstes Kapitel →](13_roadmap.md)
