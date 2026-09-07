# 7. Import und Storage

Zentrale Klasse: `storage/AbookStorage.kt` (orchestriert), unterstützt von `FolderScanner`, `LibraryLocationManager`, `DuplicateDetector` und `ChapterDurations`.

## 7.1 Unterstützte Quellen

- SAF-Datei-Import einer einzelnen `.abook`/`.zip`/Audiodatei (`importFromUri`).
- SAF-Ordner-Import über `ACTION_OPEN_DOCUMENT_TREE` mit persistierbarer Berechtigung (`importFromFolderUri`).
- Rekursiver Scan des app-verwalteten Bibliotheksordners und zusätzlich gemerkter Ordner (`scanAndImport`).
- Erneuter Scan aktualisiert vorhandene Hörbücher bzw. überspringt Duplikate.

## 7.2 Bibliotheksordner (`LibraryLocationManager`)

- Standard: ein öffentlicher Ordner (`…/Audiobooks/`) mit Unterordnern `imported_<hash>/` je importiertem Hörbuch.
- Verwaltungsdaten liegen in `…/Audiobooks/.abooklib/` (`covers/`, `metadata/`, `.nomedia`); mit sicherem internem Fallback.
- Ordner-Importe werden in diesen Bereich **kopiert**, damit die App eine stabile Quelle hat.

## 7.3 Ordnerimport (Ablauf)

1. Benutzer wählt einen Ordner; App übernimmt die persistierbare URI-Berechtigung.
2. `FolderScanner` ermittelt Audiodateien rekursiv (inkl. `CD1`/`CD2`/`Teil n`-Zusammenführung), ignoriert System-/Fremdordner und `.abooklib`.
3. Dateien werden natürlich sortiert; Kapiteltitel werden **ohne** Audio-Datei-Endung gespeichert (`AudiobookMetadataText.stripAudioExtension`).
4. Dauern werden je Datei bzw. lazily ermittelt.
5. `DuplicateDetector` vergleicht mit vorhandenen Hörbüchern; identische werden übersprungen und für die Duplikat-Ansicht gemerkt.
6. Hörbuch + Kapitel werden gespeichert; ggf. Online-Cover/Beschreibung nachgeladen.
7. UI meldet Anzahl importierter, übersprungener und als Duplikat erkannter Elemente.

## 7.4 `.abook`-Importpipeline

1. ZIP-Struktur prüfen, ZIP-Slip- und Pfadvalidierung, Größenlimit (Zip-Bomb-Schutz).
2. `manifest.json` oder `manifest.xml` erkennen und parsen (Legacy-`metadata.json` wird ebenfalls erkannt).
3. Referenzierte Audio- und Coverdateien prüfen.
4. In ein eindeutiges Zielverzeichnis extrahieren.
5. Room-Datensätze anlegen; bei Fehlern temporäre Daten bereinigen.

## 7.5 Bibliothekswartung (`LibraryViewModel` + `AudiobookRepository.cleanupDuplicatesAndOrphans`)

- **Scan / Cleanup** laufen nur noch auf ausdrückliche Nutzeraktion (kein Auto-Scan mehr beim ViewModel-Start). Re-Entrancy-Guard über `LibraryMaintenanceUiState` (`isScanning` / `isCleaning`).
- **Duplikate:** gleiche `filePath` bzw. gleicher Titel/Autor → alle bis auf den „besten" Eintrag (mit Fortschritt / zuletzt gehört) werden entfernt.
- **Verwaiste Einträge:** Pfad fehlt **oder** der Pfad ist ein Verzeichnis **ohne Audiodateien** (leerer `imported_*`-Ordner, Dateien verschoben/gelöscht) → Eintrag wird entfernt.
- **0-Minuten-Reparatur:** `recalculateZeroDurationBooks` berechnet fehlende Laufzeiten aus Kapitel- bzw. Ordnerinhalt neu.

## 7.6 Wiedergabe-relevante Schutzmaßnahme

`util/PlayableMedia.isPlayableFile` stellt sicher, dass nur reguläre Dateien (`File.isFile`) an ExoPlayer gehen. Ein Verzeichnis würde sonst mit `EISDIR` abbrechen; stattdessen zeigt die App einen Toast und startet keine Wiedergabe.

## 7.7 Fortschritts-Sync (optional)

`storage/sync/WebDavSyncManager` gleicht Hörfortschritt über einen WebDAV-Server ab (`SyncProgressUseCase`). Rein optional, kein Katalog-Streaming, kein Pflicht-Server.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](06_datenmodell.md)
 · [Nächstes Kapitel →](08_spezifikation-des-abook-formats.md)
