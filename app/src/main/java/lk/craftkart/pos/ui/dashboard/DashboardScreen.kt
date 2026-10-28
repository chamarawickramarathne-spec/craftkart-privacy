package lk.craftkart.pos.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.theme.ErrorRed
import lk.craftkart.pos.ui.theme.Terracotta
import lk.craftkart.pos.ui.theme.TealLight

@Composable
fun DashboardScreen(
    factory: AppViewModelFactory,
    onNavigate: (String) -> Unit
) {
    val vm: DashboardViewModel = viewModel(factory = factory)
    val todayTotal by vm.todayTotal.collectAsStateWithLifecycle()
    val todayCount by vm.todayCount.collectAsStateWithLifecycle()
    val lowStock by vm.lowStock.collectAsStateWithLifecycle(initialValue = emptyList())
    val items by vm.items.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            TodayCard(total = todayTotal, count = todayCount)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Inventory2,
                    value = items.size.toString(),
                    label = stringResource(R.string.items_total),
                    tint = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Warning,
                    value = lowStock.size.toString(),
                    label = stringResource(R.string.low_stock),
                    tint = if (lowStock.isEmpty()) TealLight else ErrorRed
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.low_stock_items))
        }

        if (lowStock.isEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(stringResource(R.string.no_low_stock), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            items(lowStock.take(5), key = { it.id }) { item ->
                LowStockRow(item.name, item.stockQty, onTap = { onNavigate("inventory") })
            }
        }

        item {
            SectionTitle(stringResource(R.string.quick_actions))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickButton(Icons.Filled.PointOfSale, stringResource(R.string.new_sale), Terracotta) {
                    onNavigate("pos")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickButtonSmall(
                        Modifier.weight(1f),
                        Icons.Filled.MenuBook,
                        stringResource(R.string.catalog),
                        MaterialTheme.colorScheme.secondary
                    ) { onNavigate("catalog") }
                    QuickButtonSmall(
                        Modifier.weight(1f),
                        Icons.Filled.Add,
                        stringResource(R.string.add_item),
                        Terracotta
                    ) { onNavigate("inventory/item") }
                }
                QuickButton(Icons.Filled.ReceiptLong, stringResource(R.string.sales_history), TealLight) {
                    onNavigate("sales")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickButtonSmall(
                        Modifier.weight(1f),
                        Icons.Filled.BarChart,
                        stringResource(R.string.reports),
                        MaterialTheme.colorScheme.secondary
                    ) { onNavigate("reports") }
                    QuickButtonSmall(
                        Modifier.weight(1f),
                        Icons.Filled.People,
                        stringResource(R.string.customers),
                        Terracotta
                    ) { onNavigate("customers") }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TodayCard(total: Long, count: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.today_sales),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                Money.format(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$count ${stringResource(R.string.orders_today)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    tint: androidx.compose.ui.graphics.Color
) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun LowStockRow(name: String, stock: Int, onTap: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.stock_alert, stock),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed
                )
            }
            Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorRed)
        }
    }
}

@Composable
private fun QuickButton(icon: ImageVector, label: String, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(tint, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuickButtonSmall(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(tint, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
