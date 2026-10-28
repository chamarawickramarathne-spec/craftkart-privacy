package lk.craftkart.pos.data

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ck_prefs", Context.MODE_PRIVATE)

    var businessName: String
        get() = prefs.getString("business_name", "CraftKart") ?: "CraftKart"
        set(value) = prefs.edit().putString("business_name", value.trim().ifBlank { "CraftKart" }).apply()

    var language: String
        get() = prefs.getString("lang", "en") ?: "en"
        set(value) = prefs.edit().putString("lang", value).apply()

    var receiptContact: String
        get() = prefs.getString("receipt_contact", "") ?: ""
        set(value) = prefs.edit().putString("receipt_contact", value.trim()).apply()

    var receiptThanks: String
        get() = prefs.getString("receipt_thanks", "Thank you!") ?: "Thank you!"
        set(value) = prefs.edit().putString("receipt_thanks", value.trim().ifBlank { "Thank you!" }).apply()

    var vatPercent: Int
        get() = prefs.getInt("vat_percent", 0)
        set(value) = prefs.edit().putInt("vat_percent", value.coerceIn(0, 50)).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    var accentIndex: Int
        get() = prefs.getInt("accent_index", 0)
        set(value) = prefs.edit().putInt("accent_index", value).apply()

    var lowStockAlerts: Boolean
        get() = prefs.getBoolean("low_stock_alerts", true)
        set(value) = prefs.edit().putBoolean("low_stock_alerts", value).apply()
}
