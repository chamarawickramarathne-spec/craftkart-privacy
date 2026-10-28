package lk.craftkart.pos.ui.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lk.craftkart.pos.R
import lk.craftkart.pos.data.CartLine
import lk.craftkart.pos.data.Customer
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.ui.common.EmptyState
import lk.craftkart.pos.ui.common.PriceField

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(vm: PosViewModel, onDone: (Long, Long) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cart by vm.cart.collectAsStateWithLifecycle()
    val subtotal by vm.cartSubtotal.collectAsStateWithLifecycle()
    val total by vm.cartTotal.collectAsStateWithLifecycle()
    val tax by vm.taxAmount.collectAsStateWithLifecycle()
    val payment by vm.payment.collectAsStateWithLifecycle()
    val customers by vm.customers.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedCustomer by vm.selectedCustomer.collectAsStateWithLifecycle(initialValue = null)
    var discountText by remember { mutableStateOf("") }
    var discountPctText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.cart),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (cart.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ShoppingCart,
                title = stringResource(R.string.cart),
                hint = stringResource(R.string.cart_empty)
            )
            return@Column
        }

        CustomerPicker(
            customers = customers,
            selected = selectedCustomer,
            onSelect = { vm.selectCustomer(it) }
        )

        cart.forEach { line ->
            CartLineRow(
                line = line,
                onQtyChange = { delta -> vm.changeQty(line.item.id, delta) }
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.subtotal), style = MaterialTheme.typography.bodyLarge)
            Text(Money.format(subtotal), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }

        PriceField(
            value = discountText,
            onValueChange = { input ->
                discountText = input
                vm.setDiscount(Money.parseToCents(input) ?: 0)
            },
            label = stringResource(R.string.discount_rs)
        )

        OutlinedTextField(
            value = discountPctText,
            onValueChange = { input ->
                discountPctText = input
                vm.setManualDiscountPct(input.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0)
            },
            label = { Text(stringResource(R.string.discount_pct)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (tax > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.receipt_tax), style = MaterialTheme.typography.bodyLarge)
                Text(Money.format(tax), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                Money.format(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            stringResource(R.string.payment_method),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentMethod.entries.forEach { m ->
                FilterChip(
                    selected = payment == m,
                    onClick = { vm.setPayment(m); error = null },
                    label = { Text(labelFor(m)) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                error = null
                if (payment == PaymentMethod.CREDIT && selectedCustomer == null) {
                    error = context.getString(R.string.credit_needs_customer)
                    return@Button
                }
                busy = true
                val paidTotal = total
                vm.completeSale { saleId -> onDone(saleId, paidTotal) }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.complete_sale), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CustomerPicker(
    customers: List<Customer>,
    selected: Customer?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.name ?: stringResource(R.string.walk_in)
    val isCredit = selected != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.customer)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.walk_in)) },
                    onClick = { onSelect(null); expanded = false }
                )
                customers.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text(customer.name) },
                        onClick = { onSelect(customer.id); expanded = false }
                    )
                }
            }
        }
    }
    if (isCredit) {
        Text(
            stringResource(R.string.credit_limit, Money.format(selected!!.creditLimitCents), Money.format(selected!!.balanceCents)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun labelFor(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> stringResource(R.string.cash)
    PaymentMethod.CARD -> stringResource(R.string.card)
    PaymentMethod.UPI -> stringResource(R.string.upi)
    PaymentMethod.CREDIT -> stringResource(R.string.credit)
}

@Composable
private fun CartLineRow(line: CartLine, onQtyChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(line.item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${Money.format(line.item.sellPriceCents)} × ${line.qty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            Money.format(line.subtotalCents),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onQtyChange(-1) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) { Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.remove)) }
            Text("${line.qty}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { onQtyChange(1) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add)) }
        }
    }
}