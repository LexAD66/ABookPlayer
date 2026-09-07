# 17. Schlussfolgerung

Die Doku-Stände beschreiben keine zwei getrennten Produkte, sondern Entwicklungsphasen derselben ABook-Player-Idee. Der Ist-Zustand ist eine einzige native Android-Codebasis mit Kotlin, Jetpack Compose, Material 3, Media3 und Room; die Verdrahtung erfolgt **manuell ohne DI-Bibliothek**.

Fachliche Kernmerkmale: das portable `.abook`-Format, ein robuster SAF- und Ordner-Import mit app-verwaltetem Bibliotheksordner, zuverlässige Hintergrundwiedergabe inklusive Android Auto, sowie Kapitel, Lesezeichen, Figuren, Hörstatistik und optionaler WebDAV-Fortschritts-Sync.

Aktueller Schwerpunkt ist die Library-Umstrukturierung (Phase 20): Der Backend-Teil ist umgesetzt, der UI-Umbau steht noch aus – siehe [`../ABookPlayer/redesign.md`](../ABookPlayer/redesign.md). Historische Phasen bleiben als Migrationswissen relevant, werden aber nicht mehr als parallele Architektur gepflegt.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](16_quellenzuordnung.md)
