# 🎨 VisualConcept.md

## Verbindliches visuelles Zielbild – ABook Player

Dieses Dokument bereinigt die im Design-Audit gefundenen Widersprüche zwischen Prototypen, Screenshots und aktueller Produktdokumentation. Es ist die verbindliche UI/UX-Referenz für die Umsetzung in Jetpack Compose.

Die vorhandenen JSX-Dateien und Screenshots bleiben **Design-Referenzen**. Sie sind keine finale Android-Implementierung und dürfen nicht 1:1 übernommen werden.

---

## 1. Ziel des visuellen Systems

ABook Player ist ein lokaler Hörbuch-Player für lange Hörsitzungen. Das UI muss deshalb:

- aktuelle Wiedergabe und Wiederaufnahme schnell erreichbar machen,
- große, sichere Touch-Ziele für Player-Steuerung bieten,
- lange Titel, Kapitel und Lesezeichen lesbar darstellen,
- visuelle Ermüdung in dunklen Wiedergabesituationen vermeiden,
- deutsche UI-Texte durchgehend verwenden,
- Material 3 als technisches Designsystem nutzen.

Dekorative Effekte sind nachrangig. Cover-Artwork darf Atmosphäre tragen, darf aber Steuerung, Textlesbarkeit oder Barrierefreiheit nicht überlagern.

---

## 2. Umsetzungsplan

| Schritt | Ergebnis |
|---|---|
| 1. Visuelle Rollen festlegen | Jeder Screen erhält genau eine primäre visuelle Rolle. |
| 2. Navigation entscheiden | Bottom Navigation und Kontext-Routen werden klar getrennt. |
| 3. Player-Steuerung priorisieren | Häufige Hörbuch-Aktionen werden primär, seltene Aktionen sekundär. |
| 4. Sprache und Branding bereinigen | Alle sichtbaren Texte werden deutsch; Produktname wird einheitlich. |
| 5. Tokens in Material 3 überführen | Papier- und Stage-Tokens werden in `ColorScheme`, Typography und Shapes abgebildet. |
| 6. Referenzen markieren | Alte Smart-Audiobook-Player-Screens bleiben Inspirationsmaterial, nicht Zielzustand. |

---

## 3. Visuelle Rollen pro Screen

| Screen | Visuelle Rolle | Begründung |
|---|---|---|
| Home | Warmes Bibliotheks-System | Einstieg, Wiederaufnahme und zuletzt gehörte Bücher brauchen ruhige Scanbarkeit. |
| Bibliothek | Warmes Bibliotheks-System | Cover, Autorengruppen, Suche, Filter und Fortschritt sollen wie ein geordnetes Regal wirken. |
| Hörbuchdetails | Warmes Bibliotheks-System | Metadaten, Beschreibung, Kapitelübersicht und Verwaltung sind lesende Aufgaben. |
| Import | Warmes Bibliotheks-System | Import ist ein ruhiger Verwaltungsfluss mit Status- und Fehlermeldungen. |
| Lesezeichen | Warmes Bibliotheks-System | Notizen und Zeitpositionen brauchen hohe Textlesbarkeit. |
| Player | Dunkles Stage-System | Während der Wiedergabe stehen Cover, Fortschritt und große Steuerung im Fokus. |
| Kapitel-Auswahl im Player | Dunkles Stage-System als Bottom Sheet | Kapitelwechsel ist eine Wiedergabeaktion und bleibt im Player-Kontext. |
| Schlaf-Timer | Dunkles Stage-System | Timer wird während der Wiedergabe genutzt und soll nachts nicht blenden. |
| Hörstatistik | Dunkles Stage-System mit ruhigen Karten | Statistik ist eine Auswertung der Höraktivität und darf stärker visuell verdichten. |
| Einstellungen | Neutrales Material-3-System | Einstellungen sind funktional; sie erben weder Buchregal- noch Stage-Dramatik. |

### Regel

Ein Screen darf nicht gleichzeitig warmes Papier-System, altes Smart-Audiobook-Player-Chrome und dunkles Stage-System mischen. Wenn ein Screen Kontext aus einer anderen Rolle öffnet, passiert das über Sheet, Dialog oder Detailroute mit klarer visueller Herkunft.

---

## 4. Navigation

### Primäres Modell

Die App verwendet eine einfache Shell mit zwei permanenten Top-Level-Zielen. Die Wiedergabe ist ein kontextuelles Vollbildziel, damit der Player keine Höhe an eine Bottom Navigation verliert und als dunkle Stage wirken kann.

| Ziel | Deutscher Titel | Zweck |
|---|---|---|
| `library` | Bibliothek | Bücher finden, filtern, importieren und öffnen. |
| `settings` | Einstellungen | App-Verhalten konfigurieren. |

Diese zwei Ziele erscheinen in der Bottom Navigation. Die Labels sind deutsch: **Bibliothek**, **Einstellungen**. Die Bottom Navigation wird im Player ausgeblendet.

### Kontextziele

| Route | Darstellung | Einstieg |
|---|---|---|
| `player/{audiobookId}` | Vollbild-Player im dunklen Stage-System | Bibliothek, Hörbuchdetails, aktuelle Wiedergabe |
| `book/{audiobookId}` | Vollbild-Detailseite | Bibliothek, Home, Suche |
| `chapters/{audiobookId}` | Player-Bottom-Sheet oder Detailseite | Player, Hörbuchdetails |
| `bookmarks/{audiobookId}` | Detailseite im warmen System | Hörbuchdetails, Player |
| `import` | Vollbild-Flow | Bibliothek, Home |

### Nicht mehr als Zielzustand verwenden

- Alte Smart-Audiobook-Player-Toolbar mit vielen gleichwertigen Symbolen.
- Englische Bottom-Navigation-Labels wie `Library`, `Player`, `Settings`.
- Gleichzeitige Top-Level-Navigation über Bottom Bar und redundante Toolbar-Tabs.

---

## 5. Player-Steuerung

### Primäre Steuerreihe

Die Hauptsteuerung besteht aus:

1. **10 Sekunden zurück**
2. **Play/Pause**
3. **30 Sekunden vor**

Diese drei Aktionen sind die wichtigsten Hörbuch-Aktionen und erhalten die größten Touch-Ziele.

### Sekundäre Steuerung

Sekundär sichtbar oder über kompakte Controls erreichbar:

- Kapitel öffnen
- Geschwindigkeit ändern
- Schlaf-Timer öffnen
- Lesezeichen setzen

### Erweiterte Steuerung

Nur als Long-Press, erweitertes Menü oder optionale Einstellung:

- 1 Minute zurück
- 1 Minute vor
- Stille überspringen
- Cast
- Lock-Modus
- ID3-/Metadaten-Aktionen

### Regel

1-Minuten-Sprünge dürfen nicht gleichwertig neben 10-/30-Sekunden-Sprüngen stehen. Der Player muss zuerst die häufige Hörsituation lösen: kurz zurückspulen, fortsetzen, kurz vorspringen.

### Kompaktmodus

Der Player bleibt ohne vertikales Scrollen. Bei sehr kleiner Höhe oder großer Systemschrift wird automatisch eine kompaktere Variante verwendet:

- kleineres Cover,
- reduzierte Außenabstände,
- kompaktere primäre Steuerbuttons,
- einzeiliger Titel mit Ellipse,
- sekundäre Button-Labels werden zugunsten der Icons ausgeblendet.

Diese Regel schützt die Priorität der drei Hauptaktionen, ohne die Wiedergabe in eine scrollende Einstellungsseite zu verwandeln.

---

## 6. Sprache und Branding

### Produktname

Verbindlicher sichtbarer Produktname: **ABook Player**

Interne technische Namen dürfen abweichen, sichtbare UI und Dokumentation verwenden aber **ABook Player**.

### Deutsche UI-Texte

| Nicht verwenden | Verwenden |
|---|---|
| Library | Bibliothek |
| Player | Wiedergabe |
| Settings | Einstellungen |
| Now Playing | Aktuelle Wiedergabe |
| Select chapter | Kapitel auswählen |
| Sleep timer | Schlaf-Timer |
| Your listening | Hörstatistik |
| Bookmarks | Lesezeichen |

### Regel

Englische Texte in Prototypen sind Platzhalter. In Compose, Screenshots für Abnahme und finaler Dokumentation werden deutsche Texte verwendet.

---

## 7. Material-3-Tokens

### Warmes Bibliotheks-System

| Token | Wert | Nutzung |
|---|---|---|
| Background | `#F1EADB` | App-Hintergrund für Bibliothek, Details, Lesezeichen, Import |
| Surface | `#FAF6EC` | Karten und größere Flächen |
| Surface Variant | `#E8DFC8` | Filterleisten, sekundäre Karten |
| Text Primary | `#2C241A` | Haupttext |
| Text Secondary | `#6E6354` | Metadaten, Hilfstexte |
| Accent | `#B4451E` | Auswahl, Fortschritt, primäre Akzente |

### Dunkles Stage-System

| Token | Wert | Nutzung |
|---|---|---|
| Background | `#0A0A0C` | Player, Schlaf-Timer, Hörstatistik |
| Surface | `#13131A` | Bedienelemente und Karten |
| Surface Variant | `#1D1D26` | Sheets und sekundäre Panels |
| Text Primary | `#F2EFE7` | Haupttext |
| Text Secondary | `#9D9AA0` | Zeiten, Metadaten |
| Accent | `#F5B86E` | Fortschritt, aktive Steuerung, Fokus |

### Neutrales System

Einstellungen und systemnahe Dialoge verwenden Material-3-Defaults, aber mit denselben Typografie-, Abstand- und Touch-Zielregeln.

### Regel

Das violett/cyane Zwischen-Theme und hellblaue Now-Playing-Assets sind kein verbindliches Zielsystem. Wenn sie im Code vorhanden sind, müssen sie entweder bewusst auf die obigen Tokens migriert oder als experimenteller Zwischenstand markiert werden.

Dynamic Colors/Material You sind für die visuelle Abnahme deaktiviert. Die verbindlichen Library-, Stage- und Neutral-Tokens dürfen nicht durch Wallpaper-Farben überschrieben werden.

---

## 8. Bibliothek

Die Bibliothek darf Listen- und Grid-Ansicht unterstützen, aber beide Ansichten teilen dieselbe Informationsarchitektur:

- Suche oben, dauerhaft erreichbar.
- Filter: Alle, Neu, Gestartet, Fertig.
- Sortierung: Zuletzt gehört, Titel, Autor, Importdatum.
- Cover, Titel, Autor, Fortschritt und Restzeit sind die Kerninformationen.
- Autorengruppen sind erlaubt, wenn sie die Suche nicht verdecken.
- Horizontale Cover-Reihen dürfen auf kleinen Displays nicht die einzige Navigation sein.

### Liste vs. Grid

| Ansicht | Hauptzweck |
|---|---|
| Liste | Schnelles Vergleichen, Fortschritt, Restzeit, lange Titel. |
| Grid | Cover-orientiertes Stöbern und visuelles Wiedererkennen. |

---

## 9. Referenz-Hygiene

### Verbindlich

- Dieses Dokument.
- `FeatureList.md` für Funktionsumfang.
- `Architecture.md` für technische Schichten.
- `ImportFlow.md` und `ABOOK_FORMAT.md` für Import und Format.

### Nur Referenz

- JSX-Prototypen in `project/`.
- Screenshots aus alten Smart-Audiobook-Player-Varianten.
- `_Resources/assets/library.png` und `_Resources/assets/player.png`, solange sie kein eindeutiges Library/Player-Paar zeigen.

### Asset-Regel

Neue visuelle Referenzbilder müssen eindeutig benannt werden:

- `visual-library-warm-grid.png`
- `visual-library-warm-list.png`
- `visual-player-stage.png`
- `visual-sleep-stage.png`
- `visual-bookmarks-warm.png`

Keine Datei darf `library` heißen, wenn sie einen Player-Screen zeigt.

---

## 10. Akzeptanzkriterien

Eine UI-Umsetzung gilt visuell konsistent, wenn:

- jeder Screen genau einer visuellen Rolle aus Abschnitt 3 folgt,
- alle sichtbaren Texte deutsch sind,
- Hauptnavigation und Kontextnavigation nicht konkurrieren,
- Player-Primäraktionen klar größer und schneller erreichbar sind als Nebenaktionen,
- mindestens 48 dp Touch-Ziele eingehalten werden,
- dynamische Schriftgrößen keine Texte abschneiden,
- Cover-Artwork die Bedienung nicht überlagert,
- helle und dunkle Zustände ausreichend Kontrast besitzen,
- Prototyp-Assets nur als Referenz und nicht als finaler Code behandelt werden.

---

*Weitere Details zu Features: [FeatureList.md](FeatureList.md)*  
*Technische Struktur: [Architecture.md](Architecture.md)*  
*Ordnerstruktur: [FolderStructure.md](FolderStructure.md)*
