# 14. Migrations- und Konsolidierungsleitfaden

1. Aktuelle Android-Codebasis als alleinigen produktiven Ausgangspunkt festlegen.

1. JSX/HTML-Prototypen in einen design-reference-Ordner verschieben und klar kennzeichnen.

1. Doppelte Modelle, Services und Repositories inventarisieren; vor Entfernung Abhängigkeiten und Datenmigration prüfen.

1. Ein kanonisches Domain-Modell und ein Room-Schema festlegen.

1. Legacy-.abook- oder metadata.json-Unterstützung hinter einem ImportAdapter kapseln.

1. Alte Fortschritts- und Track-IDs über explizite Migration auf stabile IDs überführen.

1. Nach jedem kleinen Schritt Unit-Tests und assembleDebug ausführen.

1. Am Ende assembleRelease, Instrumentation-Tests und manuelle Wiedergabetests durchführen.

## 14.1 Nicht ungeprüft löschen

- Room-Migrationen und exportierte Schemas.

- Dateiformat-Beispiele und Testarchive.

- Designreferenzen, solange das Compose-UI noch nicht visuell abgenommen wurde.

- Legacy-Parser, solange bestehende Nutzerdateien nicht migriert sind.

- ProGuard-/R8-Regeln für Media3, Room oder Serialisierung.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](13_roadmap.md)
 · [Nächstes Kapitel →](15_master-prompt-fuer-gemini-flash-25.md)
