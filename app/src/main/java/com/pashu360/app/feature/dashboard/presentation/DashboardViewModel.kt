package com.pashu360.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import com.pashu360.app.feature.farm.domain.repository.FarmRepository
import com.pashu360.app.feature.health.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class DashboardUiState(
    val ownerName: String = "",
    val farmName: String = "",
    val todayFormatted: String = "",
    val greetingByTime: String = "Good Morning",
    val greetingEmoji: String = "🌅",
    val cowCount: Int = 0,
    val expectedHerdSize: Int = 0,
    val vaccinesDueCount: Int = 0,
    val sickCount: Int = 0
)

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    animalRepository: AnimalRepository,
    healthRepository: HealthRepository,
    farmRepository: FarmRepository,
    sessionStore: SessionStore
) : ViewModel() {

    private val farmId = sessionStore.getActiveFarmId()

    private val nowLdt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val today: LocalDate = nowLdt.date
    private val todayFormatted: String = formatFriendlyDate(today)
    private val greetingByTime: String = greetingForHour(nowLdt.hour)
    private val greetingEmoji: String = emojiForHour(nowLdt.hour)

    private val cowCountFlow =
        animalRepository.observeAnimals(farmId, AnimalFilter.ACTIVE).map { it.size }

    private val vaccinesDueFlow = combine(
        healthRepository.countOverdueVaccines(farmId, today),
        healthRepository.countDueSoonVaccines(farmId, today, 7)
    ) { overdue, dueSoon -> overdue + dueSoon }

    private val sickFlow = healthRepository.countActiveHealthIssues(farmId)
    private val farmFlow = farmRepository.observeFarm(farmId)

    private val groupA = combine(
        cowCountFlow, vaccinesDueFlow, sickFlow, farmFlow
    ) { cows, vaccines, sick, farm ->
        Quad(cows, vaccines, sick, farm)
    }

    val uiState: StateFlow<DashboardUiState> = groupA.map { a ->
        DashboardUiState(
            ownerName = a.d?.ownerName ?: "",
            farmName = a.d?.farmName ?: "",
            todayFormatted = todayFormatted,
            greetingByTime = greetingByTime,
            greetingEmoji = greetingEmoji,
            cowCount = a.a,
            expectedHerdSize = a.d?.expectedHerdSize ?: 0,
            vaccinesDueCount = a.b,
            sickCount = a.c
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState(
            todayFormatted = todayFormatted,
            greetingByTime = greetingByTime,
            greetingEmoji = greetingEmoji
        )
    )

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}

private fun formatFriendlyDate(d: LocalDate): String {
    val day = when (d.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
    val month = when (d.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
    }
    return "$day, ${d.day} $month"
}

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Good Night"
}

private fun emojiForHour(hour: Int): String = when (hour) {
    in 5..11 -> "🌅"
    in 12..16 -> "☀️"
    in 17..20 -> "🌆"
    else -> "🌙"
}
