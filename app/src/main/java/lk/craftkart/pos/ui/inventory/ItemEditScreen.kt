package lk.craftkart.pos.ui.inventory

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.coroutines.launch
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.PhotoStore
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.ConfirmDialog
import lk.craftkart.pos.ui.common.ItemPhotoPicker
import lk.craftkart.pos.ui.common.PriceField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    factory: AppViewModelFactory,
    itemId: Long?,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val vm: InventoryViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories by vm.categories.collectAsStateWithLifecycle(initialValue = emptyList())

    val isEditing = itemId != null && itemId > 0
    var loaded by remember { mutableStateOf(!isEditing) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var sellText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("0") }
    var thresholdText by remember { mutableStateOf("3") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var unit by remember { mutableStateOf("pcs") }
    var barcode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val barcodeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra("SCAN_RESULT")?.let { barcode = it }
        }
    }

    LaunchedEffect(itemId) {
        if (isEditing) {
            vm.item(itemId!!)?.let { item ->
                name = item.name
                description = item.description
                costText = (item.costPriceCents / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
                sellText = (item.sellPriceCents / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
                stockText = item.stockQty.toString()
                thresholdText = item.lowStockThreshold.toString()
                categoryId = item.categoryId
                imagePath = item.imagePath
                unit = item.unit
                barcode = item.barcode
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) R.string.edit else R.string.new_item), fontWeight = FontWeight.Bold) },
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
        if (!loaded) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ItemPhotoPicker(
                imagePath = imagePath,
                onChanged = { imagePath = it }
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.item_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            CategoryDropdown(
                categories = categories.map { it.id to it.name },
                selectedId = categoryId,
                onSelect = { categoryId = it }
            )

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text(stringResource(R.string.barcode)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { barcodeLauncher.launch(Intent(context, CaptureActivity::class.java)) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.scan))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            UnitDropdown(
                selected = unit,
                onSelect = { unit = it }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            PriceField(
                value = costText,
                onValueChange = { costText = it },
                label = stringResource(R.string.cost_price)
            )

            PriceField(
                value = sellText,
                onValueChange = { sellText = it },
                label = stringResource(R.string.selling_price)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text(stringResource(R.string.stock_qty)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text(stringResource(R.string.low_stock_threshold)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    val sellPrice = Money.parseToCents(sellText)
                    when {
                        name.isBlank() -> error = context.getString(R.string.name_required)
                        categoryId == null -> error = context.getString(R.string.category_required)
                        sellPrice == null -> error = context.getString(R.string.price_required)
                        else -> {
                            error = null
                            scope.launch {
                                val item = lk.craftkart.pos.data.Item(
                                    id = itemId ?: 0,
                                    categoryId = categoryId!!,
                                    name = name.trim(),
                                    description = description.trim(),
                                    costPriceCents = Money.parseToCents(costText) ?: 0,
                                    sellPriceCents = sellPrice,
                                    stockQty = stockText.toIntOrNull() ?: 0,
                                    lowStockThreshold = thresholdText.toIntOrNull() ?: 3,
                                    imagePath = imagePath,
                                    unit = unit,
                                    barcode = barcode.trim()
                                )
                                if (isEditing) vm.updateItem(item) else vm.addItem(item)
                                onDone()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(if (isEditing) R.string.update else R.string.add), style = MaterialTheme.typography.titleMedium)
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.delete_confirm),
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    vm.item(itemId!!)?.let { item ->
                        vm.deleteItem(item)
                        PhotoStore.delete(context, item.imagePath)
                    }
                    onDone()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.category)) },
            placeholder = { Text(stringResource(R.string.select_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

private val unitOptions = listOf("pcs", "set", "pair", "box", "kg", "g", "m", "pack")

@Composable
private fun unitLabel(key: String): String = when (key) {
    "set" -> stringResource(R.string.unit_set)
    "pair" -> stringResource(R.string.unit_pair)
    "box" -> stringResource(R.string.unit_box)
    "kg" -> stringResource(R.string.unit_kg)
    "g" -> stringResource(R.string.unit_g)
    "m" -> stringResource(R.string.unit_m)
    "pack" -> stringResource(R.string.unit_pack)
    else -> stringResource(R.string.unit_pcs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = unitLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.unit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            unitOptions.forEach { key ->
                DropdownMenuItem(
                    text = { Text(unitLabel(key)) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
