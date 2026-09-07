# 9. Audio-Wiedergabe

Kern: `player/service/AbookPlaybackService` (`MediaLibraryService`, Foreground) + `player/controller/PlaybackController` (kapselt ExoPlayer, Single-Instance via `getInstance`).

- Play/Pause, Seek, `-10s` / `+10s` / `+30s`, vorheriges/nächstes Kapitel.
- Geschwindigkeit 0,75× bis 2,0× (pro Hörbuch speicherbar über `customSpeed`).
- Lautheits-Boost und Equalizer-Presets über `LoudnessController`; „Stille überspringen" (Skip Silence).
- Fortschritt wird gedrosselt sowie bei Pause, Kapitelwechsel, Stop und Service-Ende in Room geschrieben.
- Wiederaufnahme an Kapitel und Position ohne unerwünschten Neustart am Anfang; „Smart Rewind" spult nach längerer Pause etwas zurück.
- MediaSession- und Notification-Aktionen für Headset, Bluetooth und Sperrbildschirm; Audio-Focus und Becoming-Noisy werden behandelt.

## 9.1 Medienquellen und Fehlerfälle

- Kapitel-`MediaItem`s werden aus `chapter.audioPath` bzw. aus einer Einzelquelle gebaut.
- Nur reguläre Dateien werden akzeptiert (`util/PlayableMedia.isPlayableFile` = `File.isFile`). Verzeichnispfade werden nie an ExoPlayer übergeben – sonst bricht die Wiedergabe mit `EISDIR` ab und wirkt „tot".
- Findet der Controller keine abspielbare Datei, ruft er **kein** `play()`, sondern emittiert `PlaybackController.playbackError` (`SharedFlow<String>`); `AppRoot` zeigt die Meldung als Toast.
- Titel/Untertitel/Interpret für MediaSession/Notification/Android Auto liefert `util/AudiobookMetadataText` (entfernt Datei-Endungen, garantiert nicht-leeren Titel).

## 9.2 Android Auto

- Browsing über `MediaLibraryService`; eigener Auto-Modus-Screen (`ui/car/`).
- `-10s`/`+10s`-Custom-Actions nutzen **app-lokale** Vektor-Icons (`ic_skip_back_10`, `ic_skip_forward_10`); `android.R.drawable.*` ist in Android Auto nicht auflösbar.

## 9.3 Fortschrittsspeicherung

Positionen werden nicht bei jedem Player-Tick geschrieben, sondern gedrosselt und bei Zustandswechseln. Positionen nahe dem Kapitelende können beim Wiederaufnehmen normalisiert werden; abgeschlossene Hörbücher erhalten einen eindeutigen Status. Optionaler WebDAV-Abgleich über `SyncProgressUseCase`.

## 9.4 Sleep Timer (`SleepTimerController`)

- Vordefinierte Zeiten (15 / 30 / 45 / 60 min) und „Kapitelende".
- Läuft unabhängig vom aktuellen Screen; Restzeit im Player sichtbar; Abbrechen und Verlängern möglich.
- Shake-to-Extend (`util/ShakeDetector`) verlängert per Schütteln; „Smart Sleep Bookmark" setzt beim Einschlafen automatisch ein Lesezeichen.
- Nach Ablauf pausiert der Player (optional mit Ausblenden) und speichert den Fortschritt.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](08_spezifikation-des-abook-formats.md)
 · [Nächstes Kapitel →](10_benutzeroberflaeche-und-navigation.md)
