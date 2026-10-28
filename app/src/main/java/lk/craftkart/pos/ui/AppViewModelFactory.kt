package lk.craftkart.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.ui.dashboard.DashboardViewModel
import lk.craftkart.pos.ui.inventory.InventoryViewModel
import lk.craftkart.pos.ui.pos.PosViewModel
import lk.craftkart.pos.ui.reports.ReportsViewModel
import lk.craftkart.pos.ui.sales.SalesViewModel
import lk.craftkart.pos.ui.settings.SettingsViewModel

class AppViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PosViewModel::class.java) -> PosViewModel(repo) as T
        modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(repo) as T
        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repo) as T
        modelClass.isAssignableFrom(SalesViewModel::class.java) -> SalesViewModel(repo) as T
        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel() as T
        modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
