# 📦 ABOOK_FORMAT.md

## Spezifikation des `.abook`-Formats

**Format-Version:** 1  
**Dateiendung:** `.abook`  
**Basisformat:** ZIP-Archiv  

---

## 1. Zweck und Überblick

Das `.abook`-Format ist ein **ZIP-basiertes Hörbuch-Containerformat**, das speziell für ABook Player entwickelt wurde. Es fasst alle Bestandteile eines Hörbuchs – Audiodateien, Metadaten und Cover – in einer einzigen, portablen Datei zusammen.

### Ziele des Formats
- **Portabilität:** Gesamtes Hörbuch in einer Datei
- **Metadaten-Integrität:** Titel, Autor, Trackreihenfolge im Archiv gespeichert
- **Erweiterbarkeit:** Versioniertes Format für zukünftige Erweiterungen
- **Sicherheit:** Validierungsmechanismen gegen beschädigte oder bösartige Archive
- **Kompatibilität:** Basis-ZIP kann von Standard-Archivprogrammen geöffnet werden

---

## 2. Archivstruktur

Eine `.abook`-Datei ist ein **ZIP-Archiv** und muss mindestens folgende Inhalte besitzen:

### Pflichtbestandteile
1. **Manifest-Datei** (`manifest.xml` oder `manifest.json`) – definiert Metadaten und Trackliste
2. **Mindestens eine Audiodatei** – in einem unterstützten Format (MP3, M4A, M4B, AAC)

### Optionale Bestandteile
- **Cover-Bild** – JPG, PNG oder WebP
- Weitere Metadaten-Dateien (zukünftig)

### Beispielstruktur A (empfohlen – mit Unterordner)

```
meinbuch.abook
├── manifest.xml         ← Pflicht: Metadaten und Trackliste
├── cover.jpg            ← Optional: Titelbild
└── audio/
    ├── chapter01.mp3    ← Audiodatei, Kapitel 1
    ├── chapter02.mp3    ← Audiodatei, Kapitel 2
    └── chapter03.mp3    ← Audiodatei, Kapitel 3
```

### Beispielstruktur B (alternativ – ohne Unterordner)

```
meinbuch.abook
├── manifest.json        ← Pflicht: Metadaten und Trackliste (JSON-Variante)
├── cover.png            ← Optional: Titelbild
├── chapter01.mp3
└── chapter02.mp3
```

### Regeln zur Archivstruktur
- Alle Pfade im Manifest sind **relativ zur Archiv-Wurzel** anzugeben
- Unterordner sind erlaubt, aber nicht verschachtelt (max. eine Ebene)
- Dateinamen dürfen **keine Pfadtraversierungssequenzen** enthalten (`../`, `./`, absolute Pfade)
- Groß-/Kleinschreibung in Dateinamen muss konsistent mit dem Manifest sein

---

## 3. Manifest-Format

Das Manifest ist die zentrale Steuerdatei des Archivs. Es existiert in zwei gleichwertigen Varianten.

### 3a. XML-Manifest (`manifest.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<Abook>
  <Metadata>
    <!-- Pflichtfelder -->
    <Version>1</Version>
    <Id>book_unique_001</Id>
    <Title>Der Beispielroman</Title>
    <Author>Max Mustermann</Author>

    <!-- Optionale Felder -->
    <Narrator>Erika Beispiel</Narrator>
    <Description>Eine spannende Geschichte über...</Description>
    <Cover>cover.jpg</Cover>
    <TotalDuration>45360000</TotalDuration>  <!-- Gesamtdauer in Millisekunden -->
  </Metadata>

  <Tracks>
    <Track>
      <File>audio/chapter01.mp3</File>       <!-- Pflicht: Pfad relativ zur Archivwurzel -->
      <Title>Kapitel 1: Der Anfang</Title>   <!-- Optional: Kapitelname -->
      <Duration>9600000</Duration>            <!-- Optional: Dauer in Millisekunden -->
    </Track>
    <Track>
      <File>audio/chapter02.mp3</File>
      <Title>Kapitel 2: Die Reise</Title>
      <Duration>12340000</Duration>
    </Track>
    <Track>
      <File>audio/chapter03.mp3</File>
      <Title>Kapitel 3: Das Ende</Title>
      <Duration>23420000</Duration>
    </Track>
  </Tracks>
</Abook>
```

### 3b. JSON-Manifest (`manifest.json`)

```json
{
  "version": 1,
  "id": "book_unique_001",
  "title": "Der Beispielroman",
  "author": "Max Mustermann",
  "narrator": "Erika Beispiel",
  "description": "Eine spannende Geschichte über...",
  "cover": "cover.jpg",
  "totalDuration": 45360000,
  "tracks": [
    {
      "file": "audio/chapter01.mp3",
      "title": "Kapitel 1: Der Anfang",
      "duration": 9600000
    },
    {
      "file": "audio/chapter02.mp3",
      "title": "Kapitel 2: Die Reise",
      "duration": 12340000
    },
    {
      "file": "audio/chapter03.mp3",
      "title": "Kapitel 3: Das Ende",
      "duration": 23420000
    }
  ]
}
```

---

## 4. Felddefinitionen

### Metadaten-Felder

| Feld | XML-Tag / JSON-Key | Pflicht | Typ | Beschreibung |
|---|---|---|---|---|
| Version | `<Version>` / `version` | ✅ | Integer | Format-Version (aktuell: 1) |
| ID | `<Id>` / `id` | ✅ | String | Eindeutige Kennung des Hörbuchs |
| Titel | `<Title>` / `title` | ✅ | String | Vollständiger Titel |
| Autor | `<Author>` / `author` | ✅ | String | Name des Autors |
| Sprecher | `<Narrator>` / `narrator` | ❌ | String | Name des Sprechers |
| Beschreibung | `<Description>` / `description` | ❌ | String | Klappentext / Inhaltsangabe |
| Cover | `<Cover>` / `cover` | ❌ | String | Pfad zum Cover-Bild im Archiv |
| Gesamtdauer | `<TotalDuration>` / `totalDuration` | ❌ | Long | Gesamtdauer in Millisekunden |

### Track-Felder

| Feld | XML-Tag / JSON-Key | Pflicht | Typ | Beschreibung |
|---|---|---|---|---|
| Datei | `<File>` / `file` | ✅ | String | Pfad zur Audiodatei im Archiv |
| Titel | `<Title>` / `title` | ❌ | String | Name des Kapitels / Tracks |
| Dauer | `<Duration>` / `duration` | ❌ | Long | Dauer in Millisekunden |

---

## 5. Unterstützte Audiodateiformate

| Format | Erweiterung | Unterstützung |
|---|---|---|
| MP3 | `.mp3` | ✅ Vollständig |
| M4A | `.m4a` | ✅ Vollständig |
| M4B | `.m4b` | ✅ Vollständig (Kapitel werden ignoriert, da .abook Manifest verwendet) |
| AAC | `.aac` | ✅ Vollständig |
| FLAC | `.flac` | 🔮 Zukünftig geplant |

### Unterstützte Cover-Formate

| Format | Erweiterung | Empfohlen |
|---|---|---|
| JPEG | `.jpg`, `.jpeg` | ✅ Empfohlen |
| PNG | `.png` | ✅ Unterstützt |
| WebP | `.webp` | ✅ Unterstützt |

---

## 6. Validierungsregeln

Beim Import prüft die App das `.abook`-Archiv in mehreren Stufen:

### Stufe 1: ZIP-Validierung
- [ ] Datei ist ein gültiges ZIP-Archiv (korrekter Header)
- [ ] Archiv ist nicht passwortgeschützt
- [ ] Archiv ist nicht beschädigt (CRC-Prüfung)
- [ ] Archiv ist nicht leer

### Stufe 2: Sicherheitsvalidierung (ZIP-Slip-Schutz)
- [ ] Kein Eintrag enthält `../` im Pfad
- [ ] Kein Eintrag enthält `./` im Pfad
- [ ] Kein Eintrag hat einen absoluten Pfad (beginnt mit `/`)
- [ ] Kein Eintrag würde außerhalb des Zielverzeichnisses extrahiert werden

### Stufe 3: Manifest-Validierung
- [ ] Mindestens `manifest.xml` oder `manifest.json` vorhanden
- [ ] Manifest-Datei ist wohlgeformtes XML bzw. gültiges JSON
- [ ] `version` ist vorhanden und eine unterstützte Versionsnummer (aktuell: 1)
- [ ] `id` ist vorhanden und nicht leer
- [ ] `title` ist vorhanden und nicht leer
- [ ] `author` ist vorhanden und nicht leer
- [ ] `tracks` ist eine nicht-leere Liste

### Stufe 4: Datei-Validierung
- [ ] Jede im Manifest referenzierte Audiodatei existiert im Archiv
- [ ] Alle Audiodateien haben ein unterstütztes Format
- [ ] Das im Manifest angegebene Cover-Bild existiert (falls angegeben)

---

## 7. Trackreihenfolge

Die Reihenfolge der Tracks **muss exakt** der Reihenfolge im Manifest entsprechen:
- Die App verwendet **ausschließlich** die Manifest-Reihenfolge
- Alphabetische oder lexikographische Sortierung wird **nicht** angewendet
- Fehlende Tracks (im Manifest vorhanden, aber nicht im Archiv) führen zum **Abbruch des Imports**

---

## 8. Cover-Behandlung

1. Cover-Pfad aus Manifest lesen (Feld `cover` / `<Cover>`)
2. Cover aus ZIP extrahieren und in App-Storage speichern
3. Datenbankpfad auf extrahiertes Cover aktualisieren
4. Falls kein Cover im Manifest oder Cover-Datei nicht gefunden:
   - Standard-Placeholder-Cover verwenden
   - Kein Abbruch des Imports

---

## 9. Import-Prozess

Vollständige Beschreibung: siehe [ImportFlow.md](ImportFlow.md)

**Kurzübersicht:**

```
Schritt 1: .abook-Datei auswählen
     ↓
Schritt 2: ZIP-Struktur validieren (Sicherheits- + Integritätsprüfung)
     ↓
Schritt 3: Manifest erkennen und parsen (XML oder JSON)
     ↓
Schritt 4: Referenzierte Dateien im Archiv prüfen
     ↓
Schritt 5: Duplikat-Prüfung anhand ID oder Prüfsumme
     ↓
Schritt 6: Audiodateien und Cover in App-Storage extrahieren
     ↓
Schritt 7: Metadaten, Tracks und Quelle in Room speichern
     ↓
Schritt 8: Hörbuch in Bibliothek anzeigen
```

---

## 10. Sicherheitsvalidierung (ZIP-Slip-Schutz)

**ZIP-Slip** ist eine bekannte Sicherheitslücke, bei der manipulierte ZIP-Archive durch Pfadtraversierung (`../`) Dateien außerhalb des Zielverzeichnisses überschreiben können.

Die App schützt sich durch:

```kotlin
// Sicherheitsvalidierung für jeden ZIP-Eintrag
fun validateZipEntry(entry: ZipEntry, targetDir: File): Boolean {
    val canonicalTarget = targetDir.canonicalPath
    val canonicalEntry = File(targetDir, entry.name).canonicalPath
    // Eintrag darf nicht außerhalb des Zielverzeichnisses liegen
    return canonicalEntry.startsWith(canonicalTarget + File.separator)
}
```

**Validierungsregeln:**
- Alle Pfade werden kanonisiert vor dem Vergleich
- Absoluter Pfad des Zielverzeichnisses muss Präfix des Eintrags sein
- Bei Verstoß: Import wird mit Fehlermeldung abgebrochen

---

## 11. Duplikaterkennung

Duplikate werden auf zwei Wegen erkannt:

1. **Manifest-ID:** Gleiche `id` im Manifest wie ein vorhandenes Hörbuch
2. **Prüfsumme:** SHA-256-Hash der `.abook`-Datei wird in `ImportSourceEntity` gespeichert

**Bei erkanntem Duplikat:**
- Benutzer wird informiert
- Optionaler Re-Import (überschreibt vorhandenes Hörbuch)
- Optionales Abbrechen

---

## 12. Fehlerbehandlung

Alle Fehlermeldungen werden auf **Deutsch** angezeigt:

| Fehlercode | Deutsche Meldung | Ursache |
|---|---|---|
| `INVALID_ZIP` | „Ungültiges Archivformat. Die Datei ist kein gültiges .abook-Archiv." | ZIP-Struktur beschädigt |
| `NO_MANIFEST` | „Kein Manifest gefunden. Das Archiv enthält keine manifest.xml oder manifest.json." | Manifest fehlt |
| `INVALID_MANIFEST` | „Manifest ungültig. Die Manifest-Datei konnte nicht gelesen werden." | XML/JSON fehlerhaft |
| `UNSUPPORTED_VERSION` | „Nicht unterstützte .abook-Version. Bitte App aktualisieren." | Version > 1 |
| `MISSING_TITLE` | „Pflichtfeld fehlt: Titel ist nicht angegeben." | `title` leer |
| `MISSING_AUTHOR` | „Pflichtfeld fehlt: Autor ist nicht angegeben." | `author` leer |
| `NO_TRACKS` | „Keine Tracks gefunden. Das Manifest enthält keine Audiodateien." | Leere Track-Liste |
| `AUDIO_FILE_MISSING` | „Audiodatei nicht gefunden: {dateiname}" | Track fehlt im Archiv |
| `ZIP_SLIP` | „Sicherheitsfehler: Ungültige Dateipfade im Archiv." | ZIP-Slip-Angriff |
| `DUPLICATE` | „Dieses Hörbuch wurde bereits importiert." | Duplikat erkannt |
| `EXTRACTION_FAILED` | „Fehler beim Extrahieren. Bitte Speicherplatz prüfen." | Schreibfehler |

---

## 13. Zukünftige Export-Unterstützung

Das Format ist so gestaltet, dass ein **Export vorhandener Hörbücher** als `.abook`-Datei möglich ist:

**Geplanter Export-Workflow:**
1. Hörbuch aus Bibliothek auswählen
2. Metadaten aus Room lesen
3. Tracks aus App-Storage sammeln
4. Cover einbinden
5. Manifest generieren (XML oder JSON)
6. ZIP-Archiv mit allen Dateien erstellen
7. Als `.abook`-Datei speichern / teilen

**Versionsstrategie:**
- Version 1: Aktuelles Format (Lesen + Schreiben)
- Version 2+: Rückwärtskompatibles Parsen älterer Versionen

---

## 14. Formatversionen

| Version | Status | Änderungen |
|---|---|---|
| 1 | ✅ Aktuell | Initiales Format |
| 2 | 🔮 Geplant | FLAC-Unterstützung, erweiterte Metadaten |

---

*Dieses Dokument ist die verbindliche Referenz für das `.abook`-Format.*  
*Letzte Aktualisierung: März 2026*
