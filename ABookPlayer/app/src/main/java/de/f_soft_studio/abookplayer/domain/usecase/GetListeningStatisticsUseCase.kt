package de.f_soft_studio.abookplayer.domain.usecase

import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.DayActivity
import de.f_soft_studio.abookplayer.domain.model.ListeningStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * UseCase zur Berechnung und Kombination der Hörstatistiken.
 */
class GetListeningStatisticsUseCase(
    private val repository: AudiobookRepository
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayLabelFormat = SimpleDateFormat("EE", Locale.GERMAN) // Mo, Di, ...

    operator fun invoke(): Flow<ListeningStatistics> {
        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)

        // Date 7 days ago
        val cal7DaysAgo = Calendar.getInstance()
        cal7DaysAgo.add(Calendar.DAY_OF_YEAR, -6)
        val startDateStr = dateFormat.format(cal7DaysAgo.time)

        val totalFlow = repository.getTotalListenTimeSeconds()
        val todayFlow = repository.getListenTimeForDate(todayStr)
        val dailySummariesFlow = repository.getDailySummaries(startDateStr)
        val activeDatesFlow = repository.getActiveListeningDates()

        return combine(
            totalFlow,
            todayFlow,
            dailySummariesFlow,
            activeDatesFlow
        ) { totalSec, todaySec, summaries, activeDates ->

            // Map summaries by date string
            val summaryMap = summaries.associate { it.date to it.totalSeconds }

            // Build last 7 days list
            val last7Days = mutableListOf<DayActivity>()
            var weekTotal = 0L
            val calRunner = Calendar.getInstance()
            calRunner.add(Calendar.DAY_OF_YEAR, -6)

            for (i in 0 until 7) {
                val dateStr = dateFormat.format(calRunner.time)
                val dayLabel = dayLabelFormat.format(calRunner.time)
                val duration = summaryMap[dateStr] ?: 0L

                weekTotal += duration
                last7Days.add(
                    DayActivity(
                        dayLabel = dayLabel,
                        date = dateStr,
                        durationSeconds = duration
                    )
                )
                calRunner.add(Calendar.DAY_OF_YEAR, 1)
            }

            val streak = calculateStreak(activeDates)

            ListeningStatistics(
                todaySeconds = todaySec,
                thisWeekSeconds = weekTotal,
                totalSeconds = totalSec,
                currentStreakDays = streak,
                dailyActivityLast7Days = last7Days
            )
        }
    }

    /**
     * Berechnet die Anzahl aufeinanderfolgender Tage mit Höraktivität.
     */
    private fun calculateStreak(activeDates: List<String>): Int {
        if (activeDates.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calendar.time)

        val dateSet = activeDates.toSet()

        var currentCal = Calendar.getInstance()
        var streak = 0

        // Streak count starts today or yesterday if today has no activity yet
        if (dateSet.contains(todayStr)) {
            // Count backwards starting today
        } else if (dateSet.contains(yesterdayStr)) {
            currentCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            return 0
        }

        while (true) {
            val checkDate = dateFormat.format(currentCal.time)
            if (dateSet.contains(checkDate)) {
                streak++
                currentCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }
}
