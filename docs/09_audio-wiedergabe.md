# 9. Audio-Wiedergabe

- MediaSessionService als Foreground Service.

- ExoPlayer wird über einen PlayerController gekapselt.

- Play/Pause, Seek, ±10 Sekunden, vorheriges/nächstes Kapitel.

- Geschwindigkeit 0,75× bis 2,0×.

- Fortschritt regelmäßig und bei Pause, Kapitelwechsel, Stop sowie Service-Ende speichern.

- Wiederaufnahme an Kapitel und Position, ohne unerwünschten Neustart am Anfang.

- MediaSession- und Notification-Aktionen für Headset, Bluetooth und Sperrbildschirm.

- Audio-Focus, Becoming-Noisy und Unterbrechungen korrekt behandeln.

## 9.1 Fortschrittsspeicherung

Positionen werden nicht bei jedem Player-Tick in Room geschrieben. Empfohlen sind gedrosselte Intervalle sowie sofortige Speicherung bei wichtigen Zustandswechseln. Eine Position nahe dem Kapitelende kann beim Wiederaufnehmen auf das nächste Kapitel normalisiert werden; abgeschlossene Hörbücher erhalten einen eindeutigen Status.

## 9.2 Sleep Timer

- Vordefinierte Zeiten und optional „Kapitelende“.

- Timer läuft unabhängig vom aktuellen Screen.

- Nach Ablauf pausiert der Player und speichert den Fortschritt.

- Restzeit ist im Player sichtbar; Abbrechen und Verlängern sind möglich.

---

[← Inhaltsverzeichnis](README.md)
 · [← Vorheriges Kapitel](08_spezifikation-des-abook-formats.md)
 · [Nächstes Kapitel →](10_benutzeroberflaeche-und-navigation.md)
