package lk.craftkart.pos.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lk.craftkart.pos.R
import lk.craftkart.pos.ServiceLocator
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.ReceiptConfig
import lk.craftkart.pos.data.ReceiptGenerator
import lk.craftkart.pos.data.SaleWithItems
import lk.craftkart.pos.ui.AppViewModelFactory

private val receiptDateFmt = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    factory: AppViewModelFactory,
    saleId: Long,
    onBack: () -> Unit
) {
    val vm: SalesViewModel = viewModel(factory = factory)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<SaleWithItems?>(null) }
    var sharing by remember { mutableStateOf(false) }
    var showRefund by remember { mutableStateOf(false) }
    var refundReason by remember { mutableStateOf("") }
    var refunding by remember { mutableStateOf(false) }
    var refunded by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saleId) {
        data = vm.saleWithItems(saleId)
        refunded = (data?.sale?.refundedCents ?: 0L) > 0
    }

    LaunchedEffect(data?.sale?.customerId) {
        val cid = data?.sale?.customerId ?: return@LaunchedEffect
        customerName = ServiceLocator.repository.customerById(cid)?.name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sale_detail), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        val sale = data?.sale ?: return@Scaffold
        val items = data?.items ?: emptyList()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        ServiceLocator.settings.businessName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(R.string.receipt_business), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        receiptDateFmt.format(Instant.ofEpochMilli(sale.createdAt).atZone(ZoneId.systemDefault())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    sale.invoiceNo?.takeIf { it.isNotBlank() }?.let { inv ->
                        Text(
                            stringResource(R.string.receipt_number) + ": $inv",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        stringResource(R.string.receipt_payment) + ": " + paymentLabel(sale.paymentMethod),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    customerName?.let {
                        Text(
                            stringResource(R.string.customer) + ": $it",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (refunded) {
                        Text(
                            stringResource(R.string.refunded),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()

                    items.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.itemName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${Money.format(line.unitPriceCents)} × ${line.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                Money.format(line.subtotalCents),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))

                    if (sale.discountAmountCents > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.receipt_discount), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "-${Money.format(sale.discountAmountCents)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (sale.taxAmountCents > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.receipt_tax), style = MaterialTheme.typography.bodyLarge)
                            Text(Money.format(sale.taxAmountCents), style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.receipt_total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            Money.format(sale.totalAmountCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        ServiceLocator.settings.receiptThanks,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    val contact = ServiceLocator.settings.receiptContact
                    if (contact.isNotBlank()) {
                        Text(
                            contact,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val s = data ?: return@Button
                    sharing = true
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            ReceiptGenerator.generate(
                                context, s,
                                ReceiptConfig(
                                    businessName = ServiceLocator.settings.businessName,
                                    contact = ServiceLocator.settings.receiptContact,
                                    thanks = ServiceLocator.settings.receiptThanks
                                )
                            )
                        }
                        sharing = false
                        if (file != null) {
                            ReceiptGenerator.share(context, file)
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.pdf_failed),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = !sharing,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text(stringResource(R.string.share_receipt), modifier = Modifier.padding(start = 6.dp))
            }

            if (!refunded) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showRefund = true },
                    enabled = !sharing,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        stringResource(R.string.refund),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }

    if (showRefund) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRefund = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.refund_sale), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.refund_confirm))
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text(stringResource(R.string.refund_reason)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        refunding = true
                        val s = data?.sale
                        if (s != null) {
                            scope.launch {
                                ServiceLocator.repository.refundSale(s.id, s.totalAmountCents - s.refundedCents, refundReason)
                                data = ServiceLocator.repository.saleWithItems(s.id)
                                refunded = true
                                refunding = false
                                showRefund = false
                                refundReason = ""
                            }
                        }
                    },
                    enabled = !refunding
                ) { Text(stringResource(R.string.refund), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRefund = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun paymentLabel(key: String): String = when (key) {
    "card" -> stringResource(R.string.card)
    "upi" -> stringResource(R.string.upi)
    "credit" -> stringResource(R.string.credit)
    else -> stringResource(R.string.cash)
}
