# 4. Architektur

Die App verwendet eine pragmatische Schichtentrennung. Abhängigkeiten zeigen nach innen: UI kennt ViewModels und Domain-Modelle, die Domain kennt keine Android-Details, und die Data-Schicht implementiert Repositories für Room, SAF und .abook-Verarbeitung.

| Schicht | Verantwortung | Typische Bestandteile |
| --- | --- | --- |
| presentation / ui | Darstellung, UI-Zustand, Benutzerereignisse | Compose Screens, Komponenten, UiState, ViewModels, Navigation |
| domain | Geschäftsregeln und stabile Schnittstellen | Modelle, Repository-Interfaces, UseCases |
| data | Persistenz und Dateiverarbeitung | Room, DAO, Repository-Implementierungen, SAF-Scanner, .abook-Parser |
| playback | Wiedergabe unabhängig von Screens | PlayerController, MediaSessionService, MediaItems, Fortschrittsevents |
| di | Zusammenbau der Anwendung | Hilt-Module für Datenbank, Repositories, Player und Parser |

## 4.1 Datenfluss

```text
Compose UI
    -> ViewModel / UiState
        -> UseCase
            -> Repository-Interface
                -> Room / SAF / .abook / Media3
```

```text
Player- und Datenereignisse
    -> Repository oder PlayerController
        -> StateFlow
            -> ViewModel
                -> Compose UI
```

## 4.2 Single Source of Truth

Room ist die verbindliche Quelle für Bibliothek, Kapitel, Fortschritt, Lesezeichen und Statistik. Dateisystem und Archive liefern Importdaten; sie dürfen UI-Zustand nicht dauerhaft parallel zur Datenbank verwalten. Der Player besitzt nur den flüchtigen Wiedergabezustand und schreibt relevante Änderungen zurück.

## 4.3 Fehlerbehandlung

| Fehler | UI-Reaktion | Technische Reaktion |
| --- | --- | --- |
| Datei fehlt | „Datei nicht verfügbar“ und „Neu verknüpfen“ anbieten | Datensatz erhalten, Verfügbarkeit markieren. |
| Berechtigung fehlt | Geführte Erklärung und Ordner erneut auswählen | Persistierte URI-Berechtigung erneuern. |
| Playerfehler | Snackbar oder Dialog mit verständlicher Meldung | Fehler loggen, Session stabil halten, optional nächsten Track prüfen. |
| Ungültiges .abook | Import abbrechen und genaue Ursache anzeigen | Keine Teilimporte; temporäre Dateien bereinigen. |
| Datenbankmigration | Start nicht mit Datenverlust fortsetzen | Explizite Migrationen und Tests; destructive migration nur in Entwicklung. |

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](03_technologiestack-und-entwicklungsumgebung.md)
 · [Nächstes Kapitel →](05_empfohlene-projekt-und-paketstruktur.md)
