package lk.craftkart.pos.ui.sales

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.Sale
import lk.craftkart.pos.data.SaleWithItems

class SalesViewModel(private val repo: AppRepository) : ViewModel() {

    val sales: Flow<List<Sale>> = repo.sales

    suspend fun saleWithItems(id: Long): SaleWithItems? = repo.saleWithItems(id)}
