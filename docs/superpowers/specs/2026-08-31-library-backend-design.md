# Spec A – Library-Backend (Phase 20, Teil 1)

**Datum:** 2026-08-31
**Branch:** `feat/library-restructure`
**Vorlage:** `ABookPlayer/redesign.md` (Umsetzungsphasen 1 + 4)
**Nachfolge-Spec:** Spec B – Library-UI (Phasen 2 + 3), noch nicht geschrieben

## Ziel

Die persistente Daten- und Zustandsgrundlage der Bibliothek so weit bringen,
dass der spätere UI-Umbau (Spec B) nur noch Darstellung verdrahtet:

- „Neu importiert" sortiert nach echtem Importzeitpunkt statt nach Datenbank-ID.
- Der `LibraryViewModel`-Start löst keinen automatischen Scan und keine
  Dauer-Reparatur mehr aus.
- Scan und Cleanup haben einen expliziten, unterscheidbaren Zustand
  (läuft / erfolgreich / fehlerhaft), statt nur einmalige Snackbars.

## Nicht-Ziele

- Kein UI-Umbau (Top-Bar, Filter-Sheet, Add-Sheet, `library_management`-Screen,
  Zerlegung von `LibraryScreen.kt`) – das ist Spec B.
- Keine neuen Felder außer `addedAt`. `sourceType` und `availabilityState` aus
  `redesign.md` werden bewusst aufgeschoben (YAGNI), bis ein Feature sie braucht.
- Kein Architekturwechsel, keine neue Library.
- Favoritenpersistenz ist **bereits umgesetzt** (`isFavorite` in
  `AudiobookEntity`, DB v9, `AudiobookRepository.toggleFavorite`) und wird hier
  nur durch einen Regressionstest abgesichert.

## Ausgangslage im Code (Stand 2026-08-31)

- `AudiobookEntity` (DB-Version **9**) enthält `isFavorite`, `parentSeries`,
  `series`, `seriesOrder`, `customSpeed`, `equalizerPreset` – **kein** `addedAt`.
- `AbookDatabase` hat Migrationen bis `MIGRATION_8_9`, `fallbackToDestructiveMigration()` aktiv.
- `LibraryViewModel.kt:239`: `SortOrder.HINZUGEFUEGT_AM ->
  statusFiltered.sortedByDescending { it.id }`.
- `LibraryViewModel.kt:301-306`: `init { scanAudiobooks(); viewModelScope.launch {
  repository.recalculateZeroDurationBooks(context) } }`.
- `LibraryViewModel` hält `_detectedDuplicates` als loses `StateFlow`; `scanAudiobooks()`
  und `cleanupLibrary()` melden Ergebnisse nur über `_messageEvent`.

## Änderungen

### 1. Schema & Migration

**`domain/model/Audiobook.kt`**
- Neues Feld `val addedAt: Long = System.currentTimeMillis()`.

**`data/local/entity/AudiobookEntity.kt`**
- Neue Spalte `val addedAt: Long = 0L`.
- `toDomainModel()` und `Audiobook.toEntity()` um `addedAt` ergänzen.

**`data/local/db/AbookDatabase.kt`**
- `version = 10`.
- Neue Migration:

```kotlin
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE audiobooks ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "UPDATE audiobooks SET addedAt = " +
            "CASE WHEN lastPlayed > 0 THEN lastPlayed ELSE strftime('%s','now')*1000 END"
        )
    }
}
```

- `MIGRATION_9_10` in `addMigrations(...)` eintragen.

### 2. Domain & Repository

- Kein neuer Repository-Call nötig (Favoriten existieren bereits).
- **Import-Pfade prüfen:** Jede Stelle, die beim Import ein *neues* `Audiobook`
  konstruiert (`storage/AbookStorage.kt`, `storage/FolderScanner.kt` bzw. deren
  Aufrufer), muss `addedAt` gesetzt bekommen. Der Default
  `System.currentTimeMillis()` genügt, solange die Konstruktion benannte Argumente
  nutzt und `addedAt` nicht explizit auf `0` setzt.
- **Progress-Speicherung darf `addedAt` nicht verlieren:** `saveAudiobook(...)`
  wird für bestehende Bücher mit einem vorher gelesenen Objekt (`.copy(...)`)
  aufgerufen; `addedAt` bleibt dadurch erhalten. Im Plan mit einem Test absichern
  (Buch importieren → Fortschritt speichern → `addedAt` unverändert).

### 3. `LibraryViewModel`

- `SortOrder.HINZUGEFUEGT_AM`: `sortedByDescending { it.id }` →
  `sortedByDescending { it.addedAt }`.
- `init {}`: Aufrufe von `scanAudiobooks()` und `recalculateZeroDurationBooks(context)`
  **entfernen**. Bleibt kein Inhalt übrig, `init`-Block ganz löschen.
- `scanAudiobooks()` als öffentliche Methode bleibt unverändert nutzbar
  (manueller Auslöser über „Standardordner scannen").

### 4. Expliziter Scan-/Wartungszustand

Neuer Zustand im `LibraryViewModel`:

```kotlin
data class LibraryMaintenanceUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val lastScanMessage: String? = null,
    val duplicates: List<DuplicateMatch> = emptyList(),
    val cleanupResult: LibraryCleanupResult? = null
)
```

- Als `MutableStateFlow` privat + `asStateFlow()` öffentlich.
- `scanAudiobooks()`: `isScanning = true` vor `storage.scanAndImport()`,
  `isScanning = false` + `lastScanMessage` + `duplicates` danach (auch im
  Fehlerfall via `try/finally`).
- `cleanupLibrary()`: analog mit `isCleaning` und `cleanupResult`.
- `_detectedDuplicates` wird durch das `duplicates`-Feld dieses States ersetzt;
  `dismissDuplicate` / `deleteDuplicate` / `importFromUri` / `importFromFolderUri`
  aktualisieren das Feld im State statt des losen Flows.
- **Bestehende `messageEvent`-Snackbars bleiben** vorerst erhalten (doppelte
  Meldung ist akzeptabel; Spec B räumt die UI auf). Kein `LibraryScreen`-Umbau.
- `LibraryCleanupResult` / `DuplicateMatch` sind vorhandene Typen
  (`AudiobookRepository` bzw. `util/DuplicateDetector`) – nur importieren.

## Fehlerbehandlung

- Scan-/Cleanup-Ausnahmen werden im ViewModel gefangen: State-Flags via
  `try { ... } finally { isScanning = false }` zurücksetzen, Fehlermeldung in
  `lastScanMessage` (Scan) bzw. weiterhin `messageEvent` (Cleanup) schreiben.
- Migration: rein additiv (`ADD COLUMN` + `UPDATE`); kein Datenverlust-Risiko.
  `fallbackToDestructiveMigration()` bleibt als Sicherheitsnetz, wird durch die
  saubere Migration 9→10 aber nicht mehr getriggert.

## Tests

Datei `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`
(erweitern) und ggf. neue Test-Datei für die Migration:

1. **Sortierung `HINZUGEFUEGT_AM`** – Bücher mit absichtlich gegenläufigen
   `id`- und `addedAt`-Werten; Ergebnis ist absteigend nach `addedAt`.
2. **Kein Auto-Scan im `init`** – Fake-`AbookStorage`; nach ViewModel-Konstruktion
   wurde `scanAndImport()` **nicht** aufgerufen.
3. **`scanAudiobooks()` Zustandsverlauf** – `isScanning` geht true → false,
   `lastScanMessage` ist danach gesetzt.
4. **`addedAt` überlebt Progress-Save** – Buch mit festem `addedAt`;
   nach `saveAudiobook(book.copy(currentPosition = …))` ist `addedAt` unverändert
   (Fake-Repository).
5. **Favoritenpersistenz (Regression)** – `toggleFavorite` schlägt bis ins
   Fake-Repository durch und `favoriteBookIds` spiegelt es.
6. **Migration 9→10** – mit Room `MigrationTestHelper`: alte Zeile mit
   `lastPlayed = X` bekommt `addedAt = X`; Zeile mit `lastPlayed = 0` bekommt
   `addedAt > 0`. Falls keine Migrations-Test-Infrastruktur vorhanden ist:
   weglassen und im Plan als offenen Punkt vermerken.

## Verifikation (Definition of Done)

Aus `ABookPlayer/`:

- `./gradlew.bat test` – grün
- `./gradlew.bat assembleDebug` – grün (Schema-/Migrationsänderung)
- `PROJECT_HISTORY.md`: Phase-20-Checkliste um erledigte Punkte ergänzen
- Commit(s) mit klarer Message auf `feat/library-restructure`

## Betroffene Dateien

- `app/src/main/java/de/f_soft_studio/abookplayer/domain/model/Audiobook.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/data/local/entity/AudiobookEntity.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt`
- `app/src/main/java/de/f_soft_studio/abookplayer/storage/AbookStorage.kt` (nur falls Import `addedAt` explizit setzen muss)
- `app/src/main/java/de/f_soft_studio/abookplayer/storage/FolderScanner.kt` (dito)
- `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`
- ggf. neue Datei `app/src/androidTest/.../AbookDatabaseMigrationTest.kt` oder Unit-Äquivalent
- `PROJECT_HISTORY.md`
