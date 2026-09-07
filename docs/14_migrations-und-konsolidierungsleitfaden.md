# 14. Migrations- und Konsolidierungsleitfaden

## 14.1 Status der Konsolidierung

- Die native Codebasis unter `ABookPlayer/` ist der alleinige produktive Ausgangspunkt. ✅
- JSX/HTML-Prototypen liegen getrennt in `ABook_Player_Antigravity_Plan/` und sind reine Designreferenz. ✅
- Ein kanonisches Domain-Modell (`domain/model/`) und ein Room-Schema (v11) sind etabliert. ✅
- Legacy-`metadata.json` wird im Parser weiterhin erkannt; `manifest.json`/`manifest.xml` ist kanonisch. ✅
- Der Hörfortschritt liegt als Spalte in `audiobooks` (keine separate Progress-Entity, keine offene ID-Migration). ✅

## 14.2 Room-Migrationsleiter

`AbookDatabase` verwendet explizite Migrationen `MIGRATION_3_4` … `MIGRATION_10_11` (Details in Kapitel 6.1) plus `fallbackToDestructiveMigration()` als letzte Absicherung. Schema wird nicht exportiert (`exportSchema = false`). Jede Schemaänderung braucht eine neue nummerierte Migration.

## 14.3 Vor jeder Änderung

1. Zuerst analysieren, dann in kleinen, kompilierbaren Schritten ändern.
2. Nach jedem Schritt `./gradlew.bat test` (+ `assembleDebug` bei UI/Ressourcen/Manifest).
3. Vor release-nahen Änderungen zusätzlich `assembleRelease` und manuelle Wiedergabetests.

## 14.4 Nicht ungeprüft löschen

- Room-Migrationen.
- Dateiformat-Beispiele und Testarchive.
- Designreferenzen in `ABook_Player_Antigravity_Plan/`, solange das Compose-UI nicht visuell abgenommen ist.
- Legacy-Parserpfade, solange bestehende Nutzerdateien nicht migriert sind.
- `proguard-rules.pro` (auch wenn R8 aktuell aus ist).
- Lokale Debug-Artefakte im Repo-Root (`*.db`, `logcat_repro.txt`, `ui.xml`) sind via `.gitignore` ausgeschlossen.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](13_roadmap.md)
 · [Nächstes Kapitel →](15_master-prompt-fuer-gemini-flash-25.md)
