# ABook Player Redesign: Library und Datenstruktur

## Ziel

Die Bibliothek soll sich zuerst wie eine Hoer-App anfuehlen: Buch finden, fortsetzen, importieren, verwalten. Wartungsfunktionen wie Scan, Cleanup, Duplikate und Dauer-Reparatur bleiben verfuegbar, werden aber aus dem normalen Lese-/Hoerfluss herausgenommen.

Primaere Nutzeraufgaben:

1. Zuletzt gehoertes Hoerbuch fortsetzen.
2. Ein Hoerbuch ueber Suche, Status, Serie oder Sortierung finden.
3. Neue Hoerbuecher per Datei oder Ordner importieren.
4. Metadaten, Cover, Kapitel, Figuren und Export in den Details verwalten.
5. Bibliothek bei Bedarf scannen, aufraeumen und Duplikate pruefen.

## Aktueller Befund

Die Library ist funktional stark, aber UI und Datenzustand sind zu eng vermischt:

- Die Top-Bar enthaelt zu viele gleichrangige Aktionen: Scan, Cleanup, Ansicht, Einstellungen und Statistik.
- Mobile Filter bestehen aus mehreren horizontal scrollenden Chip-Zeilen.
- Import, Scan und Cleanup sitzen im gleichen Dialog.
- Favoriten sind nur ViewModel-Zustand und nicht persistent.
- "Neu importiert" sortiert nach Datenbank-ID statt nach echtem Importzeitpunkt.
- Der Library-ViewModel-Start loest automatisch Scan und Dauer-Reparatur aus.
- Die Serienansicht ist nur eine Sortieroption; sie wirkt aber wie ein eigener Navigationsmodus.

## Ziel-Informationsarchitektur

### Hauptbereiche

1. **Bibliothek**
   - Normale Nutzung: suchen, filtern, sortieren, Buch oeffnen, Wiedergabe starten.

2. **Hinzufuegen**
   - Datei importieren.
   - Ordner importieren.
   - Standardordner scannen.

3. **Bibliothek verwalten**
   - Scan-Status.
   - Bekannte Importquellen.
   - Duplikate.
   - Verwaiste Eintraege.
   - 0-Minuten-Hoerbuecher reparieren.
   - Speicherort-Migration.

4. **Details**
   - Wiedergabe.
   - Metadaten.
   - Cover.
   - Kapitel.
   - Figuren.
   - Export.
   - Technische Dateiinfos.

## Screen Blueprint: Library

### Ziel

Der Screen zeigt die persoenliche Hoerbuchsammlung und macht die naechste sinnvolle Aktion direkt erreichbar.

### Primaerer Nutzer

Ein mobiler Android-Nutzer, der lokal gespeicherte Hoerbuecher hoert und gelegentlich neue Dateien oder Ordner importiert.

### Entry Points

- App-Start.
- Rueckkehr vom Player.
- Rueckkehr von Details, Settings, Statistik oder Wartung.

### Top-Level Layout

#### Mobile Portrait

1. **Top Bar**
   - Titel: `Meine Bibliothek`
   - Aktionen:
     - Suche oeffnen oder Suchfeld fokussieren.
     - Ansicht wechseln: Liste/Raster.
     - Mehr-Menue.

2. **Active Filter Bar**
   - Kompakte Zeile mit maximal 2-3 sichtbaren Chips:
     - Aktiver Status, z. B. `Angefangen`.
     - Aktive Sortierung, z. B. `Zuletzt gehoert`.
     - Button `Filter`.
   - Kein dauerhaftes Anzeigen aller Filteroptionen.

3. **Content List**
   - Liste oder Raster.
   - Default: Liste, weil Titel, Autor, Sprecher, Fortschritt und Restzeit wichtig sind.
   - Raster als bewusst gewaehlt Ansicht fuer cover-zentriertes Stoebern.

4. **Mini Player**
   - Nur sichtbar, wenn ein aktuelles Hoerbuch existiert.
   - Zeigt Cover, Titel, Fortschritt, Play/Pause, +10s.
   - Tap auf die Bar oeffnet den Player.

5. **FAB**
   - Nur `Hinzufuegen`.
   - Oeffnet Bottom Sheet mit Importoptionen.

#### Landscape / Tablet

1. **Left Filter Rail**
   - Suchfeld.
   - Status.
   - Sortierung.
   - Serienmodus nur anzeigen, wenn `Sortierung = Serien` aktiv ist.

2. **Main Content**
   - Adaptive Grid oder Liste.
   - Bei breiten Displays optional Split View: links Liste, rechts Detailvorschau.

3. **Mini Player**
   - Als Bottom Bar oder kompakter rechter Bereich, aber nicht zwischen Filter und Liste.

### Content Priority

1. Aktuelles/zuletzt gehoertes Hoerbuch und Fortschritt.
2. Such- und Filterzustand.
3. Hoerbuchliste.
4. Import und Wartung.

### Primary Actions

- Hoerbuch antippen: Player oeffnen oder Wiedergabe vorbereiten.
- Details oeffnen.
- Wiedergabe im Mini Player steuern.
- Hoerbuecher hinzufuegen.

### Secondary Actions

- Mehrfachauswahl.
- Favorit setzen.
- Als beendet markieren.
- Als ungelesen markieren.
- Loeschen.
- Statistik und Einstellungen oeffnen.

### Key States

- **Empty:** Erklaert Dateiimport, Ordnerimport und Standardscan. Zeigt zwei Hauptbuttons: `Ordner importieren`, `Datei importieren`.
- **Filtered Empty:** Zeigt aktiven Filter/Suchtext und Button `Filter zuruecksetzen`.
- **Loading/Scanning:** Zeigt inline Status: `Bibliothek wird gescannt...`; Liste bleibt sichtbar, wenn Daten vorhanden sind.
- **Duplicate Found:** Zeigt Wartungs-Hinweis oder Bottom Sheet, nicht sofort einen blockierenden Dialog.
- **Error:** Snackbar fuer kurze Fehler; bei Importfehlern zusaetzlich Detailaktion `Details anzeigen`.

### Edit Pattern

- Filter: Bottom Sheet auf mobile, Filter Rail auf landscape/tablet.
- Import: Bottom Sheet.
- Mehrfachauswahl: Selection Top Bar.
- Loeschen: AlertDialog mit klarer Anzahl und Folge.
- Duplikate: eigenes Wartungs-Sheet oder eigener Wartungsscreen.

## Component Spec

### Library Top Bar

Inhalt:

- Titel `Meine Bibliothek`.
- Icon `Search`.
- Icon fuer Listen-/Rasteransicht.
- Overflow-Menue.

Overflow-Menue:

- `Bibliothek verwalten`
- `Statistiken`
- `Einstellungen`

Nicht in die Top-Bar:

- Direktes Cleanup.
- Direktes Duplikatloeschen.
- Direkter Vollscan, ausser als explizite Option im Verwaltungsbereich.

### Active Filter Bar

Zeigt nur aktive oder wichtigste Auswahl:

- Status-Chip.
- Sortier-Chip.
- `Filter`-Button.
- `Zuruecksetzen`, wenn mehr als ein Filter aktiv ist.

Alle Optionen kommen in ein Bottom Sheet:

- Status: Alle, Favoriten, Angefangen, Beendet, Ungespielt.
- Sortierung: Zuletzt gehoert, Titel, Autor, Serien, Restzeit, Neu importiert.
- Serienansicht: Stapel, Regal, Ordner. Nur aktivierbar, wenn Serien-Sortierung gewaehlt ist.

### Add Bottom Sheet

Titel: `Hoerbuecher hinzufuegen`

Optionen:

- `Ordner importieren`
  - SAF-Tree-Picker.
  - Beschreibung: mehrere Dateien und CD/Disc-Unterordner.

- `Datei importieren`
  - SAF-Datei-Picker.
  - Beschreibung: `.abook`, `.zip`, `.m4b`, Audio-Dateien.

- `Standardordner scannen`
  - Scannt bekannte lokale Ordner.
  - Zeigt anschliessend Ergebnis.

Nicht enthalten:

- Cleanup.
- Duplikate loeschen.

### Library Management Screen

Screen oder Bottom Sheet, je nach Aufwand. Empfohlen: eigener Screen, weil Funktionen potenziell destruktiv sind.

Regionen:

1. **Status Header**
   - Letzter Scan.
   - Anzahl Hoerbuecher.
   - Anzahl erkannter Probleme.

2. **Importquellen**
   - Standardordner.
   - SAF-Ordner.
   - Public Storage Folder.

3. **Wartungsaktionen**
   - `Jetzt scannen`
   - `Verwaiste Eintraege pruefen`
   - `0-Minuten-Hoerbuecher reparieren`
   - `Duplikate anzeigen`

4. **Duplikat-Liste**
   - Bestehendes Hoerbuch.
   - Kandidat.
   - Dauervergleich.
   - Speicherort-Kurzname.
   - Aktionen: `Beide behalten`, `Kopie loeschen`.

### Audiobook List Card

Pflichtfelder:

- Cover oder Fallback.
- Titel.
- Autor.
- Sprecher, falls vorhanden.
- Serie und Band, falls vorhanden.
- Fortschritt in Prozent.
- Restzeit oder Gesamtdauer.
- Details-Icon.

Zusaetzlich:

- Favorit als kleines, persistentes Symbol.
- Status-Pill: `Neu`, `Angefangen`, `Beendet`, `Fehlt`, falls `availabilityState` eingefuehrt wird.

### Audiobook Grid Card

Pflichtfelder:

- Cover.
- Fortschritt-Overlay.
- Titel.
- Autor.

Optional:

- Favorit.
- Details-Icon.

Grid darf weniger Metadaten zeigen als Liste. Liste bleibt Default fuer produktive Nutzung.

## Data-Manager Zielmodell

### Audiobook erweitern

Empfohlene neue Felder:

```kotlin
val addedAt: Long = System.currentTimeMillis()
val isFavorite: Boolean = false
val sourceType: AudiobookSourceType = AudiobookSourceType.LOCAL_FILE
val availabilityState: AvailabilityState = AvailabilityState.AVAILABLE
```

Enums:

```kotlin
enum class AudiobookSourceType {
    LOCAL_FILE,
    LOCAL_FOLDER,
    SAF_FILE,
    SAF_FOLDER,
    ABOOK_ARCHIVE,
    EXTRACTED_ARCHIVE
}

enum class AvailabilityState {
    AVAILABLE,
    MISSING_FILE,
    MISSING_PERMISSION,
    NEEDS_RESCAN
}
```

### Persistente Favoriten

Favoriten duerfen nicht im ViewModel leben. Zwei akzeptable Varianten:

1. Einfach: `isFavorite` direkt in `AudiobookEntity`.
2. Erweiterbar: eigene Tabelle `library_flags`, falls spaeter Tags, Sammlungen oder manuelle Statuswerte dazukommen.

Empfehlung fuer dieses Projekt: erst `isFavorite` in `AudiobookEntity`, weil der aktuelle Bedarf klein ist.

### Echtes Importdatum

`HINZUGEFUEGT_AM` soll nach `addedAt` sortieren, nicht nach `id`.

Migration:

- Neue Spalte `addedAt INTEGER NOT NULL DEFAULT 0`.
- Beim Lesen oder Migrieren alte Datensaetze mit `lastPlayed` oder aktuellem Zeitpunkt befuellen.
- Sortierung: `sortedByDescending { it.addedAt }`.

### Scan- und Wartungszustand

Im ViewModel sollte ein expliziter Zustand existieren:

```kotlin
data class LibraryMaintenanceUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val lastScanMessage: String? = null,
    val duplicates: List<DuplicateMatch> = emptyList(),
    val cleanupResult: LibraryCleanupResult? = null
)
```

Vorteil:

- UI kann Scan/Cleanup als Prozess darstellen.
- Duplikate blockieren nicht sofort die normale Bibliothek.
- Fehler und Ergebnisse sind unterscheidbar.

## Navigation Model

Bestehende Navigation bleibt erhalten, wird aber klarer strukturiert:

```text
library
  -> player
  -> details
      -> info
      -> characters/{audiobookId}
  -> statistics
  -> settings
  -> library_management
```

Neue Route:

- `library_management`

Keine neue Architektur notwendig. Das passt in die bestehende Compose-Navigation.

## Umsetzungsschritte

### Phase 1: Persistente Library-Grundlagen

1. `Audiobook` und `AudiobookEntity` um `isFavorite` und `addedAt` erweitern.
2. Room-Migration ergaenzen.
3. Repository-Methoden fuer Favorit und Importdatum ergaenzen.
4. `LibraryViewModel.favoriteBookIds` durch persistierte Daten ersetzen.
5. Sortierung `HINZUGEFUEGT_AM` auf `addedAt` umstellen.
6. Tests fuer Favoritenpersistenz und Importdatum ergaenzen.

### Phase 2: Library UI entlasten

1. Top-Bar auf Suche, Ansicht und Overflow reduzieren.
2. Scan, Cleanup, Statistik und Settings in Overflow bzw. eigene Ziele verschieben.
3. Mobile Filter in Bottom Sheet auslagern.
4. Serienansicht nur anzeigen, wenn Serien-Sortierung aktiv ist.
5. Empty State um `Filter zuruecksetzen` erweitern.

### Phase 3: Hinzufuegen und Verwaltung trennen

1. FAB oeffnet nur `Hoerbuecher hinzufuegen`.
2. Import-Bottom-Sheet mit Datei, Ordner, Standardscan.
3. `library_management` Screen anlegen.
4. Cleanup und Duplikate in Verwaltung verschieben.
5. Duplikate als Liste mit klaren Entscheidungen darstellen.

### Phase 4: Scan-Verhalten kontrollieren

1. Automatischen Scan im `LibraryViewModel.init` entfernen oder hinter Setting legen.
2. Dauer-Reparatur nicht doppelt im `init` starten.
3. Explizite Scan-/Cleanup-Statuswerte einfuehren.
4. Bestehende Bibliothek beim Scannen sichtbar lassen.

## Acceptance Criteria

- Favoriten bleiben nach App-Neustart erhalten.
- `Neu importiert` sortiert nach echtem Importzeitpunkt.
- Der Library-Screen zeigt im Normalzustand keine Cleanup- oder Duplikatloesch-Aktion direkt in der Top-Bar.
- Mobile Nutzer sehen maximal eine kompakte Filterzeile; alle Filteroptionen liegen in einem Bottom Sheet.
- Import und Wartung sind getrennte Flows.
- Duplikatentscheidungen zeigen genug Kontext, ohne sofort den Hauptscreen zu blockieren.
- Scan/Cleanup zeigen laufende, erfolgreiche und fehlerhafte Zustaende getrennt.
- Keine bestehende Wiedergabe-, Details- oder Importfunktion wird entfernt.

## Erste technische Ziel-Dateien

- `app/src/main/java/de/f_soft_studio/abookplayer/domain/model/Audiobook.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/data/local/entity/AudiobookEntity.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/data/repository/AudiobookRepository.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryScreen.kt`
- optional neu: `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryManagementScreen.kt`

## Nicht-Ziele

- Kein Architekturwechsel.
- Keine neue externe UI-Library.
- Kein Streaming-Katalog.
- Kein Login.
- Keine Entfernung bestehender Importpfade.

