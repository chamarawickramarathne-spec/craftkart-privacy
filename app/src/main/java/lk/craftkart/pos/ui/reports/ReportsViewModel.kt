package lk.craftkart.pos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.ReportsData

enum class ReportPeriod(val key: String) {
    TODAY("today"), WEEK("week"), MONTH("month"), LAST30("last30"), ALL("all")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(private val repo: AppRepository) : ViewModel() {

    private val _period = MutableStateFlow(ReportPeriod.TODAY)
    val period: StateFlow<ReportPeriod> = _period.asStateFlow()

    val data: StateFlow<ReportsData?> = _period
        .map { it.toRange() }
        .flatMapLatest { (from, to) -> repo.reportsBetween(from, to) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setPeriod(p: ReportPeriod) { _period.value = p }

    private fun ReportPeriod.toRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now()
        fun start(d: LocalDate) = d.atStartOfDay(zone).toInstant().toEpochMilli()
        return when (this) {
            ReportPeriod.TODAY -> start(now) to start(now.plusDays(1))
            ReportPeriod.WEEK -> start(now.with(DayOfWeek.MONDAY)) to start(now.with(DayOfWeek.MONDAY).plusWeeks(1))
            ReportPeriod.MONTH -> start(now.withDayOfMonth(1)) to start(now.withDayOfMonth(1).plusMonths(1))
            ReportPeriod.LAST30 -> (System.currentTimeMillis() - 30L * 24 * 3600 * 1000) to System.currentTimeMillis()
            ReportPeriod.ALL -> 0L to Long.MAX_VALUE
        }
    }
}