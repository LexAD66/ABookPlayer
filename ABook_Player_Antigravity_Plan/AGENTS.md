# Antigravity Agent-Regeln

## Grundregel
Arbeite sparsam mit Kontext. Dieses Projekt soll aus mehreren alten Ansätzen zu einer einzigen sauberen Codebasis konsolidiert werden.

## Verbindliche Quellen
Nur diese Dateien sind verbindlich:
- `PROJECT_BRIEF.md`
- `DECISIONS.md`
- `TODO.md`
- `AGENTS.md`

Die vorhandenen `.jsx`-Dateien und Screenshots sind Design-Referenzen, keine finale Android-Implementierung.

## Arbeitsweise
- Erzeuge kleine, kompilierbare Schritte.
- Ändere nur Dateien, die für die aktuelle Aufgabe nötig sind.
- Keine komplette Neugenerierung ohne Auftrag.
- Keine Dateien löschen ohne vorherige Liste mit Begründung.
- Keine Architektur wechseln.
- Keine unnötigen Libraries hinzufügen.
- Deutsche UI-Texte sind Pflicht.
- Kotlin-Code ausführlich auf Deutsch kommentieren.
- Erst analysieren, dann ändern.

## Sicherheitsregeln
- Keine autonomen Löschaktionen.
- Keine Terminal-Befehle mit destruktiver Wirkung ohne Bestätigung.
- Keine Projektstruktur ersetzen, wenn Migration möglich ist.

## Prioritäten
1. Kompilierbarer Code
2. Kleine, nachvollziehbare Änderungen
3. Saubere MVVM-Struktur
4. Deutsche UI-Texte
5. Gute Kommentare
6. Minimaler Tokenverbrauch

## Start-Prompt für Antigravity
```text
Lies zuerst AGENTS.md, PROJECT_BRIEF.md, DECISIONS.md und TODO.md.

Aufgabe:
Konsolidiere dieses ABook/Hörbuch-App-Projekt zu EINER sauberen Android-Codebasis.

Regeln:
- Verwende nur die aktuelle Projektstruktur.
- Nutze die JSX-Dateien und Screenshots nur als Design-Referenz.
- Übernimm aus alten Varianten nur sinnvolle Teile.
- Entferne keine Dateien ohne vorherige Liste mit Begründung.
- Erzeuge kleine, kompilierbare Schritte.
- Deutsche UI-Texte.
- Deutsche Kommentare.
- Kein Architekturwechsel.

Erster Schritt:
Analysiere das Projekt und erstelle einen kurzen Plan:
1. Was ist vorhanden?
2. Was ist doppelt?
3. Was ist kaputt oder nur Prototyp?
4. Was soll behalten werden?
5. Was soll entfernt oder archiviert werden?

Danach warte auf Bestätigung.
```
