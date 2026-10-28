package lk.craftkart.pos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lk.craftkart.pos.data.LocaleManager
import lk.craftkart.pos.ui.AppViewModelFactory
import lk.craftkart.pos.ui.catalog.CatalogScreen
import lk.craftkart.pos.ui.customers.CustomerDetailScreen
import lk.craftkart.pos.ui.customers.CustomerEditScreen
import lk.craftkart.pos.ui.customers.CustomersScreen
import lk.craftkart.pos.ui.customers.GroupsScreen
import lk.craftkart.pos.ui.dashboard.DashboardScreen
import lk.craftkart.pos.ui.inventory.CategoriesScreen
import lk.craftkart.pos.ui.inventory.InventoryScreen
import lk.craftkart.pos.ui.inventory.ItemEditScreen
import lk.craftkart.pos.ui.pos.PosScreen
import lk.craftkart.pos.ui.reports.ReportsScreen
import lk.craftkart.pos.ui.sales.SaleDetailScreen
import lk.craftkart.pos.ui.sales.SalesHistoryScreen
import lk.craftkart.pos.ui.settings.SettingsScreen
import lk.craftkart.pos.ui.theme.CraftKartTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.attach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CraftKartTheme {
                CraftKartRoot()
            }
        }
    }
}

@Composable
private fun CraftKartRoot() {
    val navController = rememberNavController()
    val factory = remember { AppViewModelFactory(ServiceLocator.repository) }

    val tabs = remember {
        listOf(
            TabItem("home", Icons.Filled.Home),
            TabItem("pos", Icons.Filled.PointOfSale),
            TabItem("inventory", Icons.Filled.Inventory2),
            TabItem("sales", Icons.Filled.ReceiptLong),
            TabItem("settings", Icons.Filled.Settings)
        )
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            if (currentRoute in topLevelRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = {
                                Text(
                                    when (tab.route) {
                                        "home" -> stringResource(R.string.tab_home)
                                        "pos" -> stringResource(R.string.tab_pos)
                                        "inventory" -> stringResource(R.string.tab_inventory)
                                        "sales" -> stringResource(R.string.tab_sales)
                                        else -> stringResource(R.string.tab_settings)
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                DashboardScreen(
                    factory = factory,
                    onNavigate = { navController.navigate(it) }
                )
            }
            composable("pos") {
                PosScreen(factory = factory)
            }
            composable("inventory") {
                InventoryScreen(
                    factory = factory,
                    onEditItem = { id -> navController.navigate("inventory/item?itemId=$id") },
                    onNewItem = { navController.navigate("inventory/item") },
                    onManageCategories = { navController.navigate("categories") }
                )
            }
            composable(
                route = "inventory/item?itemId={itemId}",
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                ItemEditScreen(
                    factory = factory,
                    itemId = entry.arguments?.getLong("itemId")?.takeIf { it > 0 },
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
            composable("categories") {
                CategoriesScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("catalog") {
                CatalogScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("sales") {
                SalesHistoryScreen(
                    factory = factory,
                    onOpenSale = { id -> navController.navigate("sales/$id") },
                    onOpenReports = { navController.navigate("reports") }
                )
            }
            composable("reports") {
                ReportsScreen(factory = factory)
            }
            composable("customers") {
                CustomersScreen(
                    factory = factory,
                    onOpenCustomer = { id -> navController.navigate("customers/detail/$id") },
                    onNewCustomer = { navController.navigate("customers/edit") },
                    onManageGroups = { navController.navigate("groups") }
                )
            }
            composable(
                route = "customers/edit?customerId={customerId}",
                arguments = listOf(
                    navArgument("customerId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val id = entry.arguments?.getLong("customerId")?.takeIf { c -> c > 0 }
                CustomerEditScreen(
                    factory = factory,
                    customerId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "customers/detail/{customerId}",
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { entry ->
                CustomerDetailScreen(
                    factory = factory,
                    customerId = entry.arguments?.getLong("customerId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("customers/edit?customerId=$id") }
                )
            }
            composable("groups") {
                GroupsScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "sales/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.LongType })
            ) { entry ->
                SaleDetailScreen(
                    factory = factory,
                    saleId = entry.arguments?.getLong("saleId") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(factory = factory)
            }
        }
    }
}

private data class TabItem(val route: String, val icon: ImageVector)

private val topLevelRoutes = setOf("home", "pos", "inventory", "sales", "settings")
