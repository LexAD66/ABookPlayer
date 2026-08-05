# ✨ FeatureList.md

## Vollständige Feature-Liste – ABook Player

---

## 🎨 UI/UX-Entscheidungsregeln

Verbindliche Details stehen in [VisualConcept.md](VisualConcept.md).

| Bereich | Entscheidung |
|---|---|
| Produktname | Sichtbar immer **ABook Player** |
| UI-Sprache | Deutsch für alle sichtbaren Texte |
| Bibliothek | Warmes Papier-/Buchsystem |
| Wiedergabe | Dunkles Stage-System mit Cover-Fokus |
| Einstellungen | Neutrales Material-3-System |
| Navigation | Hauptziele: Bibliothek, Wiedergabe, Einstellungen |
| Kontextziele | Hörbuchdetails, Kapitel, Lesezeichen und Import über Route, Sheet oder Dialog |
| Prototypen | Visuelle Referenz, keine finale Android-Implementierung |

---

## 🏠 Home-Screen

| Feature | Beschreibung | Status |
|---|---|---|
| Zuletzt gehört | Zeigt das zuletzt abgespielte Hörbuch mit Cover und Fortschritt | ✅ Geplant |
| Weitermachen | Direkter "Play"-Button für das aktuelle Hörbuch | ✅ Geplant |
| Kürzlich importiert | Liste der zuletzt hinzugefügten Hörbücher | ✅ Geplant |
| Schnellzugriff Bibliothek | Button zur vollständigen Bibliothek | ✅ Geplant |
| Fortschrittsanzeige | Prozentualer Fortschritt und verbleibende Zeit | ✅ Geplant |

---

## 📚 Bibliothek

| Feature | Beschreibung | Status |
|---|---|---|
| Listen-Ansicht | Hörbücher als vertikale Liste mit Cover, Titel, Autor, Fortschritt | ✅ Geplant |
| Grid-Ansicht | Hörbücher als Kachelraster (Cover-fokussiert) | ✅ Geplant |
| Filter: Alle | Alle Hörbücher anzeigen | ✅ Geplant |
| Filter: Neu | Noch nicht begonnene Hörbücher | ✅ Geplant |
| Filter: Gestartet | Hörbücher mit Fortschritt > 0% und < 100% | ✅ Geplant |
| Filter: Fertig | Abgeschlossene Hörbücher | ✅ Geplant |
| Filter: Quelle | Nach Herkunft filtern (Ordner, .abook) | ✅ Geplant |
| Suche | Hörbücher nach Titel oder Autor suchen | ✅ Geplant |
| Sortierung | Nach Titel, Autor, Importdatum, zuletzt gehört | ✅ Geplant |
| Manuelles Aktualisieren | Bibliothek manuell neu laden | ✅ Geplant |
| Hörbuch löschen | Aus Bibliothek entfernen (Dateien optional behalten) | ✅ Geplant |
| Hörbuch-Details | Langdruck öffnet Detailansicht mit vollständigen Metadaten | ✅ Geplant |

---

## ▶️ Wiedergabe

| Feature | Beschreibung | Status |
|---|---|---|
| Abspielen / Pausieren | Hauptsteuerung | ✅ Geplant |
| 10 Sekunden zurück | Primärer Rückwärts-Skip für verpasste Sätze | ✅ Geplant |
| 30 Sekunden vor | Primärer Vorwärts-Skip für Vorspann, Pausen oder Wiederholungen | ✅ Geplant |
| 1 Minute zurück | Erweiterte Steuerung, nicht gleichwertig zur Hauptsteuerreihe | ✅ Geplant |
| 1 Minute vor | Erweiterte Steuerung, nicht gleichwertig zur Hauptsteuerreihe | ✅ Geplant |
| Vorheriges Kapitel | Zum vorherigen Track/Kapitel springen | ✅ Geplant |
| Nächstes Kapitel | Zum nächsten Track/Kapitel springen | ✅ Geplant |
| Kapitel-Navigation | Bottom Sheet oder Detailansicht im Player-Kontext | ✅ Geplant |
| Wiedergabegeschwindigkeit | 0,5x bis 3,0x in 0,1er-Schritten | ✅ Geplant |
| Fortschrittsbalken | Seekbar mit aktueller Position und Restzeit | ✅ Geplant |
| Kapitelanzeige | Name und Nummer des aktuellen Kapitels | ✅ Geplant |
| Titelanzeige | Hörbuch-Titel und Autor im Player | ✅ Geplant |
| Großes Cover | Cover-Artwork im Vollbild-Stil | ✅ Geplant |
| Stille überspringen | Automatisches Überspringen langer Pausen | ✅ Geplant |
| Position speichern | Automatisches Speichern der aktuellen Position | ✅ Geplant |
| Position wiederherstellen | Fortsetzen an letzter Position beim Öffnen | ✅ Geplant |

### Steuerhierarchie

| Ebene | Aktionen |
|---|---|
| Primär | 10 s zurück, Play/Pause, 30 s vor |
| Sekundär | Kapitel, Geschwindigkeit, Schlaf-Timer, Lesezeichen |
| Erweitert | 1 min vor/zurück, Stille überspringen, Cast, Lock, ID3-/Metadaten-Aktionen |

---

## ⏰ Schlaf-Timer

| Feature | Beschreibung | Status |
|---|---|---|
| Timer setzen | Vordefinierte Zeiten (15, 30, 45, 60 Minuten) | ✅ Geplant |
| Benutzerdefinierter Timer | Eigene Zeit eingeben | ✅ Geplant |
| Ende-Kapitel-Option | Wiedergabe am Ende des aktuellen Kapitels stoppen | ✅ Geplant |
| Timer anzeigen | Verbleibende Zeit im Player anzeigen | ✅ Geplant |
| Timer abbrechen | Timer vorzeitig deaktivieren | ✅ Geplant |

---

## 🔖 Lesezeichen

| Feature | Beschreibung | Status |
|---|---|---|
| Lesezeichen erstellen | Aktuelle Position mit einem Tap merken | ✅ Geplant |
| Lesezeichen benennen | Optionalen Titel vergeben | ✅ Geplant |
| Notizen hinzufügen | Textnotiz zum Lesezeichen | ✅ Geplant |
| Lesezeichen-Liste | Alle Lesezeichen eines Hörbuchs anzeigen | ✅ Geplant |
| Zu Lesezeichen springen | Direktnavigation zur gespeicherten Position | ✅ Geplant |
| Lesezeichen bearbeiten | Titel und Notiz nachträglich ändern | ✅ Geplant |
| Lesezeichen löschen | Einzelne oder alle Lesezeichen entfernen | ✅ Geplant |
| Lesezeichen-Zeitstempel | Zeitposition im Hörbuch anzeigen | ✅ Geplant |

---

## 📂 Import – Ordner und Dateien

| Feature | Beschreibung | Status |
|---|---|---|
| Ordner hinzufügen | Ordner über SAF (Storage Access Framework) auswählen | ✅ Geplant |
| Ordner scannen | Alle unterstützten Audiodateien im Ordner finden | ✅ Geplant |
| Einzeldatei importieren | Einzelne Audiodatei importieren | ✅ Geplant |
| Mehrere Ordner | Mehrere Quell-Ordner gleichzeitig verwalten | ✅ Geplant |
| Metadaten lesen | ID3-Tags, M4B-Kapitel, Dateinamen auswerten | ✅ Geplant |
| Cover erkennen | Cover aus Metadaten oder `cover.jpg` im Ordner | ✅ Geplant |
| Duplikate erkennen | Bereits importierte Hörbücher nicht doppelt anlegen | ✅ Geplant |
| Bibliothek aktualisieren | Neue Dateien in bekannten Ordnern erkennen | ✅ Geplant |
| Unterstützte Formate | MP3, M4B, M4A, AAC, (zukünftig FLAC) | ✅ Geplant |

---

## 📦 Import – .abook Format

| Feature | Beschreibung | Status |
|---|---|---|
| .abook-Datei auswählen | Dateiauswahl-Dialog für .abook-Dateien | ✅ Geplant |
| ZIP-Validierung | Archivstruktur prüfen | ✅ Geplant |
| Manifest erkennen | XML oder JSON-Manifest automatisch erkennen | ✅ Geplant |
| Manifest parsen | Metadaten und Trackliste auslesen | ✅ Geplant |
| Dateien validieren | Referenzierte Audio- und Cover-Dateien prüfen | ✅ Geplant |
| ZIP-Slip-Schutz | Sicherheitsprüfung für Dateipfade im Archiv | ✅ Geplant |
| Cover importieren | Titelbild aus Archiv extrahieren | ✅ Geplant |
| Tracks importieren | Audiodateien in App-Storage extrahieren | ✅ Geplant |
| Trackreihenfolge | Exakt wie im Manifest definiert | ✅ Geplant |
| Duplikaterkennung | Bereits importierte .abook erkennen (Prüfsumme) | ✅ Geplant |
| Re-Import | Vorhandenes .abook neu importieren | ✅ Geplant |
| Fortschrittsanzeige | Import-Fortschritt in Echtzeit anzeigen | ✅ Geplant |
| Fehleranzeige | Detaillierte deutsche Fehlermeldungen | ✅ Geplant |
| Importstatus | Erfolgsmeldung nach Abschluss | ✅ Geplant |

---

## 🔔 Hintergrundwiedergabe

| Feature | Beschreibung | Status |
|---|---|---|
| Foreground-Service | Wiedergabe läuft im Hintergrund weiter | ✅ Geplant |
| Benachrichtigung | Media-Notification mit Play/Pause/Skip | ✅ Geplant |
| Sperrbildschirm | Steuerung vom Sperrbildschirm aus | ✅ Geplant |
| Bluetooth-Headset | Medientasten werden unterstützt | ✅ Geplant |
| Kabelgebundenes Headset | Kopfhörer-Taste (Play/Pause) | ✅ Geplant |
| Audio-Fokus | Andere Apps respektieren (Telefon, Navigation) | ✅ Geplant |
| Auto-Pause bei Anruf | Wiedergabe bei eingehendem Anruf pausieren | ✅ Geplant |

---

## ⚙️ Einstellungen

| Einstellung | Beschreibung | Standard |
|---|---|---|
| Standard-Geschwindigkeit | Voreingestellte Wiedergabegeschwindigkeit | 1,0x |
| Stille überspringen | Aktivieren / Deaktivieren | Deaktiviert |
| Theme | Hell / Dunkel / Systemstandard | System |
| Skip-Zeit vorne | Konfigurierbare Vorwärts-Sprungzeit | 30 s |
| Skip-Zeit hinten | Konfigurierbare Rückwärts-Sprungzeit | 10 s |
| Schlaf-Timer-Standard | Standard-Schlaf-Timer-Dauer | 30 min |
| .abook-Importverhalten | Extrahieren oder indizieren | Extrahieren |
| Bibliotheksordner | Liste verwalteter Ordner anzeigen/entfernen | – |
| Speicherort | App-internen Speicherpfad anzeigen | – |
| Alle Daten löschen | Bibliothek und Einstellungen zurücksetzen | – |

---

## 🌙 Dark Mode & Barrierefreiheit

| Feature | Beschreibung |
|---|---|
| Dunkles Theme | Vollständiges Material-3-Dunkeltheme |
| Systemtheme-Unterstützung | Automatisch hell/dunkel je nach Systemeinstellung |
| Visuelle Rollen | Warmes Bibliotheks-System, dunkles Stage-System, neutrales Einstellungs-System |
| Große Touch-Ziele | Mindestgröße 48dp für alle interaktiven Elemente |
| Serifenlose Schriftarten | Gut lesbar bei allen Schriftgrößen |
| Content Descriptions | Alle Icons haben Beschreibungen für Screenreader |
| Tablet-Unterstützung | Adaptives Layout für größere Bildschirme |

---

## 🔮 Zukünftige Features (Roadmap)

| Feature | Beschreibung |
|---|---|
| FLAC-Unterstützung | Verlustfreies Audioformat |
| .abook-Export | Eigene Hörbücher als .abook exportieren |
| Statistiken | Hörzeit, Fortschritt über Zeit |
| Cloud-Sync | Lesezeichen und Fortschritt synchronisieren |
| Widgets | Android-Homescreen-Widget |
| Auto-Import | Ordner automatisch überwachen |
| Kapitel-Export | Kapitelmarken aus M4B extrahieren |
| Mehrsprachigkeit | Englische UI-Übersetzung |
