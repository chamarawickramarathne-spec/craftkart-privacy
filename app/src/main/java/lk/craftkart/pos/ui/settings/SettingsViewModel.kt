package lk.craftkart.pos.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lk.craftkart.pos.ServiceLocator
import lk.craftkart.pos.data.ReceiptConfig

class SettingsViewModel : ViewModel() {

    private val _businessName = MutableStateFlow(ServiceLocator.settings.businessName)
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _language = MutableStateFlow(ServiceLocator.settings.language)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _receiptContact = MutableStateFlow(ServiceLocator.settings.receiptContact)
    val receiptContact: StateFlow<String> = _receiptContact.asStateFlow()

    private val _receiptThanks = MutableStateFlow(ServiceLocator.settings.receiptThanks)
    val receiptThanks: StateFlow<String> = _receiptThanks.asStateFlow()

    private val _vatPercent = MutableStateFlow(ServiceLocator.settings.vatPercent)
    val vatPercent: StateFlow<Int> = _vatPercent.asStateFlow()

    private val _themeMode = MutableStateFlow(ServiceLocator.settings.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _accentIndex = MutableStateFlow(ServiceLocator.settings.accentIndex)
    val accentIndex: StateFlow<Int> = _accentIndex.asStateFlow()

    private val _lowStockAlerts = MutableStateFlow(ServiceLocator.settings.lowStockAlerts)
    val lowStockAlerts: StateFlow<Boolean> = _lowStockAlerts.asStateFlow()

    fun saveBusinessName(name: String) {
        ServiceLocator.settings.businessName = name
        _businessName.value = ServiceLocator.settings.businessName
    }

    fun setLanguage(lang: String) {
        ServiceLocator.settings.language = lang
        _language.value = lang
    }

    fun saveReceiptFooter(contact: String, thanks: String) {
        ServiceLocator.settings.receiptContact = contact
        ServiceLocator.settings.receiptThanks = thanks
        _receiptContact.value = ServiceLocator.settings.receiptContact
        _receiptThanks.value = ServiceLocator.settings.receiptThanks
    }

    fun setVat(percent: Int) {
        ServiceLocator.settings.vatPercent = percent
        _vatPercent.value = ServiceLocator.settings.vatPercent
    }

    fun setThemeMode(mode: String) {
        ServiceLocator.settings.themeMode = mode
        _themeMode.value = mode
    }

    fun setAccent(index: Int) {
        ServiceLocator.settings.accentIndex = index
        _accentIndex.value = index
    }

    fun setLowStockAlerts(enabled: Boolean) {
        ServiceLocator.settings.lowStockAlerts = enabled
        _lowStockAlerts.value = enabled
    }

    fun receiptConfig() = ReceiptConfig(
        businessName = _businessName.value,
        contact = _receiptContact.value,
        thanks = _receiptThanks.value
    )
}