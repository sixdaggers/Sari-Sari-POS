package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String = "",
    val name: String,
    val category: String = "General",
    val price: Double,
    val cost: Double,
    val stock: Int = 0
)

