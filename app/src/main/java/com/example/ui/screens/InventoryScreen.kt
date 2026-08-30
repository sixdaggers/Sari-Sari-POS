package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.StoreViewModel
import com.example.ui.theme.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: StoreViewModel) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = remember(allProducts) {
        val list = allProducts.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf("All") + list
    }

    val filteredList = remember(allProducts, searchQuery, selectedCategory) {
        allProducts.filter { prod ->
            val matchQuery = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.barcode.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
            matchQuery && matchCat
        }
    }

    val totalItemsCount = allProducts.sumOf { it.stock }
    val lowStockCount = allProducts.count { it.stock in 1..5 }
    val outOfStockCount = allProducts.count { it.stock <= 0 }

    Scaffold(
        containerColor = AppleLightBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppleBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Product", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Section
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Inventory Management",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                "${allProducts.size} products • $totalItemsCount total units in stock",
                                fontSize = 13.sp,
                                color = AppleTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stock Alert Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (lowStockCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppleOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "⚠️ $lowStockCount Low Stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (outOfStockCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppleRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "❌ $outOfStockCount Out of Stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(42.dp),
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Filter by name or barcode...", color = AppleTextSecondary, fontSize = 13.sp) },
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
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = AppleTextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) AppleBlue else Color(0xFFE9E9EB),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedCategory = category }
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else AppleTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Products List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredList, key = { it.id }) { product ->
                    InventoryProductCard(
                        product = product,
                        onEdit = { productToEdit = product },
                        onQuickStockAdjust = { delta -> viewModel.quickAdjustStock(product.id, delta) },
                        onDelete = { viewModel.deleteProduct(product.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProductFormDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addOrUpdateProduct(it)
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        ProductFormDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onSave = {
                viewModel.addOrUpdateProduct(it)
                productToEdit = null
            }
        )
    }
}

@Composable
fun InventoryProductCard(
    product: Product,
    onEdit: () -> Unit,
    onQuickStockAdjust: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppleBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppleBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                product.category,
                                color = AppleBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (product.barcode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "• ${product.barcode}",
                                fontSize = 11.sp,
                                color = AppleTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppleTextPrimary
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AppleBlue, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AppleRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Selling Price", fontSize = 10.sp, color = AppleTextSecondary)
                    Text(formatCurrency(product.price), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppleTextPrimary)
                }

                Column {
                    Text("Cost (Puhunan)", fontSize = 10.sp, color = AppleTextSecondary)
                    Text(formatCurrency(product.cost), fontSize = 13.sp, color = AppleTextSecondary)
                }

                Column {
                    Text("Profit (Tubo)", fontSize = 10.sp, color = AppleTextSecondary)
                    Text("+${formatCurrency(product.price - product.cost)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppleGreen)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Stock Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        product.stock <= 0 -> AppleRed.copy(alpha = 0.15f)
                        product.stock in 1..5 -> AppleOrange.copy(alpha = 0.15f)
                        else -> AppleGreen.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        "${product.stock} in stock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = when {
                            product.stock <= 0 -> AppleRed
                            product.stock in 1..5 -> AppleOrange
                            else -> Color(0xFF1E7E34)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppleBorder.copy(alpha = 0.5f))

            // Quick Stock Restock Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Quick Restock:",
                    fontSize = 12.sp,
                    color = AppleTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StockPillButton(label = "-1", onClick = { onQuickStockAdjust(-1) }, isMinus = true)
                    StockPillButton(label = "+1", onClick = { onQuickStockAdjust(1) })
                    StockPillButton(label = "+5", onClick = { onQuickStockAdjust(5) })
                    StockPillButton(label = "+10", onClick = { onQuickStockAdjust(10) })
                }
            }
        }
    }
}

@Composable
fun StockPillButton(
    label: String,
    onClick: () -> Unit,
    isMinus: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isMinus) Color(0xFFFFECEB) else Color(0xFFE5F1FF),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMinus) AppleRed else AppleBlue,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val isEdit = product != null

    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Snacks") }
    var price by remember { mutableStateOf(if (product != null) product.price.toString() else "") }
    var cost by remember { mutableStateOf(if (product != null) product.cost.toString() else "") }
    var stock by remember { mutableStateOf(if (product != null) product.stock.toString() else "") }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            barcode = result.contents
        }
    }

    val categories = listOf("Beverages", "Snacks", "Noodles", "Canned Goods", "Personal Care", "Condiments", "Fresh & Bakery", "General")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppleCardLight,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEdit) "Edit Product" else "Add New Product",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(0xFFE9E9EB), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = AppleTextPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Barcode field + scan button
                    item {
                        Text("Barcode / QR Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                placeholder = { Text("Scan or enter barcode") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalIconButton(
                                onClick = {
                                    val options = ScanOptions().apply {
                                        setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                        setPrompt("Scan item barcode")
                                    }
                                    barcodeLauncher.launch(options)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = AppleBlue
                                )
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan")
                            }
                        }
                    }

                    // Name
                    item {
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Coca-Cola 290ml") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category Pills
                    item {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSel = cat == category
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) AppleBlue else Color(0xFFE9E9EB),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { category = cat }
                                ) {
                                    Text(
                                        cat,
                                        color = if (isSel) Color.White else AppleTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Selling Price & Cost Price
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Selling Price (₱)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cost (Puhunan ₱)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = cost,
                                    onValueChange = { cost = it },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Stock
                    item {
                        Text("Current Stock (Quantity)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save button
                Button(
                    onClick = {
                        val priceNum = price.toDoubleOrNull() ?: 0.0
                        val costNum = cost.toDoubleOrNull() ?: 0.0
                        val stockNum = stock.toIntOrNull() ?: 0

                        if (name.isNotBlank()) {
                            val savedProduct = Product(
                                id = product?.id ?: 0,
                                barcode = barcode.trim(),
                                name = name.trim(),
                                category = category,
                                price = priceNum,
                                cost = costNum,
                                stock = stockNum
                            )
                            onSave(savedProduct)
                        }
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isEdit) "Update Product" else "Save Product", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
