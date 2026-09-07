# 8. Spezifikation des .abook-Formats

Eine .abook-Datei ist ein ZIP-Archiv. Die kanonische Formatversion ist 1. Sie enthält ein Manifest, mindestens eine Audiodatei und optional ein Cover. Alte metadata.json-Varianten dürfen nur durch einen separaten Legacy-Adapter gelesen und beim nächsten Export in das kanonische Manifest überführt werden.

## 8.1 Archivstruktur

```text
mein-hoerbuch.abook
├── manifest.json          # Pflicht; alternativ manifest.xml
├── cover.jpg              # optional
└── audio/
    ├── 001-kapitel.mp3
    ├── 002-kapitel.mp3
    └── 003-kapitel.m4a
```

## 8.2 JSON-Beispiel

```text
{
  "version": 1,
  "id": "book_unique_001",
  "title": "Der Beispielroman",
  "author": "Max Mustermann",
  "narrator": "Erika Beispiel",
  "description": "Eine spannende Geschichte ...",
  "cover": "cover.jpg",
  "totalDuration": 45360000,
  "tracks": [
    {
      "file": "audio/001-kapitel.mp3",
      "title": "Kapitel 1: Der Anfang",
      "duration": 9600000
    }
  ]
}
```

## 8.3 Felder

| Feld | Pflicht | Typ | Bedeutung |
| --- | --- | --- | --- |
| version | Ja | Integer | Formatversion, aktuell 1 |
| id | Ja | String | Eindeutige Hörbuch-ID |
| title | Ja | String | Titel |
| author | Ja | String | Autor |
| narrator | Nein | String | Sprecher |
| description | Nein | String | Beschreibung |
| cover | Nein | String | Relativer Pfad zum Cover |
| totalDuration | Nein | Long | Gesamtdauer in Millisekunden |
| tracks[].file | Ja | String | Relativer Pfad zur Audiodatei |
| tracks[].title | Nein | String | Kapitelname |
| tracks[].duration | Nein | Long | Dauer in Millisekunden |

## 8.4 Unterstützte Medien

| Kategorie | Formate |
| --- | --- |
| Audio | MP3, M4A, M4B, AAC, OGG, OPUS, WAV, WMA, FLAC (alle erkannt; siehe `FolderScanner`/`AudiobookMetadataText`) |
| Cover | JPG/JPEG, PNG, WebP |

## 8.5 Sicherheits- und Validierungsregeln

- Kein passwortgeschütztes oder beschädigtes Archiv.

- Keine absoluten Pfade, ../-Segmente oder Extraktion außerhalb des Zielordners.

- Maximal erlaubte Eintragszahl, Einzeldateigröße und entpackte Gesamtgröße begrenzen.

- Manifestpfade müssen exakt zu Archiveinträgen passen.

- Mindestens ein unterstützter Audiotrack.

- Nur unterstützte Formatversion akzeptieren.

- Trackreihenfolge entspricht der Manifestreihenfolge; ohne Manifest nur natürliche Dateisortierung im Legacy-Import.

## 8.6 Cover-Pipeline

1. Manifest-Cover verwenden, sofern gültig.

1. Alternativ vorhandene Bilddatei im Archiv oder Ordner suchen.

1. Alternativ eingebettetes Audio-Tag-Cover extrahieren.

1. Fallback-Platzhalter verwenden.

1. Thumbnail erzeugen und im Bibliotheks-Verwaltungsordner `…/Audiobooks/.abooklib/covers/` (mit internem Fallback) ablegen; Laufzeit-Bitmaps via `util/CoverHelper`.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](07_import-und-storage.md)
 · [Nächstes Kapitel →](09_audio-wiedergabe.md)
