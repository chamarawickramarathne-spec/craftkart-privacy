-- CraftKart database schema (Room v4, identity hash 76060709a56dae412041013e21d342cb)

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    categoryId INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    costPriceCents INTEGER NOT NULL,
    sellPriceCents INTEGER NOT NULL,
    stockQty INTEGER NOT NULL,
    lowStockThreshold INTEGER NOT NULL,
    imagePath TEXT,
    unit TEXT NOT NULL,
    barcode TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS index_items_categoryId ON items (categoryId);

CREATE TABLE IF NOT EXISTS sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    totalAmountCents INTEGER NOT NULL,
    discountAmountCents INTEGER NOT NULL,
    paymentMethod TEXT NOT NULL,
    notes TEXT NOT NULL,
    invoiceNo TEXT NOT NULL,
    taxAmountCents INTEGER NOT NULL,
    customerId INTEGER,
    refundedCents INTEGER NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS sale_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    saleId INTEGER NOT NULL,
    itemId INTEGER NOT NULL,
    itemName TEXT NOT NULL,
    unitPriceCents INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    subtotalCents INTEGER NOT NULL,
    costPriceCents INTEGER NOT NULL,
    FOREIGN KEY(saleId) REFERENCES sales(id) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_sale_items_saleId ON sale_items (saleId);

CREATE TABLE IF NOT EXISTS app_meta (
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    PRIMARY KEY(key)
);

CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    balanceCents INTEGER NOT NULL,
    creditLimitCents INTEGER NOT NULL,
    loyaltyPoints INTEGER NOT NULL,
    groupId INTEGER,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    discountPercent INTEGER NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS refunds (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    saleId INTEGER NOT NULL,
    amountCents INTEGER NOT NULL,
    reason TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS stock_adjustments (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    itemId INTEGER NOT NULL,
    delta INTEGER NOT NULL,
    reason TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);
