package lk.craftkart.pos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Category::class, Item::class, Sale::class, SaleItem::class,
        AppMeta::class, Customer::class, CustomerGroup::class,
        Refund::class, StockAdjustment::class
    ],
    version = 4,
    exportSchema = true
)
abstract class CraftKartDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun itemDao(): ItemDao
    abstract fun saleDao(): SaleDao
    abstract fun appMetaDao(): AppMetaDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerGroupDao(): CustomerGroupDao
    abstract fun refundDao(): RefundDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sales ADD COLUMN invoiceNo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN taxAmountCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN customerId INTEGER")
                db.execSQL("ALTER TABLE sales ADD COLUMN refundedCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sale_items ADD COLUMN costPriceCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS app_meta (`key` TEXT NOT NULL PRIMARY KEY, `value` TEXT NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS customer_groups (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `discountPercent` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS customers (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `balanceCents` INTEGER NOT NULL, `creditLimitCents` INTEGER NOT NULL, `loyaltyPoints` INTEGER NOT NULL, `groupId` INTEGER, `createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS refunds (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `saleId` INTEGER NOT NULL, `amountCents` INTEGER NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN unit TEXT NOT NULL DEFAULT 'pcs'")
                db.execSQL("ALTER TABLE items ADD COLUMN barcode TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS stock_adjustments (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER NOT NULL, `delta` INTEGER NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }

        fun build(context: Context): CraftKartDatabase =
            Room.databaseBuilder(context, CraftKartDatabase::class.java, "craftkart.db")
                .setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}