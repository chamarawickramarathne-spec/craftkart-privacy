package lk.craftkart.pos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lk.craftkart.pos.ServiceLocator
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.CartLine
import lk.craftkart.pos.data.Category
import lk.craftkart.pos.data.Customer
import lk.craftkart.pos.data.CustomerGroup
import lk.craftkart.pos.data.Item

enum class PaymentMethod(val key: String) {
    CASH("cash"), CARD("card"), UPI("upi"), CREDIT("credit")
}

class PosViewModel(private val repo: AppRepository) : ViewModel() {

    val categories: Flow<List<Category>> = repo.categories
    val items: Flow<List<Item>> = repo.items
    val customers: Flow<List<Customer>> = repo.customers
    val customerGroups: Flow<List<CustomerGroup>> = repo.customerGroups

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

    private val _discountCents = MutableStateFlow(0L)
    val discountCents: StateFlow<Long> = _discountCents.asStateFlow()

    private val _manualDiscountPct = MutableStateFlow(0)
    val manualDiscountPct: StateFlow<Int> = _manualDiscountPct.asStateFlow()

    private val _customerId = MutableStateFlow<Long?>(null)
    val customerId: StateFlow<Long?> = _customerId.asStateFlow()

    private val _payment = MutableStateFlow(PaymentMethod.CASH)
    val payment: StateFlow<PaymentMethod> = _payment.asStateFlow()

    private val vatPercent = ServiceLocator.settings.vatPercent

    private val customerWithGroup: Flow<Pair<Customer?, CustomerGroup?>> =
        combine(_customerId, customers, customerGroups) { cid, custs, groups ->
            val cust = custs.firstOrNull { it.id == cid }
            val group = cust?.groupId?.let { gid -> groups.firstOrNull { it.id == gid } }
            cust to group
        }

    private val effectiveDiscountPct: Flow<Int> =
        combine(customerWithGroup, _manualDiscountPct) { (_, group), manual ->
            maxOf(group?.discountPercent ?: 0, manual)
        }

    val selectedCustomer: Flow<Customer?> = customerWithGroup.map { it.first }

    val filteredItems: Flow<List<Item>> = combine(items, query, categoryId) { list, q, cat ->
        val needle = q.trim()
        list.filter { item ->
            (cat == null || item.categoryId == cat) &&
                (needle.isBlank() ||
                    item.name.contains(needle, ignoreCase = true) ||
                    item.description.contains(needle, ignoreCase = true) ||
                    item.barcode.isNotBlank() && item.barcode.contains(needle, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val cartSubtotal: StateFlow<Long> = _cart
        .map { list -> list.sumOf { it.subtotalCents } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    private val totalBeforeTax: StateFlow<Long> = combine(_cart, _discountCents, effectiveDiscountPct) { list, d, pct ->
        val subtotal = list.sumOf { it.subtotalCents }
        val afterPct = subtotal * (100 - pct) / 100
        (afterPct - d).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val taxAmount: StateFlow<Long> = totalBeforeTax
        .map { it * vatPercent / 100 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val cartTotal: StateFlow<Long> = combine(totalBeforeTax, taxAmount) { base, tax -> base + tax }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val cartCount: StateFlow<Int> = _cart
        .map { list -> list.sumOf { it.qty } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(id: Long?) { _categoryId.value = id }
    suspend fun itemByBarcode(code: String): Item? = repo.itemByBarcode(code)
    fun setDiscount(cents: Long) { _discountCents.value = cents.coerceAtLeast(0) }
    fun setManualDiscountPct(pct: Int) { _manualDiscountPct.value = pct.coerceIn(0, 100) }
    fun setPayment(m: PaymentMethod) { _payment.value = m }
    fun selectCustomer(id: Long?) { _customerId.value = id }

    fun addToCart(item: Item) {
        val current = _cart.value
        val line = current.find { it.item.id == item.id }
        when {
            line == null && item.stockQty > 0 ->
                _cart.value = current + CartLine(item, 1)
            line != null && line.qty < item.stockQty ->
                changeQty(item.id, 1)
        }
    }

    fun changeQty(itemId: Long, delta: Int) {
        _cart.value = _cart.value
            .map { line ->
                if (line.item.id == itemId) {
                    line.copy(qty = (line.qty + delta).coerceIn(0, line.item.stockQty))
                } else line
            }
            .filter { it.qty > 0 }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountCents.value = 0
        _manualDiscountPct.value = 0
        _customerId.value = null
        _payment.value = PaymentMethod.CASH
    }

    fun completeSale(onDone: (Long) -> Unit) {
        val cartSnapshot = _cart.value
        if (cartSnapshot.isEmpty()) return
        viewModelScope.launch {
            val subtotal = cartSnapshot.sumOf { it.subtotalCents }
            val discount = (subtotal - totalBeforeTax.value).coerceAtLeast(0)
            val saleId = repo.completeSale(
                cart = cartSnapshot,
                discountAmountCents = discount,
                taxAmountCents = taxAmount.value,
                paymentMethod = _payment.value.key,
                customerId = _customerId.value
            )
            clearCart()
            onDone(saleId)
        }
    }
}