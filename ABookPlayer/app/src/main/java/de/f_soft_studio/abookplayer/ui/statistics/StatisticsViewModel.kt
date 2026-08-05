package de.f_soft_studio.abookplayer.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.f_soft_studio.abookplayer.domain.model.ListeningStatistics
import de.f_soft_studio.abookplayer.domain.usecase.GetListeningStatisticsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel für das Hörstatistik-Dashboard.
 */
class StatisticsViewModel(
    getListeningStatisticsUseCase: GetListeningStatisticsUseCase
) : ViewModel() {

    val statistics: StateFlow<ListeningStatistics> = getListeningStatisticsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ListeningStatistics()
        )
}
