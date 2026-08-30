package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem

@Database(entities = [Product::class, Sale::class, SaleItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
}
