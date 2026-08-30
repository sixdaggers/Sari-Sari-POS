package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SaleWithItems
import com.example.ui.StoreViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: StoreViewModel) {
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    var selectedSale by remember { mutableStateOf<SaleWithItems?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleLightBackground)
    ) {
        // Top Header
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
                    "Sales History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    "${sales.size} total transactions logged",
                    fontSize = 13.sp,
                    color = AppleTextSecondary
                )
            }
        }

        if (sales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No transactions yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary
                    )
                    Text(
                        "Completed sales will appear here automatically",
                        fontSize = 13.sp,
                        color = AppleTextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(sales, key = { it.sale.id }) { saleWithItems ->
                    TransactionItemCard(
                        saleWithItems = saleWithItems,
                        onClick = { selectedSale = saleWithItems }
                    )
                }
            }
        }
    }

    selectedSale?.let { saleWithItems ->
        TransactionDetailDialog(
            saleWithItems = saleWithItems,
            onDismiss = { selectedSale = null }
        )
    }
}

@Composable
fun TransactionItemCard(
    saleWithItems: SaleWithItems,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, AppleBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = AppleBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Receipt #${saleWithItems.sale.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppleTextPrimary
                    )
                    Text(
                        formatCurrency(saleWithItems.sale.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AppleTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val itemsSummary = saleWithItems.items.joinToString(", ") { "${it.name} (${it.quantity})" }
                Text(
                    itemsSummary,
                    fontSize = 12.sp,
                    color = AppleTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatDateTime(saleWithItems.sale.timestamp),
                        fontSize = 11.sp,
                        color = AppleTextTertiary
                    )
                    Text(
                        "+${formatCurrency(saleWithItems.sale.totalProfit)} profit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Filled.ArrowForwardIos,
                contentDescription = "View",
                tint = AppleTextTertiary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun TransactionDetailDialog(
    saleWithItems: SaleWithItems,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppleCardLight,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Transaction #${saleWithItems.sale.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                        Text(
                            formatDateTime(saleWithItems.sale.timestamp),
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFE9E9EB), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = AppleTextPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppleGrouped,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Items Purchased", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        saleWithItems.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                                    Text("${item.quantity} × ${formatCurrency(item.priceAtSale)}", fontSize = 11.sp, color = AppleTextSecondary)
                                }
                                Text(
                                    formatCurrency(item.quantity * item.priceAtSale),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTextPrimary
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppleBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppleTextPrimary)
                            Text(formatCurrency(saleWithItems.sale.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppleTextPrimary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Customer Paid (Bayad)", fontSize = 13.sp, color = AppleTextSecondary)
                            Text(formatCurrency(saleWithItems.sale.amountTendered), fontSize = 13.sp, color = AppleTextSecondary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Change (Sukli)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E7E34))
                            Text(formatCurrency(saleWithItems.sale.changeAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E7E34))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Profit (Kita)", fontSize = 13.sp, color = AppleBlue)
                            Text("+${formatCurrency(saleWithItems.sale.totalProfit)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppleBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
