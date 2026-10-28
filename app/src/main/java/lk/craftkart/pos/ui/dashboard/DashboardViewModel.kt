package lk.craftkart.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.Item

class DashboardViewModel(private val repo: AppRepository) : ViewModel() {

    val lowStock: StateFlow<List<Item>> = repo.lowStock
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val items: StateFlow<List<Item>> = repo.items
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todayTotal: StateFlow<Long> = repo.todayTotal()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val todayCount: StateFlow<Int> = repo.todayCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
}
