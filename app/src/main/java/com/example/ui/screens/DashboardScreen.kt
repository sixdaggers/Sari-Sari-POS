package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.PeriodType
import com.example.ui.StoreViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: StoreViewModel) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val revenue by viewModel.periodRevenue.collectAsStateWithLifecycle()
    val profit by viewModel.periodProfit.collectAsStateWithLifecycle()
    val transactionCount by viewModel.periodTransactionCount.collectAsStateWithLifecycle()
    val salesWithItems by viewModel.periodSales.collectAsStateWithLifecycle()

    val profitMargin = if (revenue > 0) (profit / revenue) * 100.0 else 0.0
    val averageTicket = if (transactionCount > 0) revenue / transactionCount else 0.0

    // Top selling items calculation
    val topSellingItems = remember(salesWithItems) {
        val countMap = mutableMapOf<String, Int>()
        salesWithItems.forEach { saleWithItems ->
            saleWithItems.items.forEach { item ->
                countMap[item.name] = (countMap[item.name] ?: 0) + item.quantity
            }
        }
        countMap.toList().sortedByDescending { it.second }.take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleLightBackground)
    ) {
        // Header
        Surface(
            color = AppleCardLight,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    "Sales & Profit Reports",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    "Real-time analytics for your store",
                    fontSize = 13.sp,
                    color = AppleTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Apple Segmented Control for Period
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE9E9EB),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PeriodType.values().forEach { period ->
                            val isSelected = period == selectedPeriod
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AppleCardLight else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setSelectedPeriod(period) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = period.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AppleTextPrimary else AppleTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Main Stat Cards Row (Revenue & Profit)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Sales / Gross Revenue Card
                    MetricCard(
                        title = "Gross Sales",
                        value = formatCurrency(revenue),
                        subtitle = "${selectedPeriod.label} Benta",
                        icon = Icons.Outlined.Paid,
                        accentColor = AppleBlue,
                        backgroundColor = Color(0xFFE5F1FF),
                        modifier = Modifier.weight(1f)
                    )

                    // Net Profit Card
                    MetricCard(
                        title = "Net Profit (Kita)",
                        value = formatCurrency(profit),
                        subtitle = "${String.format(java.util.Locale.US, "%.1f", profitMargin)}% margin",
                        icon = Icons.Filled.TrendingUp,
                        accentColor = AppleGreen,
                        backgroundColor = Color(0xFFE8F8EE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Secondary Stats Row (Transactions & Average Basket)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Transactions",
                        value = "$transactionCount",
                        subtitle = "Total Customers",
                        icon = Icons.Outlined.People,
                        accentColor = ApplePurple,
                        backgroundColor = Color(0xFFF3E8FF),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Avg Ticket Size",
                        value = formatCurrency(averageTicket),
                        subtitle = "Per Transaction",
                        icon = Icons.Outlined.ShoppingBag,
                        accentColor = AppleOrange,
                        backgroundColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Top Selling Items Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppleBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Top Selling Products",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                selectedPeriod.label,
                                fontSize = 12.sp,
                                color = AppleTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (topSellingItems.isEmpty()) {
                            Text(
                                "No items sold during this period",
                                fontSize = 13.sp,
                                color = AppleTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            topSellingItems.forEachIndexed { index, (name, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when (index) {
                                                0 -> AppleYellow.copy(alpha = 0.2f)
                                                1 -> Color(0xFFD1D1D6)
                                                2 -> AppleOrange.copy(alpha = 0.2f)
                                                else -> Color(0xFFE9E9EB)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppleTextPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppleTextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AppleGrouped
                                    ) {
                                        Text(
                                            "$count sold",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppleTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (index < topSellingItems.size - 1) {
                                    HorizontalDivider(
                                        color = AppleBorder.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Offline Sync & Data Integrity Info Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F8EE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppleGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = AppleGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Offline Local Database Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E7E34)
                            )
                            Text(
                                "All transactions and inventory changes are saved securely offline with zero data loss during poor connectivity.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.border(1.dp, AppleBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                subtitle,
                fontSize = 11.sp,
                color = AppleTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
