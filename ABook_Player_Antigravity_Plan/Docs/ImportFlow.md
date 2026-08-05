# 🔄 ImportFlow.md

## Import-Workflow – ABook Player

Dieses Dokument beschreibt den vollständigen Import-Prozess für alle unterstützten Quellen: Ordner, einzelne Audiodateien und `.abook`-Archive.

---

## Übersicht: Import-Quellen

| Quelle | Einstieg | Validierung | Ergebnis |
|---|---|---|---|
| Ordner | SAF-Ordnerauswahl | Dateitypen prüfen | Ein oder mehrere Hörbücher |
| Einzeldatei | SAF-Dateiauswahl | Format prüfen | Ein Hörbuch (eine Datei) |
| .abook-Archiv | SAF-Dateiauswahl | ZIP + Manifest | Ein Hörbuch mit geordneten Tracks |

---

## A. Ordner-Import-Workflow

```
┌─────────────────────────────────────────────────────┐
│ Schritt 1: Benutzer wählt Ordner (SAF-Dialog)       │
└───────────────────────────┬─────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│ Schritt 2: FolderScanner durchsucht Ordner          │
│   → Alle unterstützten Audiodateien auflisten       │
│   → Dateien nach Album-Metadaten gruppieren         │
│   → Alternativ: gesamter Ordner = ein Hörbuch       │
└───────────────────────────┬─────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│ Schritt 3: MetadataExtractor liest Metadaten        │
│   → ID3-Tags (MP3) auslesen                         │
│   → M4B-Kapitelmarken auslesen                      │
│   → Cover aus Metadaten oder cover.jpg              │
│   → Titelnamen, Autor, Tracknummer                  │
└───────────────────────────┬─────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│ Schritt 4: Duplikaterkennung                        │
│   → Bereits bekannte Ordnerpfade prüfen             │
│   → Bei Duplikat: Benutzer informieren              │
└───────────────────────────┬─────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│ Schritt 5: In Room-Datenbank speichern              │
│   → AudiobookEntity erstellen                       │
│   → TrackEntity für jede Datei erstellen            │
│   → ImportSourceEntity erstellen (Typ: FOLDER)      │
└───────────────────────────┬─────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│ Schritt 6: Hörbuch erscheint in der Bibliothek      │
└─────────────────────────────────────────────────────┘
```

---

## B. .abook-Import-Workflow (Detailliert)

### Gesamtübersicht

```
Benutzer wählt .abook-Datei
         │
         ▼
┌─────────────────────┐
│  1. ZIP-Validierung │ ──FEHLER──► "Ungültiges Archivformat"
└────────┬────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  2. Sicherheitsvalidierung│ ──FEHLER──► "Sicherheitsfehler: Ungültige Dateipfade"
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────────┐
│  3. Manifest-Erkennung       │ ──FEHLER──► "Kein Manifest gefunden"
│  (manifest.xml oder .json?)  │
└────────┬─────────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  4. Manifest parsen      │ ──FEHLER──► "Manifest ungültig"
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  5. Manifest validieren  │ ──FEHLER──► "Pflichtfeld fehlt: ..."
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  6. Dateien validieren   │ ──FEHLER──► "Audiodatei nicht gefunden: ..."
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  7. Duplikat-Prüfung     │ ──DUPLIKAT──► Dialog: Überschreiben / Abbrechen
└────────┬─────────────────┘
         │ NEU oder ÜBERSCHREIBEN
         ▼
┌──────────────────────────┐
│  8. Dateien extrahieren  │ ──FEHLER──► "Fehler beim Extrahieren"
│  (Audio + Cover)         │
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  9. In Datenbank         │
│  speichern               │
└────────┬─────────────────┘
         │ OK
         ▼
┌──────────────────────────┐
│  10. Import erfolgreich  │
│  "Import abgeschlossen"  │
└──────────────────────────┘
```

---

### Schritt 1: ZIP-Validierung

**Komponente:** `AbookArchiveValidator`

```kotlin
// Was wird geprüft:
// - Ist die Datei ein gültiges ZIP-Archiv?
// - Ist das Archiv nicht passwortgeschützt?
// - Enthält das Archiv mindestens einen Eintrag?

val result = archiveValidator.validateZipStructure(uri)
// Bei Fehler → ArchiveValidationResult.InvalidZip("Ungültiges Archivformat")
```

**Deutsche Fehlermeldung bei Fehler:**
> „Ungültiges Archivformat. Die Datei ist kein gültiges .abook-Archiv."

---

### Schritt 2: Sicherheitsvalidierung (ZIP-Slip-Schutz)

**Komponente:** `AbookArchiveValidator`

```kotlin
// Für jeden ZIP-Eintrag:
// - Kanonischen Pfad ermitteln
// - Prüfen, ob Pfad innerhalb des Zielverzeichnisses liegt
// - Bei ../Sequenzen → sofortiger Abbruch

zipFile.entries().forEach { entry ->
    val canonicalPath = File(targetDir, entry.name).canonicalPath
    if (!canonicalPath.startsWith(targetDir.canonicalPath)) {
        throw SecurityException("ZIP-Slip erkannt: ${entry.name}")
    }
}
```

**Deutsche Fehlermeldung bei Fehler:**
> „Sicherheitsfehler: Das Archiv enthält ungültige Dateipfade und wurde nicht importiert."

---

### Schritt 3: Manifest-Erkennung

**Komponente:** `AbookArchiveReader`

```kotlin
// Suchstrategie (in dieser Reihenfolge):
// 1. manifest.xml in der Archivwurzel
// 2. manifest.json in der Archivwurzel
// 3. Wenn keines gefunden → Fehler

val manifestType = when {
    zipFile.getEntry("manifest.xml") != null -> ManifestType.XML
    zipFile.getEntry("manifest.json") != null -> ManifestType.JSON
    else -> throw AbookException("Kein Manifest gefunden")
}
```

**Deutsche Fehlermeldung bei Fehler:**
> „Kein Manifest gefunden. Das Archiv enthält keine manifest.xml oder manifest.json."

---

### Schritt 4: Manifest parsen

**Komponente:** `AbookXmlManifestParser` oder `AbookJsonManifestParser`

```kotlin
// XML-Variante:
val parser = AbookXmlManifestParser()
val manifest: AbookManifest = parser.parse(manifestInputStream)

// JSON-Variante:
val parser = AbookJsonManifestParser()
val manifest: AbookManifest = parser.parse(manifestInputStream)
```

**Fehlerbehandlung:**
- Ungültiges XML → `XmlParseException` → Benutzerfehlermeldung
- Ungültiges JSON → `JsonParseException` → Benutzerfehlermeldung
- Unbekannte Felder werden ignoriert (defensive Parsing)

**Deutsche Fehlermeldung bei Fehler:**
> „Manifest ungültig. Die Manifest-Datei konnte nicht gelesen werden."

---

### Schritt 5: Manifest validieren

**Komponente:** `AbookManifestValidator` (Teil des Domain-Layers)

```kotlin
// Validierungsregeln:
// - version == 1 (oder bekannte unterstützte Version)
// - id nicht leer
// - title nicht leer
// - author nicht leer
// - tracks nicht leer
// - Jeder Track hat einen nicht-leeren file-Pfad

val validationErrors = manifestValidator.validate(manifest)
if (validationErrors.isNotEmpty()) {
    return ImportResult.Failure(validationErrors.first())
}
```

---

### Schritt 6: Referenzierte Dateien validieren

**Komponente:** `AbookArchiveReader`

```kotlin
// Für jeden Track im Manifest:
manifest.tracks.forEach { track ->
    val entry = zipFile.getEntry(track.file)
    if (entry == null) {
        throw AbookException("Audiodatei nicht gefunden: ${track.file}")
    }
}

// Optionale Cover-Prüfung:
manifest.coverPath?.let { coverPath ->
    if (zipFile.getEntry(coverPath) == null) {
        // Warnung, aber kein Abbruch
        log.warn("Cover nicht gefunden: $coverPath")
    }
}
```

**Deutsche Fehlermeldung bei Fehler:**
> „Audiodatei nicht gefunden: chapter03.mp3"

---

### Schritt 7: Duplikat-Prüfung

**Komponente:** `AbookImporter`

```kotlin
// Prüfung 1: Nach Manifest-ID
val existingById = audiobookRepo.findBySourceId(manifest.id)

// Prüfung 2: Nach SHA-256-Prüfsumme der .abook-Datei
val checksum = computeSha256(abookFile)
val existingByChecksum = importSourceRepo.findByChecksum(checksum)

if (existingById != null || existingByChecksum != null) {
    // Duplikat gefunden → Benutzer fragen
    return ImportResult.Duplicate(existingById ?: existingByChecksum!!)
}
```

---

### Schritt 8: Dateien extrahieren

**Komponente:** `AbookArchiveReader` + `AppStorageManager`

```kotlin
// Zielverzeichnis: App-interner Speicher
// Pfad: /data/data/com.example.audiobookplayer/files/abooks/{manifest.id}/

val targetDir = appStorageManager.getAbookDirectory(manifest.id)

// Audio-Dateien extrahieren:
manifest.tracks.forEach { track ->
    val entry = zipFile.getEntry(track.file)
    val targetFile = File(targetDir, track.file)
    targetFile.parentFile?.mkdirs()
    zipFile.getInputStream(entry).copyTo(targetFile.outputStream())
}

// Cover extrahieren (falls vorhanden):
manifest.coverPath?.let { coverPath ->
    val coverEntry = zipFile.getEntry(coverPath)
    if (coverEntry != null) {
        val coverFile = File(targetDir, "cover.${coverPath.extension}")
        zipFile.getInputStream(coverEntry).copyTo(coverFile.outputStream())
    }
}
```

---

### Schritt 9: In Datenbank speichern

**Komponente:** `AbookImporter` → `AudiobookRepository`

```kotlin
// AudiobookEntity anlegen
val audiobookId = audiobookRepo.save(
    AudiobookEntity(
        title = manifest.title,
        author = manifest.author,
        narrator = manifest.narrator,
        coverPath = extractedCoverPath,
        sourceType = SourceType.ABOOK,
        importTimestamp = System.currentTimeMillis()
    )
)

// TrackEntity für jeden Track anlegen
manifest.tracks.forEachIndexed { index, track ->
    trackRepo.save(
        TrackEntity(
            audiobookId = audiobookId,
            position = index,
            title = track.title ?: "Kapitel ${index + 1}",
            filePath = File(targetDir, track.file).absolutePath,
            durationMs = track.duration
        )
    )
}

// ImportSourceEntity anlegen
importSourceRepo.save(
    ImportSourceEntity(
        audiobookId = audiobookId,
        sourcePath = abookUri.toString(),
        sourceType = SourceType.ABOOK,
        manifestId = manifest.id,
        checksum = sha256Checksum,
        importTimestamp = System.currentTimeMillis()
    )
)
```

---

## C. UI-Zustand während des Imports

Die `ImportViewModel` steuert den UI-Zustand:

```kotlin
sealed class ImportUiState {
    object Idle : ImportUiState()
    object ArchivWirdGeprueft : ImportUiState()     // "Archiv wird geprüft..."
    object ManifestWirdGelesen : ImportUiState()    // "Manifest wird gelesen..."
    object CoverWirdImportiert : ImportUiState()    // "Cover wird importiert..."
    data class AudiodateienWerdenExtrahiert(
        val fortschritt: Int,                        // 0–100
        val aktuelleDatei: String
    ) : ImportUiState()                              // "Audiodateien werden importiert..."
    data class Erfolg(
        val audiobookId: Long
    ) : ImportUiState()                              // "Import erfolgreich"
    data class Fehler(
        val meldung: String
    ) : ImportUiState()                              // Fehlermeldung auf Deutsch
    data class Duplikat(
        val vorhandenesHoerbuch: Audiobook
    ) : ImportUiState()                              // Duplikat-Dialog
}
```

---

## D. Deutsche UI-Texte während des Imports

| Zustand | Angezeigter Text |
|---|---|
| Archiv-Prüfung | „Archiv wird geprüft…" |
| Manifest-Erkennung | „Manifest wird erkannt…" |
| Manifest-Lesen | „Manifest wird gelesen…" |
| Datei-Validierung | „Dateien werden überprüft…" |
| Cover-Import | „Cover wird importiert…" |
| Audio-Extraktion | „Audiodateien werden importiert… (x%)" |
| Datenbank | „Hörbuch wird gespeichert…" |
| Erfolg | „Import erfolgreich abgeschlossen!" |
| Duplikat | „Dieses Hörbuch wurde bereits importiert. Neu importieren?" |
| Fehler | [Spezifische deutsche Fehlermeldung] |

---

## E. Fehlerwiederherstellung

Nach einem fehlgeschlagenen Import:
1. Teilweise extrahierte Dateien werden gelöscht
2. Kein Datenbank-Eintrag verbleibt
3. Benutzer erhält klare Fehlermeldung
4. „Erneut versuchen"-Button wird angezeigt

```kotlin
// Aufräumen bei Fehler:
try {
    performImport(abookUri)
} catch (e: AbookImportException) {
    // Teilweise extrahierte Dateien löschen
    cleanupPartialImport(manifest?.id)
    // Benutzer informieren
    _uiState.value = ImportUiState.Fehler(e.deutscheMeldung)
}
```

---

*Weitere Details zum .abook-Format: [ABOOK_FORMAT.md](ABOOK_FORMAT.md)*  
*Architekturübersicht: [Architecture.md](Architecture.md)*
