package lk.craftkart.pos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun all(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM items WHERE categoryId = :id")
    suspend fun countItems(id: Long): Int
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name COLLATE NOCASE")
    fun all(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun byId(id: Long): Item?

    @Query("SELECT * FROM items WHERE barcode = :barcode LIMIT 1")
    suspend fun byBarcode(barcode: String): Item?

    @Query("SELECT * FROM items WHERE stockQty <= lowStockThreshold ORDER BY stockQty")
    fun lowStock(): Flow<List<Item>>

    @Query("UPDATE items SET stockQty = :qty, updatedAt = :now WHERE id = :id")
    suspend fun updateStock(id: Long, qty: Int, now: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)
}

@Dao
interface CustomerGroupDao {
    @Query("SELECT * FROM customer_groups ORDER BY name COLLATE NOCASE")
    fun all(): Flow<List<CustomerGroup>>

    @Query("SELECT * FROM customer_groups WHERE id = :id")
    suspend fun byId(id: Long): CustomerGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: CustomerGroup): Long

    @Update
    suspend fun update(group: CustomerGroup)

    @Delete
    suspend fun delete(group: CustomerGroup)

    @Query("SELECT COUNT(*) FROM customers WHERE groupId = :id")
    suspend fun countCustomers(id: Long): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE")
    fun all(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun byId(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("UPDATE customers SET balanceCents = balanceCents + :delta WHERE id = :id")
    suspend fun addBalance(id: Long, delta: Long)

    @Query("UPDATE customers SET loyaltyPoints = loyaltyPoints + :points WHERE id = :id")
    suspend fun addPoints(id: Long, points: Int)

    @Query("SELECT COALESCE(SUM(totalAmountCents - refundedCents), 0) FROM sales WHERE customerId = :customerId")
    fun spentFlow(customerId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM refunds WHERE saleId IN (SELECT id FROM sales WHERE customerId = :customerId)")
    fun refundedFlow(customerId: Long): Flow<Long>
}

@Dao
interface RefundDao {
    @Insert
    suspend fun insert(refund: Refund): Long

    @Query("SELECT * FROM refunds WHERE saleId = :saleId ORDER BY createdAt DESC")
    suspend fun forSale(saleId: Long): List<Refund>
}

@Dao
interface StockAdjustmentDao {
    @Insert
    suspend fun insert(adjustment: StockAdjustment): Long

    @Query("SELECT * FROM stock_adjustments WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun forItem(itemId: Long): Flow<List<StockAdjustment>>
}

@Dao
interface SaleDao {
    @Insert
    suspend fun insert(sale: Sale): Long

    @Insert
    suspend fun insertItem(item: SaleItem)

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun all(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun byId(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE createdAt >= :from AND createdAt < :to ORDER BY createdAt")
    fun betweenFlow(from: Long, to: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId IN (SELECT id FROM sales WHERE createdAt >= :from AND createdAt < :to)")
    fun itemsBetweenFlow(from: Long, to: Long): Flow<List<SaleItem>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun saleWithItems(id: Long): SaleWithItems?

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :from AND createdAt < :to")
    suspend fun countBetween(from: Long, to: Long): Int

    @Query("SELECT COALESCE(SUM(totalAmountCents), 0) FROM sales WHERE createdAt >= :from AND createdAt < :to")
    suspend fun sumBetween(from: Long, to: Long): Long

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :from AND createdAt < :to")
    fun countBetweenFlow(from: Long, to: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmountCents), 0) FROM sales WHERE createdAt >= :from AND createdAt < :to")
    fun sumBetweenFlow(from: Long, to: Long): Flow<Long>

    @Query("UPDATE sales SET refundedCents = :amount, notes = :notes WHERE id = :id")
    suspend fun markRefunded(id: Long, amount: Long, notes: String)

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun forCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(totalAmountCents - refundedCents), 0) FROM sales WHERE customerId = :customerId")
    fun customerSpentFlow(customerId: Long): Flow<Long>
}

@Dao
interface AppMetaDao {
    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(meta: AppMeta)
}
