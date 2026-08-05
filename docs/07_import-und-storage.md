# 7. Import und Storage

## 7.1 Unterstützte Quellen

- Ordnerauswahl über SAF mit persistierbarer Lese-Berechtigung.

- Rekursiver Scan von Unterordnern.

- Direkter Import einer .abook-Datei.

- Optionaler Scan eines vorgesehenen ABook-Verzeichnisses.

- Erneuter Scan aktualisiert ein vorhandenes Hörbuch anhand stabiler Quelle oder Manifest-ID.

## 7.2 Ordnerimport

1. Benutzer wählt einen Ordner über ACTION_OPEN_DOCUMENT_TREE.

1. App übernimmt die persistierbare URI-Berechtigung.

1. Scanner ermittelt unterstützte Audiofiles rekursiv.

1. Dateien werden natürlich sortiert und Metadaten/Dauern werden ermittelt.

1. Hörbuch und Kapitel werden in einer Transaktion gespeichert oder aktualisiert.

1. UI meldet Anzahl importierter, aktualisierter und übersprungener Elemente.

## 7.3 .abook-Importpipeline

1. ZIP-Struktur und CRC prüfen.

1. ZIP-Slip- und Pfadvalidierung durchführen.

1. manifest.json oder manifest.xml erkennen.

1. Manifest parsen und Pflichtfelder validieren.

1. Alle referenzierten Audio- und Coverdateien prüfen.

1. Duplikat anhand Manifest-ID sowie optional Inhaltshash erkennen.

1. In ein app-internes, eindeutiges Zielverzeichnis extrahieren.

1. Room-Datensätze atomar anlegen.

1. Bei Fehlern Transaktion zurückrollen und temporäre Daten löschen.

## 7.4 Importzustände und deutsche UI-Texte

| Zustand | Beispieltext |
| --- | --- |
| Auswahl | „Ordner auswählen“ / „ABook-Datei auswählen“ |
| Prüfung | „Hörbuch wird geprüft …“ |
| Import | „Hörbuch wird importiert …“ |
| Erfolg | „Import abgeschlossen“ |
| Duplikat | „Dieses Hörbuch ist bereits vorhanden.“ |
| Fehler Manifest | „Die ABook-Datei enthält kein gültiges Manifest.“ |
| Berechtigung | „Für diesen Ordner ist eine Zugriffsberechtigung erforderlich.“ |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](06_datenmodell.md)
 · [Nächstes Kapitel →](08_spezifikation-des-abook-formats.md)
