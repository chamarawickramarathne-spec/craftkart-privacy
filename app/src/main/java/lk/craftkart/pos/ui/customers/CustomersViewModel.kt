package lk.craftkart.pos.ui.customers

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.Customer
import lk.craftkart.pos.data.CustomerGroup
import lk.craftkart.pos.data.Sale

class CustomersViewModel(private val repo: AppRepository) : ViewModel() {

    val customers: Flow<List<Customer>> = repo.customers
    val groups: Flow<List<CustomerGroup>> = repo.customerGroups

    suspend fun customer(id: Long): Customer? = repo.customerById(id)
    suspend fun addCustomer(customer: Customer): Long = repo.addCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = repo.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = repo.deleteCustomer(customer)

    suspend fun addGroup(group: CustomerGroup): Long = repo.addCustomerGroup(group)
    suspend fun updateGroup(group: CustomerGroup) = repo.updateCustomerGroup(group)
    suspend fun deleteGroup(group: CustomerGroup): Boolean = repo.deleteCustomerGroup(group)

    fun customerSpent(id: Long): Flow<Long> = repo.customerSpent(id)
    fun customerRefunded(id: Long): Flow<Long> = repo.customerRefunded(id)
    fun salesForCustomer(id: Long): Flow<List<Sale>> = repo.salesForCustomer(id)

    suspend fun collectPayment(id: Long, amountCents: Long) = repo.collectPayment(id, amountCents)
    suspend fun refundsForSale(saleId: Long) = repo.refundsForSale(saleId)
}