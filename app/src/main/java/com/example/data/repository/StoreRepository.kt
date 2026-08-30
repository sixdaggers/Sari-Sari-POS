package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class PeriodStats(
    val revenue: Double,
    val profit: Double,
    val transactionCount: Int
)

class StoreRepository(
    private val productDao: ProductDao,
    private val saleDao: SaleDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleWithItems>> = saleDao.getAllSales()

    suspend fun seedInitialProductsIfEmpty() {
        if (productDao.getProductCount() == 0) {
            val starterProducts = listOf(
                Product(name = "Lucky Me! Pancit Canton Kalamansi", barcode = "4800016644874", category = "Noodles", price = 16.0, cost = 12.50, stock = 36),
                Product(name = "Lucky Me! Pancit Canton Chilimansi", barcode = "4800016644881", category = "Noodles", price = 16.0, cost = 12.50, stock = 30),
                Product(name = "Lucky Me! Supreme Beef 65g", barcode = "4800016642108", category = "Noodles", price = 22.0, cost = 17.0, stock = 18),
                Product(name = "Coca-Cola Kasalo 750ml", barcode = "4800001011119", category = "Beverages", price = 38.0, cost = 30.0, stock = 15),
                Product(name = "Coca-Cola Mismo 290ml", barcode = "4800001011126", category = "Beverages", price = 18.0, cost = 14.0, stock = 24),
                Product(name = "Royal Tru-Orange Mismo 290ml", barcode = "4800001011133", category = "Beverages", price = 18.0, cost = 14.0, stock = 20),
                Product(name = "Sprite Mismo 290ml", barcode = "4800001011140", category = "Beverages", price = 18.0, cost = 14.0, stock = 16),
                Product(name = "Nescafé 3in1 Original Sachet 28g", barcode = "4800361280017", category = "Beverages", price = 14.0, cost = 10.50, stock = 45),
                Product(name = "Kopiko Brown Coffee Sachet 53g", barcode = "8996001414002", category = "Beverages", price = 15.0, cost = 11.50, stock = 40),
                Product(name = "San Miguel Pale Pilsen 330ml", barcode = "4800008000014", category = "Beverages", price = 55.0, cost = 44.0, stock = 24),
                Product(name = "Century Tuna Flakes in Oil 155g", barcode = "4800194118029", category = "Canned Goods", price = 40.0, cost = 32.0, stock = 20),
                Product(name = "Argentina Corned Beef 150g", barcode = "4800194123450", category = "Canned Goods", price = 42.0, cost = 34.0, stock = 15),
                Product(name = "555 Sardines in Tomato Sauce 155g", barcode = "4800194111112", category = "Canned Goods", price = 24.0, cost = 19.0, stock = 25),
                Product(name = "Piattos Cheese 40g", barcode = "4800016050101", category = "Snacks", price = 18.0, cost = 14.0, stock = 25),
                Product(name = "Nova Multigrain Country Cheddar 40g", barcode = "4800016050200", category = "Snacks", price = 18.0, cost = 14.0, stock = 20),
                Product(name = "SkyFlakes Crackers Single Pack", barcode = "4800014000115", category = "Snacks", price = 9.0, cost = 6.50, stock = 50),
                Product(name = "Oishi Prawn Crackers 40g", barcode = "4800194100017", category = "Snacks", price = 12.0, cost = 9.0, stock = 30),
                Product(name = "Safeguard White Pure Soap 60g", barcode = "4902430761234", category = "Personal Care", price = 28.0, cost = 22.0, stock = 15),
                Product(name = "Palmolive Shampoo Sachet 15ml", barcode = "4800012345678", category = "Personal Care", price = 8.0, cost = 5.50, stock = 60),
                Product(name = "Colgate Total Toothpaste Sachet", barcode = "4800098765432", category = "Personal Care", price = 10.0, cost = 7.0, stock = 40),
                Product(name = "Datu Puti Soy Sauce 200ml", barcode = "4800016001011", category = "Condiments", price = 14.0, cost = 10.0, stock = 18),
                Product(name = "Datu Puti Spiced Vinegar 200ml", barcode = "4800016001028", category = "Condiments", price = 15.0, cost = 11.0, stock = 18),
                Product(name = "Fresh Farm Egg (Medium)", barcode = "1000000000001", category = "Fresh & Bakery", price = 9.0, cost = 7.0, stock = 60),
                Product(name = "Monde Special Mamon 43g", barcode = "4800016123456", category = "Fresh & Bakery", price = 18.0, cost = 14.0, stock = 14)
            )
            productDao.insertAll(starterProducts)
        }
    }

    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun updateStock(id: Int, newStock: Int) = productDao.updateStock(id, newStock)
    suspend fun deleteProductById(id: Int) = productDao.deleteProductById(id)
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    suspend fun deleteSale(saleId: Int) = saleDao.deleteSale(saleId)

    suspend fun processSale(
        items: List<SaleItemDraft>,
        amountTendered: Double,
        paymentMethod: String = "Cash"
    ): Sale {
        if (items.isEmpty()) throw IllegalArgumentException("Cart cannot be empty")
        
        var totalAmount = 0.0
        var totalProfit = 0.0
        
        for (draft in items) {
            val amount = draft.quantity * draft.product.price
            val profit = draft.quantity * (draft.product.price - draft.product.cost)
            
            totalAmount += amount
            totalProfit += profit
            
            // Reduce stock
            val updatedStock = maxOf(0, draft.product.stock - draft.quantity)
            productDao.updateStock(draft.product.id, updatedStock)
        }

        val changeAmount = maxOf(0.0, amountTendered - totalAmount)
        val sale = Sale(
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            totalProfit = totalProfit,
            amountTendered = amountTendered,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod
        )

        val saleId = saleDao.insertSale(sale).toInt()

        val saleItemsToInsert = items.map { draft ->
            SaleItem(
                saleId = saleId,
                productId = draft.product.id,
                name = draft.product.name,
                quantity = draft.quantity,
                priceAtSale = draft.product.price,
                costAtSale = draft.product.cost
            )
        }
        
        saleDao.insertSaleItems(saleItemsToInsert)
        return sale.copy(id = saleId)
    }

    fun getSalesForPeriod(period: PeriodType): Flow<List<SaleWithItems>> {
        val (start, end) = getTimestampsForPeriod(period)
        return if (period == PeriodType.ALL_TIME) {
            saleDao.getAllSales()
        } else {
            saleDao.getSalesBetween(start, end)
        }
    }

    fun getProfitForPeriod(period: PeriodType): Flow<Double?> {
        val (start, end) = getTimestampsForPeriod(period)
        return saleDao.getProfitBetween(start, end)
    }

    fun getRevenueForPeriod(period: PeriodType): Flow<Double?> {
        val (start, end) = getTimestampsForPeriod(period)
        return saleDao.getRevenueBetween(start, end)
    }

    fun getCountForPeriod(period: PeriodType): Flow<Int> {
        val (start, end) = getTimestampsForPeriod(period)
        return saleDao.getSalesCountBetween(start, end)
    }

    fun getTimestampsForPeriod(period: PeriodType): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when (period) {
            PeriodType.DAY -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }
            PeriodType.WEEK -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                }
                val start = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }
            PeriodType.MONTH -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }
            PeriodType.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}

enum class PeriodType(val label: String) {
    DAY("Today"),
    WEEK("This Week"),
    MONTH("This Month"),
    ALL_TIME("All Time")
}

data class SaleItemDraft(
    val product: Product,
    var quantity: Int
)
