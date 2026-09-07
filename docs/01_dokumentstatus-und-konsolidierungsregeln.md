# 1. Dokumentstatus und Konsolidierungsregeln

Dieses Dokument fasst die fachliche Gesamtübersicht zusammen. Es ist keine automatische Aneinanderreihung, sondern eine bereinigte Zusammenführung, die anschließend am tatsächlichen Code-Stand ausgerichtet wurde. Ältere Dokumente liefern zusätzliche Details zu SAF, Media3, Migrationen, Bookmarks, Statistik und Release-Härtung.

## 1.1 Verbindliche Priorität bei Widersprüchen

1. **Der Code ist die Wahrheit.** Bei Abweichung zwischen Doku und Quellcode gilt der Quellcode; die Doku wird nachgezogen.
2. [`../CLAUDE.md`](../CLAUDE.md), [`../ABook_Player_Antigravity_Plan/AGENTS.md`](../ABook_Player_Antigravity_Plan/AGENTS.md) und [`../PROJECT_HISTORY.md`](../PROJECT_HISTORY.md) haben Vorrang vor den Kapiteltexten.
3. Diese `docs/`-Kapitel beschreiben Ziel und Ist-Zustand der nativen Codebasis.
4. Ältere Phasen-Dokumente gelten als Implementierungs- und Migrationshistorie.
5. JSX-, HTML- und Screenshot-Dateien in `ABook_Player_Antigravity_Plan/` sind ausschließlich Designreferenzen, kein produktiver Android-Code.
6. Alte `.abook`-Formatvarianten werden nur über klar abgegrenzte Kompatibilitätspfade im Parser unterstützt.

## 1.2 Bereinigte Konflikte

| Thema | Älterer Stand | Ist-Zustand im Code |
| --- | --- | --- |
| Sprungweiten | 10 s zurück / 30 s vor | Standard ±10 s (`-10s` / `+10s`), zusätzlich `+30s`; Sprungweite in den Einstellungen konfigurierbar. |
| `.abook`-Metadaten | Teilweise `metadata.json` bzw. ältere Rebuild-Kit-Varianten | Kanonisch `manifest.json` oder `manifest.xml`; Legacy-`metadata.json` wird beim Import weiterhin erkannt. |
| Dependency Injection | „Hilt" in älteren Dokumenten | **Kein Hilt.** Manuelle Verdrahtung: `AbookDatabase`/`PlaybackController` als `getInstance`-Singletons in `MainActivity`, ViewModels via `remember { … }` in `AppRoot`. |
| Room-Schema | „v6", Migrationen bis 5→6 | **Schema-Version 11**, Migrationen `MIGRATION_3_4` … `MIGRATION_10_11`, `fallbackToDestructiveMigration()` als Notnagel. |
| App-Version | „Release 1.1.0" | **2.0.0** (versionCode 300). |
| Cloud-Sync / Android Auto | Als Nicht-Ziele des MVP geführt | Beide umgesetzt: WebDAV-Fortschritts-Sync (`storage/sync/`) und Android-Auto-Modus (`MediaLibraryService`, `ui/car/`). |
| Entwicklungsumgebung | Diverse Android-Studio-Versionen | Aktuell: AGP 8.5.2, Kotlin 2.0.20, JDK/JVM 21, compileSdk/targetSdk 35, minSdk 24. |

---

[← Inhaltsverzeichnis](README.md)
 · [Nächstes Kapitel →](02_projektueberblick.md)
