package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleWithItems
import com.example.data.repository.PeriodType
import com.example.data.repository.SaleItemDraft
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialProductsIfEmpty()
        }
    }

    val allProducts = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSales = repository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search and category filtering for POS
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredProducts = combine(allProducts, _searchQuery, _selectedCategory) { products, query, category ->
        products.filter { product ->
            val matchesQuery = query.isBlank() || 
                product.name.contains(query, ignoreCase = true) || 
                product.barcode.contains(query, ignoreCase = true) ||
                product.category.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cart State
    private val _cart = MutableStateFlow<List<SaleItemDraft>>(emptyList())
    val cart = _cart.asStateFlow()

    val cartTotal = _cart.map { items ->
        items.sumOf { it.quantity * it.product.price }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val cartItemCount = _cart.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Last completed sale (for Receipt dialog)
    private val _completedSale = MutableStateFlow<CompletedSaleReceipt?>(null)
    val completedSale = _completedSale.asStateFlow()

    // Reports Period Selection
    private val _selectedPeriod = MutableStateFlow(PeriodType.DAY)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodSales = _selectedPeriod.flatMapLatest { period ->
        repository.getSalesForPeriod(period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodRevenue = _selectedPeriod.flatMapLatest { period ->
        repository.getRevenueForPeriod(period)
    }.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodProfit = _selectedPeriod.flatMapLatest { period ->
        repository.getProfitForPeriod(period)
    }.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTransactionCount = _selectedPeriod.flatMapLatest { period ->
        repository.getCountForPeriod(period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedPeriod(period: PeriodType) {
        _selectedPeriod.value = period
    }

    fun addProductToCart(product: Product) {
        if (product.stock <= 0) return
        _cart.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null) {
                if (existingItem.quantity < product.stock) {
                    currentCart.map {
                        if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                    }
                } else {
                    currentCart
                }
            } else {
                currentCart + SaleItemDraft(product, 1)
            }
        }
    }

    fun updateCartQuantity(productId: Int, quantity: Int) {
        val product = allProducts.value.find { it.id == productId } ?: return
        _cart.update { currentCart ->
            if (quantity <= 0) {
                currentCart.filter { it.product.id != productId }
            } else {
                val clampedQty = minOf(quantity, product.stock)
                currentCart.map {
                    if (it.product.id == productId) it.copy(quantity = clampedQty) else it
                }
            }
        }
    }

    fun removeCartItem(productId: Int) {
        _cart.update { currentCart -> currentCart.filter { it.product.id != productId } }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun checkout(amountTendered: Double, paymentMethod: String = "Cash"): Boolean {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return false

        val total = currentCart.sumOf { it.quantity * it.product.price }
        if (amountTendered < total) return false

        viewModelScope.launch {
            val sale = repository.processSale(currentCart, amountTendered, paymentMethod)
            _completedSale.value = CompletedSaleReceipt(
                sale = sale,
                items = currentCart.toList()
            )
            _cart.value = emptyList()
        }
        return true
    }

    fun dismissReceipt() {
        _completedSale.value = null
    }

    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun quickAdjustStock(productId: Int, delta: Int) {
        val prod = allProducts.value.find { it.id == productId } ?: return
        val newStock = maxOf(0, prod.stock + delta)
        viewModelScope.launch {
            repository.updateStock(productId, newStock)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }

    suspend fun handleScan(barcode: String): Product? {
        val product = repository.getProductByBarcode(barcode)
        if (product != null) {
            addProductToCart(product)
        }
        return product
    }

    class Factory(private val repository: StoreRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class CompletedSaleReceipt(
    val sale: Sale,
    val items: List<SaleItemDraft>
)
