package de.f_soft_studio.abookplayer.ui.library.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.f_soft_studio.abookplayer.ui.library.SeriesDisplayMode
import de.f_soft_studio.abookplayer.ui.library.SortOrder
import de.f_soft_studio.abookplayer.ui.library.StatusFilter

/**
 * FilterBottomSheet: Ermöglicht das Filtern und Sortieren der Bibliothek in einem übersichtlichen Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    statusFilter: StatusFilter,
    sortOrder: SortOrder,
    seriesDisplayMode: SeriesDisplayMode,
    onStatusFilterChanged: (StatusFilter) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit,
    onSeriesDisplayModeChanged: (SeriesDisplayMode) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sortierung",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(onClick = onResetFilters) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zurücksetzen")
                }
            }

            // 1. Status-Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == StatusFilter.ALLE,
                        onClick = { onStatusFilterChanged(StatusFilter.ALLE) },
                        label = { Text("Alle") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = statusFilter == StatusFilter.FAVORITEN,
                        onClick = { onStatusFilterChanged(StatusFilter.FAVORITEN) },
                        label = { Text("⭐ Favoriten") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = statusFilter == StatusFilter.ANGEFANGEN,
                        onClick = { onStatusFilterChanged(StatusFilter.ANGEFANGEN) },
                        label = { Text("Angefangen") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = statusFilter == StatusFilter.BEENDET,
                        onClick = { onStatusFilterChanged(StatusFilter.BEENDET) },
                        label = { Text("Beendet") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = statusFilter == StatusFilter.UNGESPIELT,
                        onClick = { onStatusFilterChanged(StatusFilter.UNGESPIELT) },
                        label = { Text("Ungespielt") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // 2. Sortierung
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sortierung",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortOrder == SortOrder.ZULETZT_GEHOERT,
                        onClick = { onSortOrderChanged(SortOrder.ZULETZT_GEHOERT) },
                        label = { Text("Zuletzt gehört") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.TITEL,
                        onClick = { onSortOrderChanged(SortOrder.TITEL) },
                        label = { Text("Titel") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.AUTOR,
                        onClick = { onSortOrderChanged(SortOrder.AUTOR) },
                        label = { Text("Autor") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.SERIEN,
                        onClick = { onSortOrderChanged(SortOrder.SERIEN) },
                        label = { Text("Serien") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.RESTLAUFZEIT,
                        onClick = { onSortOrderChanged(SortOrder.RESTLAUFZEIT) },
                        label = { Text("Restzeit") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.HINZUGEFUEGT_AM,
                        onClick = { onSortOrderChanged(SortOrder.HINZUGEFUEGT_AM) },
                        label = { Text("Neu importiert") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // 3. Serien-Darstellungsmodi (nur sichtbar, wenn Sortierung = Serien aktiv ist)
            if (sortOrder == SortOrder.SERIEN) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Serien-Darstellung",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = seriesDisplayMode == SeriesDisplayMode.STAPEL_KARTE,
                            onClick = { onSeriesDisplayModeChanged(SeriesDisplayMode.STAPEL_KARTE) },
                            label = { Text("Stapel 🎴") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = seriesDisplayMode == SeriesDisplayMode.REIHEN_KARUSSELL,
                            onClick = { onSeriesDisplayModeChanged(SeriesDisplayMode.REIHEN_KARUSSELL) },
                            label = { Text("Regal 📚") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = seriesDisplayMode == SeriesDisplayMode.ORDNER_LISTE,
                            onClick = { onSeriesDisplayModeChanged(SeriesDisplayMode.ORDNER_LISTE) },
                            label = { Text("Ordner 📁") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fertig")
            }
        }
    }
}
