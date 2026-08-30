package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StoreViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StoreViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val cartCount by viewModel.cartItemCount.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppleLightBackground,
        bottomBar = {
            Surface(
                color = AppleCardLight,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder.copy(alpha = 0.6f))
            ) {
                NavigationBar(
                    containerColor = AppleCardLight,
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // POS Tab
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = AppleBlue,
                                            contentColor = Color.White
                                        ) {
                                            Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                    contentDescription = "POS"
                                )
                            }
                        },
                        label = { Text("POS", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            selectedTextColor = AppleBlue,
                            unselectedIconColor = AppleTextSecondary,
                            unselectedTextColor = AppleTextSecondary,
                            indicatorColor = AppleBlue.copy(alpha = 0.12f)
                        )
                    )

                    // Sales History Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTab == 1) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "History"
                            )
                        },
                        label = { Text("History", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            selectedTextColor = AppleBlue,
                            unselectedIconColor = AppleTextSecondary,
                            unselectedTextColor = AppleTextSecondary,
                            indicatorColor = AppleBlue.copy(alpha = 0.12f)
                        )
                    )

                    // Reports Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Insights else Icons.Outlined.Insights,
                                contentDescription = "Reports"
                            )
                        },
                        label = { Text("Reports", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            selectedTextColor = AppleBlue,
                            unselectedIconColor = AppleTextSecondary,
                            unselectedTextColor = AppleTextSecondary,
                            indicatorColor = AppleBlue.copy(alpha = 0.12f)
                        )
                    )

                    // Inventory Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTab == 3) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                contentDescription = "Inventory"
                            )
                        },
                        label = { Text("Inventory", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            selectedTextColor = AppleBlue,
                            unselectedIconColor = AppleTextSecondary,
                            unselectedTextColor = AppleTextSecondary,
                            indicatorColor = AppleBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(AppleLightBackground)
        ) {
            when (selectedTab) {
                0 -> PosScreen(viewModel)
                1 -> TransactionsScreen(viewModel)
                2 -> DashboardScreen(viewModel)
                3 -> InventoryScreen(viewModel)
            }
        }
    }
}
