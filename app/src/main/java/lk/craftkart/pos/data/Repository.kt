package lk.craftkart.pos.data

import androidx.room.withTransaction
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CartLine(val item: Item, val qty: Int) {
    val subtotalCents: Long get() = item.sellPriceCents * qty
}

data class PeriodSummary(
    val revenueCents: Long,
    val profitCents: Long,
    val discountCents: Long,
    val taxCents: Long,
    val refundedCents: Long,
    val orders: Int
)

data class TopSelling(val name: String, val qty: Int, val revenueCents: Long)
data class CategoryShare(val name: String, val revenueCents: Long)

data class ReportsData(
    val summary: PeriodSummary,
    val topSellers: List<TopSelling>,
    val categoryShares: List<CategoryShare>
)

class AppRepository(private val db: CraftKartDatabase) {
    private val categoryDao = db.categoryDao()
    private val itemDao = db.itemDao()
    private val saleDao = db.saleDao()
    private val metaDao = db.appMetaDao()
    private val customerDao = db.customerDao()
    private val customerGroupDao = db.customerGroupDao()
    private val refundDao = db.refundDao()
    private val stockAdjustmentDao = db.stockAdjustmentDao()

    val categories: Flow<List<Category>> = categoryDao.all()
    val items: Flow<List<Item>> = itemDao.all()
    val lowStock: Flow<List<Item>> = itemDao.lowStock()
    val sales: Flow<List<Sale>> = saleDao.all()
    val customers: Flow<List<Customer>> = customerDao.all()
    val customerGroups: Flow<List<CustomerGroup>> = customerGroupDao.all()

    suspend fun addCategory(name: String): Long =
        categoryDao.insert(Category(name = name.trim()))
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category): Boolean {
        if (categoryDao.countItems(category.id) > 0) return false
        categoryDao.delete(category)
        return true
    }

    suspend fun itemById(id: Long): Item? = itemDao.byId(id)
    suspend fun itemByBarcode(barcode: String): Item? = itemDao.byBarcode(barcode.trim())
    suspend fun addItem(item: Item): Long = itemDao.insert(item)
    suspend fun updateItem(item: Item) = itemDao.update(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteItem(item: Item) = itemDao.delete(item)

    suspend fun adjustStock(itemId: Long, delta: Int, reason: String) = db.withTransaction {
        val item = itemDao.byId(itemId) ?: return@withTransaction
        itemDao.updateStock(itemId, (item.stockQty + delta).coerceAtLeast(0))
        stockAdjustmentDao.insert(StockAdjustment(itemId = itemId, delta = delta, reason = reason.trim()))
    }
    fun stockAdjustments(itemId: Long): Flow<List<StockAdjustment>> = stockAdjustmentDao.forItem(itemId)

    suspend fun addCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)
    suspend fun customerById(id: Long): Customer? = customerDao.byId(id)
    fun customerSpent(customerId: Long): Flow<Long> = customerDao.spentFlow(customerId)
    fun customerRefunded(customerId: Long): Flow<Long> = customerDao.refundedFlow(customerId)
    suspend fun collectPayment(customerId: Long, amountCents: Long) {
        customerDao.addBalance(customerId, -amountCents.coerceAtLeast(0))
    }
    fun salesForCustomer(customerId: Long): Flow<List<Sale>> = saleDao.forCustomer(customerId)

    suspend fun addCustomerGroup(group: CustomerGroup): Long = customerGroupDao.insert(group)
    suspend fun updateCustomerGroup(group: CustomerGroup) = customerGroupDao.update(group)
    suspend fun deleteCustomerGroup(group: CustomerGroup): Boolean {
        if (customerGroupDao.countCustomers(group.id) > 0) return false
        customerGroupDao.delete(group)
        return true
    }

    suspend fun refundsForSale(saleId: Long): List<Refund> = refundDao.forSale(saleId)

    suspend fun saleWithItems(id: Long) = saleDao.saleWithItems(id)

    private fun dayRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }

    fun todayCount(): Flow<Int> = dayRange().let { saleDao.countBetweenFlow(it.first, it.second) }
    fun todayTotal(): Flow<Long> = dayRange().let { saleDao.sumBetweenFlow(it.first, it.second) }

    fun reportsBetween(from: Long, to: Long): Flow<ReportsData> =
        combine(saleDao.betweenFlow(from, to), saleDao.itemsBetweenFlow(from, to), items, categories) { sales, saleItems, allItems, cats ->
            val itemMap = allItems.associateBy { it.id }
            val catNames = cats.associate { it.id to it.name }
            val revenue = sales.sumOf { it.totalAmountCents - it.refundedCents }
            val discount = sales.sumOf { it.discountAmountCents }
            val tax = sales.sumOf { it.taxAmountCents }
            val refunded = sales.sumOf { it.refundedCents }
            val gross = saleItems.sumOf { it.subtotalCents }
            val cost = saleItems.sumOf { it.costPriceCents * it.quantity }
            val profit = (gross - discount + tax - refunded) - cost

            val topSellers = saleItems
                .groupBy { it.itemId }
                .map { (itemId, lines) ->
                    TopSelling(
                        name = lines.first().itemName,
                        qty = lines.sumOf { it.quantity },
                        revenueCents = lines.sumOf { it.subtotalCents }
                    )
                }
                .sortedByDescending { it.revenueCents }
                .take(5)

            val categoryShares = saleItems
                .groupBy { itemMap[it.itemId]?.categoryId ?: -1L }
                .map { (catId, lines) ->
                    CategoryShare(
                        name = catNames[catId] ?: "Other",
                        revenueCents = lines.sumOf { it.subtotalCents }
                    )
                }
                .sortedByDescending { it.revenueCents }
                .take(5)

            ReportsData(
                summary = PeriodSummary(revenue, profit, discount, tax, refunded, sales.size),
                topSellers = topSellers,
                categoryShares = categoryShares
            )
        }

    private suspend fun nextInvoiceNo(): String {
        val year = Year.now().value
        val key = "invoice_$year"
        val last = metaDao.get(key)?.toLongOrNull() ?: 0
        val next = last + 1
        metaDao.put(AppMeta(key, next.toString()))
        return "INV-$year-%04d".format(next)
    }

    suspend fun completeSale(
        cart: List<CartLine>,
        discountAmountCents: Long,
        taxAmountCents: Long,
        paymentMethod: String,
        customerId: Long? = null
    ): Long = db.withTransaction {
        val subtotal = cart.sumOf { it.subtotalCents }
        val total = (subtotal - discountAmountCents + taxAmountCents).coerceAtLeast(0)
        val saleId = saleDao.insert(
            Sale(
                totalAmountCents = total,
                discountAmountCents = discountAmountCents,
                paymentMethod = paymentMethod,
                invoiceNo = nextInvoiceNo(),
                taxAmountCents = taxAmountCents,
                customerId = customerId
            )
        )
        cart.forEach { line ->
            saleDao.insertItem(
                SaleItem(
                    saleId = saleId,
                    itemId = line.item.id,
                    itemName = line.item.name,
                    unitPriceCents = line.item.sellPriceCents,
                    quantity = line.qty,
                    subtotalCents = line.subtotalCents,
                    costPriceCents = line.item.costPriceCents
                )
            )
            itemDao.updateStock(line.item.id, line.item.stockQty - line.qty)
        }
        if (customerId != null) {
            customerDao.addPoints(customerId, ((total - taxAmountCents) / 1000).toInt())
            if (paymentMethod == "credit") {
                customerDao.addBalance(customerId, total)
            }
        }
        saleId
    }

    suspend fun refundSale(saleId: Long, amountCents: Long, reason: String) = db.withTransaction {
        val sale = saleDao.saleWithItems(saleId)?.sale ?: return@withTransaction
        val already = sale.refundedCents
        val refunding = amountCents.coerceIn(0, (sale.totalAmountCents - already).coerceAtLeast(0))
        if (refunding <= 0) return@withTransaction

        saleDao.markRefunded(saleId, already + refunding, if (reason.isBlank()) sale.notes else reason)
        refundDao.insert(Refund(saleId = saleId, amountCents = refunding, reason = reason.trim()))

        saleDao.saleWithItems(saleId)?.items?.forEach { line ->
            val item = itemDao.byId(line.itemId)
            if (item != null) {
                itemDao.updateStock(line.itemId, item.stockQty + line.quantity)
                stockAdjustmentDao.insert(
                    StockAdjustment(itemId = line.itemId, delta = line.quantity, reason = "Refund of sale $saleId")
                )
            }
        }
        if (sale.customerId != null) {
            customerDao.addBalance(sale.customerId, -refunding)
        }
    }
}