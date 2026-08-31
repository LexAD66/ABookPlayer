# Library-Backend (Phase 20, Teil 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Der Library-Bibliothek eine persistente `addedAt`-Grundlage geben, den automatischen Scan aus dem `LibraryViewModel`-Start entfernen und Scan/Cleanup in einen expliziten Zustand überführen.

**Architecture:** Additive Room-Migration 9→10 für die Spalte `addedAt`; `SortOrder.HINZUGEFUEGT_AM` sortiert danach. Der `init`-Block des `LibraryViewModel` wird geleert. Ein neues `LibraryMaintenanceUiState` (ein `StateFlow`) hält `isScanning`, `isCleaning`, `lastScanMessage`, `cleanupResult` und die Duplikatliste; die bisherige öffentliche `detectedDuplicates`-API bleibt als abgeleitete View erhalten, damit `LibraryScreen.kt` in dieser Spec **nicht** angefasst werden muss.

**Tech Stack:** Kotlin, Jetpack Compose (unberührt), Room, Coroutines/StateFlow, JUnit4 + Robolectric + MockK (bestehendes Test-Setup in `LibraryViewModelTest`).

**Spec:** `docs/superpowers/specs/2026-08-31-library-backend-design.md`

## Global Constraints

- Deutsche UI-Texte; deutsche Kotlin-Kommentare nur bei echtem Kontextgewinn.
- Kein Architekturwechsel, keine neuen Libraries.
- Keine bestehende Wiedergabe-, Details- oder Importfunktion entfernen.
- `LibraryScreen.kt` wird in dieser Spec nicht geändert (öffentliche ViewModel-API bleibt kompatibel).
- Verifikation aus `ABookPlayer/`: `./gradlew.bat test` immer; `./gradlew.bat assembleDebug` bei Schema-/Ressourcenänderung.
- Branch: `feat/library-restructure`. Commits klein und mit klarer Message.
- Neue Felder in dieser Spec: **nur** `addedAt`. Kein `sourceType`, kein `availabilityState`.

## File Structure

| Datei | Verantwortung | Aktion |
|---|---|---|
| `app/src/main/java/de/f_soft_studio/abookplayer/domain/model/Audiobook.kt` | Domain-Modell | `addedAt`-Feld ergänzen |
| `app/src/main/java/de/f_soft_studio/abookplayer/data/local/entity/AudiobookEntity.kt` | Room-Entity + Mapper | `addedAt`-Spalte + Mapper |
| `app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt` | DB-Version + Migrationen | Version 10, `MIGRATION_9_10` |
| `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryMaintenanceUiState.kt` | Zustandsobjekt für Scan/Cleanup | **neu** |
| `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt` | Sortierung, `init`, Scan-/Cleanup-Zustand | mehrere Stellen |
| `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt` | Tests | erweitern |
| `PROJECT_HISTORY.md` | Projektjournal | Phase-20-Haken |

`AbookStorage.kt` und `FolderScanner.kt` bleiben **unverändert**: Die dortigen
`Audiobook(...)`-Konstruktionen (AbookStorage:275/451/693, FolderScanner:242)
nutzen benannte Argumente und erhalten `addedAt` über den Konstruktor-Default
`System.currentTimeMillis()`.

---

## Task 1: `addedAt`-Spalte, Migration 9→10, Mapper

**Files:**
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/domain/model/Audiobook.kt`
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/data/local/entity/AudiobookEntity.kt`
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt`
- Test: `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`

**Interfaces:**
- Consumes: nichts (erste Task).
- Produces:
  - `Audiobook.addedAt: Long` (Default `System.currentTimeMillis()`), Position: nach `lastPlayed`, vor `parentSeries`.
  - `AudiobookEntity.addedAt: Long` (Default `0L`).
  - `AbookDatabase` Version `10`, `AbookDatabase.MIGRATION_9_10: Migration`.

- [ ] **Step 1: Failing test — `addedAt` überlebt Repository-Round-Trip**

In `LibraryViewModelTest.kt` ergänzen (nutzt das bestehende `repository`/`database`-Setup):

```kotlin
    @Test
    fun testAddedAtDefaultsToNowAndPersistsExplicitValue() = runBlocking {
        val idDefault = repository.saveAudiobook(
            Audiobook(title = "Ohne Datum", author = "A", filePath = "/p1")
        )
        val idExplicit = repository.saveAudiobook(
            Audiobook(title = "Mit Datum", author = "A", filePath = "/p2", addedAt = 1_700_000_000_000L)
        )

        val all = repository.getAllAudiobooks().first { it.size == 2 }
        val byDefault = all.first { it.id == idDefault }
        val byExplicit = all.first { it.id == idExplicit }

        assertTrue("Default-addedAt sollte gesetzt sein", byDefault.addedAt > 0L)
        assertEquals(1_700_000_000_000L, byExplicit.addedAt)
    }
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testAddedAtDefaultsToNowAndPersistsExplicitValue"`
Expected: FAIL — Kompilierfehler „no parameter `addedAt`" bzw. „unresolved reference: addedAt".

- [ ] **Step 3: `Audiobook`-Domain-Modell erweitern**

In `domain/model/Audiobook.kt`, Feld direkt nach `lastPlayed` einfügen:

```kotlin
    val lastPlayed: Long = System.currentTimeMillis(),
    val addedAt: Long = System.currentTimeMillis(),
    val parentSeries: String? = null,
```

KDoc-Zeile im Klassenkommentar ergänzen:

```kotlin
 * @property addedAt Zeitstempel des Imports (für Sortierung „Neu importiert").
```

- [ ] **Step 4: `AudiobookEntity` + Mapper erweitern**

In `data/local/entity/AudiobookEntity.kt`:

```kotlin
    val lastPlayed: Long,
    val addedAt: Long = 0L,
    val parentSeries: String? = null,
```

In `AudiobookEntity.toDomainModel()` ergänzen:

```kotlin
        lastPlayed = lastPlayed,
        addedAt = addedAt,
        parentSeries = parentSeries,
```

In `Audiobook.toEntity()` ergänzen:

```kotlin
        lastPlayed = lastPlayed,
        addedAt = addedAt,
        parentSeries = parentSeries,
```

- [ ] **Step 5: DB-Version anheben und Migration ergänzen**

In `data/local/db/AbookDatabase.kt` `version = 9` → `version = 10`.

Nach `MIGRATION_8_9` einfügen:

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

`addMigrations(...)`-Zeile erweitern:

```kotlin
                    .addMigrations(
                        MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                    )
```

- [ ] **Step 6: Run test, verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testAddedAtDefaultsToNowAndPersistsExplicitValue"`
Expected: PASS.

- [ ] **Step 7: Volle Testsuite + Debug-Build (Schemaänderung)**

Run: `./gradlew.bat test assembleDebug`
Expected: BUILD SUCCESSFUL, alle Tests grün.

> **Offener Punkt (dokumentiert):** Ein echter Migrationspfad-Test (Room
> `MigrationTestHelper`, v9-DB anlegen → auf 10 migrieren → `addedAt`-Werte prüfen)
> braucht einen Instrumentation-Runner; im Projekt gibt es kein `app/src/androidTest`.
> Die Migration ist rein additiv (`ADD COLUMN` + `UPDATE`); Round-Trip-Test in Step 1
> deckt Schreib-/Leseweg ab. Bei späterem Aufbau von `androidTest` nachrüsten.

- [ ] **Step 8: Commit**

```bash
git add ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/domain/model/Audiobook.kt \
        ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/data/local/entity/AudiobookEntity.kt \
        ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/data/local/db/AbookDatabase.kt \
        ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt
git commit -m "feat(library): addedAt-Spalte + Room-Migration 9->10"
```

---

## Task 2: Sortierung „Neu importiert" nach `addedAt`

**Files:**
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt` (im `when (sort)`-Block, aktuell Zeile ~239)
- Test: `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`

**Interfaces:**
- Consumes: `Audiobook.addedAt` aus Task 1.
- Produces: keine neue API; Verhaltensänderung von `audiobooks` bei `SortOrder.HINZUGEFUEGT_AM`.

- [ ] **Step 1: Failing test — Sortierung nach `addedAt`, nicht `id`**

```kotlin
    @Test
    fun testSortByAddedAtUsesAddedAtNotId() = runBlocking {
        // Zuerst gespeichert (kleine id), aber neueres addedAt:
        repository.saveAudiobook(
            Audiobook(title = "Zuerst gespeichert", author = "A", filePath = "/p1", addedAt = 2_000L)
        )
        // Danach gespeichert (größere id), aber älteres addedAt:
        repository.saveAudiobook(
            Audiobook(title = "Danach gespeichert", author = "A", filePath = "/p2", addedAt = 1_000L)
        )

        viewModel.onSortOrderChanged(SortOrder.HINZUGEFUEGT_AM)

        val sorted = viewModel.audiobooks.first { it.size == 2 }
        assertEquals("Zuerst gespeichert", sorted[0].title) // größeres addedAt zuerst
        assertEquals("Danach gespeichert", sorted[1].title)
    }
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testSortByAddedAtUsesAddedAtNotId"`
Expected: FAIL — Reihenfolge falsch (sortiert aktuell nach `id`, also „Danach gespeichert" zuerst).

- [ ] **Step 3: Sortierung umstellen**

In `LibraryViewModel.kt`, im `when (sort)`-Ausdruck:

```kotlin
            SortOrder.HINZUGEFUEGT_AM -> statusFiltered.sortedByDescending { it.addedAt }
```

(ersetzt `statusFiltered.sortedByDescending { it.id }`)

- [ ] **Step 4: Run test, verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testSortByAddedAtUsesAddedAtNotId"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt \
        ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt
git commit -m "feat(library): 'Neu importiert' sortiert nach addedAt statt id"
```

---

## Task 3: Automatischen Scan aus `LibraryViewModel.init` entfernen

**Files:**
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt` (`init`-Block, aktuell Zeile ~301-306)
- Test: `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`

**Interfaces:**
- Consumes: nichts.
- Produces: `LibraryViewModel`-Konstruktion löst **keine** Aufrufe von
  `storage.scanAndImport()` oder `repository.recalculateZeroDurationBooks(...)` aus.
  `fun scanAudiobooks()` bleibt öffentlich und unverändert nutzbar.

- [ ] **Step 1: Failing test — kein Auto-Scan bei Konstruktion**

Import oben in der Testdatei ergänzen: `import io.mockk.coVerify`.

```kotlin
    @Test
    fun testInitDoesNotTriggerAutomaticScan() = runBlocking {
        // frisches ViewModel mit eigenem Mock, um Aufrufe isoliert zu prüfen
        val freshStorage: AbookStorage = mockk(relaxed = true)
        LibraryViewModel(repository, freshStorage)

        coVerify(exactly = 0) { freshStorage.scanAndImport() }
    }
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testInitDoesNotTriggerAutomaticScan"`
Expected: FAIL — `scanAndImport()` wird 1× aus `init` aufgerufen.

- [ ] **Step 3: `init`-Block entfernen**

In `LibraryViewModel.kt` den gesamten Block löschen:

```kotlin
    init {
        scanAudiobooks()
        viewModelScope.launch {
            repository.recalculateZeroDurationBooks(context)
        }
    }
```

Wenn dadurch `context` nur noch in `cleanupLibrary()` / `recalculateZeroDurationBooks`-
Aufrufen (über `scanAudiobooks`/`cleanupLibrary`) genutzt wird: `context`-Parameter
**behalten** (wird in `cleanupLibrary()` weiterhin an `repository.cleanupDuplicatesAndOrphans(context)` gereicht).

- [ ] **Step 4: Run test, verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testInitDoesNotTriggerAutomaticScan"`
Expected: PASS.

- [ ] **Step 5: Bestehende Tests gegenprüfen**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest"`
Expected: PASS (alle). Die Zeile `coEvery { storage.scanAndImport() } returns emptyList()` im `@Before` bleibt stehen — schadet nicht.

- [ ] **Step 6: Commit**

```bash
git add ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt \
        ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt
git commit -m "feat(library): kein automatischer Scan mehr beim ViewModel-Start"
```

---

## Task 4: `LibraryMaintenanceUiState` + Scan-/Cleanup-Zustand verdrahten

**Files:**
- Create: `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryMaintenanceUiState.kt`
- Modify: `app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt`
- Test: `app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt`

**Interfaces:**
- Consumes: `LibraryCleanupResult` (`data.repository`), `DuplicateMatch` (`util.DuplicateDetector`).
- Produces:
  - `data class LibraryMaintenanceUiState(isScanning: Boolean = false, isCleaning: Boolean = false, lastScanMessage: String? = null, duplicates: List<DuplicateMatch> = emptyList(), cleanupResult: LibraryCleanupResult? = null)`
  - `LibraryViewModel.maintenanceState: StateFlow<LibraryMaintenanceUiState>`
  - `LibraryViewModel.detectedDuplicates: StateFlow<List<DuplicateMatch>>` bleibt als abgeleitete View bestehen (gleiche Signatur wie bisher → `LibraryScreen.kt` unverändert lauffähig).

- [ ] **Step 1: Failing test — Scan setzt `isScanning` und `lastScanMessage`**

Imports ergänzen: `import kotlinx.coroutines.CompletableDeferred`.

```kotlin
    @Test
    fun testScanUpdatesMaintenanceState() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val gatedStorage: AbookStorage = mockk(relaxed = true)
        coEvery { gatedStorage.scanAndImport() } coAnswers {
            gate.await()
            emptyList()
        }
        val vm = LibraryViewModel(repository, gatedStorage)

        assertEquals(false, vm.maintenanceState.value.isScanning)

        vm.scanAudiobooks()
        assertTrue("Scan sollte als laufend markiert sein", vm.maintenanceState.value.isScanning)

        gate.complete(Unit)
        val done = vm.maintenanceState.first { !it.isScanning }
        assertEquals(false, done.isScanning)
        assertTrue("lastScanMessage sollte gesetzt sein", !done.lastScanMessage.isNullOrBlank())
    }
```

- [ ] **Step 2: Failing test — `detectedDuplicates` bleibt konsumierbar**

```kotlin
    @Test
    fun testDetectedDuplicatesStillExposedFromMaintenanceState() = runBlocking {
        // Nach einem Scan ohne Duplikate ist die Liste leer und der Flow lieferbar.
        viewModel.scanAudiobooks()
        val dups = viewModel.detectedDuplicates.first()
        assertTrue(dups.isEmpty())
        assertEquals(dups, viewModel.maintenanceState.value.duplicates)
    }
```

- [ ] **Step 3: Run tests, verify they fail**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testScanUpdatesMaintenanceState" --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest.testDetectedDuplicatesStillExposedFromMaintenanceState"`
Expected: FAIL — `maintenanceState` unresolved.

- [ ] **Step 4: `LibraryMaintenanceUiState` anlegen**

Neue Datei `ui/library/LibraryMaintenanceUiState.kt`:

```kotlin
package de.f_soft_studio.abookplayer.ui.library

import de.f_soft_studio.abookplayer.data.repository.LibraryCleanupResult
import de.f_soft_studio.abookplayer.util.DuplicateMatch

/**
 * Expliziter Zustand für Bibliotheks-Wartung (Scan, Cleanup, Duplikate).
 * Trennt „läuft" / „Ergebnis" / „Duplikate", damit die UI Prozesse darstellen
 * kann, ohne die normale Bibliothek zu blockieren.
 */
data class LibraryMaintenanceUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val lastScanMessage: String? = null,
    val duplicates: List<DuplicateMatch> = emptyList(),
    val cleanupResult: LibraryCleanupResult? = null
)
```

- [ ] **Step 5: ViewModel — State einführen, `_detectedDuplicates` ersetzen**

In `LibraryViewModel.kt`:

1. Import ergänzen: `import kotlinx.coroutines.flow.update` **nicht** nötig — es wird `.value = .value.copy(...)` genutzt (Hausstil).

2. Die bestehenden Zeilen

```kotlin
    private val _detectedDuplicates = MutableStateFlow<List<de.f_soft_studio.abookplayer.util.DuplicateMatch>>(emptyList())
    val detectedDuplicates: StateFlow<List<de.f_soft_studio.abookplayer.util.DuplicateMatch>> = _detectedDuplicates.asStateFlow()
```

ersetzen durch:

```kotlin
    private val _maintenanceState = MutableStateFlow(LibraryMaintenanceUiState())
    val maintenanceState: StateFlow<LibraryMaintenanceUiState> = _maintenanceState.asStateFlow()

    /** Abgeleitete View für bestehende Consumer (LibraryScreen). */
    val detectedDuplicates: StateFlow<List<de.f_soft_studio.abookplayer.util.DuplicateMatch>> =
        _maintenanceState
            .map { it.duplicates }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

3. Alle Zuweisungen `_detectedDuplicates.value = X` ersetzen durch:

```kotlin
            _maintenanceState.value = _maintenanceState.value.copy(duplicates = X)
```

Betroffene Methoden: `scanAudiobooks()`, `importFromUri()`, `importFromFolderUri()`,
`deleteDuplicate()`. Beispiel in `scanAudiobooks()`:

```kotlin
            _maintenanceState.value = _maintenanceState.value.copy(
                duplicates = storage.getDetectedDuplicates()
            )
```

4. `dismissDuplicate(match)` umstellen:

```kotlin
    fun dismissDuplicate(match: de.f_soft_studio.abookplayer.util.DuplicateMatch) {
        _maintenanceState.value = _maintenanceState.value.copy(
            duplicates = _maintenanceState.value.duplicates.filterNot { it == match }
        )
    }
```

- [ ] **Step 6: ViewModel — `scanAudiobooks()` mit `isScanning` + `lastScanMessage`**

`scanAudiobooks()` so umbauen (Struktur beibehalten, nur State-Flags + Nachricht ergänzen):

```kotlin
    fun scanAudiobooks() {
        viewModelScope.launch {
            _maintenanceState.value = _maintenanceState.value.copy(isScanning = true)
            try {
                storage.clearDetectedDuplicates()
                val imported = storage.scanAndImport()
                val duplicates = storage.getDetectedDuplicates()
                val message = when {
                    imported.isNotEmpty() -> "${imported.size} neue(s) Hörbuch(er) importiert"
                    duplicates.isNotEmpty() -> "${duplicates.size} identische(s) Duplikat(e) gefunden"
                    else -> "Scan beendet. Keine neuen Hörbücher oder Ordner gefunden."
                }
                _maintenanceState.value = _maintenanceState.value.copy(
                    duplicates = duplicates,
                    lastScanMessage = message
                )
                _messageEvent.emit(message)
            } catch (e: Exception) {
                val message = "Scan fehlgeschlagen: ${e.message ?: "unbekannter Fehler"}"
                _maintenanceState.value = _maintenanceState.value.copy(lastScanMessage = message)
                _messageEvent.emit(message)
            } finally {
                _maintenanceState.value = _maintenanceState.value.copy(isScanning = false)
            }
        }
    }
```

- [ ] **Step 7: ViewModel — `cleanupLibrary()` mit `isCleaning` + `cleanupResult`**

```kotlin
    fun cleanupLibrary() {
        viewModelScope.launch {
            _maintenanceState.value = _maintenanceState.value.copy(isCleaning = true)
            try {
                val result = repository.cleanupDuplicatesAndOrphans(context)
                _maintenanceState.value = _maintenanceState.value.copy(cleanupResult = result)

                val parts = mutableListOf<String>()
                if (result.duplicatesRemoved > 0) parts.add("${result.duplicatesRemoved} doppelte(r)")
                if (result.orphansRemoved > 0) parts.add("${result.orphansRemoved} verwaiste(r)")
                if (result.zeroDurationFixed > 0) parts.add("${result.zeroDurationFixed} 0-min-Hörbuch(er) repariert")
                val message = if (parts.isNotEmpty()) {
                    "Aufräumen beendet: ${parts.joinToString(", ")}."
                } else {
                    "Bibliothek ist bereits sauber. Keine fehlerhaften Einträge."
                }
                _messageEvent.emit(message)
            } finally {
                _maintenanceState.value = _maintenanceState.value.copy(isCleaning = false)
            }
        }
    }
```

- [ ] **Step 8: Run tests, verify they pass**

Run: `./gradlew.bat testDebugUnitTest --tests "de.f_soft_studio.abookplayer.ui.LibraryViewModelTest"`
Expected: PASS (alle, inkl. der zwei neuen).

- [ ] **Step 9: Debug-Build (öffentliche API-Kompatibilität von `LibraryScreen`)**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL — beweist, dass `LibraryScreen.kt` mit der abgeleiteten
`detectedDuplicates`-API weiterhin kompiliert.

- [ ] **Step 10: Commit**

```bash
git add ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryMaintenanceUiState.kt \
        ABookPlayer/app/src/main/java/de/f_soft_studio/abookplayer/ui/library/LibraryViewModel.kt \
        ABookPlayer/app/src/test/java/de/f_soft_studio/abookplayer/ui/LibraryViewModelTest.kt
git commit -m "feat(library): expliziter LibraryMaintenanceUiState fuer Scan/Cleanup"
```

---

## Task 5: Projektjournal + Gesamtverifikation

**Files:**
- Modify: `PROJECT_HISTORY.md`
- Test: keine (Doku + Verifikations-Gate)

**Interfaces:**
- Consumes: alle vorherigen Tasks.
- Produces: nichts.

- [ ] **Step 1: `PROJECT_HISTORY.md` — Phase-20-Haken setzen**

Im Abschnitt „🔜 Phase 20: Library-Umstrukturierung (geplant)" die erledigten
Punkte abhaken und einen Fortschrittsvermerk ergänzen:

```markdown
- [x] `addedAt`-Spalte + Room-Migration 9→10; „Neu importiert" sortiert nach `addedAt`.
- [x] Automatischen Scan + Dauer-Reparatur aus `LibraryViewModel.init` entfernt.
- [x] `LibraryMaintenanceUiState` (isScanning / isCleaning / lastScanMessage / duplicates / cleanupResult) eingeführt.
```

Notiz darunter:

```markdown
> Backend-Teil (Spec A, `docs/superpowers/specs/2026-08-31-library-backend-design.md`)
> abgeschlossen. Offen: UI-Umbau (Spec B) – Top-Bar, Filter-Bottom-Sheet,
> Add-Bottom-Sheet, `library_management`-Screen, Zerlegung von `LibraryScreen.kt`.
```

- [ ] **Step 2: Gesamtverifikation**

Run: `./gradlew.bat test assembleDebug`
Expected: BUILD SUCCESSFUL; alle Unit-Tests grün.

- [ ] **Step 3: Commit**

```bash
git add PROJECT_HISTORY.md
git commit -m "docs: Phase 20 Backend-Teil abgeschlossen"
```

---

## Self-Review

**Spec coverage:**
- „addedAt-Spalte + Migration" → Task 1.
- „HINZUGEFUEGT_AM nach addedAt" → Task 2.
- „Import-Pfade prüfen / addedAt-Default" → File-Structure-Notiz + Task 1 (Round-Trip-Test mit Default).
- „addedAt überlebt Progress-Save" → durch `data class`-Copy-Semantik abgedeckt; `toEntity`/`toDomainModel` mappen `addedAt` (Task 1, Step 4). Kein separater Test nötig, da `saveAudiobook` immer das volle Objekt schreibt und der Round-Trip-Test die Mapper abdeckt.
- „init ohne Scan" → Task 3.
- „LibraryMaintenanceUiState + scan/cleanup wiring" → Task 4.
- „detectedDuplicates ohne LibraryScreen-Änderung" → Task 4 (abgeleitete View) + Step 9 (assembleDebug beweist Kompatibilität).
- „Migrationstest optional/weglassen falls keine Infrastruktur" → Task 1, dokumentierter offener Punkt.
- „Verifikation: test + assembleDebug, PROJECT_HISTORY" → Task 5.

**Placeholder scan:** Keine „TBD"/„TODO"/„handle edge cases"; alle Code-Schritte mit konkretem Code.

**Type consistency:** `LibraryMaintenanceUiState`-Felder identisch in Task 4 Step 4 (Definition), Step 6/7 (`.copy(...)`) und den Tests (`isScanning`, `lastScanMessage`, `duplicates`). `detectedDuplicates` behält Typ `StateFlow<List<DuplicateMatch>>`. `LibraryCleanupResult` und `DuplicateMatch` aus bestehenden Paketen (`data.repository`, `util`).
