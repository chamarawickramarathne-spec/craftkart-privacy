package lk.craftkart.pos.ui.pos

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lk.craftkart.pos.R
import lk.craftkart.pos.ServiceLocator
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.ReceiptConfig
import lk.craftkart.pos.data.ReceiptGenerator
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.CategoryChips
import lk.craftkart.pos.ui.common.EmptyState
import lk.craftkart.pos.ui.theme.ErrorRed

data class SaleSuccess(val saleId: Long, val totalCents: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(factory: AppViewModelFactory) {
    val vm: PosViewModel = viewModel(factory = factory)
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val categories by vm.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val items by vm.filteredItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val query by vm.query.collectAsStateWithLifecycle()
    val categoryId by vm.categoryId.collectAsStateWithLifecycle()
    val cartCount by vm.cartCount.collectAsStateWithLifecycle()

    var showCart by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf<SaleSuccess?>(null) }
    var sharing by remember { mutableStateOf(false) }
    var successData by remember { mutableStateOf<lk.craftkart.pos.data.SaleWithItems?>(null) }

    val barcodeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")?.trim()
            if (!code.isNullOrBlank()) {
                scope.launch {
                    val item = vm.itemByBarcode(code)
                    if (item != null) {
                        vm.addToCart(item)
                        android.widget.Toast.makeText(context, item.name, android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.barcode_not_found),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(success?.saleId) {
        successData = success?.saleId?.let { ServiceLocator.repository.saleWithItems(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_sale), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (cartCount > 0) {
                FloatingActionButton(
                    onClick = { showCart = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White)
                        Text("$cartCount", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                placeholder = { Text(stringResource(R.string.search_items)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { barcodeLauncher.launch(Intent(context, CaptureActivity::class.java)) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.scan))
                    }
                },
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items, key = { it.id }) { item ->
                        PosItemCard(
                            name = item.name,
                            priceCents = item.sellPriceCents,
                            stock = item.stockQty,
                            imagePath = item.imagePath,
                            lowStock = item.stockQty <= item.lowStockThreshold,
                            onTap = { vm.addToCart(item) }
                        )
                    }
                }
            }
        }
    }

    if (showCart) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCart = false },
            sheetState = sheetState
        ) {
            CartSheet(
                vm = vm,
                onDone = { saleId, paidTotal ->
                    showCart = false
                    success = SaleSuccess(saleId, paidTotal)
                }
            )
        }
    }

    success?.let { s ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { success = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.sale_complete), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.sale_saved))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        Money.format(s.totalCents),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { success = null }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val s = successData ?: return@TextButton
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
                    enabled = !sharing
                ) { Text(stringResource(R.string.share_receipt)) }
            }
        )
    }
}

@Composable
private fun PosItemCard(
    name: String,
    priceCents: Long,
    stock: Int,
    imagePath: String?,
    lowStock: Boolean,
    onTap: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            if (imagePath != null) {
                AsyncImage(
                    model = java.io.File(imagePath),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            if (lowStock) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(ErrorRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(R.string.stock_alert, stock),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                Money.format(priceCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
