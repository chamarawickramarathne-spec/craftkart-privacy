package lk.craftkart.pos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.Category
import lk.craftkart.pos.data.Item

class InventoryViewModel(private val repo: AppRepository) : ViewModel() {

    val categories: Flow<List<Category>> = repo.categories
    val items: Flow<List<Item>> = repo.items
    val lowStock: Flow<List<Item>> = repo.lowStock

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    val filteredItems: Flow<List<Item>> = combine(items, query, categoryId) { list, q, cat ->
        val needle = q.trim()
        list.filter { item ->
            (cat == null || item.categoryId == cat) &&
                (needle.isBlank() || item.name.contains(needle, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(id: Long?) { _categoryId.value = id }

    suspend fun item(id: Long) = repo.itemById(id)
    suspend fun addItem(item: Item): Long = repo.addItem(item)
    suspend fun updateItem(item: Item) = repo.updateItem(item)
    suspend fun deleteItem(item: Item) = repo.deleteItem(item)
    suspend fun adjustStock(itemId: Long, delta: Int, reason: String) =
        repo.adjustStock(itemId, delta, reason)

    suspend fun addCategory(name: String): Long = repo.addCategory(name)
    suspend fun updateCategory(category: Category) = repo.updateCategory(category)
    suspend fun deleteCategory(category: Category): Boolean = repo.deleteCategory(category)
}
