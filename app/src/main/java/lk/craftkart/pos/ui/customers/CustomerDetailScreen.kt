package lk.craftkart.pos.ui.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Customer
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.Sale
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.LoadingBox
import lk.craftkart.pos.ui.common.PriceField

private val dateFmt = DateTimeFormatter.ofPattern("MMM d, h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    factory: AppViewModelFactory,
    customerId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val vm: CustomersViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    var customer by remember { mutableStateOf<Customer?>(null) }
    var spent by remember { mutableStateOf(0L) }
    var refunded by remember { mutableStateOf(0L) }
    val sales by vm.salesForCustomer(customerId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showPay by remember { mutableStateOf(false) }
    var payText by remember { mutableStateOf("") }

    LaunchedEffect(customerId) {
        customer = vm.customer(customerId)
        spent = vm.customerSpent(customerId).firstOrNull() ?: 0L
        refunded = vm.customerRefunded(customerId).firstOrNull() ?: 0L
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(customerId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (customer == null) {
            LoadingBox(modifier = Modifier.fillMaxWidth().padding(padding))
            return@Scaffold
        }
        val c = customer!!

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(stringResource(R.string.phone), c.phone.ifBlank { "-" })
                        StatRow(stringResource(R.string.balance), Money.format(c.balanceCents), error = c.balanceCents > 0)
                        StatRow(stringResource(R.string.credit_limit_label), Money.format(c.creditLimitCents))
                        StatRow(stringResource(R.string.spent), Money.format(spent))
                        StatRow(stringResource(R.string.loyalty_points), c.loyaltyPoints.toString())
                        if (refunded > 0) {
                            StatRow(stringResource(R.string.report_refunds), Money.format(refunded))
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { showPay = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = c.balanceCents > 0
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null)
                    Text(stringResource(R.string.collect_payment), modifier = Modifier.padding(start = 6.dp))
                }
            }

            item {
                Text(stringResource(R.string.purchase_history), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (sales.isEmpty()) {
                item { Text(stringResource(R.string.no_sales), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(sales, key = { it.id }) { sale ->
                    SaleRow(sale = sale)
                }
            }
        }
    }

    if (showPay) {
        AlertDialog(
            onDismissRequest = { showPay = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.collect_payment), fontWeight = FontWeight.Bold) },
            text = {
                PriceField(
                    value = payText,
                    onValueChange = { payText = it },
                    label = stringResource(R.string.payment_amount)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = Money.parseToCents(payText) ?: 0L
                    if (amount > 0) {
                        scope.launch {
                            vm.collectPayment(customerId, amount)
                            customer = vm.customer(customerId)
                        }
                    }
                    showPay = false
                    payText = ""
                }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = { showPay = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, error: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SaleRow(sale: Sale) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                if (sale.invoiceNo.isNotBlank()) {
                    Text(sale.invoiceNo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    dateFmt.format(Instant.ofEpochMilli(sale.createdAt).atZone(ZoneId.systemDefault())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                Money.format(sale.totalAmountCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}