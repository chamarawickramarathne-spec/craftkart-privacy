package lk.craftkart.pos.ui.customers

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Customer
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    factory: AppViewModelFactory,
    customerId: Long?,
    onBack: () -> Unit
) {
    val vm: CustomersViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val groups by vm.groups.collectAsStateWithLifecycle(initialValue = emptyList())

    val isEditing = customerId != null && customerId > 0
    var loaded by remember { mutableStateOf(!isEditing) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var creditText by remember { mutableStateOf("0") }
    var groupId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (isEditing) {
            vm.customer(customerId!!)?.let { c ->
                name = c.name
                phone = c.phone
                creditText = (c.creditLimitCents / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
                groupId = c.groupId
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) R.string.edit else R.string.new_customer), fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.customer_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            GroupDropdown(
                groups = groups.map { it.id to it.name },
                selectedId = groupId,
                onSelect = { groupId = it }
            )
            OutlinedTextField(
                value = creditText,
                onValueChange = { creditText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                label = { Text(stringResource(R.string.credit_limit_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = context.getString(R.string.name_required)
                        else -> {
                            error = null
                            scope.launch {
                                val customer = Customer(
                                    id = customerId ?: 0,
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    creditLimitCents = Money.parseToCents(creditText) ?: 0,
                                    groupId = groupId
                                )
                                if (isEditing) vm.updateCustomer(customer) else vm.addCustomer(customer)
                                onBack()
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
                    onClick = { showDelete = true },
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

    if (showDelete) {
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.delete_customer_confirm),
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { showDelete = false },
            onConfirm = {
                showDelete = false
                scope.launch {
                    vm.customer(customerId!!)?.let { vm.deleteCustomer(it) }
                    onBack()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDropdown(
    groups: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.first == selectedId }?.second ?: stringResource(R.string.walk_in)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.customer_groups)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.walk_in)) },
                onClick = { onSelect(null); expanded = false }
            )
            groups.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(id); expanded = false }
                )
            }
        }
    }
}