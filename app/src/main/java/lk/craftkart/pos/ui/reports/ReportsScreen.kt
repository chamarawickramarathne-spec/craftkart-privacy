package lk.craftkart.pos.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import lk.craftkart.pos.R
import lk.craftkart.pos.data.Money
import lk.craftkart.pos.data.TopSelling
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.common.EmptyState
import lk.craftkart.pos.ui.theme.ErrorRed
import lk.craftkart.pos.ui.theme.Saffron
import lk.craftkart.pos.ui.theme.Teal
import lk.craftkart.pos.ui.theme.Terracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(factory: AppViewModelFactory) {
    val vm: ReportsViewModel = viewModel(factory = factory)
    val period by vm.period.collectAsStateWithLifecycle()
    val data by vm.data.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports), fontWeight = FontWeight.Bold) },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { vm.setPeriod(p) },
                        label = { Text(periodLabel(p)) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            val report = data ?: run {
                EmptyState(
                    icon = Icons.Filled.BarChart,
                    title = stringResource(R.string.reports),
                    hint = stringResource(R.string.no_report_data)
                )
                return@Column
            }
            val s = report.summary

            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricBox(
                            Modifier.weight(1f),
                            stringResource(R.string.report_revenue),
                            Money.format(s.revenueCents),
                            MaterialTheme.colorScheme.primary
                        )
                        MetricBox(
                            Modifier.weight(1f),
                            stringResource(R.string.report_profit),
                            Money.format(s.profitCents),
                            if (s.profitCents >= 0) MaterialTheme.colorScheme.secondary else ErrorRed
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricBox(
                            Modifier.weight(1f),
                            stringResource(R.string.report_orders),
                            s.orders.toString(),
                            Saffron
                        )
                        MetricBox(
                            Modifier.weight(1f),
                            stringResource(R.string.report_discount),
                            Money.format(s.discountCents),
                            Terracotta
                        )
                    }
                    if (s.refundedCents > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.report_refunds), style = MaterialTheme.typography.bodyLarge)
                            Text(Money.format(s.refundedCents), style = MaterialTheme.typography.bodyLarge, color = ErrorRed)
                        }
                    }
                }
            }

            if (report.topSellers.isNotEmpty()) {
                SectionTitle(stringResource(R.string.top_sellers))
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        report.topSellers.forEach { top ->
                            TopSellerBar(top, report.topSellers.maxOf { it.revenueCents })
                        }
                    }
                }
            }

            if (report.categoryShares.isNotEmpty()) {
                SectionTitle(stringResource(R.string.category_breakdown))
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DonutChart(report.categoryShares.map { it.revenueCents })
                        report.categoryShares.forEachIndexed { i, share ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(Modifier.size(12.dp).background(donutColors[i % donutColors.size], RoundedCornerShape(4.dp)))
                                Text(share.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(Money.format(share.revenueCents), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun periodLabel(p: ReportPeriod): String = when (p) {
    ReportPeriod.TODAY -> stringResource(R.string.report_today)
    ReportPeriod.WEEK -> stringResource(R.string.report_week)
    ReportPeriod.MONTH -> stringResource(R.string.report_month)
    ReportPeriod.LAST30 -> stringResource(R.string.report_last30)
    ReportPeriod.ALL -> stringResource(R.string.report_all)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun MetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
private fun TopSellerBar(top: TopSelling, maxRevenue: Long) {
    val fraction = if (maxRevenue <= 0) 0f else (top.revenueCents.toFloat() / maxRevenue.toFloat()).coerceIn(0.05f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(top.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                "${top.qty} · ${Money.format(top.revenueCents)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(10.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
        )
    }
}

private val donutColors = listOf(Terracotta, Teal, Saffron, ErrorRed)

@Composable
private fun DonutChart(values: List<Long>) {
    val total = values.sum().toFloat().coerceAtLeast(1f)
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(140.dp)) {
            var start = -90f
            values.forEachIndexed { i, v ->
                val sweep = v.toFloat() / total * 360f
                drawArc(
                    color = donutColors[i % donutColors.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 26.dp.toPx(), cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
    }
}