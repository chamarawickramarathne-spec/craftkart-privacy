package lk.craftkart.pos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Item
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.PhotoStore
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.CategoryChips
import lk.craftkart.pos.ui.common.ConfirmDialog
import lk.craftkart.pos.ui.common.EmptyState
import lk.craftkart.pos.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    factory: AppViewModelFactory,
    onEditItem: (Long) -> Unit,
    onNewItem: () -> Unit,
    onManageCategories: () -> Unit
) {
    val vm: InventoryViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories by vm.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val items by vm.filteredItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val query by vm.query.collectAsStateWithLifecycle()
    val categoryId by vm.categoryId.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<Item?>(null) }
    var adjustTarget by remember { mutableStateOf<Item?>(null) }
    var adjustDelta by remember { mutableStateOf("") }
    var adjustReason by remember { mutableStateOf("") }
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.tab_inventory), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onManageCategories) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.categories_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewItem, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_item), tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                placeholder = { Text(stringResource(R.string.search_items)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            CategoryChips(
                categories = listOf<Pair<Long?, String>>(null to stringResource(R.string.all)) +
                    categories.map { it.id to it.name },
                selectedId = categoryId,
                onSelect = vm::setCategory
            )

            if (items.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = stringResource(R.string.no_items),
                    hint = stringResource(R.string.no_items_hint)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        InventoryRow(
                            item = item,
                            categoryName = categoryNames[item.categoryId] ?: "",
                            onTap = { onEditItem(item.id) },
                            onAdjust = { adjustTarget = item },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.delete_confirm),
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { deleteTarget = null },
            onConfirm = {
                deleteTarget = null
                scope.launch {
                    vm.deleteItem(target)
                    PhotoStore.delete(context, target.imagePath)
                }
            }
        )
    }

    adjustTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { adjustTarget = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.adjust_stock), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${target.name} · ${target.stockQty} ${unitLabelFor(target.unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = adjustDelta,
                        onValueChange = { adjustDelta = it.filter { c -> c.isDigit() || c == '-' }.take(6) },
                        label = { Text(stringResource(R.string.stock_change)) },
                        placeholder = { Text("+5 / -2") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text(stringResource(R.string.adjustment_reason)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val delta = adjustDelta.trim().toIntOrNull()
                        if (delta != null && delta != 0) {
                            scope.launch {
                                vm.adjustStock(target.id, delta, adjustReason)
                            }
                        }
                        adjustTarget = null
                        adjustDelta = ""
                        adjustReason = ""
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { adjustTarget = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun unitLabelFor(key: String): String = when (key) {
    "set" -> stringResource(R.string.unit_set)
    "pair" -> stringResource(R.string.unit_pair)
    "box" -> stringResource(R.string.unit_box)
    "kg" -> stringResource(R.string.unit_kg)
    "g" -> stringResource(R.string.unit_g)
    "m" -> stringResource(R.string.unit_m)
    "pack" -> stringResource(R.string.unit_pack)
    else -> stringResource(R.string.unit_pcs)
}

@Composable
private fun InventoryRow(
    item: Item,
    categoryName: String,
    onTap: () -> Unit,
    onAdjust: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imagePath != null) {
                    AsyncImage(
                        model = java.io.File(item.imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(categoryName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        Money.format(item.sellPriceCents),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "· ${item.stockQty} ${unitLabelFor(item.unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.stockQty <= item.lowStockThreshold) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onAdjust) {
                Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.adjust_stock), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = ErrorRed)
            }
        }
    }
}
