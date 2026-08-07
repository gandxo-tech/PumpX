package com.example.presentation.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.data.local.PushupSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class TimeFilter {
    DAYS_7, DAYS_30, MONTHS_3, ALL
}

data class DailyChartPoint(
    val dayLabel: String,
    val dateMs: Long,
    val pushupsCount: Int,
    val bonusMinutes: Int
)

data class StatsUiState(
    val filter: TimeFilter = TimeFilter.DAYS_7,
    val totalPushups: Int = 0,
    val totalBonusMinutes: Int = 0,
    val totalSessions: Int = 0,
    val activeDaysCount: Int = 0,
    val validationRatePercent: Int = 0,
    val chartPoints: List<DailyChartPoint> = emptyList(),
    val dynamicHook: String = "",
    val selectedPoint: DailyChartPoint? = null
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PumpXApplication
    private val pushupRepository = app.pushupRepository

    private val _filter = MutableStateFlow(TimeFilter.DAYS_7)
    val filter: StateFlow<TimeFilter> = _filter.asStateFlow()

    private val _selectedPoint = MutableStateFlow<DailyChartPoint?>(null)
    val selectedPoint: StateFlow<DailyChartPoint?> = _selectedPoint.asStateFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        pushupRepository.allSessions,
        _filter,
        _selectedPoint
    ) { sessions, timeFilter, selected ->
        val rangeStartMs = calculateStartTimeMs(timeFilter)
        val filteredSessions = if (timeFilter == TimeFilter.ALL) {
            sessions
        } else {
            sessions.filter { it.timestamp >= rangeStartMs }
        }

        val totalPushups = filteredSessions.sumOf { it.pushupsCount }
        val totalBonus = filteredSessions.sumOf { it.unlockedBonusMinutes }
        val totalSessionsCount = filteredSessions.size

        val activeDays = filteredSessions
            .map { getDayStartEpoch(it.timestamp) }
            .distinct()
            .size

        val validationRate = if (totalSessionsCount > 0) {
            val valid = filteredSessions.count { it.pushupsCount >= it.targetPushups }
            ((valid.toDouble() / totalSessionsCount) * 100).toInt()
        } else 0

        val chartPoints = generateChartPoints(filteredSessions, timeFilter)

        val hook = when {
            totalPushups > 100 -> "« Nouveau record. 🔥 »"
            totalPushups > 30 -> "« Cette semaine était solide. »"
            totalPushups > 0 -> "« Le compteur commence à te connaître. 👀 »"
            else -> "« Le bouton “encore 5 minutes” est inquiet. 😂 »"
        }

        StatsUiState(
            filter = timeFilter,
            totalPushups = totalPushups,
            totalBonusMinutes = totalBonus,
            totalSessions = totalSessionsCount,
            activeDaysCount = activeDays,
            validationRatePercent = validationRate,
            chartPoints = chartPoints,
            dynamicHook = hook,
            selectedPoint = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun setFilter(filter: TimeFilter) {
        _filter.value = filter
        _selectedPoint.value = null
    }

    fun selectPoint(point: DailyChartPoint?) {
        _selectedPoint.value = point
    }

    private fun calculateStartTimeMs(filter: TimeFilter): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (filter) {
            TimeFilter.DAYS_7 -> calendar.add(Calendar.DAY_OF_YEAR, -6)
            TimeFilter.DAYS_30 -> calendar.add(Calendar.DAY_OF_YEAR, -29)
            TimeFilter.MONTHS_3 -> calendar.add(Calendar.DAY_OF_YEAR, -89)
            TimeFilter.ALL -> calendar.timeInMillis = 0L
        }
        return calendar.timeInMillis
    }

    private fun getDayStartEpoch(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun generateChartPoints(sessions: List<PushupSessionEntity>, filter: TimeFilter): List<DailyChartPoint> {
        val numDays = when (filter) {
            TimeFilter.DAYS_7 -> 7
            TimeFilter.DAYS_30 -> 30
            TimeFilter.MONTHS_3 -> 90
            TimeFilter.ALL -> 30
        }

        val dayPoints = mutableListOf<DailyChartPoint>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayNames = arrayOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")

        for (i in (numDays - 1) downTo 0) {
            val tempCal = cal.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = tempCal.timeInMillis
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = tempCal.timeInMillis - 1

            val daySessions = sessions.filter { it.timestamp in dayStart..dayEnd }
            val pushups = daySessions.sumOf { it.pushupsCount }
            val bonus = daySessions.sumOf { it.unlockedBonusMinutes }

            val label = if (numDays <= 7) {
                val dayOfWeek = (cal.clone() as Calendar).apply { timeInMillis = dayStart }.get(Calendar.DAY_OF_WEEK) - 1
                dayNames[dayOfWeek % 7]
            } else {
                val m = (cal.clone() as Calendar).apply { timeInMillis = dayStart }.get(Calendar.MONTH) + 1
                val d = (cal.clone() as Calendar).apply { timeInMillis = dayStart }.get(Calendar.DAY_OF_MONTH)
                "$d/$m"
            }

            dayPoints.add(
                DailyChartPoint(
                    dayLabel = label,
                    dateMs = dayStart,
                    pushupsCount = pushups,
                    bonusMinutes = bonus
                )
            )
        }

        return dayPoints
    }
}
