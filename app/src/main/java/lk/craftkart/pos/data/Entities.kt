package lk.craftkart.pos.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoryId")]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val description: String = "",
    val costPriceCents: Long = 0,
    val sellPriceCents: Long = 0,
    val stockQty: Int = 0,
    val lowStockThreshold: Int = 3,
    val imagePath: String? = null,
    val unit: String = "pcs",
    val barcode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalAmountCents: Long,
    val discountAmountCents: Long = 0,
    val paymentMethod: String,
    val notes: String = "",
    val invoiceNo: String = "",
    val taxAmountCents: Long = 0,
    val customerId: Long? = null,
    val refundedCents: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [ForeignKey(
        entity = Sale::class,
        parentColumns = ["id"],
        childColumns = ["saleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val itemId: Long,
    val itemName: String,
    val unitPriceCents: Long,
    val quantity: Int,
    val subtotalCents: Long,
    val costPriceCents: Long = 0
)

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(parentColumn = "id", entityColumn = "saleId")
    val items: List<SaleItem>
)

data class ItemWithCategory(
    @Embedded val item: Item,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category
)

@Entity(tableName = "app_meta")
data class AppMeta(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "customer_groups")
data class CustomerGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val discountPercent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val balanceCents: Long = 0,
    val creditLimitCents: Long = 0,
    val loyaltyPoints: Int = 0,
    val groupId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "refunds")
data class Refund(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val amountCents: Long,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_adjustments")
data class StockAdjustment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val delta: Int,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
