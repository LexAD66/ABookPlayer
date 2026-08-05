# 1. Dokumentstatus und Konsolidierungsregeln

Dieses Dokument ersetzt die verstreuten Markdown-Dateien der beiden Archive als fachliche Gesamtübersicht. Es ist keine automatische Aneinanderreihung, sondern eine bereinigte Zusammenführung. Der neuere Antigravity-Stand bildet die Zielarchitektur; ältere Dokumente liefern zusätzliche Details zu SAF, Media3, Migrationen, Bookmarks, Statistik und Release-Härtung.

## 1.1 Verbindliche Priorität bei Widersprüchen

1. Aktuelle Projektentscheidungen und der konsolidierte TODO-Stand haben Vorrang.

1. Die ausführliche Antigravity-Dokumentation ist die technische Zielbeschreibung.

1. Ältere Phasen-Dokumente gelten als Implementierungs- und Migrationshistorie.

1. JSX-, HTML- und Screenshot-Dateien sind ausschließlich Designreferenzen, kein produktiver Android-Code.

1. Alte Formatvarianten werden nur über klar abgegrenzte Kompatibilitätsadapter unterstützt.

## 1.2 Bereinigte Konflikte

| Thema | Älterer Stand | Konsolidierte Festlegung |
| --- | --- | --- |
| Sprungweiten | 10 Sekunden zurück / 30 Sekunden vor | Standardmäßig ±10 Sekunden; später in Einstellungen konfigurierbar. |
| .abook-Metadaten | Teilweise metadata.json bzw. ältere Rebuild-Kit-Varianten | Kanonisch manifest.json oder manifest.xml, Formatversion 1; Legacy-Importer optional. |
| Android-Studio-Version | Panda 1 / 2025.3.1 | Zielumgebung: Android Studio Otter 2025.2.1 mit JDK 21; Abhängigkeiten projektbezogen prüfen. |
| Projektstatus | Teils als geplant beschrieben | Antigravity-TODO meldet Phasen 0–7 inklusive Builds und Unit-Tests als abgeschlossen; reale Codebasis bleibt vor Release erneut zu verifizieren. |

---

[← Inhaltsverzeichnis](README.md)
 · [Nächstes Kapitel →](02_projektueberblick.md)
