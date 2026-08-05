package de.f_soft_studio.abookplayer.domain.model

data class DayActivity(
    val dayLabel: String, // z. B. "Mo", "Di", ...
    val date: String,     // "yyyy-MM-dd"
    val durationSeconds: Long
)

/**
 * Domain-Modell für das Hörstatistik-Dashboard.
 */
data class ListeningStatistics(
    val todaySeconds: Long = 0L,
    val thisWeekSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val currentStreakDays: Int = 0,
    val dailyActivityLast7Days: List<DayActivity> = emptyList()
)
