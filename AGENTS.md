# CraftKart — App Modification Memory

Handmade shop **Inventory + POS** app for Android (native, offline-first).
Package: `lk.craftkart.pos` · App name: **CraftKart** · Currency: LKR (Rs)

## App Details
- **Platform:** Android (min SDK 26 / Android 8.0+, tested target Android 9-15)
- **Language:** Kotlin 2.0.21 · Jetpack Compose (Material 3) · Room (SQLite)
- **Storage:** Local only (Room DB `craftkart.db`, images in app `filesDir/item_images`)
- **i18n:** English (default) + Sinhala (values-si), in-app language toggle
- **Payments:** Cash / Card / UPI (stored as string key)
- **DB schema:** `sql/schema.sql` (Room export in `app/schemas`)
- **Build:** `gradlew.bat assembleRelease` → signed APK in `releases/`
- **Keystore:** `keystore/craftkart-release.jks` + `keystore.properties` (root)

## Modification Log
| Mod | Version | Date | Changes |
|-----|---------|------|---------|
| 0   | 1.0.0   | 2026-08-11 | Initial release. Dashboard, POS (search + categories + cart + discount + payment + auto stock deduction), Inventory CRUD with camera/gallery photos, Categories manage, Catalog view, Sales history + receipt detail, Settings (business name, EN/SI language, currency display). Low-stock alerts. Release APK `releases/CraftKart-v1.0.0.apk`. |
| 1   | 1.0.1   | 2026-08-11 | Fix: Today's Sales on Home now updates live via Room Flow (was one-time snapshot requiring app restart). Replaced suspend queries with reactive Flow aggregates. Release APK `releases/CraftKart-v1.0.1.apk`. |
| 2   | 1.1.0   | 2026-08-11 | Receipts & Reports. PDF receipt generation + share via FileProvider (`ReceiptGenerator.kt`), configurable footer/thanks/contact in Settings, Reports screen (Today/Week/Month/Last30/All filters, revenue/orders/avg/profit cards, top sellers, category donut) with bar-chart entry from Sales History. VAT % setting in Settings. DB stays v3. Release APK `releases/CraftKart-v1.1.0.apk`. |
| 3   | 1.2.0   | 2026-08-12 | Customers, credit & refunds. DB v4 (customers, customer_groups, refunds, stock_adjustments, app_meta; sales gains invoiceNo/taxAmountCents/customerId/refundedCents; sale_items gains costPriceCents; items gains unit/barcode). Invoice numbers INV-YYYY-%04d, per-sale cost capture, credit payment (adds to customer balance w/ limit check), loyalty points, customer groups w/ % discount, refunds restore stock + log adjustment + reverse balance, stock adjustment logging. Release APK `releases/CraftKart-v1.2.0.apk`. |
| 4   | 1.3.0   | 2026-08-12 | Usability. Refund dialog (reason + full-return + stock restore) from sale detail, customer quick actions on Dashboard (Reports + Customers), Customers screen wired to navigation (edit/detail/groups routes), invoice no + customer name shown on sale detail. Release APK `releases/CraftKart-v1.3.0.apk`. |
| 5   | 1.4.0   | 2026-08-12 | Barcode & stock. ZXing (journeyapps `zxing-android-embedded` 4.3.0) barcode/QR scanning: scan button on Item edit (fills barcode field) and on POS search (quick add to cart by barcode, toast if not found). Item unit picker (pcs/set/pair/box/kg/g/m/pack). Inventory stock adjust dialog (+/- qty + reason) logging to stock_adjustments. DB stays v4. Release APK `releases/CraftKart-v1.4.0.apk`. |
| 6   | 1.4.1   | 2026-08-12 | CRITICAL FIX: app crashed at launch ("keeps closing") for installs upgrading from a pre-v4 database. Room migration validation failed because `MIGRATION_2_3` rebuilt `sales` with a foreign key + extra index the `Sale` entity doesn't declare (and its `RENAME` rewired `sale_items` FK to a dropped table). Fix: `MIGRATION_2_3` now only creates the customer tables (`MIGRATION_1_2` already adds the needed sales columns); removed the stray `index_stock_adjustments_itemId` from `MIGRATION_3_4`. Verified against `4.json` (schema matches, data preserved). DB stays v4, identity hash unchanged. Release APK `releases/CraftKart-v1.4.1.apk`. |
| 7   | 1.4.2   | 2026-10-28 | Play Store prep: version bump, generated store graphics (512x512 icon, 1024x500 feature), phone screenshots, privacy policy via GitHub Pages, built release AAB. No functional changes. Release AAB `app/build/outputs/bundle/release/app-release.aab`. |

## Build Notes
- First build configures `local.properties` with `sdk.dir`; Android SDK at
  `C:\Users\Nwick\AppData\Local\Android\Sdk`, JDK 17 at
  `C:\Users\Nwick\.local\jdk-17.0.20+8` (set `JAVA_HOME` per command).
- Release APK is R8-minified + resource-shrunk, signed with v2 scheme.
- Receipt printing: planned future mod (button placeholders already in UI).
- Future: Supabase sync (data currently stays on device).

## Update Process
1. Make changes → bump `versionCode`/`versionName` in `app/build.gradle.kts`.
2. Add a new row to the Modification Log above (increment Mod number).
3. Regenerate `media_support.txt` content for the new version.
4. Rebuild: `gradlew.bat :app:assembleRelease` and copy APK to `releases/`.
