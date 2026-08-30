package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getSalesBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<SaleWithItems>>

    @Query("SELECT SUM(totalProfit) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getProfitBetween(startTimestamp: Long, endTimestamp: Long): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getRevenueBetween(startTimestamp: Long, endTimestamp: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getSalesCountBetween(startTimestamp: Long, endTimestamp: Long): Flow<Int>

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSale(saleId: Int)
}

