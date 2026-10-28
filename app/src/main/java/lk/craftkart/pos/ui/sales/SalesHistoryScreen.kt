package lk.craftkart.pos.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.Sale
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.EmptyState

private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    factory: AppViewModelFactory,
    onOpenSale: (Long) -> Unit,
    onOpenReports: () -> Unit
) {
    val vm: SalesViewModel = viewModel(factory = factory)
    val sales by vm.sales.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sales_history), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenReports) {
                        Icon(Icons.Filled.BarChart, contentDescription = stringResource(R.string.reports))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (sales.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ReceiptLong,
                title = stringResource(R.string.no_sales),
                hint = stringResource(R.string.no_sales_hint)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                SaleRow(sale = sale, onTap = { onOpenSale(sale.id) })
            }
        }
    }
}

@Composable
private fun SaleRow(sale: Sale, onTap: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Column(modifier = Modifier.weight(1f)) {
                if (sale.invoiceNo.isNotBlank()) {
                    Text(
                        sale.invoiceNo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    dateFmt.format(Instant.ofEpochMilli(sale.createdAt).atZone(ZoneId.systemDefault())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        paymentLabel(sale.paymentMethod),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (sale.refundedCents > 0) {
                        Text(
                            stringResource(R.string.refunded),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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

@Composable
private fun paymentLabel(key: String): String = when (key) {
    "card" -> stringResource(R.string.card)
    "upi" -> stringResource(R.string.upi)
    "credit" -> stringResource(R.string.credit)
    else -> stringResource(R.string.cash)
}
