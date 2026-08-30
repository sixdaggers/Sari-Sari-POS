package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.repository.SaleItemDraft
import com.example.ui.CompletedSaleReceipt
import com.example.ui.StoreViewModel
import com.example.ui.theme.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    return format.format(amount)
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: StoreViewModel) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val completedReceipt by viewModel.completedSale.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCheckoutSheet by remember { mutableStateOf(false) }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            coroutineScope.launch {
                val scanned = viewModel.handleScan(result.contents)
                if (scanned != null) {
                    Toast.makeText(context, "Added ${scanned.name} to cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Barcode not found (${result.contents})", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val categories = remember(allProducts) {
        val set = allProducts.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf("All") + set
    }

    Box(modifier = Modifier.fillMaxSize().background(AppleLightBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                color = AppleCardLight,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Sari-Sari Store",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                "Quick Point of Sale",
                                fontSize = 13.sp,
                                color = AppleTextSecondary
                            )
                        }

                        // Barcode Scan Button
                        FilledTonalButton(
                            onClick = {
                                val options = ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                    setPrompt("Scan item barcode / QR code")
                                    setCameraId(0)
                                    setBeepEnabled(true)
                                    setBarcodeImageEnabled(true)
                                    setOrientationLocked(false)
                                }
                                barcodeLauncher.launch(options)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = AppleBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Apple-style Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE9E9EB)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = AppleTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = {
                                    Text("Search products or barcodes...", color = AppleTextSecondary, fontSize = 14.sp)
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = AppleTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Pill Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) AppleBlue else Color(0xFFE9E9EB),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.setSelectedCategory(category) }
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else AppleTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Product Cards Grid
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = AppleTextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products found", color = AppleTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        if (searchQuery.isNotEmpty()) {
                            Text("Try searching for another keyword", color = AppleTextTertiary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(bottom = if (cartCount > 0) 100.dp else 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val inCartQuantity = cart.find { it.product.id == product.id }?.quantity ?: 0
                        ProductEcommerceCard(
                            product = product,
                            inCartQuantity = inCartQuantity,
                            onAddToCart = { viewModel.addProductToCart(product) }
                        )
                    }
                }
            }
        }

        // Sticky Bottom Cart Bar (Apple Style Floating Bar)
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(12.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = AppleCardLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "$cartCount item${if (cartCount > 1) "s" else ""} in cart",
                                fontSize = 12.sp,
                                color = AppleTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                formatCurrency(cartTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                        }
                    }

                    Button(
                        onClick = { showCheckoutSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Pay / Bayad", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    // Checkout Bottom Sheet / Dialog with Change Calculator
    if (showCheckoutSheet) {
        CheckoutAndChangeDialog(
            cart = cart,
            cartTotal = cartTotal,
            onUpdateQuantity = { productId, qty -> viewModel.updateCartQuantity(productId, qty) },
            onRemoveItem = { productId -> viewModel.removeCartItem(productId) },
            onClearCart = {
                viewModel.clearCart()
                showCheckoutSheet = false
            },
            onDismiss = { showCheckoutSheet = false },
            onConfirmSale = { tendered, method ->
                val success = viewModel.checkout(tendered, method)
                if (success) {
                    showCheckoutSheet = false
                }
            }
        )
    }

    // Receipt Dialog (Apple style clean invoice)
    completedReceipt?.let { receipt ->
        ReceiptDialog(
            receipt = receipt,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
fun ProductEcommerceCard(
    product: Product,
    inCartQuantity: Int,
    onAddToCart: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..5

    val categoryColor = when (product.category.lowercase()) {
        "beverages" -> Color(0xFF007AFF)
        "snacks" -> Color(0xFFFF9500)
        "noodles" -> Color(0xFFFF3B30)
        "canned goods" -> Color(0xFF5856D6)
        "personal care" -> Color(0xFF30B0C7)
        "fresh & bakery" -> Color(0xFF34C759)
        "condiments" -> Color(0xFFAF52DE)
        else -> Color(0xFF8E8E93)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isOutOfStock) { onAddToCart() }
            .border(
                width = if (inCartQuantity > 0) 1.5.dp else 1.dp,
                color = if (inCartQuantity > 0) AppleBlue else AppleBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category tag and Stock badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = product.category,
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isOutOfStock) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppleRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "Out of Stock",
                            color = AppleRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isLowStock) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppleOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${product.stock} left",
                            color = AppleOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        "${product.stock} in stock",
                        color = AppleTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isOutOfStock) AppleTextSecondary else AppleTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(38.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Add to Cart button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formatCurrency(product.price),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isOutOfStock) AppleTextSecondary else AppleTextPrimary
                    )
                    val profitMargin = product.price - product.cost
                    if (profitMargin > 0) {
                        Text(
                            text = "+${formatCurrency(profitMargin)} tubo",
                            fontSize = 10.sp,
                            color = AppleGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Add button / Cart counter
                if (isOutOfStock) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE9E9EB),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Block,
                            contentDescription = "Out of Stock",
                            tint = AppleTextSecondary,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                } else if (inCartQuantity > 0) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppleBlue)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$inCartQuantity",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add more",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = AppleBlue.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onAddToCart() }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add to Cart",
                            tint = AppleBlue,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutAndChangeDialog(
    cart: List<SaleItemDraft>,
    cartTotal: Double,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmSale: (Double, String) -> Unit
) {
    var tenderedText by remember { mutableStateOf(if (cartTotal > 0) String.format(Locale.US, "%.0f", cartTotal) else "") }
    val tenderedAmount = tenderedText.toDoubleOrNull() ?: 0.0
    val change = tenderedAmount - cartTotal
    val isEnough = tenderedAmount >= cartTotal && cartTotal > 0

    // Quick cash presets
    val quickPresets = remember(cartTotal) {
        val list = mutableListOf<Double>()
        list.add(cartTotal) // Exact
        val standardBills = listOf(20.0, 50.0, 100.0, 200.0, 500.0, 1000.0)
        standardBills.filter { it >= cartTotal }.forEach {
            if (!list.contains(it)) list.add(it)
        }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppleCardLight,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Checkout / Bayad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppleTextPrimary)
                        Text("${cart.sumOf { it.quantity }} items", fontSize = 12.sp, color = AppleTextSecondary)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Cart items summary list
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppleGrouped,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cart, key = { it.product.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.product.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = AppleTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${formatCurrency(item.product.price)} each",
                                        fontSize = 11.sp,
                                        color = AppleTextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE5E5EA),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .clickable { onUpdateQuantity(item.product.id, item.quantity - 1) }
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Minus", modifier = Modifier.padding(5.dp), tint = AppleTextPrimary)
                                    }

                                    Text(
                                        "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        color = AppleTextPrimary
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = AppleBlue.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .clickable { onUpdateQuantity(item.product.id, item.quantity + 1) }
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Plus", modifier = Modifier.padding(5.dp), tint = AppleBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    formatCurrency(item.quantity * item.product.price),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppleTextPrimary,
                                    modifier = Modifier.widthIn(min = 60.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Due", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppleTextSecondary)
                    Text(
                        formatCurrency(cartTotal),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Tendered Input (Bayad ng Customer)
                Text("Customer Payment (Bayad)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppleGrouped,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        Text("₱", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppleTextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        TextField(
                            value = tenderedText,
                            onValueChange = { tenderedText = it },
                            placeholder = { Text("0.00", color = AppleTextSecondary, fontSize = 20.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Presets Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPresets) { amount ->
                        val isExact = amount == cartTotal
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tenderedAmount == amount) AppleBlue else Color(0xFFE9E9EB),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    tenderedText = String.format(Locale.US, "%.0f", amount)
                                }
                        ) {
                            Text(
                                text = if (isExact) "Exact (${formatCurrency(amount)})" else formatCurrency(amount),
                                color = if (tenderedAmount == amount) Color.White else AppleTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Change / Sukli Live Calculation Display
                if (isEnough) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F8EE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change (Sukli)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E7E34))
                            }
                            Text(
                                formatCurrency(change),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1E7E34)
                            )
                        }
                    }
                } else if (tenderedAmount > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFECEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = AppleRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kulang (Lacking)", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppleRed)
                            }
                            Text(
                                formatCurrency(cartTotal - tenderedAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppleRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Complete Button
                Button(
                    onClick = {
                        onConfirmSale(tenderedAmount, "Cash")
                    },
                    enabled = isEnough,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFC7C7CC),
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Complete Sale (Tapos)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    receipt: CompletedSaleReceipt,
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F8EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Success",
                        tint = AppleGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Sale Completed!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppleTextPrimary)
                Text(formatDateTime(receipt.sale.timestamp), fontSize = 12.sp, color = AppleTextSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Slip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppleGrouped,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        receipt.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.product.name} x${item.quantity}",
                                    fontSize = 13.sp,
                                    color = AppleTextPrimary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    formatCurrency(item.quantity * item.product.price),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleTextPrimary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = AppleBorder
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppleTextPrimary)
                            Text(formatCurrency(receipt.sale.totalAmount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppleTextPrimary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Tendered (Bayad)", fontSize = 13.sp, color = AppleTextSecondary)
                            Text(formatCurrency(receipt.sale.amountTendered), fontSize = 13.sp, color = AppleTextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Change (Sukli)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E7E34))
                            Text(formatCurrency(receipt.sale.changeAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E7E34))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Profit Earned (Tubo)", fontSize = 12.sp, color = AppleBlue)
                            Text("+${formatCurrency(receipt.sale.totalProfit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("New Transaction", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
